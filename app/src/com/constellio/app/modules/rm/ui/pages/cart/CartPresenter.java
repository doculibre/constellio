package com.constellio.app.modules.rm.ui.pages.cart;

import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.extensions.api.RMModuleExtensions;
import com.constellio.app.modules.rm.model.enums.DecommissioningListType;
import com.constellio.app.modules.rm.model.enums.FolderStatus;
import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.reports.builders.search.SearchResultReportParameters;
import com.constellio.app.modules.rm.reports.builders.search.SearchResultReportWriterFactory;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Cart;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.components.NewReportPresenter;
import com.constellio.app.ui.framework.components.RecordFieldFactory;
import com.constellio.app.ui.framework.data.RecordVOWithDistinctSchemasDataProvider;
import com.constellio.app.ui.framework.reports.NewReportWriterFactory;
import com.constellio.app.ui.framework.reports.ReportWithCaptionVO;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.app.ui.pages.search.batchProcessing.BatchProcessingPresenterService;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.RecordUpdateOptions;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.extensions.ModelLayerCollectionExtensions;
import com.constellio.model.services.reports.ReportServices;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import static com.constellio.app.modules.rm.model.enums.FolderStatus.ACTIVE;
import static com.constellio.app.modules.rm.model.enums.FolderStatus.SEMI_ACTIVE;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;

public class CartPresenter extends SingleSchemaBasePresenter<CartView> implements NewReportPresenter {
	private transient RMSchemasRecordsServices rm;
	private transient Cart cart;
	private String cartId;
	private List<Record> selectedRecords;

	private transient BatchProcessingPresenterService batchProcessingPresenterService;
	private transient ModelLayerCollectionExtensions modelLayerExtensions;
	private transient RMModuleExtensions rmModuleExtensions;

	public CartPresenter(String cartId, CartView view) {
		super(view, Cart.DEFAULT_SCHEMA);

		modelLayerExtensions = modelLayerFactory.getExtensions().forCollection(view.getCollection());
		rmModuleExtensions = appLayerFactory.getExtensions().forCollection(view.getCollection()).forModule(ConstellioRMModule.ID);
		selectedRecords = new ArrayList<>();
		if (StringUtils.isNotBlank(cartId)) {
			forParams(cartId);
		}
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

	public boolean isDefaultCart() {
		return getCurrentUser().getId().equals(cartId);
	}

	public RecordVOWithDistinctSchemasDataProvider getFolderRecords() {
		return new RecordVOWithDistinctSchemasDataProvider(
				getSchemas(), new RecordToVOBuilder(), modelLayerFactory, view.getSessionContext()) {
			@Override
			protected LogicalSearchQuery getQuery() {
				return new LogicalSearchQuery(from(rm().folder.schemaType()).where(rm().folder.favorites()).isEqualTo(cartId))
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
				return new LogicalSearchQuery(from(rm().documentSchemaType()).where(rm().document.favorites()).isEqualTo(cartId))
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
							from(rm().containerRecord.schemaType()).where(rm().containerRecord.favorites()).isEqualTo(cartId))
							.filteredByStatus(StatusFilter.ACTIVES)
							.sortAsc(Schemas.TITLE);
				} else if (user.hasAny(RMPermissionsTo.DISPLAY_CONTAINERS, RMPermissionsTo.MANAGE_CONTAINERS).onSomething()) {
					List<String> adminUnitIds = getConceptsWithPermissionsForCurrentUser(RMPermissionsTo.DISPLAY_CONTAINERS,
							RMPermissionsTo.MANAGE_CONTAINERS);
					return new LogicalSearchQuery(
							from(rm().containerRecord.schemaType()).where(rm().containerRecord.favorites()).isEqualTo(cartId)
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

	public List<Record> getAllCartItemRecords() {
		return searchServices().search(new LogicalSearchQuery(from(rm().document.schemaType(), rm().folder.schemaType(), rm().containerRecord.schemaType())
				.where(rm().document.favorites()).isEqualTo(cartId)
				.orWhere(rm().folder.favorites()).isEqualTo(cartId)
				.orWhere(rm().containerRecord.favorites()).isEqualTo(cartId)));
	}

	protected List<MetadataSchemaVO> getSchemas() {
		MetadataSchemaToVOBuilder builder = new MetadataSchemaToVOBuilder();
		return asList(
				builder.build(schema(Folder.DEFAULT_SCHEMA), VIEW_MODE.TABLE, view.getSessionContext()),
				builder.build(schema(Folder.DEFAULT_SCHEMA), VIEW_MODE.TABLE, view.getSessionContext()),
				builder.build(schema(Folder.DEFAULT_SCHEMA), VIEW_MODE.TABLE, view.getSessionContext()));
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
				LogicalSearchQuery nonDeletedFolders = new LogicalSearchQuery(from(rm().folder.schemaType()).where(Schemas.LOGICALLY_DELETED_ON).isFalseOrNull().andWhere(rm().folder.favorites()).isEqualTo(cartId));
				nonDeletedFolders.filteredWithUser(currentUser);
				return searchServices().searchRecordIds(nonDeletedFolders);
			case Document.SCHEMA_TYPE:
				LogicalSearchQuery nonDeletedDocuments = new LogicalSearchQuery(from(rm().document.schemaType()).where(Schemas.LOGICALLY_DELETED_ON).isFalseOrNull().andWhere(rm().document.favorites()).isEqualTo(cartId));
				nonDeletedDocuments.filteredWithUser(currentUser);
				return searchServices().searchRecordIds(nonDeletedDocuments);
			case ContainerRecord.SCHEMA_TYPE:
				LogicalSearchQuery nonDeletedContainerRecord = new LogicalSearchQuery(from(rm().containerRecord.schemaType()).where(Schemas.LOGICALLY_DELETED_ON).isFalseOrNull().andWhere(rm().containerRecord.favorites()).isEqualTo(cartId));
				nonDeletedContainerRecord.filteredWithUser(currentUser);
				return searchServices().searchRecordIds(nonDeletedContainerRecord);
			default:
				throw new RuntimeException("Unsupported type : " + schemaType);
		}
	}

	public RecordVO newRecordVO(List<String> selectedRecordIds, String schema, SessionContext sessionContext) {
		return batchProcessingPresenterService().newRecordVO(schema, sessionContext, selectedRecordIds);
	}

	public RecordFieldFactory newRecordFieldFactory(String schemaType, String selectedType, List<String> records) {
		return batchProcessingPresenterService().newRecordFieldFactory(schemaType, selectedType, records);
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
				return new LogicalSearchQuery(from(rm().folder.schemaType()).where(rm().folder.favorites()).isEqualTo(cartId))
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
				return new LogicalSearchQuery(from(rm().documentSchemaType()).where(rm().document.favorites()).isEqualTo(cartId))
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
							from(rm().containerRecord.schemaType()).where(rm().containerRecord.favorites()).isEqualTo(cartId))
							.filteredByStatus(StatusFilter.ACTIVES).setFreeTextQuery(freeText)
							.sortAsc(Schemas.TITLE);
				} else if (user.hasAny(RMPermissionsTo.DISPLAY_CONTAINERS, RMPermissionsTo.MANAGE_CONTAINERS).onSomething()) {
					List<String> adminUnitIds = getConceptsWithPermissionsForCurrentUser(RMPermissionsTo.DISPLAY_CONTAINERS,
							RMPermissionsTo.MANAGE_CONTAINERS);
					return new LogicalSearchQuery(
							from(rm().containerRecord.schemaType()).where(rm().containerRecord.favorites()).isEqualTo(cartId)
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

	public boolean isAnyFolderBorrowed() {
		return searchServices().getResultsCount(from(rm().folder.schemaType()).where(rm().folder.borrowed()).isTrue()
				.andWhere(rm().folder.favorites()).isEqualTo(cartId)) > 0;
	}

	public void addToSelectedRecords(String recordId) {
		selectedRecords.add(getRecord(recordId));
	}

	public void removeFromSelectedRecords(String recordId) {
		selectedRecords.remove(getRecord(recordId));
	}

	public List<Record> getSelectedRecords() {
		return selectedRecords;
	}

	public void emptySelectedRecords() {
		selectedRecords = new ArrayList<>();
	}

}