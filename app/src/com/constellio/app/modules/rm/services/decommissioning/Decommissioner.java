/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.modules.rm.services.decommissioning;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.model.enums.DecommissioningType;
import com.constellio.app.modules.rm.model.enums.DisposalType;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.structures.DecomListContainerDetail;
import com.constellio.app.modules.rm.wrappers.structures.DecomListFolderDetail;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.ContentVersion;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.contents.ContentConversionManager;
import com.constellio.model.services.contents.ContentImpl;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

public abstract class Decommissioner {
	protected final DecommissioningService decommissioningService;
	protected final ModelLayerFactory modelLayerFactory;
	protected final RMSchemasRecordsServices rm;
	protected final RMConfigs configs;
	protected final SearchServices searchServices;
	protected final ContentManager contentManager;

	protected DecommissioningList decommissioningList;
	private ContentConversionManager conversionManager;
	private Transaction transaction;
	private List<Record> recordsToDelete;
	private LocalDate processingDate;
	private User user;

	public static Decommissioner forList(DecommissioningList decommissioningList, DecommissioningService decommissioningService) {
		switch (decommissioningList.getDecommissioningListType()) {
		case FOLDERS_TO_CLOSE:
			return new ClosingDecommissioner(decommissioningService);
		case FOLDERS_TO_TRANSFER:
			return new TransferringDecommissioner(decommissioningService);
		case FOLDERS_TO_DEPOSIT:
			return decommissioningService.isSortable(decommissioningList) ?
					new SortingDecommissioner(decommissioningService, true) :
					new DepositingDecommissioner(decommissioningService);
		case FOLDERS_TO_DESTROY:
			return decommissioningService.isSortable(decommissioningList) ?
					new SortingDecommissioner(decommissioningService, false) :
					new DestroyingDecommissioner(decommissioningService);
		}
		throw new RuntimeException("Unknown decommissioning type");
	}

	protected Decommissioner(DecommissioningService decommissioningService) {
		this.decommissioningService = decommissioningService;
		modelLayerFactory = decommissioningService.getModelLayerFactory();
		rm = decommissioningService.getRMSchemasRecordServices();
		configs = decommissioningService.getRMConfigs();
		searchServices = modelLayerFactory.newSearchServices();
		contentManager = modelLayerFactory.getContentManager();
	}

	public void process(DecommissioningList decommissioningList, User user, LocalDate processingDate) {
		prepare(decommissioningList, user, processingDate);
		validate();
		processFolders();
		processContainers();
		markProcessed();
		execute();
	}

	protected LocalDate getProcessingDate() {
		return processingDate;
	}

	protected void add(RecordWrapper record) {
		transaction.addUpdate(record.getWrappedRecord());
	}

	protected void delete(RecordWrapper record) {
		recordsToDelete.add(record.getWrappedRecord());
	}

	private void validate() {
		if (!decommissioningService.isProcessable(decommissioningList, user)) {
			// TODO: Proper exception
			throw new RuntimeException("The decommissioning list cannot be processed");
		}
	}

	private void prepare(DecommissioningList decommissioningList, User user, LocalDate processingDate) {
		this.decommissioningList = decommissioningList;
		this.processingDate = processingDate;
		this.user = user;
		transaction = new Transaction().setUser(user);
		recordsToDelete = new ArrayList<>();
	}

	private void processFolders() {
		try {
			conversionManager = new ContentConversionManager(modelLayerFactory);

			for (DecomListFolderDetail detail : decommissioningList.getFolderDetails()) {
				Folder folder = rm.getFolder(detail.getFolderId());
				preprocessFolder(folder, detail);
				processFolder(folder, detail);
				add(folder);
			}
		} finally {
			conversionManager.close();
		}
	}

	protected void preprocessFolder(Folder folder, DecomListFolderDetail detail) {
		if (folder.getCloseDateEntered() == null) {
			folder.setCloseDateEntered(processingDate);
		}
		if (detail.getContainerRecordId() != null) {
			folder.setContainer(detail.getContainerRecordId());
		}
	}

	protected abstract void processFolder(Folder folder, DecomListFolderDetail detail);

	protected void markFolderTransferred(Folder folder) {
		folder.setActualTransferDate(processingDate);
	}

	protected void markFolderDeposited(Folder folder) {
		folder.setActualDepositDate(processingDate);
	}

	protected void markFolderDestroyed(Folder folder) {
		folder.setActualDestructionDate(processingDate);
	}

	protected void removeFolderFromContainer(Folder folder) {
		String containerId = folder.getContainer();
		if (containerId == null) {
			return;
		}
		folder.setContainer((String) null);
		for (DecomListContainerDetail detail : decommissioningList.getContainerDetails()) {
			if (detail.getContainerRecordId().equals(containerId)) {
				detail.setFull(false);
			}
		}
	}

	protected void cleanupDocumentsIn(Folder folder, boolean purgeMinorVersions, boolean createPDFa) {
		if (!purgeMinorVersions && !createPDFa) {
			return;
		}
		for (Document document : getDocumentsWithContentInFolder(folder)) {
			Content content = document.getContent();
			if (purgeMinorVersions) {
				content = purgeMinorVersions(content);
			}
			if (createPDFa && content != null) {
				content = createPDFa(content);
			}
			add(document.setContent(content));
		}
	}

	protected void destroyDocumentsIn(Folder folder) {
		for (Document document : getDocumentsWithContentInFolder(folder)) {
			destroyContent(document.getContent());
			add(document.setContent(null));
		}
	}

	public Content purgeMinorVersions(Content content) {
		List<ContentVersion> history = new ArrayList<>();
		for (ContentVersion version : new ArrayList<>(content.getHistoryVersions())) {
			if (version.isMajor()) {
				history.add(version);
			} else {
				contentManager.silentlyMarkForDeletionIfNotReferenced(version.getHash());
			}
		}

		ContentVersion current = content.getCurrentVersion();
		if (!content.getCurrentVersion().isMajor() && configs.purgeCurrentVersionIfMinor()) {
			contentManager.silentlyMarkForDeletionIfNotReferenced(current.getHash());

			if (history.isEmpty()) {
				return null;
			}

			int lastIndex = history.size() - 1;
			current = history.get(lastIndex);
			history.remove(lastIndex);
		}

		return ContentImpl.create(content.getId(), current, history);
	}

	private Content createPDFa(Content content) {
		return conversionManager.convertToPDF(content);
	}

	private void destroyContent(Content content) {
		for (ContentVersion version : new ArrayList<>(content.getHistoryVersions())) {
			contentManager.markForDeletionIfNotReferenced(version.getHash());
		}
		contentManager.markForDeletionIfNotReferenced(content.getCurrentVersion().getHash());
	}

	private List<Document> getDocumentsWithContentInFolder(Folder folder) {
		LogicalSearchQuery query = new LogicalSearchQuery(from(rm.documentSchemaType())
				.where(rm.documentFolder()).isEqualTo(folder)
				.andWhere(rm.documentContent()).isNotNull());
		return rm.wrapDocuments(searchServices.search(query));
	}

	private void processContainers() {
		for (DecomListContainerDetail detail : decommissioningList.getContainerDetails()) {
			processContainer(rm.getContainerRecord(detail.getContainerRecordId()), detail);
		}
	}

	protected void updateContainer(ContainerRecord container, DecomListContainerDetail detail) {
		container.setDecommissioningType(getDecommissioningTypeForContainer());

		if (detail.isFull()) {
			if (container.getCompletionDate() == null) {
				container.setCompletionDate(processingDate);
			}
		} else {
			container.setCompletionDate(null);
		}
		container.setFull(detail.isFull());

		add(container);
	}

	protected abstract void processContainer(ContainerRecord container, DecomListContainerDetail detail);

	protected boolean isContainerEmpty(ContainerRecord container, List<String> destroyedFolders) {
		LogicalSearchCondition condition = from(rm.folderSchemaType()).where(rm.folderContainer()).isEqualTo(container);
		if (!destroyedFolders.isEmpty()) {
			condition = condition.andWhere(Schemas.IDENTIFIER).isNotIn(destroyedFolders);
		}
		return searchServices.getResultsCount(condition) == 0;
	}

	protected DecommissioningType getDecommissioningTypeForContainer() {
		return decommissioningList.getDecommissioningListType().getDecommissioningType();
	}

	private void markProcessed() {
		add(decommissioningList.setProcessingDate(processingDate).setProcessingUser(user));
	}

	private void execute() {
		RecordServices recordServices = modelLayerFactory.newRecordServices();
		try {
			recordServices.execute(transaction);
			for (Record record : recordsToDelete) {
				recordServices.logicallyDelete(record, user);
			}
			contentManager.deleteUnreferencedContents();
		} catch (RecordServicesException e) {
			// TODO: Proper exception
			throw new RuntimeException(e);
		}
	}
}

class ClosingDecommissioner extends Decommissioner {

	protected ClosingDecommissioner(DecommissioningService decommissioningService) {
		super(decommissioningService);
	}

	@Override
	protected void processFolder(Folder folder, DecomListFolderDetail detail) {
		// No need to do anything here
	}

	@Override
	protected void processContainer(ContainerRecord container, DecomListContainerDetail detail) {
		//  No need to do anything here
	}
}

class TransferringDecommissioner extends Decommissioner {

	protected TransferringDecommissioner(DecommissioningService decommissioningService) {
		super(decommissioningService);
	}

	@Override
	protected void processFolder(Folder folder, DecomListFolderDetail detail) {
		markFolderTransferred(folder);
		processDocumentsIn(folder);
	}

	@Override
	protected void processContainer(ContainerRecord container, DecomListContainerDetail detail) {
		container.setRealTransferDate(getProcessingDate());
		updateContainer(container, detail);
	}

	private void processDocumentsIn(Folder folder) {
		cleanupDocumentsIn(folder, configs.purgeMinorVersionsOnTransfer(), configs.createPDFaOnTransfer());
	}
}

abstract class DeactivatingDecommissioner extends Decommissioner {
	protected List<String> destroyedFolders;

	protected DeactivatingDecommissioner(DecommissioningService decommissioningService) {
		super(decommissioningService);
		destroyedFolders = new ArrayList<>();
	}

	protected void processDepositedFolder(Folder folder, DecomListFolderDetail detail) {
		markFolderDeposited(folder);
		processDocumentsInDeposited(folder);
	}

	protected void processDepositedContainer(ContainerRecord container, DecomListContainerDetail detail) {
		container.setRealDepositDate(getProcessingDate());
		updateContainer(container, detail);
	}

	protected void processDeletedFolder(Folder folder, DecomListFolderDetail detail) {
		removeFolderFromContainer(folder);
		markFolderDestroyed(folder);
		destroyedFolders.add(folder.getId());
		processDocumentsInDeleted(folder);
	}

	protected void processDeletedContainer(ContainerRecord container, DecomListContainerDetail detail) {
		if (isContainerEmpty(container, destroyedFolders)) {
			delete(container);
		} else {
			container.setFull(detail.isFull());
			add(container);
		}
	}

	private void processDocumentsInDeposited(Folder folder) {
		cleanupDocumentsIn(folder, shouldPurgeMinorVersions(), shouldCreatePDFa());
	}

	private void processDocumentsInDeleted(Folder folder) {
		destroyDocumentsIn(folder);
	}

	private boolean shouldPurgeMinorVersions() {
		return decommissioningList.isFromActive() ?
				configs.purgeMinorVersionsOnTransfer() :
				configs.purgeMinorVersionsOnDeposit();
	}

	private boolean shouldCreatePDFa() {
		return decommissioningList.isFromActive() ? configs.createPDFaOnTransfer() : configs.createPDFaOnDeposit();
	}
}

class DepositingDecommissioner extends DeactivatingDecommissioner {

	protected DepositingDecommissioner(DecommissioningService decommissioningService) {
		super(decommissioningService);
	}

	@Override
	protected void processFolder(Folder folder, DecomListFolderDetail detail) {
		processDepositedFolder(folder, detail);
	}

	@Override
	protected void processContainer(ContainerRecord container, DecomListContainerDetail detail) {
		processDepositedContainer(container, detail);
	}
}

class DestroyingDecommissioner extends DeactivatingDecommissioner {

	protected DestroyingDecommissioner(DecommissioningService decommissioningService) {
		super(decommissioningService);
	}

	@Override
	protected void processFolder(Folder folder, DecomListFolderDetail detail) {
		processDeletedFolder(folder, detail);
	}

	@Override
	protected void processContainer(ContainerRecord container, DecomListContainerDetail detail) {
		processDeletedContainer(container, detail);
	}
}

class SortingDecommissioner extends DeactivatingDecommissioner {
	private final boolean depositByDefault;

	protected SortingDecommissioner(DecommissioningService decommissioningService, boolean depositByDefault) {
		super(decommissioningService);
		this.depositByDefault = depositByDefault;
	}

	@Override
	protected void processFolder(Folder folder, DecomListFolderDetail detail) {
		if (shouldDeposit(folder, detail)) {
			processDepositedFolder(folder, detail);
		} else {
			processDeletedFolder(folder, detail);
		}
	}

	@Override
	protected void processContainer(ContainerRecord container, DecomListContainerDetail detail) {
		if (isContainerEmpty(container, destroyedFolders)) {
			delete(container);
		} else {
			processDepositedContainer(container, detail);
		}
	}

	@Override
	protected DecommissioningType getDecommissioningTypeForContainer() {
		return DecommissioningType.DEPOSIT;
	}

	private boolean shouldDeposit(Folder folder, DecomListFolderDetail detail) {
		if (folder.getInactiveDisposalType() == DisposalType.SORT) {
			return depositByDefault != detail.isReversedSort();
		} else {
			return depositByDefault;
		}
	}
}
