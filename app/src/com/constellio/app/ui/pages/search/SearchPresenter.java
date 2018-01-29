package com.constellio.app.ui.pages.search;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.data.dao.services.idGenerator.UUIDV1Generator.newRandomId;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import com.constellio.app.api.extensions.taxonomies.UserSearchEvent;
import com.constellio.app.entities.schemasDisplay.MetadataDisplayConfig;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbDocument;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbFolder;
import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplate;
import com.constellio.app.modules.rm.reports.builders.search.stats.StatsReportParameters;
import com.constellio.app.modules.rm.reports.factories.labels.ExampleReportParameters;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.corrector.CorrectorExcluderManager;
import com.constellio.app.services.corrector.CorrectorExclusion;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.FacetVO;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.MetadataToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.components.NewReportPresenter;
import com.constellio.app.ui.framework.components.SearchResultDetailedTable;
import com.constellio.app.ui.framework.components.breadcrumb.BaseBreadcrumbTrail;
import com.constellio.app.ui.framework.data.SearchResultVODataProvider;
import com.constellio.app.ui.framework.reports.NewReportWriterFactory;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.base.UIContext;
import com.constellio.data.dao.dto.records.FacetValue;
import com.constellio.data.utils.AccentApostropheCleaner;
import com.constellio.data.utils.KeySetMap;
import com.constellio.data.utils.TimeProvider;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.enums.SearchPageLength;
import com.constellio.model.entities.enums.SearchSortType;
import com.constellio.model.entities.modules.Module;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Capsule;
import com.constellio.model.entities.records.wrappers.Facet;
import com.constellio.model.entities.records.wrappers.SavedSearch;
import com.constellio.model.entities.records.wrappers.SearchEvent;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.records.wrappers.structure.FacetType;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.extensions.ConstellioModulesManager;
import com.constellio.model.services.logging.SearchEventServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.RecordServicesRuntimeException;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.schemas.MetadataList;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.search.SPEQueryResponse;
import com.constellio.model.services.search.SearchBoostManager;
import com.constellio.model.services.search.SearchConfigurationsManager;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.search.cache.SerializedCacheSearchService;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryFacetFilters;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.search.zipContents.ZipContentsService;
import com.constellio.model.services.search.zipContents.ZipContentsService.NoContentToZipRuntimeException;
import com.vaadin.server.StreamResource.StreamSource;

public abstract class SearchPresenter<T extends SearchView> extends BasePresenter<T> implements NewReportPresenter {

	private static final String ZIP_CONTENT_RESOURCE = "zipContentsFolder";

	public int getDefaultPageLength() {
		SearchPageLength defaultPageLength = getCurrentUser().getDefaultPageLength();
		return defaultPageLength != null ? defaultPageLength.getValue() : SearchResultDetailedTable.DEFAULT_PAGE_LENGTH;
	}

	public enum SortOrder {ASCENDING, DESCENDING}

	private static Logger LOGGER = LoggerFactory.getLogger(SearchPresenter.class);

	protected Map<String, String[]> extraSolrParams = new HashMap<>();
	KeySetMap<String, String> facetSelections = new KeySetMap<>();
	Map<String, Boolean> facetStatus = new HashMap<>();
	List<String> suggestions;
	String sortCriterion;
	SortOrder sortOrder;
	String resultsViewMode;
	String collection;
	transient SchemasDisplayManager schemasDisplayManager;
	transient SearchPresenterService service;
	boolean highlighter = true;
	int selectedPageLength;
	boolean allowDownloadZip = true;
	int lastPageNumber;
	private CorrectorExcluderManager correctorExcluderManager;

	public int getSelectedPageLength() {
		return selectedPageLength;
	}

	public void setSelectedPageLength(int selectedPageLength) {
		this.selectedPageLength = selectedPageLength;
	}

	public SearchPresenter(T view) {
		super(view);
		init(view.getConstellioFactories(), view.getSessionContext());
		initSortParameters();
		correctorExcluderManager = appLayerFactory.getCorrectorExcluderManager();
	}

	private void initSortParameters() {
		SearchSortType searchSortType = modelLayerFactory.getSystemConfigs().getSearchSortType();
		switch (searchSortType) {
		case RELEVENCE:
			sortOrder = SortOrder.DESCENDING;
			this.sortCriterion = null;
			break;
		case PATH_ASC:
			this.sortCriterion = Schemas.PATH.getCode();
			this.sortOrder = SortOrder.ASCENDING;
			break;
		case PATH_DES:
			this.sortCriterion = Schemas.PATH.getCode();
			this.sortOrder = SortOrder.DESCENDING;
			break;
		case ID_ASC:
			this.sortCriterion = Schemas.IDENTIFIER.getCode();
			this.sortOrder = SortOrder.ASCENDING;
			break;
		case ID_DES:
			this.sortCriterion = Schemas.IDENTIFIER.getCode();
			this.sortOrder = SortOrder.DESCENDING;
			break;
		case CREATION_DATE_ASC:
			this.sortCriterion = Schemas.CREATED_ON.getCode();
			this.sortOrder = SortOrder.ASCENDING;
			break;
		case CREATION_DATE_DES:
			this.sortCriterion = Schemas.CREATED_ON.getCode();
			this.sortOrder = SortOrder.DESCENDING;
			break;
		case MODIFICATION_DATE_ASC:
			this.sortCriterion = Schemas.MODIFIED_ON.getCode();
			this.sortOrder = SortOrder.ASCENDING;
			break;
		case MODIFICATION_DATE_DES:
			this.sortCriterion = Schemas.MODIFIED_ON.getCode();
			this.sortOrder = SortOrder.DESCENDING;
			break;
		default:
			throw new RuntimeException("Unsupported type " + searchSortType);
		}
	}

	public boolean isAllowDownloadZip() {
		return allowDownloadZip;
	}

	public void setExtraSolrParams(Map<String, String[]> extraSolrParams) {
		this.extraSolrParams = extraSolrParams;
	}

	public Map<String, String[]> getExtraSolrParams() {
		return extraSolrParams;
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
		SessionContext sessionContext = ConstellioUI.getCurrentSessionContext();
		init(constellioFactories, sessionContext);
	}

	private void init(ConstellioFactories constellioFactories, SessionContext sessionContext) {
		collection = sessionContext.getCurrentCollection();
		service = new SearchPresenterService(collection, constellioFactories.getModelLayerFactory());
		schemasDisplayManager = constellioFactories.getAppLayerFactory().getMetadataSchemasDisplayManager();

		ConstellioModulesManager modulesManager = constellioFactories.getAppLayerFactory().getModulesManager();
		Module rmModule = modulesManager.getInstalledModule(ConstellioRMModule.ID);
		allowDownloadZip = modulesManager.isModuleEnabled(collection, rmModule);
	}

	public void resetFacetAndOrder() {
		resetFacetSelection();
		//TODO
		initSortParameters();
		//sortOrder = SortOrder.ASCENDING;
	}

	public String getResultsViewMode() {
		return resultsViewMode;
	}

	public abstract Record getTemporarySearchRecord();

	public abstract SearchPresenter<T> forRequestParameters(String params);

	public abstract boolean mustDisplayResults();

	public abstract int getPageNumber();

	public abstract void setPageNumber(int pageNumber);

	public int getLastPageNumber() {
		return lastPageNumber;
	}

	public List<FacetVO> getFacets(SearchResultVODataProvider dataProvider) {
		//Call #1
		if (dataProvider == null /* || dataProvider.getFieldFacetValues() == null */) {
			return service.getFacets(getSearchQuery(), facetStatus, getCurrentLocale());
		} else {
			return service.buildFacetVOs(dataProvider.getFieldFacetValues(), dataProvider.getQueryFacetsValues(),
					facetStatus, getCurrentLocale());

		}
	}

	public String getSortCriterion() {
		return sortCriterion;
	}

	public SortOrder getSortOrder() {
		return sortOrder;
	}

	public String getUserSearchExpression() {
		return null;
	}

	public List<Capsule> getCapsuleForCurrentSearch() {
		List<Capsule> correspondingCapsules = new ArrayList<>();
		if (Toggle.ADVANCED_SEARCH_CONFIGS.isEnabled()) {
			SchemasRecordsServices schemasRecordsServices = new SchemasRecordsServices(collection,
					appLayerFactory.getModelLayerFactory());
			if (StringUtils.isNotEmpty(getUserSearchExpression())) {
				String lowerCasedSearchTerms = getUserSearchExpression().toLowerCase();
				String approstropheTrimmedSearchTerms = AccentApostropheCleaner.cleanAll(lowerCasedSearchTerms);
				String[] searchTerms = approstropheTrimmedSearchTerms.split(" ");
				MetadataSchema defaultCapsuleSchema = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection)
						.getSchemaType(Capsule.SCHEMA_TYPE).getDefaultSchema();
				//LogicalSearchCondition condition = from(defaultCapsuleSchema).where(defaultCapsuleSchema.getMetadata(Capsule.KEYWORDS)).isContaining(asList(searchTerms));
				//TODO Check for a more efficient way to fix this.
				LogicalSearchCondition condition = from(defaultCapsuleSchema).returnAll();

				List<Capsule> allCapsules = schemasRecordsServices
						.wrapCapsules(searchServices().search(new LogicalSearchQuery(condition)));

				for (Capsule capsule : allCapsules) {
					if (CollectionUtils.containsAny(asList(searchTerms), capsule.getKeywords())) {
						correspondingCapsules.add(capsule);
					}
				}
			}
		}
		return correspondingCapsules;
	}

	public boolean mustDisplaySuggestions(SearchResultVODataProvider dataProvider) {
		if (dataProvider.size() != 0) {
			return false;
		}

		SPEQueryResponse suggestionsResponse = searchServices()
				.query(getSearchQuery().setNumberOfRows(0).setSpellcheck(true));
		if (suggestionsResponse.isCorrectlySpelt()) {
			return false;
		}
		suggestions = suggestionsResponse.getSpellCheckerSuggestions();

		return !suggestions.isEmpty();
	}

	public List<String> getSuggestions() {
		return suggestions;
	}

	public SearchResultVODataProvider getSearchResults(final boolean facets) {
		//Call #4

		final SearchResultVODataProvider dataProvider = new SearchResultVODataProvider(new RecordToVOBuilder(), appLayerFactory,
				view.getSessionContext()) {
			@Override
			public LogicalSearchQuery getQuery() {
				LogicalSearchQuery query = getSearchQuery().setHighlighting(highlighter).setOverridedQueryParams(extraSolrParams);
				if (facets) {
					service.configureQueryToComputeFacets(query);
				}
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

			boolean hasExtensionsBeenNotified = false;

			@Override
			protected void onQuery(LogicalSearchQuery query, SPEQueryResponse response) {

				if (!hasExtensionsBeenNotified) {
					hasExtensionsBeenNotified = true;
					SavedSearch search = new SavedSearch(recordServices().newRecordWithSchema(schema(SavedSearch.DEFAULT_SCHEMA)),
							types())
							.setUser(getCurrentUser().getId())
							.setSortField(sortCriterion)
							.setSortOrder(SavedSearch.SortOrder.valueOf(sortOrder.name()))
							.setSelectedFacets(facetSelections.getNestedMap())
							.setTemporary(false);

					search = prepareSavedSearch(search);

					LocalDateTime queryDateTime = TimeProvider.getLocalDateTime();
					String username = view.getSessionContext().getCurrentUser().getUsername();
					String language = view.getSessionContext().getCurrentLocale().getLanguage();
					UserSearchEvent param = new UserSearchEvent(response, query, search, queryDateTime, language, username);

					appLayerFactory.getExtensions().forCollection(view.getSessionContext().getCurrentCollection())
							.notifyNewUserSearch(param);
				}
			}

			@Override
			public int size() {
				SerializedCacheSearchService searchServices = new SerializedCacheSearchService(modelLayerFactory, queryCache,
						true);
				if (size == null) {
					SPEQueryResponse response = searchServices.query(query, resultsPerPage);
					logSearchEvent(this, response);
					size = response.getRecords().size();
				}
				return size;
			}
		};

		return dataProvider;
	}

	private void logSearchEvent(SearchResultVODataProvider dataProvider, SPEQueryResponse response) {
		ModifiableSolrParams modifiableSolrParams = modelLayerFactory.newSearchServices()
				.addSolrModifiableParams(dataProvider.getQuery());

		SchemasRecordsServices schemasRecordsServices = new SchemasRecordsServices(collection, modelLayerFactory);
		SearchEvent searchEvent = schemasRecordsServices.newSearchEvent();

		ArrayList<String> paramList = new ArrayList<>();

		for (String paramName : modifiableSolrParams.getParameterNames()) {
			if (!paramName.equals("qf") && !paramName.equals("pf")
					&& !paramName.equals("fl")) {
				if (paramName.equals("q")) {
					searchEvent.setQuery(StringUtils.stripAccents(modifiableSolrParams.get(paramName).toLowerCase()));
				} else {
					String[] values = modifiableSolrParams.getParams(paramName);

					if (values.length == 1) {
						paramList.add(paramName + "=" + values[0]);
					} else if (values.length > 1) {
						StringBuilder valuesAsOneStringBuilder = new StringBuilder();
						for (String value : values) {
							valuesAsOneStringBuilder.append(paramName).append("=").append(value).append(";");
						}
						paramList.add(valuesAsOneStringBuilder.toString());
					}

				}
			}
		}
		searchEvent.setParams(paramList);
		searchEvent.setQTime(response.getQtime());
		searchEvent.setNumFound(response.getNumFound());
		UIContext uiContext = view.getUIContext();
		SearchEvent oldSearchEvent = schemasRecordsServices
				.wrapSearchEvent((Record) uiContext.getAttribute("CURRENT_SEARCH_EVENT"));

		if (!areSearchEventEqual(oldSearchEvent, searchEvent)) {
			view.getUIContext().setAttribute("CURRENT_SEARCH_EVENT", searchEvent.getWrappedRecord());
			SearchEventServices searchEventServices = new SearchEventServices(view.getCollection(), modelLayerFactory);
			searchEventServices.save(searchEvent);
		}
	}

	private boolean areSearchEventEqual(SearchEvent searchEvenFromSessionContext, SearchEvent searchEvent) {
		if (searchEvenFromSessionContext == null && searchEvent != null
				|| searchEvent == null && searchEvenFromSessionContext != null
				|| searchEvent == null && searchEvenFromSessionContext == null) {
			return false;
		}

		if (searchEvenFromSessionContext.getUsername() == null &&
				searchEvent.getUsername() != null
				|| searchEvenFromSessionContext.getUsername() != null &&
				searchEvent.getUsername() == null) {
			return false;
		}

		boolean isUserEqual = searchEvent.getUsername() == null && searchEvenFromSessionContext.getUsername() == null
				|| searchEvenFromSessionContext.getUsername()
				.equals(searchEvent.getUsername());

		boolean isQEqual = searchEvenFromSessionContext.getQuery() == null &&
				searchEvent.getQuery() == null || searchEvenFromSessionContext.getQuery()
				.equals(searchEvent.getQuery());

		if (!isQEqual || !isUserEqual) {
			return false;
		}

		for (String param : searchEvenFromSessionContext.getParams()) {
			if (!searchEvent.getParams().contains(param)) {
				return false;
			}
		}

		for (String param : searchEvent.getParams()) {
			if (!searchEvenFromSessionContext.getParams().contains(param)) {
				return false;
			}
		}

		return true;
	}

	private List<MetadataSchemaVO> getSchemas() {
		MetadataSchemaToVOBuilder builder = new MetadataSchemaToVOBuilder();
		return asList(
				builder.build(schema(Folder.DEFAULT_SCHEMA), RecordVO.VIEW_MODE.TABLE, view.getSessionContext()),
				builder.build(schema(Folder.DEFAULT_SCHEMA), RecordVO.VIEW_MODE.TABLE, view.getSessionContext()),
				builder.build(schema(Folder.DEFAULT_SCHEMA), RecordVO.VIEW_MODE.TABLE, view.getSessionContext()));
	}

	// TODO RecordVODataProvider for search results

	public void facetValueSelected(String facetId, String facetValue) {
		facetSelections.get(facetId).add(facetValue);
		view.refreshSearchResultsAndFacets();
	}

	public void facetValueDeselected(String facetId, String facetValue) {
		facetSelections.get(facetId).remove(facetValue);
		view.refreshSearchResultsAndFacets();
	}

	public void facetDeselected(String facetId) {
		facetSelections.get(facetId).clear();
		view.refreshSearchResultsAndFacets();
	}

	public void facetOpened(String facetId) {
		facetStatus.put(facetId, true);
	}

	public void facetClosed(String facetId) {
		facetStatus.put(facetId, false);
	}

	public KeySetMap<String, String> getFacetSelections() {
		return facetSelections;
	}

	public void setFacetSelections(Map<String, Set<String>> facetSelections) {
		this.facetSelections.putAll(facetSelections);
	}

	public void sortCriterionSelected(String sortCriterion, SortOrder sortOrder) {
		this.sortCriterion = sortCriterion;
		this.sortOrder = sortOrder;
		view.refreshSearchResults(true, true);
	}

	@Override
	public List<String> getSupportedReports() {
		List<String> supportedReports = new ArrayList<>();
		if (view.computeStatistics()) {
			supportedReports.add("Reports.FolderLinearMeasureStats");
		}
		return supportedReports;
	}

	@Override
	public NewReportWriterFactory getReport(String report) {
		switch (report) {
		case "Reports.fakeReport":
			return getRmReportBuilderFactories().exampleBuilderFactory.getValue();
		case "Reports.FolderLinearMeasureStats":
			return getRmReportBuilderFactories().statsBuilderFactory.getValue();
		}
		throw new UnknownReportRuntimeException("BUG: Unknown report " + report);
	}

	public String getZippedContentsFilename() {
		return $("SearchView.contentZip");
	}

	public StreamSource getZippedContents() {
		return new StreamSource() {
			@Override
			public InputStream getStream() {
				File folder = modelLayerFactory.getDataLayerFactory().getIOServicesFactory().newFileService()
						.newTemporaryFolder(ZIP_CONTENT_RESOURCE);
				File file = new File(folder, getZippedContentsFilename());
				try {
					new ZipContentsService(modelLayerFactory, collection)
							.zipContentsOfRecords(view.getSelectedRecordIds(), file);
					return new FileInputStream(file);
				} catch (NoContentToZipRuntimeException e) {
					LOGGER.error("Error while zipping", e);
					view.showErrorMessage($("SearchView.noContentInSelectedRecords"));
					return null;
				} catch (Exception e) {
					LOGGER.error("Error while zipping", e);
					view.showErrorMessage($("SearchView.zipContentsError"));
					return null;
				}
			}
		};
	}

	public String getSortCriterionValueAmong(List<MetadataVO> sortableMetadata) {
		if (this.sortCriterion == null) {
			return null;
		}
		if (!this.sortCriterion.startsWith("global_")) {
			return this.sortCriterion;
		} else {
			String localCode = new SchemaUtils().getLocalCodeFromMetadataCode(this.sortCriterion);
			for (MetadataVO metadata : sortableMetadata) {
				if (metadata.getLocalCode().equals(localCode)) {
					return metadata.getCode();
				}
			}
		}
		return this.sortCriterion;
	}

	public abstract void suggestionSelected(String suggestion);

	public abstract List<MetadataVO> getMetadataAllowedInSort();

	public abstract boolean isPreferAnalyzedFields();

	protected abstract LogicalSearchCondition getSearchCondition();

	protected LogicalSearchQuery getSearchQuery() {
		String userSearchExpression = getUserSearchExpression();
		LogicalSearchQuery query = new LogicalSearchQuery(getSearchCondition())
				.setOverridedQueryParams(extraSolrParams)
				.setFreeTextQuery(userSearchExpression)
				.filteredWithUser(getCurrentUser())
				.filteredByStatus(StatusFilter.ACTIVES)
				.setPreferAnalyzedFields(isPreferAnalyzedFields());

		//		query.setReturnedMetadatas(ReturnedMetadatasFilter.onlyFields(
		//				schemasDisplayManager.getReturnedFieldsForSearch(collection)));
		query.setReturnedMetadatas(ReturnedMetadatasFilter.allExceptContentAndLargeText());

		SchemasRecordsServices schemas = new SchemasRecordsServices(collection, modelLayerFactory);
		LogicalSearchQueryFacetFilters filters = query.getFacetFilters();
		filters.clear();
		for (Entry<String, Set<String>> selection : facetSelections.getMapEntries()) {
			try {
				Facet facet = schemas.getFacet(selection.getKey());
				if (!selection.getValue().isEmpty()) {
					if (facet.getFacetType() == FacetType.FIELD) {
						filters.selectedFieldFacetValues(facet.getFieldDataStoreCode(), selection.getValue());
					} else if (facet.getFacetType() == FacetType.QUERY) {
						filters.selectedQueryFacetValues(facet.getId(), selection.getValue());
					}
				}
			} catch (RecordServicesRuntimeException.NoSuchRecordWithId id) {
				LOGGER.warn("Facet '" + id + "' has been deleted");
			}
		}
		return query;
	}

	public void setHighlighter(boolean highlighter) {
		this.highlighter = highlighter;
	}

	protected void resetFacetSelection() {
		facetSelections.clear();
		initSortParameters();
	}

	protected SavedSearch getSavedSearch(String id) {
		Record record = recordServices().getDocumentById(id);
		return new SavedSearch(record, types());
	}

	Metadata getMetadata(String code) {
		if (code.startsWith("global_")) {
			return Schemas.getGlobalMetadata(code);
		}
		SchemaUtils utils = new SchemaUtils();
		String schemaCode = utils.getSchemaCode(code);
		return schema(schemaCode).getMetadata(utils.getLocalCode(code, schemaCode));
	}

	protected List<MetadataVO> getMetadataAllowedInAdvancedSearch(String schemaTypeCode) {
		MetadataSchemaType schemaType = types().getSchemaType(schemaTypeCode);
		List<FacetValue> schema_s = modelLayerFactory.newSearchServices().query(new LogicalSearchQuery()
				.setNumberOfRows(0)
				.setCondition(from(schemaType).returnAll()).addFieldFacet("schema_s").filteredWithUser(getCurrentUser()))
				.getFieldFacetValues("schema_s");
		Set<String> metadataCodes = new HashSet<>();
		if (Toggle.RESTRICT_METADATAS_TO_THOSE_OF_SCHEMAS_WITH_RECORDS.isEnabled()) {
			if (schema_s != null) {
				for (FacetValue facetValue : schema_s) {
					if (facetValue.getQuantity() > 0) {
						String schema = facetValue.getValue();
						for (Metadata metadata : types().getSchema(schema).getMetadatas()) {
							if (metadata.getInheritance() != null && metadata.isEnabled()) {
								metadataCodes.add(metadata.getInheritance().getCode());
							} else if (metadata.getInheritance() == null && metadata.isEnabled()) {
								metadataCodes.add(metadata.getCode());
							}
						}
					}
				}
			}
		} else {
			for (Metadata metadata : schemaType.getAllMetadatas()) {
				metadataCodes.add(metadata.getCode());
			}
		}

		MetadataToVOBuilder builder = new MetadataToVOBuilder();

		List<MetadataVO> result = new ArrayList<>();
		//		result.add(builder.build(schemaType.getMetadataWithAtomicCode(CommonMetadataBuilder.PATH), view.getSessionContext()));
		MetadataList allMetadatas = schemaType.getAllMetadatas();
		for (Metadata metadata : allMetadatas) {
			if (!schemaType.hasSecurity() || (metadataCodes.contains(metadata.getCode()))) {
				boolean isTextOrString =
						metadata.getType() == MetadataValueType.STRING || metadata.getType() == MetadataValueType.TEXT;
				MetadataDisplayConfig config = schemasDisplayManager().getMetadata(view.getCollection(), metadata.getCode());
				if (config.isVisibleInAdvancedSearch() &&
						isMetadataVisibleForUser(metadata, getCurrentUser()) &&
						(!isTextOrString || isTextOrString && metadata.isSearchable() ||
								Schemas.PATH.getLocalCode().equals(metadata.getLocalCode()) ||
								ConnectorSmbFolder.PARENT_CONNECTOR_URL.equals(metadata.getLocalCode()) ||
								ConnectorSmbDocument.PARENT_CONNECTOR_URL.equals(metadata.getLocalCode()))) {
					result.add(builder.build(metadata, view.getSessionContext()));
				}
			}
		}
		return result;
	}

	private boolean isMetadataVisibleForUser(Metadata metadata, User currentUser) {
		if (MetadataValueType.REFERENCE.equals(metadata.getType())) {
			String referencedSchemaType = metadata.getAllowedReferences().getTypeWithAllowedSchemas();
			Taxonomy taxonomy = appLayerFactory.getModelLayerFactory().getTaxonomiesManager()
					.getTaxonomyFor(collection, referencedSchemaType);
			if (taxonomy != null) {
				List<String> taxonomyGroupIds = taxonomy.getGroupIds();
				List<String> taxonomyUserIds = taxonomy.getUserIds();
				List<String> userGroups = currentUser.getUserGroups();
				for (String group : taxonomyGroupIds) {
					for (String userGroup : userGroups) {
						if (userGroup.equals(group)) {
							return true;
						}
					}
				}
				return (taxonomyGroupIds.isEmpty() && taxonomyUserIds.isEmpty()) || taxonomyUserIds.contains(currentUser.getId());
			} else {
				return true;
			}
		}
		return true;
	}

	protected MetadataVO getMetadataVO(String metadataCode) {
		return presenterService().getMetadataVO(metadataCode, view.getSessionContext());
	}

	protected List<MetadataVO> getMetadataAllowedInSort(String schemaTypeCode) {
		MetadataSchemaType schemaType = schemaType(schemaTypeCode);
		return getMetadataAllowedInSort(schemaType);
	}

	protected List<MetadataVO> getMetadataAllowedInSort(MetadataSchemaType schemaType) {
		MetadataToVOBuilder builder = new MetadataToVOBuilder();

		List<MetadataVO> result = new ArrayList<>();
		for (Metadata metadata : schemaType.getAllMetadatas()) {
			if (metadata.isSortable()) {
				result.add(builder.build(metadata, view.getSessionContext()));
			}
		}
		return result;
	}

	protected List<LabelTemplate> getTemplates() {
		return appLayerFactory.getLabelTemplateManager().listTemplates(null);
	}

	protected boolean saveSearch(String title, boolean publicAccess) {
		Record record = recordServices().newRecordWithSchema(schema(SavedSearch.DEFAULT_SCHEMA), newRandomId());
		SavedSearch search = new SavedSearch(record, types())
				.setTitle(title)
				.setUser(publicAccess ? null : getCurrentUser().getId())
				.setPublic(publicAccess)
				.setSortField(sortCriterion)
				.setSortOrder(SavedSearch.SortOrder.valueOf(sortOrder.name()))
				.setSelectedFacets(facetSelections.getNestedMap())
				.setTemporary(false);

		try {
			recordServices().add(prepareSavedSearch(search));
		} catch (RecordServicesException e) {
			view.showErrorMessage($("SearchView.errorSavingSearch"));
			return false;
		}
		view.showMessage($("SearchView.searchSaved"));
		return true;
	}

	protected abstract SavedSearch saveTemporarySearch(boolean refreshPage);

	protected SavedSearch prepareSavedSearch(SavedSearch search) {
		return search;
	}

	protected void updateUIContext(SavedSearch savedSearch) {
		String searchId = savedSearch.getId();
		boolean advancedSearch = StringUtils.isNotBlank(savedSearch.getSchemaFilter());
		UIContext uiContext = view.getUIContext();
		uiContext.setAttribute(BaseBreadcrumbTrail.SEARCH_ID, searchId);
		uiContext.setAttribute(BaseBreadcrumbTrail.ADVANCED_SEARCH, advancedSearch);
		uiContext.clearAttribute(BaseBreadcrumbTrail.TAXONOMY_CODE);
	}

	protected SearchBoostManager searchBoostManager() {
		return modelLayerFactory.getSearchBoostManager();
	}

	@Override
	public Object getReportParameters(String report) {
		switch (report) {
		case "Reports.fakeReport":
			return new ExampleReportParameters(view.getSelectedRecordIds());
		case "Reports.FolderLinearMeasureStats":
			return new StatsReportParameters(view.getCollection(), appLayerFactory, getSearchQuery());
		}
		throw new UnknownReportRuntimeException("BUG: Unknown report " + report);
	}

	protected void addToSelectionButtonClicked() {
		SessionContext sessionContext = view.getSessionContext();
		List<String> selectedSearchResultRecordIds = view.getSelectedRecordIds();
		boolean someElementsNotAdded = false;
		for (String selectedRecordId : selectedSearchResultRecordIds) {
			Record record = modelLayerFactory.newRecordServices().getDocumentById(selectedRecordId);

			if (asList(Folder.SCHEMA_TYPE, Document.SCHEMA_TYPE, ContainerRecord.SCHEMA_TYPE).contains(record.getTypeCode())) {
				sessionContext.addSelectedRecordId(selectedRecordId, record == null ? null : record.getTypeCode());
			} else {
				someElementsNotAdded = true;
			}

		}

		if (someElementsNotAdded) {
			view.showErrorMessage($("ConstellioHeader.selection.cannotAddRecords"));
		}
	}

	public void deleteSuggestionButtonClicked(String exclusionString, String collection) {
		CorrectorExclusion correctorExclusion = new CorrectorExclusion();
		correctorExclusion.setCollection(collection);
		correctorExclusion.setExclusion(exclusionString);
		correctorExcluderManager.addExclusion(correctorExclusion);
	}

	public List<String> getAllNonExcluded(String collection, List<String> correctedList) {
		List<CorrectorExclusion> allExclusion = correctorExcluderManager.getAllExclusion(collection);

		List<String> allExclusionFormCollection = new ArrayList<String>();

		for (String corrected : correctedList) {
			boolean found = false;
			for (CorrectorExclusion correctorExclusion : allExclusion) {
				if (correctorExclusion.getCollection().equals(collection) &&
						corrected.equals(correctorExclusion.getExclusion())) {
					found = true;
					break;
				}
			}
			if (!found) {
				allExclusionFormCollection.add(corrected);
			}
		}

		return allExclusionFormCollection;
	}

	public void searchResultClicked(RecordVO searchResultVO) {
		SearchEventServices searchEventServices = new SearchEventServices(view.getCollection(), modelLayerFactory);
		Record searchEvent = (Record) view.getUIContext().getAttribute("CURRENT_SEARCH_EVENT");

		searchEventServices.incrementClickCounter(searchEvent.getId());
	}

	public void searchNavigationButtonClicked() {
		SearchEventServices searchEventServices = new SearchEventServices(view.getCollection(), modelLayerFactory);
		Record searchEvent = (Record) view.getUIContext().getAttribute("CURRENT_SEARCH_EVENT");

		searchEventServices.incrementPageNavigationCounter(searchEvent.getId());
	}

	public void searchResultElevationClicked(RecordVO recordVO) {
		String freeTextQuery = getSearchQuery().getFreeTextQuery();
		Record record = recordVO.getRecord();
		SearchConfigurationsManager searchConfigurationsManager = modelLayerFactory.getSearchConfigurationsManager();
		if (!searchConfigurationsManager.isElevated(freeTextQuery, record)) {
			searchConfigurationsManager.setElevated(freeTextQuery, record, false);
		} else {
			searchConfigurationsManager.removeElevated(freeTextQuery, record.getId());
		}
		view.refreshSearchResultsAndFacets();
	}

	public void searchResultExclusionClicked(RecordVO recordVO) {
		String freeTextQuery = getSearchQuery().getFreeTextQuery();
		Record record = recordVO.getRecord();
		SearchConfigurationsManager searchConfigurationsManager = modelLayerFactory.getSearchConfigurationsManager();
		if (!searchConfigurationsManager.isExcluded(freeTextQuery, record)) {
			searchConfigurationsManager.setElevated(freeTextQuery, record, true);
		}
		view.refreshSearchResultsAndFacets();
	}
}
