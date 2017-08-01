package com.constellio.app.ui.pages.search;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.data.dao.services.idGenerator.UUIDV1Generator.newRandomId;
import static com.constellio.model.entities.schemas.Schemas.IDENTIFIER;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.entities.schemasDisplay.MetadataDisplayConfig;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.extensions.AppLayerCollectionExtensions;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplate;
import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplateManager;
import com.constellio.app.modules.rm.reports.builders.search.SearchResultReportParameters;
import com.constellio.app.modules.rm.reports.builders.search.SearchResultReportWriterFactory;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Cart;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.StorageSpace;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.MetadataToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.components.RecordFieldFactory;
import com.constellio.app.ui.framework.components.SearchResultTable;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.framework.reports.NewReportWriterFactory;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.search.batchProcessing.BatchProcessingPresenter;
import com.constellio.app.ui.pages.search.batchProcessing.BatchProcessingPresenterService;
import com.constellio.app.ui.pages.search.batchProcessing.entities.BatchProcessResults;
import com.constellio.app.ui.pages.search.criteria.ConditionBuilder;
import com.constellio.app.ui.pages.search.criteria.ConditionException;
import com.constellio.app.ui.pages.search.criteria.ConditionException.ConditionException_EmptyCondition;
import com.constellio.app.ui.pages.search.criteria.ConditionException.ConditionException_TooManyClosedParentheses;
import com.constellio.app.ui.pages.search.criteria.ConditionException.ConditionException_UnclosedParentheses;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.batchprocess.BatchProcess;
import com.constellio.model.entities.batchprocess.BatchProcessAction;
import com.constellio.model.entities.enums.BatchProcessingMode;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.SavedSearch;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.services.batch.actions.ChangeValueOfMetadataBatchProcessAction;
import com.constellio.model.services.batch.manager.BatchProcessesManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordImpl;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.reports.ReportServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

public class AdvancedSearchPresenter extends SearchPresenter<AdvancedSearchView> implements BatchProcessingPresenter {
	private static final Logger LOGGER = LoggerFactory.getLogger(AdvancedSearchPresenter.class);

	String searchExpression;
	String schemaTypeCode;
	private int pageNumber;
	private String searchID;
	private SearchResultTable result;
	private boolean batchProcessOnAllSearchResults;

	private transient LogicalSearchCondition condition;

	private transient BatchProcessingPresenterService batchProcessingPresenterService;

	public AdvancedSearchPresenter(AdvancedSearchView view) {
		super(view);
	}

	public void setSchemaType(String schemaType) {
		this.schemaTypeCode = schemaType;
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return true;
	}

	@Override
	public AdvancedSearchPresenter forRequestParameters(String params) {
		if (StringUtils.isNotBlank(params)) {
			String[] parts = params.split("/", 2);
			searchID = parts[1];
			SavedSearch search = getSavedSearch(searchID);
			setSavedSearch(search);
		} else {
			searchExpression = StringUtils.stripToNull(view.getSearchExpression());
			resetFacetSelection();
			schemaTypeCode = view.getSchemaType();
			pageNumber = 1;
			resultsViewMode = SearchResultsViewMode.DETAILED;
			saveTemporarySearch(true);
		}
		return this;
	}

	private void setSavedSearch(SavedSearch search) {
		searchExpression = search.getFreeTextSearch();
		facetSelections.putAll(search.getSelectedFacets());
		sortCriterion = search.getSortField();
		sortOrder = SortOrder.valueOf(search.getSortOrder().name());
		schemaTypeCode = search.getSchemaFilter();
		pageNumber = search.getPageNumber();
		resultsViewMode = search.getResultsViewMode() != null ? search.getResultsViewMode() : SearchResultsViewMode.DETAILED;
		setSelectedPageLength(search.getPageLength());

		view.setSchemaType(schemaTypeCode);
		view.setSearchExpression(searchExpression);
		view.setSearchCriteria(search.getAdvancedSearch());
	}

	@Override
	public int getPageNumber() {
		return pageNumber;
	}

	@Override
	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
	}

	@Override
	public boolean mustDisplayResults() {
		if (StringUtils.isBlank(schemaTypeCode)) {
			return false;
		}
		try {
			buildSearchCondition();
			return true;
		} catch (ConditionException_EmptyCondition e) {
			view.showErrorMessage($("AdvancedSearchView.emptyCondition"));
		} catch (ConditionException_TooManyClosedParentheses e) {
			view.showErrorMessage($("AdvancedSearchView.tooManyClosedParentheses"));
		} catch (ConditionException_UnclosedParentheses e) {
			view.showErrorMessage($("AdvancedSearchView.unclosedParentheses"));
		} catch (ConditionException e) {
			throw new RuntimeException("BUG: Uncaught ConditionException", e);
		}
		return false;
	}

	@Override
	public void suggestionSelected(String suggestion) {
		searchExpression = suggestion;
		view.setSearchExpression(suggestion);
		view.refreshSearchResultsAndFacets();
	}

	@Override
	public String getUserSearchExpression() {
		return searchExpression;
	}

	public void batchEditRequested(List<String> selectedRecordIds, String code, Object value) {
		Map<String, Object> changes = new HashMap<>();
		changes.put(code, value);
		BatchProcessAction action = new ChangeValueOfMetadataBatchProcessAction(changes);

		BatchProcessesManager manager = modelLayerFactory.getBatchProcessesManager();
		LogicalSearchCondition condition = fromAllSchemasIn(collection).where(IDENTIFIER).isIn(selectedRecordIds);
		BatchProcess process = manager.addBatchProcessInStandby(condition, action, "userBatchProcess");
		manager.markAsPending(process);
	}

	@Override
	public List<MetadataVO> getMetadataAllowedInSort() {
		return getMetadataAllowedInSort(schemaTypeCode);
	}

	@Override
	public boolean isPreferAnalyzedFields() {
		return false;
	}

	public List<MetadataVO> getMetadataAllowedInBatchEdit() {
		MetadataToVOBuilder builder = new MetadataToVOBuilder();

		List<MetadataVO> result = new ArrayList<>();
		Language language = Language.withCode(view.getSessionContext().getCurrentLocale().getLanguage());
		for (Metadata metadata : types().getSchemaType(schemaTypeCode).getAllMetadatas().sortAscTitle(language)) {
			if (isBatchEditable(metadata)) {
				result.add(builder.build(metadata, view.getSessionContext()));
			}
		}
		return result;
	}

	@Override
	protected LogicalSearchCondition getSearchCondition() {
		if (condition == null) {
			try {
				buildSearchCondition();
			} catch (ConditionException e) {
				throw new RuntimeException("Unexpected exception (should be unreachable)", e);
			}
		}
		return condition;
	}

	BatchProcessingPresenterService batchProcessingPresenterService() {
		if (batchProcessingPresenterService == null) {
			Locale locale = view.getSessionContext().getCurrentLocale();
			batchProcessingPresenterService = new BatchProcessingPresenterService(collection, appLayerFactory, locale);
		}
		return batchProcessingPresenterService;
	}

	public RecordVODataProvider getSearchResultsAsRecordVOs() {
		MetadataSchemaVO schemaVO = new MetadataSchemaToVOBuilder().build(
				schemaType(schemaTypeCode).getDefaultSchema(), RecordVO.VIEW_MODE.SEARCH, view.getSessionContext());
		return new RecordVODataProvider(schemaVO, new RecordToVOBuilder(), modelLayerFactory, view.getSessionContext()) {
			@Override
			protected LogicalSearchQuery getQuery() {
				LogicalSearchQuery query = getSearchQuery().setHighlighting(highlighter).setOverridedQueryParams(extraSolrParams);
				if (sortCriterion == null) {
					if (StringUtils.isNotBlank(getUserSearchExpression())) {
						query.setFieldBoosts(searchBoostManager().getAllSearchBoostsByMetadataType(view.getCollection()));
						query.setQueryBoosts(searchBoostManager().getAllSearchBoostsByQueryType(view.getCollection()));
					}
					return query;
				}
				Metadata metadata = getMetadata(sortCriterion);
				return sortOrder == SortOrder.ASCENDING ? query.sortAsc(metadata) : query.sortDesc(metadata);
			}
		};
	}

	void buildSearchCondition()
			throws ConditionException {
		String languageCode = searchServices().getLanguageCode(view.getCollection());
		MetadataSchemaType type = schemaType(schemaTypeCode);
		condition = (view.getSearchCriteria().isEmpty()) ?
				from(type).returnAll() :
				new ConditionBuilder(type, languageCode).build(view.getSearchCriteria());
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

	public List<LabelTemplate> getCustomTemplates() {
		LabelTemplateManager labelTemplateManager = appLayerFactory.getLabelTemplateManager();
		return labelTemplateManager.listExtensionTemplates(schemaTypeCode);
	}

	public List<LabelTemplate> getDefaultTemplates() {
		LabelTemplateManager labelTemplateManager = appLayerFactory.getLabelTemplateManager();
		return labelTemplateManager.listTemplates(schemaTypeCode);
	}

	public Boolean computeStatistics() {
		return schemaTypeCode != null && schemaTypeCode.equals(Folder.SCHEMA_TYPE);
	}

	@Override
	public List<String> getSupportedReports() {
		List<String> supportedReports = super.getSupportedReports();
		ReportServices reportServices = new ReportServices(modelLayerFactory, collection);
		List<String> userReports = reportServices.getUserReportTitles(getCurrentUser(), view.getSchemaType());
		supportedReports.addAll(userReports);
		return supportedReports;
	}

	@Override
	public NewReportWriterFactory<SearchResultReportParameters> getReport(String reportTitle) {
		try {
			return super.getReport(reportTitle);
		} catch (UnknownReportRuntimeException e) {
			/**/
			return new SearchResultReportWriterFactory(appLayerFactory);
		}
	}

	public void addToCartRequested(List<String> recordIds, RecordVO cartVO) {
		// TODO: Create an extension for this
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		Cart cart = rm.getOrCreateCart(getCurrentUser(), cartVO.getId());
		switch (schemaTypeCode) {
		case Folder.SCHEMA_TYPE:
			cart.addFolders(recordIds);
			break;
		case Document.SCHEMA_TYPE:
			cart.addDocuments(recordIds);
			break;
		case ContainerRecord.SCHEMA_TYPE:
			cart.addContainers(recordIds);
			break;
		}
		try {
			recordServices().add(cart);
			view.showMessage($("SearchView.addedToCart"));
		} catch (RecordServicesException e) {
			view.showErrorMessage($(e));
		}
	}

	public void createNewCartAndAddToItRequested(String title) {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, modelLayerFactory);
		Cart cart = rm.newCart();
		cart.setTitle(title);
		cart.setOwner(getCurrentUser());
		List<String> selectedRecords = view.getSelectedRecordIds();
		switch (schemaTypeCode) {
		case Folder.SCHEMA_TYPE:
			cart.addFolders(selectedRecords);
			break;
		case Document.SCHEMA_TYPE:
			cart.addDocuments(selectedRecords);
			break;
		case ContainerRecord.SCHEMA_TYPE:
			cart.addContainers(selectedRecords);
			break;
		}
		try {
			recordServices().execute(new Transaction(cart.getWrappedRecord()).setUser(getCurrentUser()));
			view.showMessage($("SearchView.addedToCart"));
		} catch (RecordServicesException e) {
			e.printStackTrace();
		}
	}

	public RecordVODataProvider getOwnedCartsDataProvider() {
		MetadataSchemaToVOBuilder schemaToVOBuilder = new MetadataSchemaToVOBuilder();
		final RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		final MetadataSchemaVO cartSchemaVO = schemaToVOBuilder
				.build(rm.cartSchema(), RecordVO.VIEW_MODE.TABLE, view.getSessionContext());
		return new RecordVODataProvider(cartSchemaVO, new RecordToVOBuilder(), modelLayerFactory, view.getSessionContext()) {
			@Override
			protected LogicalSearchQuery getQuery() {
				return new LogicalSearchQuery(from(rm.cartSchema()).where(rm.cartOwner())
						.isEqualTo(getCurrentUser().getId())).sortAsc(Schemas.TITLE);
			}
		};
	}

	public RecordVODataProvider getSharedCartsDataProvider() {
		MetadataSchemaToVOBuilder schemaToVOBuilder = new MetadataSchemaToVOBuilder();
		final RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		final MetadataSchemaVO cartSchemaVO = schemaToVOBuilder
				.build(rm.cartSchema(), RecordVO.VIEW_MODE.TABLE, view.getSessionContext());
		return new RecordVODataProvider(cartSchemaVO, new RecordToVOBuilder(), modelLayerFactory, view.getSessionContext()) {
			@Override
			protected LogicalSearchQuery getQuery() {
				return new LogicalSearchQuery(from(rm.cartSchema()).where(rm.cartSharedWithUsers())
						.isContaining(Arrays.asList(getCurrentUser().getId()))).sortAsc(Schemas.TITLE);
			}
		};
	}

	@Override
	protected SavedSearch prepareSavedSearch(SavedSearch search) {
		return search.setSearchType(AdvancedSearchView.SEARCH_TYPE)
				.setSchemaFilter(schemaTypeCode)
				.setFreeTextSearch(searchExpression)
				.setAdvancedSearch(view.getSearchCriteria())
				.setPageNumber(pageNumber);
	}

	public Record getTemporarySearchRecord() {
		try {
			return modelLayerFactory.getRecordsCaches().getCache(collection).get(searchID);
		} catch (Exception e) {
			//TODO exception
			e.printStackTrace();
		}

		return null;
	}

	protected SavedSearch saveTemporarySearch(boolean refreshPage) {
		Record tmpSearchRecord;
		if (searchID == null) {
			tmpSearchRecord = recordServices()
					.newRecordWithSchema(schema(SavedSearch.DEFAULT_SCHEMA), newRandomId());
		} else {
			tmpSearchRecord = getTemporarySearchRecord();
		}

		SavedSearch search = new SavedSearch(tmpSearchRecord, types())
				.setTitle("temporaryAdvance")
				.setUser(getCurrentUser().getId())
				.setPublic(false)
				.setSortField(sortCriterion)
				.setSortOrder(SavedSearch.SortOrder.valueOf(sortOrder.name()))
				.setSelectedFacets(facetSelections.getNestedMap())
				.setTemporary(true)
				.setSearchType(AdvancedSearchView.SEARCH_TYPE)
				.setSchemaFilter(schemaTypeCode)
				.setFreeTextSearch(searchExpression)
				.setAdvancedSearch(view.getSearchCriteria())
				.setPageNumber(pageNumber)
				.setResultsViewMode(resultsViewMode)
				.setPageLength(selectedPageLength);
		try {
			((RecordImpl) search.getWrappedRecord()).markAsSaved(search.getVersion() + 1, search.getSchema());
			modelLayerFactory.getRecordsCaches().getCache(collection).forceInsert(search.getWrappedRecord());

			//recordServices().update(search);
			updateUIContext(search);
			if (refreshPage) {
				view.navigate().to().advancedSearchReplay(search.getId());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return search;
	}

	@Override
	public String getOriginType() {
		return batchProcessingPresenterService().getOriginType(buildBatchProcessLogicalSearchQuery());
	}

	@Override
	public RecordVO newRecordVO(String schema, SessionContext sessionContext) {
		return batchProcessingPresenterService().newRecordVO(schema, sessionContext, buildBatchProcessLogicalSearchQuery());
	}

	@Override
	public InputStream simulateButtonClicked(String selectedType, RecordVO viewObject)
			throws RecordServicesException {
		BatchProcessResults results = batchProcessingPresenterService()
				.simulate(selectedType, buildBatchProcessLogicalSearchQuery().setNumberOfRows(100), viewObject, getCurrentUser());
		return batchProcessingPresenterService().formatBatchProcessingResults(results);
	}

	@Override
	public void processBatchButtonClicked(String selectedType, RecordVO viewObject)
			throws RecordServicesException {
		BatchProcessResults results = batchProcessingPresenterService()
				.execute(selectedType, buildBatchProcessLogicalSearchQuery(), viewObject, getCurrentUser());
		if (searchID != null) {
			view.navigate().to().advancedSearchReplay(searchID);
		} else {
			view.navigate().to().advancedSearch();
		}

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
		return batchProcessingPresenterService()
				.newRecordFieldFactory(schemaType, selectedType, buildBatchProcessLogicalSearchQuery());
	}

	@Override
	public boolean hasWriteAccessOnAllRecords() {
		LogicalSearchQuery query = buildBatchProcessLogicalSearchQuery();
		return searchServices().getResultsCount(query.filteredWithUserWrite(getCurrentUser())) == searchServices()
				.getResultsCount(query);
	}

	@Override
	public long getNumberOfRecords() {
		return (int) searchServices().getResultsCount(buildBatchProcessLogicalSearchQuery());
	}

	public void switchToTableView() {
		resultsViewMode = SearchResultsViewMode.TABLE;
		saveTemporarySearch(true);
	}

	public void switchToDetailedView() {
		resultsViewMode = SearchResultsViewMode.DETAILED;
		saveTemporarySearch(true);
	}

	public int getMaxSelectableResults() {
		return modelLayerFactory.getSystemConfigurationsManager().getValue(ConstellioEIMConfigs.MAX_SELECTABLE_SEARCH_RESULTS);
	}

	@Override
	public Object getReportParameters(String report) {
		switch (report) {
		case "Reports.fakeReport":
		case "Reports.FolderLinearMeasureStats":
			return super.getReportParameters(report);
		}
		return new SearchResultReportParameters(view.getSelectedRecordIds(), view.getSchemaType(),
				collection, report, getCurrentUser(), getSearchQuery());
	}

	public boolean hasCurrentUserPermissionToUseCart() {
		return getCurrentUser().has(RMPermissionsTo.USE_CART).globally();
	}

	public LogicalSearchQuery buildBatchProcessLogicalSearchQuery() {
		//		if (((AdvancedSearchViewImpl) view).isSelectAllMode()) {
		if (ContainerRecord.SCHEMA_TYPE.equals(schemaTypeCode) || StorageSpace.SCHEMA_TYPE.equals(schemaTypeCode)) {
			if (!batchProcessOnAllSearchResults) {
				return buildUnsecuredLogicalSearchQueryWithSelectedIds();
			} else {
				return buildUnsecuredLogicalSearchQueryWithUnselectedIds();
			}
		} else {
			if (!batchProcessOnAllSearchResults) {
				return buildLogicalSearchQueryWithSelectedIds();
			} else {
				return buildLogicalSearchQueryWithUnselectedIds();
			}
		}
	}

	public LogicalSearchQuery buildLogicalSearchQueryWithSelectedIds() {
		LogicalSearchQuery query = new LogicalSearchQuery()
				.setCondition(condition.andWhere(Schemas.IDENTIFIER).isIn(view.getSelectedRecordIds())
						.andWhere(Schemas.LOGICALLY_DELETED_STATUS).isFalseOrNull())
				.filteredWithUser(getCurrentUser()).filteredWithUserWrite(getCurrentUser())
				.setPreferAnalyzedFields(isPreferAnalyzedFields());
		if (searchExpression != null && !searchExpression.isEmpty()) {
			query.setFreeTextQuery(searchExpression);
		}
		return query;
	}

	public LogicalSearchQuery buildLogicalSearchQueryWithUnselectedIds() {
		LogicalSearchQuery query = new LogicalSearchQuery()
				.setCondition(condition.andWhere(Schemas.IDENTIFIER).isNotIn(view.getUnselectedRecordIds())
						.andWhere(Schemas.LOGICALLY_DELETED_STATUS).isFalseOrNull())
				.filteredWithUser(getCurrentUser()).filteredWithUserWrite(getCurrentUser())
				.setPreferAnalyzedFields(isPreferAnalyzedFields());
		if (searchExpression != null && !searchExpression.isEmpty()) {
			query.setFreeTextQuery(searchExpression);
		}
		return query;
	}

	public LogicalSearchQuery buildUnsecuredLogicalSearchQueryWithSelectedIds() {
		LogicalSearchQuery query = new LogicalSearchQuery()
				.setCondition(condition.andWhere(Schemas.IDENTIFIER).isIn(view.getSelectedRecordIds())
						.andWhere(Schemas.LOGICALLY_DELETED_STATUS).isFalseOrNull())
				.setPreferAnalyzedFields(isPreferAnalyzedFields());
		if (searchExpression != null && !searchExpression.isEmpty()) {
			query.setFreeTextQuery(searchExpression);
		}
		return query;
	}

	public LogicalSearchQuery buildUnsecuredLogicalSearchQueryWithUnselectedIds() {
		LogicalSearchQuery query = new LogicalSearchQuery()
				.setCondition(condition.andWhere(Schemas.IDENTIFIER).isNotIn(view.getUnselectedRecordIds())
						.andWhere(Schemas.LOGICALLY_DELETED_STATUS).isFalseOrNull())
				.setPreferAnalyzedFields(isPreferAnalyzedFields());
		if (searchExpression != null && !searchExpression.isEmpty()) {
			query.setFreeTextQuery(searchExpression);
		}
		return query;
	}

	public LogicalSearchQuery buildLogicalSearchQueryWithAllSearchResults() {
		LogicalSearchQuery query = new LogicalSearchQuery()
				.filteredWithUser(getCurrentUser()).filteredWithUserWrite(getCurrentUser())
				.setPreferAnalyzedFields(isPreferAnalyzedFields());
		if (searchExpression != null && !searchExpression.isEmpty()) {
			query.setFreeTextQuery(searchExpression);
		}
		return query;
	}

	public void logRecordView(RecordVO recordVO) {
		Record record = presenterService().getRecord(recordVO.getId());
		ModelLayerFactory modelLayerFactory = view.getConstellioFactories().getModelLayerFactory();
		User user = getCurrentUser();
		modelLayerFactory.newLoggingServices().logRecordView(record, user);
		setChanged();
		notifyObservers(recordVO);
	}

	@Override
	public void allSearchResultsButtonClicked() {
		batchProcessOnAllSearchResults = true;
	}

	@Override
	public void selectedSearchResultsButtonClicked() {
		batchProcessOnAllSearchResults = false;
	}

	@Override
	public boolean isSearchResultsSelectionForm() {
		return true;
	}

	public void setResult(SearchResultTable result) {
		this.result = result;
	}

	public User getUser() {
		return getCurrentUser();
	}

	public List<RecordVO> getRecordVOList(List<String> ids) {
		List<RecordVO> recordsVO = new ArrayList<>();
		RecordServices recordServices = modelLayerFactory.newRecordServices();
		RecordToVOBuilder builder = new RecordToVOBuilder();
		for (String id : ids) {
			recordsVO.add(builder.build(recordServices.getDocumentById(id), RecordVO.VIEW_MODE.FORM, view.getSessionContext()));
		}
		return recordsVO;
	}

}
