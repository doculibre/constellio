package com.constellio.app.ui.pages.search;

import com.constellio.app.api.extensions.BatchProcessingExtension;
import com.constellio.app.api.extensions.params.SearchPageConditionParam;
import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.extensions.api.RMModuleExtensions;
import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplate;
import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplateManager;
import com.constellio.app.modules.rm.reports.builders.search.SearchResultReportParameters;
import com.constellio.app.modules.rm.reports.builders.search.SearchResultReportWriterFactory;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Cart;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.StorageSpace;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.components.SearchResultTable;
import com.constellio.app.ui.framework.reports.NewReportWriterFactory;
import com.constellio.app.ui.framework.reports.ReportWithCaptionVO;
import com.constellio.app.ui.pages.search.batchProcessing.BatchProcessingPresenterService;
import com.constellio.app.ui.pages.search.criteria.ConditionBuilder;
import com.constellio.app.ui.pages.search.criteria.ConditionException;
import com.constellio.app.ui.pages.search.criteria.ConditionException.ConditionException_EmptyCondition;
import com.constellio.app.ui.pages.search.criteria.ConditionException.ConditionException_TooManyClosedParentheses;
import com.constellio.app.ui.pages.search.criteria.ConditionException.ConditionException_UnclosedParentheses;
import com.constellio.data.frameworks.extensions.VaultBehaviorsList;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.SavedSearch;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.extensions.ModelLayerCollectionExtensions;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordImpl;
import com.constellio.model.services.reports.ReportServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.search.query.logical.ongoing.OngoingLogicalSearchCondition;
import com.vaadin.ui.Component;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.data.dao.services.cache.InsertionReason.WAS_MODIFIED;
import static com.constellio.data.dao.services.idGenerator.UUIDV1Generator.newRandomId;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class AdvancedSearchPresenter extends SearchPresenter<AdvancedSearchView> {
	private static final Logger LOGGER = LoggerFactory.getLogger(AdvancedSearchPresenter.class);

	String searchExpression;
	String schemaTypeCode;
	String schemaCode;
	private int pageNumber;
	private List<String> listSearchableMetadataSchemaType;
	private String searchID;
	private SearchResultTable result;
	private boolean batchProcessOnAllSearchResults;
	private Boolean hasAnyReport;

	private transient LogicalSearchCondition condition;
	private transient BatchProcessingPresenterService batchProcessingPresenterService;
	private transient ModelLayerCollectionExtensions modelLayerExtensions;
	private transient VaultBehaviorsList<BatchProcessingExtension> batchProcessingExtensions;
	private transient RMModuleExtensions rmModuleExtensions;
	private transient RMSchemasRecordsServices rm;
	
	private boolean usingCustomSort = false;

	public AdvancedSearchPresenter(AdvancedSearchView view) {
		super(view);

		rmModuleExtensions = appLayerFactory.getExtensions().forCollection(view.getCollection()).forModule(ConstellioRMModule.ID);
		modelLayerExtensions = modelLayerFactory.getExtensions().forCollection(view.getCollection());
		batchProcessingExtensions = appLayerFactory.getExtensions().forCollection(view.getCollection()).batchProcessingExtensions;
	}

	@Override
	public String getSavedSearchId() {
		return searchID;
	}

	@Override
	public void sortCriterionSelected(String sortCriterion, SortOrder sortOrder) {
		usingCustomSort = sortCriterion != null && sortOrder != null;
		super.sortCriterionSelected(sortCriterion, sortOrder);
	}

	@Override
	protected LogicalSearchQuery getSearchQuery() {
		LogicalSearchQuery query = super.getSearchQuery();
		if (!usingCustomSort) {
			query.clearSort();
			query.sortAsc(Schemas.TITLE);
			if (modelLayerFactory.getSystemConfigs().isAddingSecondarySortWhenSortingByScoreOrTitle()) {
				query.sortAsc(Schemas.IDENTIFIER);
			}
		}
		return query;
	}

	public void setSchemaType(String schemaType) {
		this.schemaTypeCode = schemaType;
		setSchemaTypeOnPresenterService();
	}

	private void setSchemaTypeOnPresenterService() {
		if (schemaTypeCode != null) {
			service.setMetadataSchemaTypesList(Arrays.asList(modelLayerFactory.getMetadataSchemasManager()
					.getSchemaTypes(collection).getSchemaType(schemaTypeCode)));
		} else {
			service.setMetadataSchemaTypesList(new ArrayList<MetadataSchemaType>());
		}
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return true;
	}

	@Override
	public AdvancedSearchPresenter forRequestParameters(String params) {
		if (StringUtils.isNotBlank(params)) {
			String[] parts = params.split("/", 3);
			searchID = parts[1];
			SavedSearch search = getSavedSearch(searchID);
			setSavedSearch(search);
			updateUIContext(search);
		} else {
			searchExpression = StringUtils.stripToNull(view.getSearchExpression());
			resetFacetSelection();
			schemaTypeCode = view.getSchemaType();
			pageNumber = 1;
			resultsViewMode = DEFAULT_VIEW_MODE;
			saveTemporarySearch(false);
		}
		setSchemaTypeOnPresenterService();
		return this;
	}


	private void setSavedSearch(SavedSearch search) {
		searchExpression = search.getFreeTextSearch();
		facetSelections.putAll(search.getSelectedFacets());
		sortCriterion = search.getSortField();
		sortOrder = SortOrder.valueOf(search.getSortOrder().name());
		schemaTypeCode = search.getSchemaFilter();
		schemaCode = search.getSchemaCodeFilter();
		pageNumber = search.getPageNumber();
		resultsViewMode = search.getResultsViewMode();
		setSelectedPageLength(search.getPageLength());

		view.setSchemaType(schemaTypeCode);
		view.setSchema(schemaCode);
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

	private Map<String, Object> getChanges(String code, Object value) {
		Map<String, Object> changes = new HashMap<>();
		changes.put(code, value);
		if ((Task.DEFAULT_SCHEMA + "_" + Task.ASSIGNEE).equals(code)) {
			changes.put((Task.DEFAULT_SCHEMA + "_" + Task.ASSIGNED_ON), LocalDate.now());
			changes.put((Task.DEFAULT_SCHEMA + "_" + Task.ASSIGNER), getCurrentUser().getId());
		}
		return changes;
	}

	@Override
	public List<MetadataVO> getMetadataAllowedInSort() {
		return getMetadataAllowedInSort(schemaTypeCode);
	}

	@Override
	public boolean isPreferAnalyzedFields() {
		return false;
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

	void buildSearchCondition()
			throws ConditionException {
		String languageCode = searchServices().getLanguageCode(view.getCollection());
		MetadataSchemaType type = schemaType(schemaTypeCode);
		condition = (view.getSearchCriteria().isEmpty()) ?
					generateFrom(type).returnAll() :
					new ConditionBuilder(type, schemaCode, languageCode).build(view.getSearchCriteria());
		condition = appCollectionExtentions.adjustSearchPageCondition(new SearchPageConditionParam((Component) view, condition, getCurrentUser()));
	}

	private OngoingLogicalSearchCondition generateFrom(MetadataSchemaType type) {
		if (org.apache.commons.lang.StringUtils.isBlank(schemaCode)) {
			return from(type);
		}

		return from(type.getSchema(schemaCode));
	}

	public List<LabelTemplate> getDefaultTemplates() {
		LabelTemplateManager labelTemplateManager = appLayerFactory.getLabelTemplateManager();
		return labelTemplateManager.listTemplates(schemaTypeCode);
	}

	public Boolean computeStatistics() {
		return schemaTypeCode != null && schemaTypeCode.equals(Folder.SCHEMA_TYPE) && isStatisticReportEnabled();
	}

	@Override
	public List<ReportWithCaptionVO> getSupportedReports() {
		List<ReportWithCaptionVO> supportedReports = super.getSupportedReports();
		ReportServices reportServices = new ReportServices(modelLayerFactory, collection);
		List<String> userReports = reportServices.getUserReportTitles(getCurrentUser(), view.getSchemaType());
		if (userReports != null) {
			for (String reportTitle : userReports) {
				supportedReports.add(new ReportWithCaptionVO(reportTitle, reportTitle));
			}
		}
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

	private List<Record> getRecords(List<String> recordIds) {
		return modelLayerFactory.newRecordServices().getRecordsById(collection, recordIds);
	}

	private void addFoldersToCart(String cartId, List<Record> records) {
		if (rm().numberOfFoldersInFavoritesReachesLimit(cartId, rm().getListRecordsIds(records))) {
			view.showMessage("DisplayFolderViewImpl.cartCannotContainMoreThanAThousandFolders");
		} else {
			for (Record record : records) {
				rm().wrapFolder(record).addFavorite(cartId);
			}
		}
	}

	private void addDocumentsToCart(String cartId, List<Record> records) {
		if (rm().numberOfDocumentsInFavoritesReachesLimit(cartId, rm().getListRecordsIds(records))) {
			view.showMessage($("DisplayDocumentView.cartCannotContainMoreThanAThousandDocuments"));
		} else {
			for (Record record : records) {
				rm().wrapDocument(record).addFavorite(cartId);
			}
		}
	}

	private void addContainersToCart(String cartId, List<Record> records) {
		if (rm().numberOfContainersInFavoritesReachesLimit(cartId, rm().getListRecordsIds(records))) {
			view.showMessage($("DisplayContainerViewImpl.cartCannotContainMoreThanAThousandContainers"));
		} else {
			for (Record record : records) {
				rm().wrapContainerRecord(record).addFavorite(cartId);
			}
		}
	}

	@Override
	protected SavedSearch prepareSavedSearch(SavedSearch search) {
		return search.setSearchType(AdvancedSearchView.SEARCH_TYPE)
				.setSchemaFilter(schemaTypeCode)
				.setSchemaCodeFilter(schemaCode)
				.setFreeTextSearch(searchExpression)
				.setAdvancedSearch(view.getSearchCriteria())
				.setPageNumber(pageNumber);
	}

	public Record getTemporarySearchRecord() {
		try {
			return recordServices().getDocumentById(searchID);
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
			if (tmpSearchRecord != null) {
				SavedSearch savedSearch = new SavedSearch(tmpSearchRecord, types());
				if (!savedSearch.isTemporary()) {
					tmpSearchRecord = recordServices()
							.newRecordWithSchema(schema(SavedSearch.DEFAULT_SCHEMA), newRandomId());
				}
			}
		}

		SavedSearch search = new SavedSearch(tmpSearchRecord, types())
				.setTitle($("SearchView.savedSearch.temporaryAdvance"))
				.setUser(getCurrentUser().getId())
				.setPublic(false)
				.setSortField(sortCriterion)
				.setSortOrder(SavedSearch.SortOrder.valueOf(sortOrder.name()))
				.setSelectedFacets(facetSelections.getNestedMap())
				.setTemporary(true)
				.setSearchType(AdvancedSearchView.SEARCH_TYPE)
				.setSchemaFilter(schemaTypeCode)
				.setSchemaCodeFilter(schemaCode)
				.setFreeTextSearch(searchExpression)
				.setAdvancedSearch(view.getSearchCriteria())
				.setPageNumber(pageNumber)
				.setResultsViewMode(resultsViewMode)
				.setPageLength(selectedPageLength);
		try {
			((RecordImpl) search.getWrappedRecord()).markAsSaved(search.getVersion() + 1, search.getSchema());
			modelLayerFactory.getRecordsCaches().getCache(collection).insert(search.getWrappedRecord(), WAS_MODIFIED);

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
	public Object getReportParameters(String report) {
		switch (report) {
			case "Reports.fakeReport":
			case "Reports.FolderLinearMeasureStats":
				return super.getReportParameters(report);
		}

		return new SearchResultReportParameters(view.getSelectedRecordIds(), view.getSchemaType(),
				collection, report, getCurrentUser(), buildReportLogicalSearchQuery());
	}

	public boolean hasCurrentUserPermissionToUseCart() {
		return getCurrentUser().has(RMPermissionsTo.USE_MY_CART).globally();
	}

	public LogicalSearchQuery buildReportLogicalSearchQuery() {
		return buildLogicalSearchQuery().filteredWithUserRead(getUser());
	}

	private LogicalSearchQuery buildLogicalSearchQuery() {
		List<String> selectedRecordIds = view.getSelectedRecordIds();
		LogicalSearchQuery query = null;
		if (ContainerRecord.SCHEMA_TYPE.equals(schemaTypeCode) || StorageSpace.SCHEMA_TYPE.equals(schemaTypeCode)) {
			if (!batchProcessOnAllSearchResults) {
				query = buildUnsecuredLogicalSearchQueryWithSelectedIds();
			} else if (selectedRecordIds != null && !selectedRecordIds.isEmpty()) {
				query = buildUnsecuredLogicalSearchQueryWithUnselectedIds();
			} else {
				query = buildUnsecuredLogicalSearchQueryWithAllRecords();
			}
		} else {
			if (!batchProcessOnAllSearchResults) {
				query = buildLogicalSearchQueryWithSelectedIds();
			} else if (selectedRecordIds != null && !selectedRecordIds.isEmpty()) {
				query = buildLogicalSearchQueryWithUnselectedIds();
			} else {
				query = buildLogicalSearchQueryWithAllRecords();
			}
		}

		if (searchExpression != null && !searchExpression.isEmpty()) {
			query.setFreeTextQuery(searchExpression);
		}

		if (sortCriterion != null && !sortCriterion.isEmpty()) {
			Metadata metadata = getMetadata(sortCriterion);
			if (sortOrder == SortOrder.ASCENDING) {
				query.sortAsc(metadata);
			} else {
				query.sortDesc(metadata);
			}
		}

		return query;
	}

	public LogicalSearchQuery buildLogicalSearchQueryWithSelectedIds() {
		LogicalSearchQuery query = getSearchQuery();
		query.setCondition(query.getCondition().andWhere(Schemas.IDENTIFIER).isIn(view.getSelectedRecordIds())
				.andWhere(Schemas.LOGICALLY_DELETED_STATUS).isFalseOrNull())
				.filteredWithUserRead(getCurrentUser()).filteredWithUserWrite(getCurrentUser())
				.setPreferAnalyzedFields(isPreferAnalyzedFields());
		return query;
	}

	public LogicalSearchQuery buildLogicalSearchQueryWithUnselectedIds() {
		LogicalSearchQuery query = getSearchQuery();
		query.setCondition(query.getCondition().andWhere(Schemas.IDENTIFIER).isNotIn(view.getUnselectedRecordIds())
				.andWhere(Schemas.LOGICALLY_DELETED_STATUS).isFalseOrNull())
				.filteredWithUserRead(getCurrentUser()).filteredWithUserWrite(getCurrentUser())
				.setPreferAnalyzedFields(isPreferAnalyzedFields());
		return query;
	}

	public LogicalSearchQuery buildLogicalSearchQueryWithAllRecords() {
		LogicalSearchQuery query = getSearchQuery();
		query.setCondition(query.getCondition().andWhere(Schemas.LOGICALLY_DELETED_STATUS).isFalseOrNull())
				.filteredWithUserRead(getCurrentUser()).filteredWithUserWrite(getCurrentUser())
				.setPreferAnalyzedFields(isPreferAnalyzedFields());
		return query;
	}

	public LogicalSearchQuery buildUnsecuredLogicalSearchQueryWithSelectedIds() {
		LogicalSearchQuery query = getSearchQuery();
		query.setCondition(query.getCondition().andWhere(Schemas.IDENTIFIER).isIn(view.getSelectedRecordIds())
				.andWhere(Schemas.LOGICALLY_DELETED_STATUS).isFalseOrNull())
				.setPreferAnalyzedFields(isPreferAnalyzedFields());
		return query;
	}

	public LogicalSearchQuery buildUnsecuredLogicalSearchQueryWithUnselectedIds() {
		LogicalSearchQuery query = getSearchQuery();
		query.setCondition(query.getCondition().andWhere(Schemas.IDENTIFIER).isNotIn(view.getUnselectedRecordIds())
				.andWhere(Schemas.LOGICALLY_DELETED_STATUS).isFalseOrNull())
				.setPreferAnalyzedFields(isPreferAnalyzedFields());
		return query;
	}

	public LogicalSearchQuery buildUnsecuredLogicalSearchQueryWithAllRecords() {
		LogicalSearchQuery query = getSearchQuery();
		query.setCondition(query.getCondition().andWhere(Schemas.LOGICALLY_DELETED_STATUS).isFalseOrNull())
				.setPreferAnalyzedFields(isPreferAnalyzedFields());
		return query;
	}


	public void setResult(SearchResultTable result) {
		this.result = result;
	}

	public boolean isStatisticReportEnabled() {
		return new ConstellioEIMConfigs(modelLayerFactory).isStatisticReportEnabled();
	}

	private RMSchemasRecordsServices rm() {
		if (rm == null) {
			rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		}
		return rm;
	}

	public MetadataSchemaVO getSchema() {
		return new MetadataSchemaToVOBuilder()
				.build(schema(Cart.DEFAULT_SCHEMA), RecordVO.VIEW_MODE.TABLE, view.getSessionContext());
	}
}