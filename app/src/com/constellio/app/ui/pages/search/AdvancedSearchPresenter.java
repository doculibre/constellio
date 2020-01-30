package com.constellio.app.ui.pages.search;

import com.constellio.app.api.extensions.BatchProcessingExtension;
import com.constellio.app.api.extensions.BatchProcessingExtension.BatchProcessFeededByIdsParams;
import com.constellio.app.api.extensions.BatchProcessingExtension.BatchProcessFeededByQueryParams;
import com.constellio.app.api.extensions.params.SearchPageConditionParam;
import com.constellio.app.entities.batchProcess.ChangeValueOfMetadataBatchAsyncTask;
import com.constellio.app.entities.schemasDisplay.MetadataDisplayConfig;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.extensions.AppLayerCollectionExtensions;
import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.extensions.api.AdvancedSearchPresenterExtension;
import com.constellio.app.modules.rm.extensions.api.RMModuleExtensions;
import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplate;
import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplateManager;
import com.constellio.app.modules.rm.reports.builders.search.SearchResultReportParameters;
import com.constellio.app.modules.rm.reports.builders.search.SearchResultReportWriterFactory;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Cart;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.PrintableReport;
import com.constellio.app.modules.rm.wrappers.StorageSpace;
import com.constellio.app.modules.tasks.model.wrappers.Task;
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
import com.constellio.app.ui.framework.reports.ReportWithCaptionVO;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.search.batchProcessing.BatchProcessingPresenter;
import com.constellio.app.ui.pages.search.batchProcessing.BatchProcessingPresenterService;
import com.constellio.app.ui.pages.search.batchProcessing.entities.BatchProcessResults;
import com.constellio.app.ui.pages.search.criteria.ConditionBuilder;
import com.constellio.app.ui.pages.search.criteria.ConditionException;
import com.constellio.app.ui.pages.search.criteria.ConditionException.ConditionException_EmptyCondition;
import com.constellio.app.ui.pages.search.criteria.ConditionException.ConditionException_TooManyClosedParentheses;
import com.constellio.app.ui.pages.search.criteria.ConditionException.ConditionException_UnclosedParentheses;
import com.constellio.data.dao.services.bigVault.solr.SolrUtils;
import com.constellio.data.frameworks.extensions.VaultBehaviorsList;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.batchprocess.AsyncTask;
import com.constellio.model.entities.batchprocess.AsyncTaskCreationRequest;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.RecordUpdateOptions;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Report;
import com.constellio.model.entities.records.wrappers.SavedSearch;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.extensions.ModelLayerCollectionExtensions;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.batch.manager.BatchProcessesManager;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordImpl;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.reports.ReportServices;
import com.constellio.model.services.search.SPEQueryResponse;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.search.query.logical.ongoing.OngoingLogicalSearchCondition;
import com.vaadin.ui.Component;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.data.dao.services.cache.InsertionReason.WAS_MODIFIED;
import static com.constellio.data.dao.services.idGenerator.UUIDV1Generator.newRandomId;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class AdvancedSearchPresenter extends SearchPresenter<AdvancedSearchView> implements BatchProcessingPresenter {
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

	public boolean hasBatchProcessPermission() {
		return getCurrentUser().has(CorePermissions.MODIFY_RECORDS_USING_BATCH_PROCESS).globally() || getCurrentUser().has(CorePermissions.MODIFY_RECORDS_USING_BATCH_PROCESS).onSomething();
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

	public boolean batchEditRequested(String code, Object value, String schemaType) {
		LogicalSearchQuery query = buildBatchProcessLogicalSearchQuery();
		SearchServices searchServices = modelLayerFactory.newSearchServices();
		ModifiableSolrParams params = searchServices.addSolrModifiableParams(query);

		Map<String, Object> changes = getChanges(code, value);
		AsyncTask asyncTask = new ChangeValueOfMetadataBatchAsyncTask(changes, SolrUtils.toSingleQueryString(params),
				null, searchServices().getResultsCount(query));

		String username = getCurrentUser() == null ? null : getCurrentUser().getUsername();
		AsyncTaskCreationRequest asyncTaskRequest = new AsyncTaskCreationRequest(asyncTask, collection, "userBatchProcess");
		asyncTaskRequest.setUsername(username);

		BatchProcessesManager manager = modelLayerFactory.getBatchProcessesManager();
		manager.addAsyncTask(asyncTaskRequest);
		return true;
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

	public List<MetadataVO> getMetadataAllowedInBatchEdit(String schemaType) {
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
	public ValidationErrors validateBatchProcessing() {
		ValidationErrors errors = new ValidationErrors();

		for (BatchProcessingExtension extension : batchProcessingExtensions) {
			if (batchProcessOnAllSearchResults) {
				extension.validateBatchProcess(
						new BatchProcessFeededByQueryParams(errors, buildBatchProcessLogicalSearchQuery(), schemaTypeCode));
			} else {
				extension.validateBatchProcess(
						new BatchProcessFeededByIdsParams(errors, view.getSelectedRecordIds(), schemaTypeCode));
			}
		}
		return errors;
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
			public LogicalSearchQuery getQuery() {
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

	private boolean isRMModuleActivated() {
		return appLayerFactory.getModulesManager().isModuleEnabled(collection, new ConstellioRMModule());
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

	public List<LabelTemplate> getCustomTemplates() {
		LabelTemplateManager labelTemplateManager = appLayerFactory.getLabelTemplateManager();
		return labelTemplateManager.listExtensionTemplates(schemaTypeCode);
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

	public void addToCartRequested(List<String> recordIds, RecordVO cartVO) {
		// TODO: Create an extension for this
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		Cart cart = rm.getOrCreateCart(getCurrentUser(), cartVO.getId());
		addToCartRequested(recordIds, cart);
	}

	public void addToCartRequested(List<String> recordIds, Cart cart) {
		List<Record> records = getRecords(recordIds);
		String cartId = cart.getId();
		switch (schemaTypeCode) {
			case Folder.SCHEMA_TYPE:
				addFoldersToCart(cartId, records);
				break;
			case Document.SCHEMA_TYPE:
				addDocumentsToCart(cartId, records);
				break;
			case ContainerRecord.SCHEMA_TYPE:
				addContainersToCart(cartId, records);
				break;
		}
		try {
			recordServices().add(cart);
			Transaction transaction = new Transaction(records);
			transaction.setOptions(RecordUpdateOptions.validationExceptionSafeOptions());
			recordServices().execute(transaction);
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
		String cartId = cart.getId();
		List<Record> records = getRecords(view.getSelectedRecordIds());
		switch (schemaTypeCode) {
			case Folder.SCHEMA_TYPE:
				addFoldersToCart(cartId, records);
				break;
			case Document.SCHEMA_TYPE:
				addDocumentsToCart(cartId, records);
				break;
			case ContainerRecord.SCHEMA_TYPE:
				addContainersToCart(cartId, records);
				break;
		}
		try {
			recordServices().execute(new Transaction(cart.getWrappedRecord()).setUser(getCurrentUser()));
			Transaction transaction = new Transaction(records);
			transaction.setOptions(RecordUpdateOptions.validationExceptionSafeOptions());
			recordServices().execute(transaction);
			view.showMessage($("SearchView.addedToCart"));
		} catch (RecordServicesException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	private List<Record> getRecords(List<String> recordIds) {
		return modelLayerFactory.newRecordServices().getRecordsById(collection, recordIds);
	}

	private void addFoldersToCart(String cartId, List<Record> records) {
		if (rm().numberOfFoldersInFavoritesReachesLimit(cartId, records.size())) {
			view.showMessage("DisplayFolderViewImpl.cartCannotContainMoreThanAThousandFolders");
		} else {
			for (Record record : records) {
				rm().wrapFolder(record).addFavorite(cartId);
			}
		}
	}

	private void addDocumentsToCart(String cartId, List<Record> records) {
		if (rm().numberOfDocumentsInFavoritesReachesLimit(cartId, records.size())) {
			view.showMessage($("DisplayDocumentView.cartCannotContainMoreThanAThousandDocuments"));
		} else {
			for (Record record : records) {
				rm().wrapDocument(record).addFavorite(cartId);
			}
		}
	}

	private void addContainersToCart(String cartId, List<Record> records) {
		if (rm().numberOfContainersInFavoritesReachesLimit(cartId, records.size())) {
			view.showMessage($("DisplayContainerViewImpl.cartCannotContainMoreThanAThousandContainers"));
		} else {
			for (Record record : records) {
				rm().wrapContainerRecord(record).addFavorite(cartId);
			}
		}
	}

	public RecordVODataProvider getOwnedCartsDataProvider() {
		MetadataSchemaToVOBuilder schemaToVOBuilder = new MetadataSchemaToVOBuilder();
		final RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		final MetadataSchemaVO cartSchemaVO = schemaToVOBuilder
				.build(rm.cartSchema(), RecordVO.VIEW_MODE.TABLE, view.getSessionContext());
		return new RecordVODataProvider(cartSchemaVO, new RecordToVOBuilder(), modelLayerFactory, view.getSessionContext()) {
			@Override
			public LogicalSearchQuery getQuery() {
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
			public LogicalSearchQuery getQuery() {
				return new LogicalSearchQuery(from(rm.cartSchema()).where(rm.cartSharedWithUsers())
						.isContaining(Arrays.asList(getCurrentUser().getId()))).sortAsc(Schemas.TITLE);
			}
		};
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
	public String getOriginSchema(String schemaType, String selectedType) {
		return batchProcessingPresenterService().getOriginSchema(schemaType, selectedType, buildBatchProcessLogicalSearchQuery());
	}

	@Override
	public RecordVO newRecordVO(String schema, String schemaType, SessionContext sessionContext) {
		return batchProcessingPresenterService().newRecordVO(schema, sessionContext, buildBatchProcessLogicalSearchQuery());
	}

	@Override
	public InputStream simulateButtonClicked(String selectedType, String schemaType, RecordVO viewObject,
											 List<String> metadatasToEmpty)
			throws RecordServicesException {
		BatchProcessResults results = batchProcessingPresenterService()
				.simulate(selectedType, buildBatchProcessLogicalSearchQuery().setNumberOfRows(100), viewObject, metadatasToEmpty, getCurrentUser());
		return batchProcessingPresenterService().formatBatchProcessingResults(results);
	}

	@Override
	public boolean validateUserHaveBatchProcessPermissionOnAllRecords(String schemaTypeCode) {
		if (!getCurrentUser().has(CorePermissions.MODIFY_RECORDS_USING_BATCH_PROCESS).globally()) {
			return false;
		}

		LogicalSearchQuery logicalSearchQuery = buildBatchProcessLogicalSearchQuery();
		long numFound = searchServices().query(logicalSearchQuery).getNumFound();
		logicalSearchQuery = logicalSearchQuery.filteredWithUser(getUser(), CorePermissions.MODIFY_RECORDS_USING_BATCH_PROCESS);
		SPEQueryResponse speQueryResponse = searchServices().query(logicalSearchQuery);
		long numFoundWithFilter = speQueryResponse.getNumFound();

		return numFoundWithFilter == numFound;
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

	@Override
	public boolean processBatchButtonClicked(String selectedType, String schemaType, RecordVO viewObject,
											 List<String> metadatasToEmpty)
			throws RecordServicesException {
		batchProcessingPresenterService()
				.execute(selectedType, buildBatchProcessLogicalSearchQuery(), viewObject, metadatasToEmpty, getCurrentUser());
		if (searchID != null) {
			view.navigate().to().advancedSearchReplay(searchID);
		} else {
			view.navigate().to().advancedSearch();
		}
		return true;
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
		return batchProcessingPresenterService()
				.newRecordFieldFactory(schemaType, selectedType, buildBatchProcessLogicalSearchQuery());
	}

	@Override
	public boolean hasWriteAccessOnAllRecords(String schemaType) {
		LogicalSearchQuery query = buildBatchProcessLogicalSearchQuery();
		return searchServices().getResultsCount(query.filteredWithUserWrite(getCurrentUser())) == searchServices()
				.getResultsCount(query);
	}

	@Override
	public long getNumberOfRecords(String schemaType) {
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
				collection, report, getCurrentUser(), buildReportLogicalSearchQuery());
	}

	public boolean hasCurrentUserPermissionToUseCart() {
		return getCurrentUser().has(RMPermissionsTo.USE_MY_CART).globally();
	}

	public LogicalSearchQuery buildReportLogicalSearchQuery() {
		return buildLogicalSearchQuery().filteredWithUser(getUser());
	}

	public LogicalSearchQuery buildBatchProcessLogicalSearchQuery() {
		LogicalSearchQuery query = buildLogicalSearchQuery();
		for (AdvancedSearchPresenterExtension extension : rmModuleExtensions.getAdvancedSearchPresenterExtensions()) {
			query = extension.addAdditionalSearchQueryFilters(
					new AdvancedSearchPresenterExtension.AddAdditionalSearchQueryFiltersParams(query, schemaTypeCode));
		}
		return query;
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
				.filteredWithUser(getCurrentUser()).filteredWithUserWrite(getCurrentUser())
				.setPreferAnalyzedFields(isPreferAnalyzedFields());
		return query;
	}

	public LogicalSearchQuery buildLogicalSearchQueryWithUnselectedIds() {
		LogicalSearchQuery query = getSearchQuery();
		query.setCondition(query.getCondition().andWhere(Schemas.IDENTIFIER).isNotIn(view.getUnselectedRecordIds())
				.andWhere(Schemas.LOGICALLY_DELETED_STATUS).isFalseOrNull())
				.filteredWithUser(getCurrentUser()).filteredWithUserWrite(getCurrentUser())
				.setPreferAnalyzedFields(isPreferAnalyzedFields());
		return query;
	}

	public LogicalSearchQuery buildLogicalSearchQueryWithAllRecords() {
		LogicalSearchQuery query = getSearchQuery();
		query.setCondition(query.getCondition().andWhere(Schemas.LOGICALLY_DELETED_STATUS).isFalseOrNull())
				.filteredWithUser(getCurrentUser()).filteredWithUserWrite(getCurrentUser())
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

	public LogicalSearchQuery buildLogicalSearchQueryWithAllSearchResults() {
		LogicalSearchQuery query = new LogicalSearchQuery()
				.filteredWithUser(getCurrentUser()).filteredWithUserWrite(getCurrentUser())
				.setPreferAnalyzedFields(isPreferAnalyzedFields());
		return query;
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

	public List<RecordVO> getRecordVOList(List<String> ids) {
		List<RecordVO> recordsVO = new ArrayList<>();
		RecordServices recordServices = modelLayerFactory.newRecordServices();
		RecordToVOBuilder builder = new RecordToVOBuilder();
		for (String id : ids) {
			recordsVO.add(builder.build(recordServices.getDocumentById(id), RecordVO.VIEW_MODE.FORM, view.getSessionContext()));
		}
		return recordsVO;
	}

	public boolean hasAnyReportForSchemaType(String schemaTypeCode) {
		if (this.hasAnyReport == null) {
			MetadataSchemaTypes collectionsTypes = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection);
			MetadataSchema reportSchema = collectionsTypes.getSchema(Report.DEFAULT_SCHEMA);
			MetadataSchema metadataReportSchema = collectionsTypes.getSchema(PrintableReport.SCHEMA_NAME);

			this.hasAnyReport = false;
			for (Report report : coreSchemas().getAllReports()) {
				if (schemaTypeCode.equals(report.getSchemaTypeCode())) {
					hasAnyReport = true;
				}
			}

			if (!hasAnyReport && collectionsTypes.hasSchema(PrintableReport.SCHEMA_NAME)) {
				RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
				for (PrintableReport report : rm.getAllPrintableReports()) {
					if (schemaTypeCode.equals(report.getReportType())) {
						hasAnyReport = true;
					}
				}
			}

		}
		return hasAnyReport;
	}

	public List<String> getListSearchableMetadataSchemaType() {
		if (listSearchableMetadataSchemaType == null) {
			listSearchableMetadataSchemaType = new ArrayList<>();
			for (MetadataSchemaType schemaType : modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection)
					.getSchemaTypes()) {
				if (isMetadataSchemaTypesSearchable(schemaType)) {
					listSearchableMetadataSchemaType.add(schemaType.getCode());
				}
			}
		}
		return listSearchableMetadataSchemaType;
	}

	private boolean isMetadataSchemaTypesSearchable(MetadataSchemaType types) {
		return schemasDisplayManager().getType(collection, types.getCode()).isAdvancedSearch();
	}

	public boolean isStatisticReportEnabled() {
		return new ConstellioEIMConfigs(modelLayerFactory).isStatisticReportEnabled();
	}

	public boolean isPdfGenerationActionPossible(List<String> recordIds) {
		List<Record> records = modelLayerFactory.newRecordServices().getRecordsById(collection, recordIds);
		for (Record record : records) {
			if (!rmModuleExtensions.isCreatePDFAActionPossibleOnDocument(rm().wrapDocument(record), getCurrentUser())) {
				view.showErrorMessage($("AdvancedSearchView.actionBlockedByExtension"));
				return false;
			}
		}
		return true;
	}

	private RMSchemasRecordsServices rm() {
		if (rm == null) {
			rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		}
		return rm;
	}

	public List<Cart> getOwnedCarts() {
		return rm().wrapCarts(searchServices().search(new LogicalSearchQuery(from(rm().cartSchema()).where(rm().cart.owner())
				.isEqualTo(getCurrentUser().getId())).sortAsc(Schemas.TITLE)));
	}

	public void addToDefaultFavorite(List<String> selectedRecordIds) {
		List<Record> records = getRecords(selectedRecordIds);
		String currentUserId = getCurrentUser().getId();
		switch (schemaTypeCode) {
			case Folder.SCHEMA_TYPE:
				addFoldersToCart(currentUserId, records);
				break;
			case Document.SCHEMA_TYPE:
				addDocumentsToCart(currentUserId, records);
				break;
			case ContainerRecord.SCHEMA_TYPE:
				addContainersToCart(currentUserId, records);
				break;
		}
		try {
			Transaction transaction = new Transaction(records);
			transaction.setOptions(RecordUpdateOptions.validationExceptionSafeOptions());
			recordServices().execute(transaction);
			view.showMessage($("SearchView.addedToDefaultFavorites"));
		} catch (RecordServicesException e) {
			view.showErrorMessage($(e));
		}
	}

	public MetadataSchemaVO getSchema() {
		return new MetadataSchemaToVOBuilder()
				.build(schema(Cart.DEFAULT_SCHEMA), RecordVO.VIEW_MODE.TABLE, view.getSessionContext());
	}
}