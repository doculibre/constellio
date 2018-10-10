package com.constellio.app.modules.rm.ui.pages.cart;

import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.extensions.api.RMModuleExtensions;
import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplate;
import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplateManager;
import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.cart.CartEmailService;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningService;
import com.constellio.app.modules.rm.wrappers.Cart;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.data.RecordVOWithDistinctSchemasDataProvider;
import com.constellio.app.ui.pages.search.batchProcessing.BatchProcessingPresenterService;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.extensions.ModelLayerCollectionExtensions;
import com.constellio.model.services.emails.EmailServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;

public class DefaultCartPresenter extends CartPresenter {
	private transient RMSchemasRecordsServices rm;
	private transient Cart cart;
	private String batchProcessSchemaType;

	private transient BatchProcessingPresenterService batchProcessingPresenterService;
	private transient ModelLayerCollectionExtensions modelLayerExtensions;
	private transient RMModuleExtensions rmModuleExtensions;

	public DefaultCartPresenter(DefaultCartView view) {
		super(view);
		modelLayerExtensions = modelLayerFactory.getExtensions().forCollection(view.getCollection());
		rmModuleExtensions = appLayerFactory.getExtensions().forCollection(view.getCollection()).forModule(ConstellioRMModule.ID);
		rm = new RMSchemasRecordsServices(collection, appLayerFactory);
	}

	public RecordVOWithDistinctSchemasDataProvider getFolderRecords() {
		final Metadata metadata = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection).getMetadata(Folder.DEFAULT_SCHEMA + "_" + Folder.FAVORITES_LIST);
		return new RecordVOWithDistinctSchemasDataProvider(
				getSchemas(), new RecordToVOBuilder(), modelLayerFactory, view.getSessionContext()) {
			@Override
			protected LogicalSearchQuery getQuery() {
				LogicalSearchQuery logicalSearchQuery = new LogicalSearchQuery(from(rm.folder.schemaType()).where(metadata).isContaining(asList(getCurrentUser().getId())))
						.filteredWithUser(getCurrentUser()).filteredByStatus(StatusFilter.ACTIVES)
						.sortAsc(Schemas.TITLE);
				return logicalSearchQuery;
			}
		};
	}

	public RecordVOWithDistinctSchemasDataProvider getDocumentRecords() {
		final Metadata metadata = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection).getMetadata(Document.DEFAULT_SCHEMA + "_" + Document.FAVORITES_LIST);
		return new RecordVOWithDistinctSchemasDataProvider(
				getSchemas(), new RecordToVOBuilder(), modelLayerFactory, view.getSessionContext()) {
			@Override
			protected LogicalSearchQuery getQuery() {
				return new LogicalSearchQuery(from(rm.documentSchemaType()).where(metadata).isContaining(asList(getCurrentUser().getId())))
						.filteredWithUser(getCurrentUser()).filteredByStatus(StatusFilter.ACTIVES)
						.sortAsc(Schemas.TITLE);
			}
		};
	}

	public RecordVOWithDistinctSchemasDataProvider getContainerRecords() {
		final Metadata metadata = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection).getMetadata(ContainerRecord.DEFAULT_SCHEMA + "_" + ContainerRecord.FAVORITES_LIST);
		return new RecordVOWithDistinctSchemasDataProvider(
				getSchemas(), new RecordToVOBuilder(), modelLayerFactory, view.getSessionContext()) {
			@Override
			protected LogicalSearchQuery getQuery() {
				return new LogicalSearchQuery(from(rm.documentSchemaType()).where(metadata).isContaining(asList(getCurrentUser().getId())))
						.filteredWithUser(getCurrentUser()).filteredByStatus(StatusFilter.ACTIVES)
						.sortAsc(Schemas.TITLE);
			}
		};
	}

	public List<LabelTemplate> getCustomTemplates(String schemaType) {
		LabelTemplateManager labelTemplateManager = appLayerFactory.getLabelTemplateManager();
		return labelTemplateManager.listExtensionTemplates(schemaType);
	}

	public List<LabelTemplate> getDefaultTemplates(String schemaType) {
		LabelTemplateManager labelTemplateManager = appLayerFactory.getLabelTemplateManager();
		return labelTemplateManager.listTemplates(schemaType);
	}

	public List<String> getNotDeletedRecordsIds(String schemaType) {
		User currentUser = getCurrentUser();
		switch (schemaType) {
			case Folder.SCHEMA_TYPE:
				List<String> folders = getFoldersIds();
				return getNonDeletedRecordsIds(rm.getFolders(folders), currentUser);
			case Document.SCHEMA_TYPE:
				List<String> documents = getDocumentsIds();
				return getNonDeletedRecordsIds(rm.getDocuments(documents), currentUser);
			case ContainerRecord.SCHEMA_TYPE:
				List<String> containers = getContainersIds();
				return getNonDeletedRecordsIds(rm.getContainerRecords(containers), currentUser);
			default:
				throw new RuntimeException("Unsupported type : " + schemaType);
		}
	}

	public List<String> getFoldersIds() {
		List<Folder> folders = getFolders();
		List<String> foldersIds = new ArrayList<>();
		for (Folder folder : folders) {
			foldersIds.add(folder.getId());
		}
		return foldersIds;
	}

	private List<Folder> getFolders() {
		final Metadata metadata = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection).getMetadata(Folder.DEFAULT_SCHEMA + "_" + Folder.FAVORITES_LIST);
		LogicalSearchQuery logicalSearchQuery = new LogicalSearchQuery(from(rm.folder.schemaType()).where(metadata).isContaining(asList(getCurrentUser().getId())));
		return rm.searchFolders(logicalSearchQuery);
	}

	public List<String> getDocumentsIds() {
		List<Document> documents = getDocuments();
		List<String> documentsIds = new ArrayList<>();
		for (Document document : documents) {
			documentsIds.add(document.getId());
		}
		return documentsIds;
	}

	private List<Document> getDocuments() {
		final Metadata metadata = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection).getMetadata(Document.DEFAULT_SCHEMA + "_" + Document.FAVORITES_LIST);
		LogicalSearchQuery logicalSearchQuery = new LogicalSearchQuery(from(rm.document.schemaType()).where(metadata).isContaining(asList(getCurrentUser().getId())));
		return rm.searchDocuments(logicalSearchQuery);
	}

	public List<String> getContainersIds() {
		List<ContainerRecord> containers = getContainers();
		List<String> containersIds = new ArrayList<>();
		for (ContainerRecord container : containers) {
			containersIds.add(container.getId());
		}
		return containersIds;
	}

	private List<ContainerRecord> getContainers() {
		final Metadata metadata = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection).getMetadata(ContainerRecord.DEFAULT_SCHEMA + "_" + ContainerRecord.FAVORITES_LIST);
		LogicalSearchQuery logicalSearchQuery = new LogicalSearchQuery(from(rm.containerRecord.schemaType()).where(metadata).isContaining(asList(getCurrentUser().getId())));
		return rm.searchContainerRecords(logicalSearchQuery);
	}

	private List<String> getNonDeletedRecordsIds(List<? extends RecordWrapper> records, User currentUser) {
		ArrayList<String> ids = new ArrayList<>();
		for (RecordWrapper record : records) {
			if (!record.isLogicallyDeletedStatus() && currentUser.hasReadAccess().on(record)) {
				ids.add(record.getId());
			}
		}
		return ids;
	}

	public boolean isLabelsButtonVisible(String schemaType) {
		switch (schemaType) {
			case Folder.SCHEMA_TYPE:
				return getFolders().size() != 0;
			case ContainerRecord.SCHEMA_TYPE:
				return getContainers().size() != 0;
			default:
				throw new RuntimeException("No labels for type : " + schemaType);
		}
	}

	public boolean canPrepareEmail() {
		// TODO: Maybe better test
		return cartHasRecords() && getContainers().isEmpty();
	}

	public void emailPreparationRequested() {
		EmailServices.EmailMessage emailMessage = new CartEmailService(collection, modelLayerFactory).createEmailForCart(getCurrentUser().getId(), getDocumentsIds(), getCurrentUser());
		String filename = emailMessage.getFilename();
		InputStream stream = emailMessage.getInputStream();
		view.startDownload(stream, filename);
	}

	public void duplicationRequested() {
		if (!canDuplicate()) {
			view.showErrorMessage($("CartView.cannotDuplicate"));
			return;
		}
		List<Folder> folders = getFolders();
		for (Folder folder : folders) {
			if (!rmModuleExtensions.isCopyActionPossibleOnFolder(folder, getCurrentUser())) {
				view.showErrorMessage($("CartView.actionBlockedByExtension"));
				return;
			}
		}

		try {
			DecommissioningService service = new DecommissioningService(view.getCollection(), appLayerFactory);
			for (Folder folder : folders) {
				if (!folder.isLogicallyDeletedStatus()) {
					service.duplicateStructureAndSave(folder, getCurrentUser());
				}
			}
			view.showMessage($("CartView.duplicated"));
		} catch (RecordServicesException.ValidationException e) {
			view.showErrorMessage($(e.getErrors()));
		} catch (Exception e) {
			view.showErrorMessage(e.getMessage());
		}
	}

	public boolean canDuplicate() {
		return cartHasOnlyFolders() && canDuplicateFolders(getCurrentUser());
	}

	private boolean cartHasOnlyFolders() {
		return !getFolders().isEmpty() && getDocuments().isEmpty() && getContainers().isEmpty();
	}

	private boolean canDuplicateFolders(User user) {
		for (Folder folder : getFolders()) {
			RecordWrapper parent = folder.getParentFolder() != null ?
								   rm().getFolder(folder.getParentFolder()) :
								   rm().getAdministrativeUnit(folder.getAdministrativeUnitEntered());
			if (!user.hasWriteAccess().on(parent)) {
				return false;
			}
			switch (folder.getPermissionStatus()) {
				case SEMI_ACTIVE:
					if (!user.has(RMPermissionsTo.DUPLICATE_SEMIACTIVE_FOLDER).on(folder)) {
						return false;
					}
					break;
				case INACTIVE_DEPOSITED:
				case INACTIVE_DESTROYED:
					if (!user.has(RMPermissionsTo.DUPLICATE_INACTIVE_FOLDER).on(folder)) {
						return false;
					}
					break;
			}
		}
		return true;
	}

	private boolean cartHasRecords() {
		return !getFoldersIds().isEmpty();
	}

	public void displayRecordRequested(RecordVO recordVO) {
	}

	public void itemRemovalRequested(RecordVO recordVO) {
	}

	public void deletionRequested(String reason) {
		if (!canDelete()) {
			view.showErrorMessage($("CartView.cannotDelete"));
			return;
		}
		for (Record record : recordServices().getRecordsById(view.getCollection(), getAllItems())) {
			if (modelLayerExtensions.isDeleteBlocked(record, getCurrentUser())) {
				view.showErrorMessage($("CartView.actionBlockedByExtension"));
				return;
			}
		}

		for (Record record : recordServices().getRecordsById(view.getCollection(), getAllItems())) {
			delete(record, reason);
		}
		cartEmptyingRequested();
	}

	public void cartEmptyingRequested() {
		for (Folder folder : getFolders()) {
			folder.removeFavorite(getCurrentUser().getId());
			addOrUpdate(folder.getWrappedRecord());
		}
		view.navigate().to(RMViews.class).defaultCart();
	}

	public List<String> getAllItems() {
		List<String> result = new ArrayList<>();
		result.addAll(getFoldersIds());
		result.addAll(getDocumentsIds());
		result.addAll(getContainersIds());
		return result;
	}

	public boolean canDelete() {
		return cartHasRecords() && getContainers().isEmpty()
			   && canDeleteFolders(getCurrentUser()) && canDeleteDocuments(getCurrentUser());
	}

	private boolean canDeleteFolders(User user) {
		for (Folder folder : getFolders()) {
			if (!user.hasDeleteAccess().on(folder)) {
				return false;
			}
			switch (folder.getPermissionStatus()) {
				case SEMI_ACTIVE:
					if (!user.has(RMPermissionsTo.DELETE_SEMIACTIVE_FOLDERS).on(folder)) {
						return false;
					}
					break;
				case INACTIVE_DEPOSITED:
				case INACTIVE_DESTROYED:
					if (!user.has(RMPermissionsTo.DELETE_INACTIVE_FOLDERS).on(folder)) {
						return false;
					}
					break;
			}
		}
		return true;
	}

	private boolean canDeleteDocuments(User user) {
		for (Document document : getDocuments()) {
			if (!user.hasDeleteAccess().on(document)) {
				return false;
			}
			switch (document.getArchivisticStatus()) {
				case SEMI_ACTIVE:
					if (!user.has(RMPermissionsTo.DELETE_SEMIACTIVE_DOCUMENT).on(document)) {
						return false;
					}
					break;
				case INACTIVE_DEPOSITED:
				case INACTIVE_DESTROYED:
					if (!user.has(RMPermissionsTo.DELETE_INACTIVE_DOCUMENT).on(document)) {
						return false;
					}
			}
			if (document.isPublished() && !user.has(RMPermissionsTo.DELETE_PUBLISHED_DOCUMENT).on(document)) {
				return false;
			}
			if (getCurrentBorrowerOf(document) != null && !getCurrentUser().has(RMPermissionsTo.DELETE_BORROWED_DOCUMENT)
					.on(document)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean processBatchButtonClicked(String selectedType, String schemaType, RecordVO viewObject)
			throws RecordServicesException {
		return processBatchButtonClicked(selectedType, getNotDeletedRecordsIds(schemaType), viewObject);
	}

	public boolean processBatchButtonClicked(String selectedType, List<String> records, RecordVO viewObject)
			throws RecordServicesException {
		for (Record record : recordServices().getRecordsById(view.getCollection(), records)) {
			if (modelLayerExtensions.isModifyBlocked(record, getCurrentUser())) {
				view.showErrorMessage($("CartView.actionBlockedByExtension"));
				return false;
			}
		}

		batchProcessingPresenterService()
				.execute(selectedType, records, viewObject, getCurrentUser());
		view.navigate().to(RMViews.class).defaultCart();
		return true;
	}
}
