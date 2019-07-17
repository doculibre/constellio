package com.constellio.app.ui.pages.search;

import com.constellio.app.api.extensions.params.SearchPageConditionParam;
import com.constellio.app.entities.schemasDisplay.SchemaTypeDisplayConfig;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.StorageSpace;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.SavedSearch;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemasRuntimeException.NoSuchMetadataWithAtomicCode;
import com.constellio.model.services.records.RecordImpl;
import com.constellio.model.services.schemas.MetadataList;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.vaadin.ui.Component;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.data.dao.services.cache.InsertionReason.WAS_MODIFIED;
import static com.constellio.data.dao.services.idGenerator.UUIDV1Generator.newRandomId;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;

public class SimpleSearchPresenter extends SearchPresenter<SimpleSearchView> {
	private static final Logger LOGGER = LoggerFactory.getLogger(SimpleSearchPresenter.class);

	private int pageNumber;
	private String searchExpression;
	private String searchID;

	public SimpleSearchPresenter(SimpleSearchView view) {
		super(view);
	}

	@Override
	public SimpleSearchPresenter forRequestParameters(String params) {
		if (StringUtils.isNotBlank(params)) {
			String[] parts = params.split("/", 3);
			if (parts.length == 3) {
				try {
					pageNumber = Integer.parseInt(parts[2]);
					searchExpression = parts[1];
				} catch (NumberFormatException e) {
					pageNumber = 1;
					searchExpression = parts[1] + "/" + parts[2];
				}
			} else {
				pageNumber = 1;
				searchExpression = parts[1];
			}
			if ("s".equals(parts[0])) {
				searchID = parts[1];
				SavedSearch search = getSavedSearch(searchID);
				setSavedSearch(search);
				updateUIContext(search);
			} else {
				searchID = null;
				resultsViewMode = DEFAULT_VIEW_MODE;
				saveTemporarySearch(false);
			}
		} else {
			searchExpression = "";
			resultsViewMode = DEFAULT_VIEW_MODE;
		}
		return this;
	}

	private void setSavedSearch(SavedSearch search) {
		searchExpression = search.getFreeTextSearch();
		facetSelections.putAll(search.getSelectedFacets());
		sortCriterion = search.getSortField();
		if (search.getSortOrder() != null) {
			sortOrder = SortOrder.valueOf(search.getSortOrder().name());
		}
		pageNumber = search.getPageNumber();
		resultsViewMode = search.getResultsViewMode() != null ? search.getResultsViewMode() : DEFAULT_VIEW_MODE;
		setSelectedPageLength(search.getPageLength());
	}

	@Override
	void init(ConstellioFactories constellioFactories, SessionContext sessionContext) {
		super.init(constellioFactories, sessionContext);

		User user = view.getConstellioFactories().getAppLayerFactory()
				.getModelLayerFactory().newUserServices().getUserInCollection(
						view.getSessionContext().getCurrentUser().getUsername(),
						collection);

		if (allowedSchemaTypes().isEmpty()) {
			service = new SearchPresenterService(collection, user, modelLayerFactory, null);
		} else {
			service = new SearchPresenterService(collection, user, modelLayerFactory, allowedSchemaTypes());
		}

	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return true;
	}

	@Override
	public boolean mustDisplayResults() {
		return !searchExpression.isEmpty();
	}

	@Override
	public int getPageNumber() {
		return pageNumber;
	}

	public void setPageNumber(int pageNumber) {
		this.lastPageNumber = this.pageNumber;
		this.pageNumber = pageNumber;
	}

	@Override
	public String getUserSearchExpression() {
		return searchExpression;
	}

	public void setSearchExpression(String searchExpression) {
		this.searchExpression = searchExpression;
	}

	@Override
	public void suggestionSelected(String suggestion) {
		view.navigate().to().simpleSearch(suggestion);
	}

	@Override
	public List<MetadataVO> getMetadataAllowedInSort() {
		List<MetadataSchemaType> schemaTypes = allowedSchemaTypes();
		switch (schemaTypes.size()) {
			case 0:
				return new ArrayList<>();
			case 1:
				return getMetadataAllowedInSort(schemaTypes.get(0).getCode());
			default:
				return getCommonMetadataAllowedInSort(schemaTypes);
		}
	}

	@Override
	public boolean isPreferAnalyzedFields() {
		return true;
	}

	private List<MetadataVO> getCommonMetadataAllowedInSort(List<MetadataSchemaType> schemaTypes) {
		List<MetadataVO> result = new ArrayList<>();
		Set<String> resultCodes = new HashSet<>();
		Set<String> notAcceptedLocalCode = new HashSet<>();
		for (MetadataSchemaType metadataSchemaType : schemaTypes) {
			MetadataList nonAccessibleMetadataList = metadataSchemaType.getAllMetadatas()
					.onlyNotAccessibleGloballyBy(getCurrentUser());
			for (MetadataVO metadata : getMetadataAllowedInSortWithNoSecurity(metadataSchemaType)) {
				if (isLocalCodeInMetadataList(metadata.getLocalCode(), nonAccessibleMetadataList)) {
					notAcceptedLocalCode.add(metadata.getLocalCode());
				} else if (resultCodes.add(metadata.getLocalCode())) {
					result.add(metadata);
				}
			}
		}

		filterMetadataVOWithLocalCodeSet(result, notAcceptedLocalCode);

		return result;
	}

	private void filterMetadataVOWithLocalCodeSet(List<MetadataVO> result, Set<String> notAcceptedLocalCode) {
		Iterator<MetadataVO> resultIterator = result.iterator();
		while (resultIterator.hasNext()) {
			MetadataVO resultItem = resultIterator.next();
			if (notAcceptedLocalCode.contains(resultItem.getLocalCode())) {
				resultIterator.remove();
			}
		}
	}

	private boolean isMetadataInAllTypes(String localCode, List<MetadataSchemaType> types) {
		for (MetadataSchemaType each : types) {
			try {
				each.getMetadataWithAtomicCode(localCode);
			} catch (NoSuchMetadataWithAtomicCode e) {
				return false;
			}
		}
		return true;
	}


	@Override
	protected LogicalSearchCondition getSearchCondition() {
		LogicalSearchCondition logicalSearchCondition;
		if (allowedSchemaTypes().isEmpty()) {
			logicalSearchCondition = fromAllSchemasIn(view.getCollection()).returnAll();
		} else {
			logicalSearchCondition = from(allowedSchemaTypes()).returnAll();
		}

		logicalSearchCondition = appCollectionExtentions.adjustSearchPageCondition(new SearchPageConditionParam((Component) view, logicalSearchCondition, getCurrentUser()));
		return logicalSearchCondition;
	}

	@Override
	protected SavedSearch prepareSavedSearch(SavedSearch search) {
		return search.setSearchType(SimpleSearchView.SEARCH_TYPE)
				.setFreeTextSearch(searchExpression)
				.setPageNumber(pageNumber);
	}

	protected List<MetadataSchemaType> allowedSchemaTypes() {
		List<MetadataSchemaType> result = new ArrayList<>();
		if (types() != null) {
			for (MetadataSchemaType type : types().getSchemaTypes()) {
				SchemaTypeDisplayConfig config = schemasDisplayManager()
						.getType(view.getSessionContext().getCurrentCollection(), type.getCode());
				if (config.isSimpleSearch() && isVisibleForUser(type, getCurrentUser())) {
					result.add(type);
				}
			}
		}


		return result;
	}

	private boolean isVisibleForUser(MetadataSchemaType type, User currentUser) {
		if (ContainerRecord.SCHEMA_TYPE.equals(type.getCode()) && !currentUser
				.hasAny(RMPermissionsTo.DISPLAY_CONTAINERS, RMPermissionsTo.MANAGE_CONTAINERS).onSomething()) {
			return false;
		} else if (StorageSpace.SCHEMA_TYPE.equals(type.getCode()) && !currentUser.has(RMPermissionsTo.MANAGE_STORAGE_SPACES)
				.globally()) {
			return false;
		}
		return true;
	}

	public Record getTemporarySearchRecord() {
		//MetadataSchema schema = schema(SavedSearch.DEFAULT_SCHEMA);
		try {
			return recordServices().getDocumentById(searchID);
			/*
			return searchServices().searchSingleResult(from(schema).where(schema.getMetadata(SavedSearch.USER))
					.isEqualTo(getCurrentUser())
					.andWhere(schema.getMetadata(SavedSearch.TEMPORARY)).isEqualTo(true)
					.andWhere(schema.getMetadata(SavedSearch.SEARCH_TYPE)).isEqualTo(SimpleSearchView.SEARCH_TYPE));
					*/
		} catch (Exception e) {
			//TODO exception
			e.printStackTrace();
		}

		return null;
	}

	protected SavedSearch saveTemporarySearch(boolean refreshPage) {
		Record tmpSearchRecord;
		if (searchID == null) {
			tmpSearchRecord = recordServices().newRecordWithSchema(schema(SavedSearch.DEFAULT_SCHEMA), newRandomId());
		} else {
			tmpSearchRecord = getTemporarySearchRecord();
			if (tmpSearchRecord != null) {
				SavedSearch savedSearch = new SavedSearch(tmpSearchRecord, types());
				if (!Boolean.TRUE.equals(savedSearch.isTemporary())) {
					tmpSearchRecord = recordServices()
							.newRecordWithSchema(schema(SavedSearch.DEFAULT_SCHEMA), newRandomId());
				}
			}
		}

		SavedSearch search = new SavedSearch(tmpSearchRecord, types())
				.setTitle($("SearchView.savedSearch.temporarySimple"))
				.setUser(getCurrentUser().getId())
				.setPublic(false)
				.setSortField(sortCriterion)
				.setSortOrder(SavedSearch.SortOrder.valueOf(sortOrder.name()))
				.setSelectedFacets(facetSelections.getNestedMap())
				.setTemporary(true)
				.setSearchType(SimpleSearchView.SEARCH_TYPE)
				.setFreeTextSearch(searchExpression)
				.setPageNumber(pageNumber)
				.setPageLength(selectedPageLength);
		((RecordImpl) search.getWrappedRecord()).markAsSaved(search.getVersion() + 1, search.getSchema());
		modelLayerFactory.getRecordsCaches().getCache(collection).insert(search.getWrappedRecord(), WAS_MODIFIED);
		//			recordServices().update(search);
		updateUIContext(search);
		if (refreshPage) {
			view.navigate().to().simpleSearchReplay(search.getId());
		}
		return search;
	}
}
