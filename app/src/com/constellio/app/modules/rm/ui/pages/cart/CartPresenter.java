package com.constellio.app.modules.rm.ui.pages.cart;

import com.constellio.app.entities.batchProcess.ChangeValueOfMetadataBatchAsyncTask;
import com.constellio.app.entities.schemasDisplay.MetadataDisplayConfig;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.extensions.AppLayerCollectionExtensions;
import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.extensions.api.RMModuleExtensions;
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
import com.constellio.app.modules.rm.wrappers.structures.FolderDetailStatus;
import com.constellio.app.modules.rm.wrappers.utils.DecomListUtil;
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
import com.constellio.app.ui.framework.reports.ReportWithCaptionVO;
import com.constellio.app.ui.i18n.i18n;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.app.ui.pages.search.batchProcessing.BatchProcessingPresenter;
import com.constellio.app.ui.pages.search.batchProcessing.BatchProcessingPresenterService;
import com.constellio.app.ui.pages.search.batchProcessing.entities.BatchProcessResults;
import com.constellio.app.ui.util.MessageUtils;
import com.constellio.data.dao.dto.records.OptimisticLockingResolution;
import com.constellio.data.dao.services.bigVault.solr.SolrUtils;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.batchprocess.AsyncTask;
import com.constellio.model.entities.batchprocess.AsyncTaskCreationRequest;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.RecordUpdateOptions;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.extensions.ModelLayerCollectionExtensions;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.batch.manager.BatchProcessesManager;
import com.constellio.model.services.emails.EmailServices.EmailMessage;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.reports.ReportServices;
import com.constellio.model.services.search.SPEQueryResponse;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.jgoodies.common.base.Strings;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import static com.constellio.app.modules.rm.model.enums.FolderStatus.ACTIVE;
import static com.constellio.app.modules.rm.model.enums.FolderStatus.SEMI_ACTIVE;
import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.entities.schemas.Schemas.IDENTIFIER;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;
import static java.util.Arrays.asList;

public class CartPresenter extends SingleSchemaBasePresenter<CartView> implements BatchProcessingPresenter, NewReportPresenter {
	private transient RMSchemasRecordsServices rm;
	private transient Cart cart;
	private String cartId;
	private String batchProcessSchemaType;
	private RecordServices recordServices;

	private transient BatchProcessingPresenterService batchProcessingPresenterService;
	private transient ModelLayerCollectionExtensions modelLayerExtensions;
	private transient RMModuleExtensions rmModuleExtensions;

	public CartPresenter(CartView view) {
		super(view, Cart.DEFAULT_SCHEMA);

		modelLayerExtensions = modelLayerFactory.getExtensions().forCollection(view.getCollection());
		rmModuleExtensions = appLayerFactory.getExtensions().forCollection(view.getCollection()).forModule(ConstellioRMModule.ID);
		recordServices = modelLayerFactory.newRecordServices();
	}

	public MetadataSchema getCartMetadataSchema() {
		return modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection).getSchema(Cart.DEFAULT_SCHEMA);
	}

	public boolean havePermisionToGroupCart() {
		return getCurrentUser().has(RMPermissionsTo.USE_GROUP_CART).globally();
	}

	public void itemRemovalRequested(RecordVO recordVO) {
		Record record = recordVO.getRecord();
		removeFromFavorite(record);
		addOrUpdate(record, RecordUpdateOptions.validationExceptionSafeOptions());
		view.navigate().to(RMViews.class).cart(cartId);
	}

	public boolean canEmptyCart() {
		return cartHasRecords();
	}

	public boolean renameFavoritetsGroup(String name) {

		if (Strings.isNotBlank(name)) {
			try {
				cart.setTitle(name);
				recordServices.update(cart.getWrappedRecord());
			} catch (RecordServicesException e) {
				throw new RuntimeException("Unexpected error when updating cart");
			}
		} else {
			view.showErrorMessage(i18n.$("requiredFieldWithName", i18n.$("title")));
			return false;
		}

		return true;
	}

	public void cartEmptyingRequested() {
		List<Record> records = getCartRecords();
		for (Record record : records) {
			removeFromFavorite(record);
		}
		Transaction transaction = new Transaction();
		transaction.setOptimisticLockingResolution(OptimisticLockingResolution.EXCEPTION);
		transaction.setOptions(RecordUpdateOptions.validationExceptionSafeOptions());
		transaction.addUpdate(records);
		try {
			recordServices().execute(transaction);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
		view.navigate().to(RMViews.class).cart(cartId);
	}

	public User getCurrentUser() {
		return super.getCurrentUser();
	}

	private void removeFromFavorite(Record record) {
		String schemaCode = record.getSchemaCode();

		if (schemaCode.startsWith(Folder.SCHEMA_TYPE)) {
			Folder folder = rm().wrapFolder(record);
			folder.removeFavorite(cartId);
		} else if (schemaCode.startsWith(Document.SCHEMA_TYPE)) {
			Document document = rm().wrapDocument(record);
			document.removeFavorite(cartId);
		} else if (schemaCode.startsWith(ContainerRecord.SCHEMA_TYPE)) {
			ContainerRecord containerRecord = rm().wrapContainerRecord(record);
			containerRecord.removeFavorite(cartId);
		}
	}

	public Cart getCart() {
		return cart;
	}

	public RecordVO getCartAsRecordVO() {
		return new RecordToVOBuilder().build(cart.getWrappedRecord(), VIEW_MODE.DISPLAY, view.getSessionContext());
	}

	public boolean canPrepareEmail() {
		// TODO: Maybe better test
		return cartHasRecords() && cartContainerIsEmpty();
	}

	public void emailPreparationRequested() {
		EmailMessage emailMessage = new CartEmailService(collection, modelLayerFactory)
				.createEmailForCart(cartOwner(), getCartDocumentIds(), getCurrentUser());
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
		List<Folder> folders = getCartFolders();
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

	public boolean canDelete() {
		return cartHasRecords() && canDeleteContainers(getCurrentUser())
			   && canDeleteFolders(getCurrentUser()) && canDeleteDocuments(getCurrentUser()) && hasCartBatchDeletePermission();
	}

	public void deletionRequested(String reason) {
		if (!canDelete()) {
			view.showErrorMessage($("CartView.cannotDelete"));
			return;
		}
		for (Record record : recordServices().getRecordsById(view.getCollection(), getAllCartItems())) {
			ValidationErrors validateDeleteAuthorized = modelLayerExtensions.validateDeleteAuthorized(record, getCurrentUser());
			if (!validateDeleteAuthorized.isEmpty()) {
				MessageUtils.getCannotDeleteWindow(validateDeleteAuthorized).openWindow();
				return;
			}
		}

		for (Record record : recordServices().getRecordsById(view.getCollection(), getAllCartItems())) {
			delete(record, reason);
		}
		cartEmptyingRequested();
	}

	public RecordVOWithDistinctSchemasDataProvider getFolderRecords() {
		return new RecordVOWithDistinctSchemasDataProvider(
				getSchemas(), new RecordToVOBuilder(), modelLayerFactory, view.getSessionContext()) {
			@Override
			protected LogicalSearchQuery getQuery() {
				return new LogicalSearchQuery(from(rm().folder.schemaType()).where(Schemas.IDENTIFIER).isIn(getAllCartItems()))
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
				return new LogicalSearchQuery(from(rm().documentSchemaType()).where(Schemas.IDENTIFIER).isIn(getAllCartItems()))
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
							from(rm().containerRecord.schemaType()).where(Schemas.IDENTIFIER).isIn(getAllCartItems()))
							.filteredByStatus(StatusFilter.ACTIVES)
							.sortAsc(Schemas.TITLE);
				} else if (user.hasAny(RMPermissionsTo.DISPLAY_CONTAINERS, RMPermissionsTo.MANAGE_CONTAINERS).onSomething()) {
					List<String> adminUnitIds = getConceptsWithPermissionsForCurrentUser(RMPermissionsTo.DISPLAY_CONTAINERS,
							RMPermissionsTo.MANAGE_CONTAINERS);
					return new LogicalSearchQuery(
							from(rm().containerRecord.schemaType()).where(Schemas.IDENTIFIER).isIn(getAllCartItems())
									.andWhere(schema(ContainerRecord.DEFAULT_SCHEMA)
											.getMetadata(ContainerRecord.ADMINISTRATIVE_UNITS)).isIn(adminUnitIds))
							.filteredByStatus(StatusFilter.ACTIVES)
							.sortAsc(Schemas.TITLE);
				} else {
					return LogicalSearchQuery.returningNoResults();
				}
			}
		};
	}

	protected List<MetadataSchemaVO> getSchemas() {
		MetadataSchemaToVOBuilder builder = new MetadataSchemaToVOBuilder();
		return asList(
				builder.build(schema(Folder.DEFAULT_SCHEMA), VIEW_MODE.TABLE, view.getSessionContext()),
				builder.build(schema(Folder.DEFAULT_SCHEMA), VIEW_MODE.TABLE, view.getSessionContext()),
				builder.build(schema(Folder.DEFAULT_SCHEMA), VIEW_MODE.TABLE, view.getSessionContext()));
	}

	public boolean hasCartBatchDeletePermission() {
		return getCurrentUser().has(RMPermissionsTo.CART_BATCH_DELETE).globally();
	}

	public boolean isDefaultCart() {
		return getCurrentUser().getId().equals(cartId);
	}

	String cartOwner() {
		return isDefaultCart() ? getCurrentUser().getId() : cart.getOwner();
	}

	private boolean cartHasOnlyFolders() {
		return !cartFoldersIsEmpty() && cartDocumentsIsEmpty() && cartContainerIsEmpty();
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

	private boolean canDeleteContainers(User user) {
		for (ContainerRecord container : getCartContainers()) {
			if (!user.has(RMPermissionsTo.DELETE_CONTAINERS).on(container)) {
				return false;
			}
		}
		return true;
	}

	protected String getCurrentBorrowerOf(Document document) {
		return document.getContent() == null ? null : document.getContent().getCheckoutUserId();
	}

	List<Folder> getNotDeletedCartFolders() {
		List<Folder> cartFolders = getCartFolders();
		Iterator<Folder> iterator = cartFolders.iterator();
		while (iterator.hasNext()) {
			Folder currentFolder = iterator.next();
			if (currentFolder.isLogicallyDeletedStatus()) {
				iterator.remove();
			}
		}
		return cartFolders;
	}

	List<FolderVO> getNotDeletedCartFoldersVO() {
		FolderToVOBuilder builder = new FolderToVOBuilder();
		List<FolderVO> folderVOS = new ArrayList<>();
		for (Folder folder : this.getCartFolders()) {
			if (!folder.isLogicallyDeletedStatus()) {
				folderVOS.add(builder.build(folder.getWrappedRecord(), VIEW_MODE.DISPLAY, view.getSessionContext()));
			}
		}
		return folderVOS;
	}

	List<DocumentVO> getNotDeletedCartDocumentVO() {
		DocumentToVOBuilder builder = new DocumentToVOBuilder(modelLayerFactory);
		List<DocumentVO> documentVOS = new ArrayList<>();
		for (Document document : this.getCartDocuments()) {
			if (!document.isLogicallyDeletedStatus()) {
				documentVOS.add(builder.build(document.getWrappedRecord(), VIEW_MODE.DISPLAY, view.getSessionContext()));
			}
		}
		return documentVOS;
	}

	protected RMSchemasRecordsServices rm() {
		if (rm == null) {
			rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		}
		return rm;
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		if (params != null && user.getId().equals(params)) {
			return user.has(RMPermissionsTo.USE_MY_CART).globally();
		}

		return true;
	}

	public void forParams(String parameters) {
		cartId = parameters;
		if (!isDefaultCart()) {
			cart = rm().getCart(cartId);
		}
	}

	public List<String> getNotDeletedRecordsIds(String schemaType) {
		User currentUser = getCurrentUser();
		switch (schemaType) {
			case Folder.SCHEMA_TYPE:
				List<String> folders = getCartFolderIds();
				return getNonDeletedRecordsIds(rm().getFolders(folders), currentUser);
			case Document.SCHEMA_TYPE:
				List<String> documents = getCartDocumentIds();
				return getNonDeletedRecordsIds(rm().getDocuments(documents), currentUser);
			case ContainerRecord.SCHEMA_TYPE:
				List<String> containers = getCartContainersIds();
				return getNonDeletedRecordsIds(rm().getContainerRecords(containers), currentUser);
			default:
				throw new RuntimeException("Unsupported type : " + schemaType);
		}
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

	@Override
	public String getOriginSchema(String schemaType, String selectedType) {
		return batchProcessingPresenterService().getOriginSchema(schemaType, selectedType, getNotDeletedRecordsIds(schemaType));
	}

	@Override
	public RecordVO newRecordVO(String schema, String schemaType, SessionContext sessionContext) {
		return newRecordVO(getNotDeletedRecordsIds(schemaType), schema, sessionContext);
	}

	public RecordVO newRecordVO(List<String> selectedRecordIds, String schema, SessionContext sessionContext) {
		return batchProcessingPresenterService().newRecordVO(schema, sessionContext, selectedRecordIds);
	}

	@Override
	public InputStream simulateButtonClicked(String selectedType, String schemaType, RecordVO viewObject,
											 List<String> metadatasToEmpty)
			throws RecordServicesException {
		return simulateButtonClicked(selectedType, getNotDeletedRecordsIds(schemaType), viewObject, metadatasToEmpty);
	}

	public InputStream simulateButtonClicked(String selectedType, List<String> records, RecordVO viewObject,
											 List<String> metadatasToEmpty)
			throws RecordServicesException {
		BatchProcessResults results = batchProcessingPresenterService()
				.simulate(selectedType, records, viewObject, metadatasToEmpty, getCurrentUser());
		return batchProcessingPresenterService().formatBatchProcessingResults(results);
	}

	@Override
	public boolean processBatchButtonClicked(String selectedType, String schemaType, RecordVO viewObject,
											 List<String> metadatasToEmpty)
			throws RecordServicesException {
		return processBatchButtonClicked(selectedType, getNotDeletedRecordsIds(schemaType), viewObject, metadatasToEmpty);
	}

	public boolean processBatchButtonClicked(String selectedType, List<String> records, RecordVO viewObject,
											 List<String> metadatasToEmpty)
			throws RecordServicesException {
		for (Record record : recordServices().getRecordsById(view.getCollection(), records)) {
			if (modelLayerExtensions.isModifyBlocked(record, getCurrentUser())) {
				view.showErrorMessage($("CartView.actionBlockedByExtension"));
				return false;
			}
		}

		batchProcessingPresenterService()
				.execute(selectedType, records, viewObject, metadatasToEmpty, getCurrentUser());
		view.navigate().to(RMViews.class).cart(cartId);
		return true;
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
	public AppLayerCollectionExtensions getBatchProcessingExtension() {
		return batchProcessingPresenterService().getBatchProcessingExtension();
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
				return !cartFoldersIsEmpty();
			case ContainerRecord.SCHEMA_TYPE:
				return !cartContainerIsEmpty();
			case Document.SCHEMA_TYPE:
				return !cartDocumentsIsEmpty();

			default:
				throw new RuntimeException("No labels for type : " + schemaType);
		}
	}

	public boolean isBatchProcessingButtonVisible(String schemaType) {
		if (ContainerRecord.SCHEMA_TYPE.equals(schemaType) && !getCurrentUser().has(RMPermissionsTo.MANAGE_CONTAINERS)
				.onSomething()) {
			return false;
		}

		if (!getCurrentUser().has(CorePermissions.MODIFY_RECORDS_USING_BATCH_PROCESS).onSomething()) {
			return false;
		}

		return getNotDeletedRecordsIds(schemaType).size() != 0;
	}

	public void shareWithUsersRequested(List<String> userids) {
		List<Folder> folders = getCartFolders();
		for (Folder folder : folders) {
			if (!rmModuleExtensions.isShareActionPossibleOnFolder(folder, getCurrentUser())) {
				view.showErrorMessage($("CartView.actionBlockedByExtension"));
				return;
			}
		}
		List<Document> documents = getCartDocuments();
		for (Document document : documents) {
			if (!rmModuleExtensions.isShareActionPossibleOnDocument(document, getCurrentUser())) {
				view.showErrorMessage($("CartView.actionBlockedByExtension"));
				return;
			}
		}
		cart.setSharedWithUsers(userids);
		addOrUpdate(cart.getWrappedRecord());
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

		}
		if (folder.getCloseDate() != null || folder.hasExpectedDates()) {
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
		DecommissioningList list = rm().newDecommissioningList();
		list.setTitle(title);
		list.setAdministrativeUnit(getCommonAdministrativeUnit(getCartFolders()));
		list.setDecommissioningListType(decomType);
		List<String> folderIds = getNotDeletedCartFolders().stream().map(Folder::getId).collect(Collectors.toList());
		if (isDecommissioningListWithSelectedFolders()) {
			DecomListUtil.setFolderDetailsInDecomList(
					collection, appLayerFactory, list, folderIds, FolderDetailStatus.SELECTED);
		} else {
			DecomListUtil.setFolderDetailsInDecomList(
					collection, appLayerFactory, list, folderIds, FolderDetailStatus.INCLUDED);
		}

		try {
			recordServices().add(list, getCurrentUser());
			view.navigate().to(RMViews.class).displayDecommissioningList(list.getId());
		} catch (RecordServicesException e) {
			e.printStackTrace();
		}
	}

	public void displayRecordRequested(RecordVO recordVO) {
		switch (recordVO.getSchema().getTypeCode()) {
			case Folder.SCHEMA_TYPE:
				view.navigate().to(RMViews.class).displayFolderFromFavorites(recordVO.getId(), cartId);
				break;
			case Document.SCHEMA_TYPE:
				view.navigate().to(RMViews.class).displayDocumentFromFavorites(recordVO.getId(), cartId);
				break;
			case ContainerRecord.SCHEMA_TYPE:
				view.navigate().to(RMViews.class).displayContainerFromFavorites(recordVO.getId(), cartId);
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
				return new LogicalSearchQuery(from(rm().folder.schemaType()).where(Schemas.IDENTIFIER).isIn(getAllCartItems()))
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
				return new LogicalSearchQuery(from(rm().documentSchemaType()).where(Schemas.IDENTIFIER).isIn(getAllCartItems()))
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
							from(rm().containerRecord.schemaType()).where(Schemas.IDENTIFIER).isIn(getAllCartItems()))
							.filteredByStatus(StatusFilter.ACTIVES).setFreeTextQuery(freeText)
							.sortAsc(Schemas.TITLE);
				} else if (user.hasAny(RMPermissionsTo.DISPLAY_CONTAINERS, RMPermissionsTo.MANAGE_CONTAINERS).onSomething()) {
					List<String> adminUnitIds = getConceptsWithPermissionsForCurrentUser(RMPermissionsTo.DISPLAY_CONTAINERS,
							RMPermissionsTo.MANAGE_CONTAINERS);
					return new LogicalSearchQuery(
							from(rm().containerRecord.schemaType()).where(Schemas.IDENTIFIER).isIn(getAllCartItems())
									.andWhere(schema(ContainerRecord.DEFAULT_SCHEMA)
											.getMetadata(ContainerRecord.ADMINISTRATIVE_UNIT)).isIn(adminUnitIds))
							.filteredByStatus(StatusFilter.ACTIVES).setFreeTextQuery(freeText)
							.sortAsc(Schemas.TITLE);
				} else {
					return LogicalSearchQuery.returningNoResults();
				}
			}
		};
	}

	@Override
	public List<ReportWithCaptionVO> getSupportedReports() {
		List<ReportWithCaptionVO> supportedReports = new ArrayList<>();
		ReportServices reportServices = new ReportServices(modelLayerFactory, collection);
		List<String> userReports = reportServices.getUserReportTitles(getCurrentUser(), view.getCurrentSchemaType());
		if (userReports != null) {
			for (String reportTitle : userReports) {
				supportedReports.add(new ReportWithCaptionVO(reportTitle, reportTitle));
			}
		}
		return supportedReports;
	}

	@Override
	public NewReportWriterFactory<SearchResultReportParameters> getReport(String report) {
		return new SearchResultReportWriterFactory(appLayerFactory);
	}

	@Override
	public Object getReportParameters(String report) {
		List<String> recordids = getNotDeletedRecordsIds(view.getCurrentSchemaType());

		return new SearchResultReportParameters(recordids, view.getCurrentSchemaType(),
				collection, report, getCurrentUser(), null);
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
		List<Folder> folders = rm.getFolders(getCartFolderIds());
		for (Folder folder : folders) {
			if (folder.getCurrentDecommissioningList() != null) {
				return true;
			}
		}
		return false;
	}

	public boolean isAnyFolderASubFolder() {
		return  searchServices().getResultsCount(from(rm().folder.schemaType()).where(rm().folder.parentFolder()).isNotNull()
				.andWhere(Schemas.IDENTIFIER).isIn(getCartFolderIds())) > 0;
	}

	public boolean isSubFolderDecommissioningAllowed() {
		return appLayerFactory.getModelLayerFactory().getSystemConfigurationsManager().getValue(RMConfigs.SUB_FOLDER_DECOMMISSIONING);
	}

	public boolean batchEditRequested(String code, Object value, String schemaType) {
		List<String> recordIds = schemaType.equals(Folder.SCHEMA_TYPE) ? getCartFolderIds() : getCartDocumentIds();
		for (Record record : recordServices().getRecordsById(view.getCollection(), recordIds)) {
			if (modelLayerExtensions.isModifyBlocked(record, getCurrentUser())) {
				view.showErrorMessage($("CartView.actionBlockedByExtension"));
				return false;
			}
		}

		Map<String, Object> changes = new HashMap<>();
		changes.put(code, value);

		LogicalSearchQuery query = new LogicalSearchQuery(fromAllSchemasIn(collection).where(IDENTIFIER)
				.isIn(getNotDeletedRecordsIds(schemaType))).filteredWithUserWrite(getCurrentUser());
		SearchServices searchServices = modelLayerFactory.newSearchServices();
		ModifiableSolrParams params = searchServices.addSolrModifiableParams(query);

		AsyncTask asyncTask = new ChangeValueOfMetadataBatchAsyncTask(changes, SolrUtils.toSingleQueryString(params),
				null, searchServices().getResultsCount(query));

		String username = getCurrentUser() == null ? null : getCurrentUser().getUsername();
		AsyncTaskCreationRequest asyncTaskRequest = new AsyncTaskCreationRequest(asyncTask, collection, "userBatchProcess");
		asyncTaskRequest.setUsername(username);

		BatchProcessesManager manager = modelLayerFactory.getBatchProcessesManager();
		manager.addAsyncTask(asyncTaskRequest);
		return true;
	}

	public List<MetadataVO> getMetadataAllowedInBatchEdit(String schemaType) {
		MetadataToVOBuilder builder = new MetadataToVOBuilder();

		List<MetadataVO> result = new ArrayList<>();
		Language language = Language.withCode(view.getSessionContext().getCurrentLocale().getLanguage());
		for (Metadata metadata : types().getSchemaType(schemaType).getAllMetadatas().sortAscTitle(language)) {
			if (isBatchEditable(metadata) && !metadata.isEssential()) {
				result.add(builder.build(metadata, view.getSessionContext()));
			}
		}
		return result;
	}

	@Override
	public ValidationErrors validateBatchProcessing() {
		// FIXME
		return new ValidationErrors();
	}

	@Override
	public boolean validateUserHaveBatchProcessPermissionOnAllRecords(String schemaType) {
		if (!getCurrentUser().has(CorePermissions.MODIFY_RECORDS_USING_BATCH_PROCESS).globally()) {
			return false;
		}

		switch (schemaType) {
		case Folder.SCHEMA_TYPE:
			return doesQueryAndQueryWithFilterOnBatchProcessPermHaveSameResult(getCartFoldersLogicalSearchQuery());
		case ContainerRecord.SCHEMA_TYPE:
			return doesQueryAndQueryWithFilterOnBatchProcessPermHaveSameResult(getCartContainersLogicalSearchQuery());
		case Document.SCHEMA_TYPE:
			return doesQueryAndQueryWithFilterOnBatchProcessPermHaveSameResult(getCartDocumentsLogicalSearchQuery());

		default:
			throw new RuntimeException("No labels for type : " + schemaType);
		}
	}

	private boolean doesQueryAndQueryWithFilterOnBatchProcessPermHaveSameResult(LogicalSearchQuery logicalSearchQuery) {
		SPEQueryResponse speQueryResponse = searchServices().query(logicalSearchQuery);

		LogicalSearchQuery logicalSearchQueryWithFilter = logicalSearchQuery
				.filteredWithUser(getCurrentUser(), CorePermissions.MODIFY_RECORDS_USING_BATCH_PROCESS);
		long numberFound = searchServices().getResultsCount(logicalSearchQueryWithFilter);

		return speQueryResponse.getNumFound() == numberFound;
	}

	@Override
	public boolean validateUserHaveBatchProcessPermissionForRecordCount(String schemaType) {
		if (!getCurrentUser().has(CorePermissions.MODIFY_UNLIMITED_RECORDS_USING_BATCH_PROCESS).globally()) {
			ConstellioEIMConfigs systemConfigs = modelLayerFactory.getSystemConfigs();
			int batchProcessingLimit = systemConfigs.getBatchProcessingLimit();
			if (batchProcessingLimit != -1 && getNumberOfRecords(schemaType) > batchProcessingLimit) {
				return false;
			}
		}

		return true;
	}

	private boolean isBatchEditable(Metadata metadata) {
		return !metadata.isSystemReserved()
			   && !metadata.isUnmodifiable()
			   && metadata.isEnabled()
			   && !metadata.getType().isStructureOrContent()
			   && metadata.getDataEntry().getType() == DataEntryType.MANUAL
			   && isNotHidden(metadata)
			   // XXX: Not supported in the backend
				&& metadata.getType() != MetadataValueType.ENUM;
	}

	private boolean isNotHidden(Metadata metadata) {
		MetadataDisplayConfig config = schemasDisplayManager().getMetadata(view.getCollection(), metadata.getCode());
		return config.getInputType() != MetadataInputType.HIDDEN;
	}

	public boolean canCurrentUserBuildDecommissioningList() {
		return getCurrentUser().has(RMPermissionsTo.CREATE_DECOMMISSIONING_LIST).onSomething() ||
			   getCurrentUser().has(RMPermissionsTo.CREATE_TRANSFER_DECOMMISSIONING_LIST).onSomething();
	}

	public boolean isPdfGenerationActionPossible(List<String> recordIds) {
		List<Record> records = rm().get(recordIds);
		for (Record record : records) {
			if (!rmModuleExtensions.isCreatePDFAActionPossibleOnDocument(rm().wrapDocument(record), getCurrentUser())) {
				view.showErrorMessage($("CartView.actionBlockedByExtension"));
				return false;
			}
		}
		return true;
	}

	public boolean isDecommissioningActionPossible() {
		List<Record> records = rm().get(getCartFolderIds());
		for (Record record : records) {
			Folder folder = rm().wrapFolder(record);
			if (!rmModuleExtensions.isDecommissioningActionPossibleOnFolder(folder, getCurrentUser())) {
				view.showErrorMessage($("CartView.actionBlockedByExtension"));
				return false;
			}
		}
		return true;
	}

	public boolean isNeedingAReasonToDeleteRecords() {
		return new RMConfigs(modelLayerFactory.getSystemConfigurationsManager()).isNeedingAReasonBeforeDeletingFolders();
	}

	public boolean isDecommissioningListWithSelectedFolders() {
		return new RMConfigs(modelLayerFactory.getSystemConfigurationsManager()).isDecommissioningListWithSelectedFolders();
	}

	public List<String> getCartFolderIds() {
		List<Folder> folders = getCartFolders();
		List<String> foldersIds = new ArrayList<>();
		for (Folder folder : folders) {
			foldersIds.add(folder.getId());
		}
		return foldersIds;
	}

	public List<Record> getCartFolderRecords() {
		List<Folder> folders = getCartFolders();
		List<Record> foldersRecords = new ArrayList<>();
		for (Folder folder : folders) {
			foldersRecords.add(folder.getWrappedRecord());
		}
		return foldersRecords;
	}

	protected List<Folder> getCartFolders() {
		LogicalSearchQuery logicalSearchQuery = getCartFoldersLogicalSearchQuery();
		return rm().searchFolders(logicalSearchQuery);
	}

	@NotNull
	private LogicalSearchQuery getCartFoldersLogicalSearchQuery() {
		return new LogicalSearchQuery(from(rm().folder.schemaType()).where(rm().folder.favorites()).isEqualTo(cartId));
	}

	public List<String> getCartDocumentIds() {
		List<Document> documents = getCartDocuments();
		List<String> documentsIds = new ArrayList<>();
		for (Document document : documents) {
			documentsIds.add(document.getId());
		}
		return documentsIds;
	}

	public List<Record> getCartDocumentRecords() {
		List<Document> documents = getCartDocuments();
		List<Record> documentsRecords = new ArrayList<>();
		for (Document document : documents) {
			documentsRecords.add(document.getWrappedRecord());
		}
		return documentsRecords;
	}

	private List<Document> getCartDocuments() {
		LogicalSearchQuery logicalSearchQuery = getCartDocumentsLogicalSearchQuery();
		return rm().searchDocuments(logicalSearchQuery);
	}

	@NotNull
	private LogicalSearchQuery getCartDocumentsLogicalSearchQuery() {
		return new LogicalSearchQuery(from(rm().document.schemaType()).where(rm().document.favorites()).isEqualTo(cartId));
	}

	public List<String> getCartContainersIds() {
		List<ContainerRecord> containers = getCartContainers();
		List<String> containersIds = new ArrayList<>();
		for (ContainerRecord container : containers) {
			containersIds.add(container.getId());
		}
		return containersIds;
	}

	public List<Record> getCartContainersRecords() {
		List<ContainerRecord> containers = getCartContainers();
		List<Record> containersRecords = new ArrayList<>();
		for (ContainerRecord container : containers) {
			containersRecords.add(container.getWrappedRecord());
		}
		return containersRecords;
	}

	private List<ContainerRecord> getCartContainers() {
		LogicalSearchQuery logicalSearchQuery = getCartContainersLogicalSearchQuery();
		return rm().searchContainerRecords(logicalSearchQuery);
	}

	private List<Record> getCartRecords() {
		List<Record> records = new ArrayList<>();
		LogicalSearchQuery logicalSearchQuery = getCartFoldersLogicalSearchQuery();
		SearchServices searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
		records.addAll(searchServices.search(logicalSearchQuery));
		logicalSearchQuery = getCartDocumentsLogicalSearchQuery();
		records.addAll((searchServices.search(logicalSearchQuery)));
		logicalSearchQuery = getCartContainersLogicalSearchQuery();
		records.addAll((searchServices.search(logicalSearchQuery)));
		return records;
	}

	protected boolean cartFoldersIsEmpty() {
		LogicalSearchQuery logicalSearchQuery = getCartFoldersLogicalSearchQuery();
		return searchServices().getResultsCount(logicalSearchQuery) == 0;
	}

	private boolean cartDocumentsIsEmpty() {
		LogicalSearchQuery logicalSearchQuery = getCartDocumentsLogicalSearchQuery();
		return searchServices().getResultsCount(logicalSearchQuery) == 0;
	}

	private boolean cartContainerIsEmpty() {
		LogicalSearchQuery logicalSearchQuery = getCartContainersLogicalSearchQuery();
		return searchServices().getResultsCount(logicalSearchQuery) == 0;
	}

	@NotNull
	private LogicalSearchQuery getCartContainersLogicalSearchQuery() {
		return new LogicalSearchQuery(
				from(rm().containerRecord.schemaType()).where(rm().containerRecord.favorites()).isEqualTo(cartId));
	}

	public List<String> getAllCartItems() {
		List<String> result = new ArrayList<>();
		result.addAll(getCartFolderIds());
		result.addAll(getCartDocumentIds());
		result.addAll(getCartContainersIds());
		return result;
	}

	public List<Record> getAllCartItemRecords() {
		List<Record> result = new ArrayList<>();
		result.addAll(getCartFolderRecords());
		result.addAll(getCartDocumentRecords());
		result.addAll(getCartContainersRecords());
		return result;
	}

	public boolean cartHasRecords() {
		return !(cartFoldersIsEmpty() && cartDocumentsIsEmpty() && cartContainerIsEmpty());
	}

	public boolean canRenameFavoriteGroup() {
		if (isDefaultCart()) {
			return false;
		}
		List<String> sharedWithUsers = cart.getSharedWithUsers();
		return (sharedWithUsers == null || sharedWithUsers.size() <= 0);
	}
}