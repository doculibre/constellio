package com.constellio.app.modules.rm.ui.pages.cart;

import static com.constellio.app.modules.rm.model.enums.FolderStatus.ACTIVE;
import static com.constellio.app.modules.rm.model.enums.FolderStatus.SEMI_ACTIVE;
import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import com.constellio.app.extensions.AppLayerCollectionExtensions;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.model.enums.DecommissioningListType;
import com.constellio.app.modules.rm.model.enums.FolderStatus;
import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplate;
import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplateManager;
import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.reports.builders.search.SearchResultReportBuilderFactory;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.cart.CartEmlService;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningService;
import com.constellio.app.modules.rm.wrappers.*;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.components.RecordFieldFactory;
import com.constellio.app.ui.framework.components.ReportPresenter;
import com.constellio.app.ui.framework.data.RecordVOWithDistinctSchemasDataProvider;
import com.constellio.app.ui.framework.reports.ReportBuilderFactory;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.app.ui.pages.search.batchProcessing.BatchProcessingPresenter;
import com.constellio.app.ui.pages.search.batchProcessing.BatchProcessingPresenterService;
import com.constellio.app.ui.pages.search.batchProcessing.entities.BatchProcessResults;
import com.constellio.model.entities.enums.BatchProcessingMode;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.reports.ReportServices;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

public class CartPresenter extends SingleSchemaBasePresenter<CartView> implements BatchProcessingPresenter, ReportPresenter {
	private transient RMSchemasRecordsServices rm;
	private transient Cart cart;
	private String cartId;

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
		return cartHasRecords();
	}

	public void emailPreparationRequested() {
		InputStream stream = new CartEmlService(collection, modelLayerFactory).createEmlForCart(cart());
		view.startDownload(stream);
	}

	public boolean canDuplicate() {
		return cartHasOnlyFolders() && canDuplicateFolders(getCurrentUser());
	}

	public void duplicationRequested() {
		if (!canDuplicate()) {
			view.showErrorMessage($("CartView.cannotDuplicate"));
			return;
		}
		DecommissioningService service = new DecommissioningService(view.getCollection(), modelLayerFactory);
		for (Folder folder : getCartFolders()) {
			service.duplicateStructureAndSave(folder, getCurrentUser());
		}
		view.showMessage($("CartView.duplicated"));
	}

	public boolean canDelete() {
		return cartHasRecords() && cart().getContainers().isEmpty()
				&& canDeleteFolders(getCurrentUser()) && canDeleteDocuments(getCurrentUser());
	}

	public void deletionRequested() {
		if (!canDelete()) {
			view.showErrorMessage($("CartView.cannotDelete"));
			return;
		}
		for (Record record : recordServices().getRecordsById(view.getCollection(), cart().getAllItems())) {
			delete(record);
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
				return new LogicalSearchQuery(from(rm.containerRecord.schemaType()).where(Schemas.IDENTIFIER).isIn(cart().getAllItems()))
						.filteredWithUser(getCurrentUser()).filteredByStatus(StatusFilter.ACTIVES)
						.sortAsc(Schemas.TITLE);
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
		}
		return true;
	}

	List<Folder> getCartFolders() {
		return rm().wrapFolders(recordServices().getRecordsById(view.getCollection(), cart().getFolders()));
	}

	private List<Document> getCartDocuments() {
		return rm().wrapDocuments(recordServices().getRecordsById(view.getCollection(), cart().getDocuments()));
	}

	private RMSchemasRecordsServices rm() {
		if (rm == null) {
			rm = new RMSchemasRecordsServices(collection, modelLayerFactory);
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

	public List<String> getRecordsIds(String schemaType) {
		switch (schemaType) {
		case Folder.SCHEMA_TYPE:
			return cart().getFolders();
		case Document.SCHEMA_TYPE:
			return cart().getDocuments();
		case ContainerRecord.SCHEMA_TYPE:
			return cart().getContainers();
		default:
			throw new RuntimeException("Unsupported type : " + schemaType);
		}
	}

	@Override
	public String getOriginType(List<String> selectedRecordIds) {
		return batchProcessingPresenterService().getOriginType(selectedRecordIds);
	}

	@Override
	public RecordVO newRecordVO(List<String> selectedRecordIds, String schema, SessionContext sessionContext) {
		return batchProcessingPresenterService().newRecordVO(schema, sessionContext, selectedRecordIds);
	}

	@Override
	public InputStream simulateButtonClicked(String selectedType, List<String> records, RecordVO viewObject)
			throws RecordServicesException {
		BatchProcessResults results = batchProcessingPresenterService()
				.simulate(selectedType, records, viewObject, getCurrentUser());
		return batchProcessingPresenterService().formatBatchProcessingResults(results);
	}

	@Override
	public InputStream processBatchButtonClicked(String selectedType, List<String> records, RecordVO viewObject)
			throws RecordServicesException {
		BatchProcessResults results = batchProcessingPresenterService()
				.execute(selectedType, records, viewObject, getCurrentUser());
		return batchProcessingPresenterService().formatBatchProcessingResults(results);
	}

	@Override
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
	public RecordFieldFactory newRecordFieldFactory(String schemaType, String selectedType, List<String> records) {
		return batchProcessingPresenterService().newRecordFieldFactory(schemaType, selectedType, records);
	}

	public List<LabelTemplate> getTemplates(String schemaType) {
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
		return getRecordsIds(schemaType).size() != 0;
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
		list.setFolderDetailsFrom(getCartFolders());

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
				return new LogicalSearchQuery(from(rm.containerRecord.schemaType()).where(Schemas.IDENTIFIER).isIn(cart().getAllItems()))
						.filteredWithUser(getCurrentUser()).filteredByStatus(StatusFilter.ACTIVES).setFreeTextQuery(freeText)
						.sortAsc(Schemas.TITLE);
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
	public ReportBuilderFactory getReport(String report) {
		List<String> recordids = getRecordsIds(view.getCurrentSchemaType());
		LogicalSearchQuery query = new LogicalSearchQuery(from(rm.schemaType(view.getCurrentSchemaType())).returnAll());
		return new SearchResultReportBuilderFactory(appLayerFactory, recordids, view.getCurrentSchemaType(),
				collection, report, getCurrentUser(), query);
	}
}
