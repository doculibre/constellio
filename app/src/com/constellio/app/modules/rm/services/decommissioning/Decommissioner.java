package com.constellio.app.modules.rm.services.decommissioning;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.model.enums.DecommissioningListType;
import com.constellio.app.modules.rm.model.enums.DecommissioningType;
import com.constellio.app.modules.rm.model.enums.DisposalType;
import com.constellio.app.modules.rm.model.enums.FolderStatus;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.logging.DecommissioningLoggingService;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.structures.DecomListContainerDetail;
import com.constellio.app.modules.rm.wrappers.structures.DecomListFolderDetail;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.io.services.facades.FileService;
import com.constellio.data.utils.ImpossibleRuntimeException;
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
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public abstract class Decommissioner {
	protected final DecommissioningService decommissioningService;
	AppLayerFactory appLayerFactory;
	ModelLayerFactory modelLayerFactory;
	protected final RMSchemasRecordsServices rm;
	protected final RMConfigs configs;
	protected final SearchServices searchServices;
	protected final ContentManager contentManager;
	protected final DecommissioningLoggingService loggingServices;

	protected DecommissioningList decommissioningList;
	private ContentConversionManager conversionManager;
	private Transaction transaction;
	private List<Record> recordsToDelete;
	private LocalDate processingDate;
	private User user;

	public static Decommissioner forList(DecommissioningList decommissioningList, DecommissioningService decommissioningService, AppLayerFactory appLayerFactory) {
		switch (decommissioningList.getDecommissioningListType()) {
		case FOLDERS_TO_CLOSE:
			return new ClosingDecommissioner(decommissioningService, appLayerFactory);
		case FOLDERS_TO_TRANSFER:
		case DOCUMENTS_TO_TRANSFER:
			return new TransferringDecommissioner(decommissioningService, appLayerFactory);
		case FOLDERS_TO_DEPOSIT:
		case DOCUMENTS_TO_DEPOSIT:
			return decommissioningService.isSortable(decommissioningList) ?
					new SortingDecommissioner(decommissioningService, true, appLayerFactory) :
					new DepositingDecommissioner(decommissioningService, appLayerFactory);
		case FOLDERS_TO_DESTROY:
		case DOCUMENTS_TO_DESTROY:
			return decommissioningService.isSortable(decommissioningList) ?
					new SortingDecommissioner(decommissioningService, false, appLayerFactory) :
					new DestroyingDecommissioner(decommissioningService, appLayerFactory);
		}
		throw new ImpossibleRuntimeException("Unknown decommissioning type: " + decommissioningList.getDecommissioningListType());
	}

	protected Decommissioner(DecommissioningService decommissioningService, AppLayerFactory appLayerFactory) {
		this.decommissioningService = decommissioningService;
		this.appLayerFactory = appLayerFactory;
		this.modelLayerFactory = appLayerFactory.getModelLayerFactory();
		rm = decommissioningService.getRMSchemasRecordServices();
		configs = decommissioningService.getRMConfigs();
		searchServices = modelLayerFactory.newSearchServices();
		contentManager = modelLayerFactory.getContentManager();
		loggingServices = new DecommissioningLoggingService(modelLayerFactory);
	}

	public void process(DecommissioningList decommissioningList, User user, LocalDate processingDate) {
		prepare(decommissioningList, user, processingDate);
		validate();
		saveCertificates(decommissioningList);
		try (ContentConversionManager manager = new ContentConversionManager(modelLayerFactory)) {
			conversionManager = manager;
			if (decommissioningList.getDecommissioningListType().isFolderList()) {
				processFolders();
				processContainers();
			} else {
				processDocuments();
			}
		}
		markProcessed();
		execute(true);
	}

	public void approve(DecommissioningList decommissioningList, User user, LocalDate processingDate) {
		prepare(decommissioningList, user, processingDate);
		approveFolders();
		markApproved();
		execute(false);
	}

	protected void approveFolders() {
		for (DecomListFolderDetail detail : decommissioningList.getFolderDetails()) {
			if (detail.isFolderExcluded()) {
				continue;
			}
			Folder folder = rm.getFolder(detail.getFolderId());
			add(folder.setPermissionStatus(getPermissionStatusFor(folder, detail)));
		}
	}

	protected abstract FolderStatus getPermissionStatusFor(Folder folder, DecomListFolderDetail detail);

	private void markApproved() {
		add(decommissioningList.setApprovalDate(processingDate).setApprovalUser(user));
	}
	
	protected void removeManualArchivisticStatus(Folder folder) {
		folder.setManualArchivisticStatus(null);
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
		transaction = new Transaction();
		recordsToDelete = new ArrayList<>();
	}

	private void saveCertificates(DecommissioningList decommissioningList) {
		if (!decommissioningList.getDecommissioningListType().isDestroyal()) {
			return;
		}
		FileService fileService = modelLayerFactory.getIOServicesFactory()
				.newFileService();
		DecomCertificateService service = new DecomCertificateService(rm, searchServices, contentManager, fileService,
				user, decommissioningList, appLayerFactory);
		service.computeContents();
		Content documentsContent = service.getDocumentsContent();
		Content foldersContent = service.getFoldersContent();
		decommissioningList.setDocumentsReportContent(documentsContent);
		decommissioningList.setFoldersReportContent(foldersContent);
	}

	private void processFolders() {
		DecommissioningListType decommissioningListType = decommissioningList.getDecommissioningListType();
		for (DecomListFolderDetail detail : decommissioningList.getFolderDetails()) {
			if (detail.isFolderExcluded()) {
				continue;
			}
			Folder folder = rm.getFolder(detail.getFolderId());
			preprocessFolder(folder, detail, decommissioningListType);
			processFolder(folder, detail);
			add(folder);
		}
	}

	protected abstract void processDocuments();

	protected void preprocessFolder(Folder folder, DecomListFolderDetail detail, DecommissioningListType decommissioningListType) {
		if (folder.getCloseDateEntered() == null && DecommissioningListType.FOLDERS_TO_CLOSE.equals(decommissioningListType)) {
			folder.setCloseDateEntered(processingDate);
		}
		if (detail.getContainerRecordId() != null) {
			folder.setContainer(detail.getContainerRecordId());
		}
		if (detail.getFolderLinearSize() != null) {
			folder.setLinearSize(detail.getFolderLinearSize());
		}
	}

	protected abstract void processFolder(Folder folder, DecomListFolderDetail detail);

	protected void markFolderTransferred(Folder folder) {
		folder.setActualTransferDate(processingDate);
	}

	protected void markDocumentTransferred(Document document) {
		document.setActualTransferDateEntered(processingDate);
	}

	protected void markFolderDeposited(Folder folder) {
		folder.setActualDepositDate(processingDate);
	}

	protected void markDocumentDeposited(Document document) {
		document.setActualDepositDateEntered(processingDate);
	}

	protected void markFolderDestroyed(Folder folder) {
		folder.setActualDestructionDate(processingDate);
	}

	protected void markDocumentDestroyed(Document document) {
		document.setActualDestructionDateEntered(processingDate);
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
		cleanupDocuments(getAllDocumentsInFolder(folder), purgeMinorVersions, createPDFa, null);
	}

	protected void cleanupDocuments(
			List<Document> documents, boolean purgeMinorVersions, boolean createPDFa, DocumentUpdater updater) {
		for (Document document : documents) {
			Content content = document.getContent();
			if (content != null) {
				if (purgeMinorVersions) {
					content = purgeMinorVersions(content);
				}
				if (createPDFa && content != null) {
					content = createPDFa(content);
					loggingServices.logPdfAGeneration(document, user);
				}
			}
			if (updater != null) {
				updater.update(document);
			}
			add(document.setContent(content));
		}
	}

	protected void destroyDocumentsIn(Folder folder) {
		destroyDocuments(getAllDocumentsInFolder(folder), null);
	}

	protected void destroyDocuments(List<Document> documents, DocumentUpdater updater) {
		for (Document document : documents) {
			if (document.getContent() != null) {
				destroyContent(document.getContent());
			}
			if (updater != null) {
				updater.update(document);
			}
			add(document.setContent(null));
			if (configs.deleteDocumentRecordsWithDestruction()) {
				delete(document);
			}
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
		return conversionManager.replaceContentByPDFA(content);
	}

	private void destroyContent(Content content) {
		for (ContentVersion version : new ArrayList<>(content.getHistoryVersions())) {
			contentManager.markForDeletionIfNotReferenced(version.getHash());
		}
		contentManager.markForDeletionIfNotReferenced(content.getCurrentVersion().getHash());
	}

	protected List<Document> getListDocuments() {
		LogicalSearchQuery query = new LogicalSearchQuery(from(rm.documentSchemaType())
				.where(Schemas.IDENTIFIER).isIn(decommissioningList.getDocuments()));
		return rm.wrapDocuments(searchServices.search(query));
	}

	private List<Document> getAllDocumentsInFolder(Folder folder) {
		LogicalSearchQuery query = new LogicalSearchQuery(from(rm.documentSchemaType())
				.where(rm.documentFolder()).isEqualTo(folder));
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
		LogicalSearchCondition condition = from(rm.folder.schemaType()).where(rm.folder.container()).isEqualTo(container);
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

	private void execute(boolean logging) {
		if (logging) {
			loggingServices.logDecommissioning(decommissioningList, user);
		}
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

	public interface DocumentUpdater {
		void update(Document document);
	}
}

class ClosingDecommissioner extends Decommissioner {

	protected ClosingDecommissioner(DecommissioningService decommissioningService, AppLayerFactory appLayerFactory) {
		super(decommissioningService, appLayerFactory);
	}

	@Override
	protected FolderStatus getPermissionStatusFor(Folder folder, DecomListFolderDetail detail) {
		return folder.getPermissionStatus();
	}

	@Override
	protected void processFolder(Folder folder, DecomListFolderDetail detail) {
		// No need to do anything here
	}

	@Override
	protected void processContainer(ContainerRecord container, DecomListContainerDetail detail) {
		//  No need to do anything here
	}

	@Override
	protected void processDocuments() {
		// No need to do anything here
	}

	@Override
	protected void approveFolders() {
		// No need to do anything here
	}
}

class TransferringDecommissioner extends Decommissioner {

	protected TransferringDecommissioner(DecommissioningService decommissioningService, AppLayerFactory appLayerFactory) {
		super(decommissioningService, appLayerFactory);
	}

	@Override
	protected FolderStatus getPermissionStatusFor(Folder folder, DecomListFolderDetail detail) {
		return FolderStatus.SEMI_ACTIVE;
	}

	@Override
	protected void processFolder(Folder folder, DecomListFolderDetail detail) {
		removeManualArchivisticStatus(folder);
		markFolderTransferred(folder);
		processDocumentsIn(folder);
	}

	@Override
	protected void processContainer(ContainerRecord container, DecomListContainerDetail detail) {
		container.setRealTransferDate(getProcessingDate());
		updateContainer(container, detail);
	}

	@Override
	protected void processDocuments() {
		cleanupDocuments(getListDocuments(), configs.purgeMinorVersionsOnTransfer(), configs.createPDFaOnTransfer(),
				new DocumentUpdater() {
					@Override
					public void update(Document document) {
						markDocumentTransferred(document);
					}
				});
	}

	private void processDocumentsIn(Folder folder) {
		cleanupDocumentsIn(folder, configs.purgeMinorVersionsOnTransfer(), configs.createPDFaOnTransfer());
	}
}

abstract class DeactivatingDecommissioner extends Decommissioner {
	protected List<String> destroyedFolders;

	protected DeactivatingDecommissioner(DecommissioningService decommissioningService, AppLayerFactory appLayerFactory) {
		super(decommissioningService, appLayerFactory);
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
		if (configs.deleteFolderRecordsWithDestruction()) {
			delete(folder);
		}
		destroyedFolders.add(folder.getId());
		processDocumentsInDeleted(folder);
	}

	protected void processDeletedContainer(ContainerRecord container, DecomListContainerDetail detail) {
		if (isContainerEmpty(container, destroyedFolders)) {
			if (configs.isContainerRecyclingAllowed()) {
				add(decommissioningService.prepareToRecycle(container));
			} else {
				delete(container);
			}
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

	protected boolean shouldPurgeMinorVersions() {
		return decommissioningList.isFromActive() ?
				configs.purgeMinorVersionsOnTransfer() :
				configs.purgeMinorVersionsOnDeposit();
	}

	protected boolean shouldCreatePDFa() {
		return decommissioningList.isFromActive() ? configs.createPDFaOnTransfer() : configs.createPDFaOnDeposit();
	}
}

class DepositingDecommissioner extends DeactivatingDecommissioner {

	protected DepositingDecommissioner(DecommissioningService decommissioningService, AppLayerFactory appLayerFactory) {
		super(decommissioningService, appLayerFactory);
	}

	@Override
	protected FolderStatus getPermissionStatusFor(Folder folder, DecomListFolderDetail detail) {
		return FolderStatus.INACTIVE_DEPOSITED;
	}

	@Override
	protected void processFolder(Folder folder, DecomListFolderDetail detail) {
		removeManualArchivisticStatus(folder);
		processDepositedFolder(folder, detail);
	}

	@Override
	protected void processContainer(ContainerRecord container, DecomListContainerDetail detail) {
		processDepositedContainer(container, detail);
	}

	@Override
	protected void processDocuments() {
		cleanupDocuments(getListDocuments(), shouldPurgeMinorVersions(), shouldCreatePDFa(), new DocumentUpdater() {
			@Override
			public void update(Document document) {
				markDocumentDeposited(document);
			}
		});
	}
}

class DestroyingDecommissioner extends DeactivatingDecommissioner {

	protected DestroyingDecommissioner(DecommissioningService decommissioningService, AppLayerFactory appLayerFactory) {
		super(decommissioningService, appLayerFactory);
	}

	@Override
	protected FolderStatus getPermissionStatusFor(Folder folder, DecomListFolderDetail detail) {
		return FolderStatus.INACTIVE_DESTROYED;
	}

	@Override
	protected void processFolder(Folder folder, DecomListFolderDetail detail) {
		removeManualArchivisticStatus(folder);
		processDeletedFolder(folder, detail);
	}

	@Override
	protected void processContainer(ContainerRecord container, DecomListContainerDetail detail) {
		processDeletedContainer(container, detail);
	}

	@Override
	protected void processDocuments() {
		destroyDocuments(getListDocuments(), new DocumentUpdater() {
			@Override
			public void update(Document document) {
				markDocumentDestroyed(document);
			}
		});
	}
}

class SortingDecommissioner extends DeactivatingDecommissioner {
	private final boolean depositByDefault;

	protected SortingDecommissioner(DecommissioningService decommissioningService, boolean depositByDefault, AppLayerFactory appLayerFactory) {
		super(decommissioningService, appLayerFactory);
		this.depositByDefault = depositByDefault;
	}

	@Override
	protected FolderStatus getPermissionStatusFor(Folder folder, DecomListFolderDetail detail) {
		return shouldDeposit(folder, detail) ? FolderStatus.INACTIVE_DEPOSITED : FolderStatus.INACTIVE_DESTROYED;
	}

	@Override
	protected void processFolder(Folder folder, DecomListFolderDetail detail) {
		removeManualArchivisticStatus(folder);
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
	protected void processDocuments() {
		// This is never called on documents for the time being
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
