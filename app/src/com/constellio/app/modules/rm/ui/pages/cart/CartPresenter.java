package com.constellio.app.modules.rm.ui.pages.cart;

import static com.constellio.app.modules.rm.model.enums.FolderStatus.ACTIVE;
import static com.constellio.app.modules.rm.model.enums.FolderStatus.SEMI_ACTIVE;
import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.entities.schemas.Schemas.IDENTIFIER;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;

import java.io.InputStream;
import java.util.*;

import com.constellio.app.entities.schemasDisplay.MetadataDisplayConfig;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.extensions.AppLayerCollectionExtensions;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.model.enums.DecomListStatus;
import com.constellio.app.modules.rm.model.enums.DecommissioningListType;
import com.constellio.app.modules.rm.model.enums.FolderStatus;
import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplate;
import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplateManager;
import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.reports.builders.search.SearchResultReportParameters;
import com.constellio.app.modules.rm.reports.builders.search.SearchResultReportWriterFactory;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.cart.CartEmailService;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningService;
import com.constellio.app.modules.rm.ui.builders.DocumentToVOBuilder;
import com.constellio.app.modules.rm.ui.builders.FolderToVOBuilder;
import com.constellio.app.modules.rm.ui.entities.DocumentVO;
import com.constellio.app.modules.rm.ui.entities.FolderVO;
import com.constellio.app.modules.rm.wrappers.Cart;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.MetadataToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.components.NewReportPresenter;
import com.constellio.app.ui.framework.components.RecordFieldFactory;
import com.constellio.app.ui.framework.data.RecordVOWithDistinctSchemasDataProvider;
import com.constellio.app.ui.framework.reports.NewReportWriterFactory;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.app.ui.pages.search.batchProcessing.BatchProcessingPresenter;
import com.constellio.app.ui.pages.search.batchProcessing.BatchProcessingPresenterService;
import com.constellio.app.ui.pages.search.batchProcessing.entities.BatchProcessResults;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.batchprocess.BatchProcess;
import com.constellio.model.entities.batchprocess.BatchProcessAction;
import com.constellio.model.entities.enums.BatchProcessingMode;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.services.batch.actions.ChangeValueOfMetadataBatchProcessAction;
import com.constellio.model.services.batch.manager.BatchProcessesManager;
import com.constellio.model.services.emails.EmailServices.EmailMessage;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.reports.ReportServices;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

public class CartPresenter extends SingleSchemaBasePresenter<CartView> implements BatchProcessingPresenter, NewReportPresenter {
	private transient RMSchemasRecordsServices rm;
	private transient Cart cart;
	private String cartId;
	private String batchProcessSchemaType;

	private transient BatchProcessingPresenterService batchProcessingPresenterService;

	public CartPresenter(CartView view) {
		super(view, Cart.DEFAULT_SCHEMA);
	}

	public void itemRemovalRequested(RecordVO record) {
		Cart cart = cart();
		switch (record.getSchema().getTypeCode()) {
		case Folder.SCHEMA_TYPE:
			cart.removeFolder(record.getId());
			break;
		case Document.SCHEMA_TYPE:
			cart.removeDocument(record.getId());
			break;
		case ContainerRecord.SCHEMA_TYPE:
			cart.removeContainer(record.getId());
			break;
		}
		addOrUpdate(cart.getWrappedRecord());
		view.navigate().to(RMViews.class).cart(cart.getId());
	}

	public boolean canEmptyCart() {
		return cartHasRecords();
	}

	public void cartEmptyingRequested() {
		addOrUpdate(cart().empty().getWrappedRecord());
		view.navigate().to(RMViews.class).cart(cart().getId());
	}

	public boolean canPrepareEmail() {
		// TODO: Maybe better test
		return cartHasRecords() && cart().getContainers().isEmpty();
	}

	public void emailPreparationRequested() {
		EmailMessage emailMessage = new CartEmailService(collection, modelLayerFactory).createEmailForCart(cart());
		String filename = emailMessage.getFilename();
    	InputStream stream = emailMessage.getInputStream();
		view.startDownload(stream, filename);
	}

	public boolean canDuplicate() {
		return cartHasOnlyFolders() && canDuplicateFolders(getCurrentUser());
	}

	public void duplicationRequested() {
		if (!canDuplicate()) {
			view.showErrorMessage($("CartView.cannotDuplicate"));
			return;
		}
		try {
			DecommissioningService service = new DecommissioningService(view.getCollection(), appLayerFactory);
			for (Folder folder : getCartFolders()) {
				if(!folder.isLogicallyDeletedStatus()) {
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

	public boolean canDelete() {
		return cartHasRecords() && cart().getContainers().isEmpty()
				&& canDeleteFolders(getCurrentUser()) && canDeleteDocuments(getCurrentUser());
	}

	public void deletionRequested(String reason) {
		if (!canDelete()) {
			view.showErrorMessage($("CartView.cannotDelete"));
			return;
		}
		for (Record record : recordServices().getRecordsById(view.getCollection(), cart().getAllItems())) {
			delete(record, reason);
		}
		cartEmptyingRequested();
	}

	public RecordVOWithDistinctSchemasDataProvider getFolderRecords() {
		return new RecordVOWithDistinctSchemasDataProvider(
				getSchemas(), new RecordToVOBuilder(), modelLayerFactory, view.getSessionContext()) {
			@Override
			protected LogicalSearchQuery getQuery() {
				return new LogicalSearchQuery(from(rm.folder.schemaType()).where(Schemas.IDENTIFIER).isIn(cart().getAllItems()))
						.filteredWithUser(getCurrentUser()).filteredByStatus(StatusFilter.ACTIVES)
						.sortAsc(Schemas.TITLE);
			}
		};
	}

	public RecordVOWithDistinctSchemasDataProvider getDocumentRecords() {
		return new RecordVOWithDistinctSchemasDataProvider(
				getSchemas(), new RecordToVOBuilder(), modelLayerFactory, view.getSessionContext()) {
			@Override
			protected LogicalSearchQuery getQuery() {
				return new LogicalSearchQuery(from(rm.documentSchemaType()).where(Schemas.IDENTIFIER).isIn(cart().getAllItems()))
						.filteredWithUser(getCurrentUser()).filteredByStatus(StatusFilter.ACTIVES)
						.sortAsc(Schemas.TITLE);
			}
		};
	}

	public RecordVOWithDistinctSchemasDataProvider getContainerRecords() {
		return new RecordVOWithDistinctSchemasDataProvider(
				getSchemas(), new RecordToVOBuilder(), modelLayerFactory, view.getSessionContext()) {
			@Override
			protected LogicalSearchQuery getQuery() {
				User user = getCurrentUser();
				if (user.hasAny(RMPermissionsTo.DISPLAY_CONTAINERS, RMPermissionsTo.MANAGE_CONTAINERS).globally()) {
					return new LogicalSearchQuery(
							from(rm.containerRecord.schemaType()).where(Schemas.IDENTIFIER).isIn(cart().getAllItems()))
							.filteredByStatus(StatusFilter.ACTIVES)
							.sortAsc(Schemas.TITLE);
				} else if (user.hasAny(RMPermissionsTo.DISPLAY_CONTAINERS, RMPermissionsTo.MANAGE_CONTAINERS).onSomething()) {
					List<String> adminUnitIds = getConceptsWithPermissionsForCurrentUser(RMPermissionsTo.DISPLAY_CONTAINERS, RMPermissionsTo.MANAGE_CONTAINERS);
					return new LogicalSearchQuery(
							from(rm.containerRecord.schemaType()).where(Schemas.IDENTIFIER).isIn(cart().getAllItems())
							.andWhere(schema(ContainerRecord.DEFAULT_SCHEMA).getMetadata(ContainerRecord.ADMINISTRATIVE_UNIT)).isIn(adminUnitIds))
							.filteredByStatus(StatusFilter.ACTIVES)
							.sortAsc(Schemas.TITLE);
				} else {
					return LogicalSearchQuery.returningNoResults();
				}
			}
		};
	}

	private List<MetadataSchemaVO> getSchemas() {
		MetadataSchemaToVOBuilder builder = new MetadataSchemaToVOBuilder();
		return Arrays.asList(
				builder.build(schema(Folder.DEFAULT_SCHEMA), VIEW_MODE.TABLE, view.getSessionContext()),
				builder.build(schema(Folder.DEFAULT_SCHEMA), VIEW_MODE.TABLE, view.getSessionContext()),
				builder.build(schema(Folder.DEFAULT_SCHEMA), VIEW_MODE.TABLE, view.getSessionContext()));
	}

	Cart cart() {
		if (cart == null) {
			//			cart = rm().getOrCreateUserCart(getCurrentUser());
			cart = rm().getCart(cartId);
		}
		return cart;
	}

	private boolean cartHasRecords() {
		return !cart().isEmpty();
	}

	private boolean cartHasOnlyFolders() {
		return !cart().getFolders().isEmpty() && cart().getDocuments().isEmpty() && cart().getContainers().isEmpty();
	}

	private boolean canDuplicateFolders(User user) {
		for (Folder folder : getCartFolders()) {
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

	private boolean canDeleteFolders(User user) {
		for (Folder folder : getCartFolders()) {
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
		for (Document document : getCartDocuments()) {
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

	private String getCurrentBorrowerOf(Document document) {
		return document.getContent() == null ? null : document.getContent().getCheckoutUserId();
	}

	List<Folder> getCartFolders() {
		return rm().wrapFolders(recordServices().getRecordsById(view.getCollection(), cart().getFolders()));
	}

	List<Folder> getNotDeletedCartFolders() {
		List<Folder> cartFolders = getCartFolders();
		Iterator<Folder> iterator = cartFolders.iterator();
		while (iterator.hasNext()) {
			Folder currentFolder = iterator.next();
			if(currentFolder.isLogicallyDeletedStatus()) {
				iterator.remove();
			}
		}
		return cartFolders;
	}

	List<FolderVO> getNotDeletedCartFoldersVO(){
		FolderToVOBuilder builder = new FolderToVOBuilder();
		List<FolderVO> folderVOS = new ArrayList<>();
		for(Folder folder : this.getCartFolders()){
			if(!folder.isLogicallyDeletedStatus()) {
				folderVOS.add(builder.build(folder.getWrappedRecord(), VIEW_MODE.DISPLAY, view.getSessionContext()));
			}
		}
		return folderVOS;
	}

	List<DocumentVO> getNotDeletedCartDocumentVO() {
		DocumentToVOBuilder builder = new DocumentToVOBuilder(modelLayerFactory);
		List<DocumentVO> documentVOS = new ArrayList<>();
		for(Document document : this.getCartDocuments()) {
			if(!document.isLogicallyDeletedStatus()) {
				documentVOS.add(builder.build(document.getWrappedRecord(), VIEW_MODE.DISPLAY, view.getSessionContext()));
			}
		}
		return documentVOS;
	}

	List<String> getCartFolderIds() {
		return cart().getFolders();
	}

	List<String> getCartContainerIds() {
		return cart().getContainers();
	}

	List<String> getCartDocumentIds() {
		return cart().getDocuments();
	}

	private List<Document> getCartDocuments() {
		return rm().wrapDocuments(recordServices().getRecordsById(view.getCollection(), cart().getDocuments()));
	}

	private RMSchemasRecordsServices rm() {
		if (rm == null) {
			rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		}
		return rm;
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return true;
	}

	public void forParams(String parameters) {
		cartId = parameters;
	}

	public List<String> getNotDeletedRecordsIds(String schemaType) {
		switch (schemaType) {
		case Folder.SCHEMA_TYPE:
			List<String> folders = cart().getFolders();
			return getNonDeletedRecordsIds(rm.getFolders(folders));
		case Document.SCHEMA_TYPE:
			List<String> documents = cart().getDocuments();
			return getNonDeletedRecordsIds(rm.getDocuments(documents));
		case ContainerRecord.SCHEMA_TYPE:
			List<String> containers = cart().getContainers();
			return getNonDeletedRecordsIds(rm.getContainerRecords(containers));
		default:
			throw new RuntimeException("Unsupported type : " + schemaType);
		}
	}

	private List<String> getNonDeletedRecordsIds(List<? extends RecordWrapper> records) {
		ArrayList<String> ids = new ArrayList<>();
		for(RecordWrapper record: records) {
			if(!record.isLogicallyDeletedStatus()) {
				ids.add(record.getId());
			}
		}
		return ids;
	}

	@Override
	public String getOriginType(String schemaType) {
		return batchProcessingPresenterService().getOriginType(getNotDeletedRecordsIds(schemaType));
	}

	public String getOriginType(List<String> selectedRecordIds) {
		return batchProcessingPresenterService().getOriginType(selectedRecordIds);
	}

	@Override
	public RecordVO newRecordVO(String schema, String schemaType, SessionContext sessionContext) {
		return newRecordVO(getNotDeletedRecordsIds(schemaType), schema, sessionContext);
	}

	public RecordVO newRecordVO(List<String> selectedRecordIds, String schema, SessionContext sessionContext) {
		return batchProcessingPresenterService().newRecordVO(schema, sessionContext, selectedRecordIds);
	}

	@Override
	public InputStream simulateButtonClicked(String selectedType, String schemaType, RecordVO viewObject)
			throws RecordServicesException {
		return simulateButtonClicked(selectedType, getNotDeletedRecordsIds(schemaType), viewObject);
	}

	public InputStream simulateButtonClicked(String selectedType, List<String> records, RecordVO viewObject)
			throws RecordServicesException {
		BatchProcessResults results = batchProcessingPresenterService()
				.simulate(selectedType, records, viewObject, getCurrentUser());
		return batchProcessingPresenterService().formatBatchProcessingResults(results);
	}

	@Override
	public void processBatchButtonClicked(String selectedType, String schemaType, RecordVO viewObject)
			throws RecordServicesException {
		processBatchButtonClicked(selectedType, getNotDeletedRecordsIds(schemaType), viewObject);
	}

	public void processBatchButtonClicked(String selectedType, List<String> records, RecordVO viewObject)
			throws RecordServicesException {
		batchProcessingPresenterService()
				.execute(selectedType, records, viewObject, getCurrentUser());
		view.navigate().to(RMViews.class).cart(cartId);
	}

	@Override
	public boolean hasWriteAccessOnAllRecords(String schemaType) {
		return hasWriteAccessOnAllRecords(getNotDeletedRecordsIds(schemaType));
	}

	@Override
	public long getNumberOfRecords(String schemaType) {
		return (long) getNotDeletedRecordsIds(schemaType).size();
	}

	public boolean hasWriteAccessOnAllRecords(List<String> selectedRecordIds) {
		return batchProcessingPresenterService().hasWriteAccessOnAllRecords(getCurrentUser(), selectedRecordIds);
	}

	@Override
	public BatchProcessingMode getBatchProcessingMode() {
		return batchProcessingPresenterService().getBatchProcessingMode();
	}

	@Override
	public AppLayerCollectionExtensions getBatchProcessingExtension() {
		return batchProcessingPresenterService().getBatchProcessingExtension();
	}

	@Override
	public String getSchema(String schemaType, String type) {
		return batchProcessingPresenterService().getSchema(schemaType, type);
	}

	@Override
	public String getTypeSchemaType(String schemaType) {
		return batchProcessingPresenterService().getTypeSchemaType(schemaType);
	}

	@Override
	public RecordFieldFactory newRecordFieldFactory(String schemaType, String selectedType) {
		return newRecordFieldFactory(schemaType, selectedType, getNotDeletedRecordsIds(schemaType));
	}

	public RecordFieldFactory newRecordFieldFactory(String schemaType, String selectedType, List<String> records) {
		return batchProcessingPresenterService().newRecordFieldFactory(schemaType, selectedType, records);
	}

	public List<LabelTemplate> getCustomTemplates(String schemaType) {
		LabelTemplateManager labelTemplateManager = appLayerFactory.getLabelTemplateManager();
		return labelTemplateManager.listExtensionTemplates(schemaType);
	}

	public List<LabelTemplate> getDefaultTemplates(String schemaType) {
		LabelTemplateManager labelTemplateManager = appLayerFactory.getLabelTemplateManager();
		return labelTemplateManager.listTemplates(schemaType);
	}

	public boolean isLabelsButtonVisible(String schemaType) {
		switch (schemaType) {
		case Folder.SCHEMA_TYPE:
			return cart().getFolders().size() != 0;
		case ContainerRecord.SCHEMA_TYPE:
			return cart().getContainers().size() != 0;
		default:
			throw new RuntimeException("No labels for type : " + schemaType);
		}
	}

	public boolean isBatchProcessingButtonVisible(String schemaType) {
		boolean hasRightToProcessSchemaType = true;
		if (ContainerRecord.SCHEMA_TYPE.equals(schemaType) && !getCurrentUser().has(RMPermissionsTo.MANAGE_CONTAINERS)
				.onSomething()) {
			hasRightToProcessSchemaType = false;
		}
		return getNotDeletedRecordsIds(schemaType).size() != 0 && hasRightToProcessSchemaType;
	}

	public void shareWithUsersRequested(List<String> userids) {
		cart().setSharedWithUsers(userids);
		addOrUpdate(cart().getWrappedRecord());
	}

	BatchProcessingPresenterService batchProcessingPresenterService() {
		if (batchProcessingPresenterService == null) {
			Locale locale = view.getSessionContext().getCurrentLocale();
			batchProcessingPresenterService = new BatchProcessingPresenterService(collection, appLayerFactory, locale);
		}
		return batchProcessingPresenterService;
	}

	List<DecommissioningListType> getCommonDecommissioningListTypes(List<Folder> folders) {
		List<DecommissioningListType> commonTypes = new ArrayList<>();
		boolean first = true;

		FolderStatus folderStatus = null;

		for (Folder folder : folders) {
			if (folderStatus == null) {
				folderStatus = folder.getArchivisticStatus();
			} else if (folderStatus != folder.getArchivisticStatus()) {
				return commonTypes;
			}
		}

		for (Folder folder : folders) {
			if (first) {
				commonTypes.addAll(findDecommissioningListTypes(folder));

				first = false;
			} else {
				List<DecommissioningListType> types = findDecommissioningListTypes(folder);
				Iterator<DecommissioningListType> commonTypesIterator = commonTypes.iterator();
				while (commonTypesIterator.hasNext()) {
					if (!types.contains(commonTypesIterator.next())) {
						commonTypesIterator.remove();
					}
				}
			}

		}

		return commonTypes;
	}

	private List<DecommissioningListType> findDecommissioningListTypes(Folder folder) {
		List<DecommissioningListType> types = new ArrayList<>();
		if (folder.getCloseDate() == null) {
			types.add(DecommissioningListType.FOLDERS_TO_CLOSE);

		} else {
			if (folder.getArchivisticStatus() == ACTIVE) {
				types.add(DecommissioningListType.FOLDERS_TO_TRANSFER);
			}
			if (folder.getArchivisticStatus() == SEMI_ACTIVE || folder.getArchivisticStatus() == ACTIVE) {
				if (folder.getExpectedDepositDate() != null) {
					types.add(DecommissioningListType.FOLDERS_TO_DEPOSIT);
				}
				if (folder.getExpectedDestructionDate() != null) {
					types.add(DecommissioningListType.FOLDERS_TO_DESTROY);
				}
			}
		}

		return types;
	}

	String getCommonAdministrativeUnit(List<Folder> folders) {
		String administrativeUnit = null;

		for (Folder folder : folders) {
			if (administrativeUnit == null) {
				administrativeUnit = folder.getAdministrativeUnit();
			} else {
				if (!administrativeUnit.equals(folder.getAdministrativeUnit())) {
					return null;
				}
			}
		}

		return administrativeUnit;
	}

	public void buildDecommissioningListRequested(String title, DecommissioningListType decomType) {
		DecommissioningList list = rm.newDecommissioningList();
		list.setTitle(title);
		list.setAdministrativeUnit(getCommonAdministrativeUnit(getCartFolders()));
		list.setDecommissioningListType(decomType);
		list.setFolderDetailsFor(getNotDeletedCartFolders());

		try {
			recordServices().add(list);
			view.navigate().to(RMViews.class).displayDecommissioningList(list.getId());
		} catch (RecordServicesException e) {
			e.printStackTrace();
		}
	}

	public void displayRecordRequested(RecordVO recordVO) {
		switch (recordVO.getSchema().getTypeCode()) {
		case Folder.SCHEMA_TYPE:
			view.navigate().to(RMViews.class).displayFolder(recordVO.getId());
			break;
		case Document.SCHEMA_TYPE:
			view.navigate().to(RMViews.class).displayDocument(recordVO.getId());
			break;
		case ContainerRecord.SCHEMA_TYPE:
			view.navigate().to(RMViews.class).displayContainer(recordVO.getId());
			break;
		}
	}

	public void folderFilterButtonClicked() {
		view.filterFolderTable();
	}

	public void documentFilterButtonClicked() {
		view.filterDocumentTable();
	}

	public void containerFilterButtonClicked() {
		view.filterContainerTable();
	}

	public RecordVOWithDistinctSchemasDataProvider getFilteredFolderRecords(final String freeText) {
		return new RecordVOWithDistinctSchemasDataProvider(
				getSchemas(), new RecordToVOBuilder(), modelLayerFactory, view.getSessionContext()) {
			@Override
			protected LogicalSearchQuery getQuery() {
				return new LogicalSearchQuery(from(rm.folder.schemaType()).where(Schemas.IDENTIFIER).isIn(cart().getAllItems()))
						.filteredWithUser(getCurrentUser()).filteredByStatus(StatusFilter.ACTIVES).setFreeTextQuery(freeText)
						.sortAsc(Schemas.TITLE);
			}
		};
	}

	public RecordVOWithDistinctSchemasDataProvider getFilteredDocumentRecords(final String freeText) {
		return new RecordVOWithDistinctSchemasDataProvider(
				getSchemas(), new RecordToVOBuilder(), modelLayerFactory, view.getSessionContext()) {
			@Override
			protected LogicalSearchQuery getQuery() {
				return new LogicalSearchQuery(from(rm.documentSchemaType()).where(Schemas.IDENTIFIER).isIn(cart().getAllItems()))
						.filteredWithUser(getCurrentUser()).filteredByStatus(StatusFilter.ACTIVES).setFreeTextQuery(freeText)
						.sortAsc(Schemas.TITLE);
			}
		};
	}

	public RecordVOWithDistinctSchemasDataProvider getFilteredContainerRecords(final String freeText) {
		return new RecordVOWithDistinctSchemasDataProvider(
				getSchemas(), new RecordToVOBuilder(), modelLayerFactory, view.getSessionContext()) {
			@Override
			protected LogicalSearchQuery getQuery() {
				User user = getCurrentUser();
				if (user.hasAny(RMPermissionsTo.DISPLAY_CONTAINERS, RMPermissionsTo.MANAGE_CONTAINERS).globally()) {
					return new LogicalSearchQuery(
							from(rm.containerRecord.schemaType()).where(Schemas.IDENTIFIER).isIn(cart().getAllItems()))
							.filteredByStatus(StatusFilter.ACTIVES).setFreeTextQuery(freeText)
							.sortAsc(Schemas.TITLE);
				} else if (user.hasAny(RMPermissionsTo.DISPLAY_CONTAINERS, RMPermissionsTo.MANAGE_CONTAINERS).onSomething()) {
					List<String> adminUnitIds = getConceptsWithPermissionsForCurrentUser(RMPermissionsTo.DISPLAY_CONTAINERS, RMPermissionsTo.MANAGE_CONTAINERS);
					return new LogicalSearchQuery(
							from(rm.containerRecord.schemaType()).where(Schemas.IDENTIFIER).isIn(cart().getAllItems())
							.andWhere(schema(ContainerRecord.DEFAULT_SCHEMA).getMetadata(ContainerRecord.ADMINISTRATIVE_UNIT)).isIn(adminUnitIds))
							.filteredByStatus(StatusFilter.ACTIVES).setFreeTextQuery(freeText)
							.sortAsc(Schemas.TITLE);
				} else {
					return LogicalSearchQuery.returningNoResults();
				}
			}
		};
	}

	@Override
	public List<String> getSupportedReports() {
		List<String> supportedReports = new ArrayList<>();
		ReportServices reportServices = new ReportServices(modelLayerFactory, collection);
		List<String> userReports = reportServices.getUserReportTitles(getCurrentUser(), view.getCurrentSchemaType());
		supportedReports.addAll(userReports);
		return supportedReports;
	}

	@Override
	public NewReportWriterFactory<SearchResultReportParameters> getReport(String report) {
		return new SearchResultReportWriterFactory(appLayerFactory);
	}

	@Override
	public Object getReportParameters(String report) {
		List<String> recordids = getNotDeletedRecordsIds(view.getCurrentSchemaType());
		LogicalSearchQuery query = new LogicalSearchQuery(from(rm.schemaType(view.getCurrentSchemaType())).returnAll());

		return new SearchResultReportParameters(recordids, view.getCurrentSchemaType(),
				collection, report, getCurrentUser(), query);
	}

	public void backButtonClicked() {
		view.navigate().to(RMViews.class).listCarts();
	}

	public CartPresenter setBatchProcessSchemaType(String batchProcessSchemaType) {
		this.batchProcessSchemaType = batchProcessSchemaType;
		return this;
	}

	@Override
	public void allSearchResultsButtonClicked() {
		throw new RuntimeException("Should not have been called");
	}

	@Override
	public void selectedSearchResultsButtonClicked() {
		throw new RuntimeException("Should not have been called");
	}

	@Override
	public boolean isSearchResultsSelectionForm() {
		return false;
	}

	public boolean isAnyFolderBorrowed() {
		return searchServices().getResultsCount(from(rm().folder.schemaType()).where(rm().folder.borrowed()).isTrue()
				.andWhere(Schemas.IDENTIFIER).isIn(getCartFolderIds())) > 0;
	}

	public boolean isAnyFolderInDecommissioningList() {
		return searchServices().getResultsCount(
				from(rm().decommissioningList.schemaType()).where(rm().decommissioningList.status())
						.isNotEqual(DecomListStatus.PROCESSED)
						.andWhere(rm().decommissioningList.folders()).isContaining(getCartFolderIds())) > 0;
	}

	public void batchEditRequested(String code, Object value, String schemaType) {
		Map<String, Object> changes = new HashMap<>();
		changes.put(code, value);
		BatchProcessAction action = new ChangeValueOfMetadataBatchProcessAction(changes);
		String username = getCurrentUser() == null ? null : getCurrentUser().getUsername();

		BatchProcessesManager manager = modelLayerFactory.getBatchProcessesManager();
		LogicalSearchCondition condition = fromAllSchemasIn(collection).where(IDENTIFIER).isIn(getNotDeletedRecordsIds(schemaType));
		BatchProcess process = manager.addBatchProcessInStandby(condition, action, username, "userBatchProcess");
		manager.markAsPending(process);
	}

	public List<MetadataVO> getMetadataAllowedInBatchEdit(String schemaType) {
		MetadataToVOBuilder builder = new MetadataToVOBuilder();

		List<MetadataVO> result = new ArrayList<>();
		Language language = Language.withCode(view.getSessionContext().getCurrentLocale().getLanguage());
		for (Metadata metadata : types().getSchemaType(schemaType).getAllMetadatas().sortAscTitle(language)) {
			if (isBatchEditable(metadata)) {
				result.add(builder.build(metadata, view.getSessionContext()));
			}
		}
		return result;
	}

	private boolean isBatchEditable(Metadata metadata) {
		return !metadata.isSystemReserved()
				&& !metadata.isUnmodifiable()
				&& metadata.isEnabled()
				&& !metadata.getType().isStructureOrContent()
				&& metadata.getDataEntry().getType() == DataEntryType.MANUAL
				&& isNotHidden(metadata)
				// XXX: Not supported in the backend
				&& metadata.getType() != MetadataValueType.ENUM
				;
	}

	private boolean isNotHidden(Metadata metadata) {
		MetadataDisplayConfig config = schemasDisplayManager().getMetadata(view.getCollection(), metadata.getCode());
		return config.getInputType() != MetadataInputType.HIDDEN;
	}
}