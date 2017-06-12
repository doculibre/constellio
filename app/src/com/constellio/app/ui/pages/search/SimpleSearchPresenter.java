package com.constellio.app.ui.pages.search;

import com.constellio.app.entities.schemasDisplay.SchemaTypeDisplayConfig;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.StorageSpace;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.SavedSearch;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemasRuntimeException.NoSuchMetadataWithAtomicCode;
import com.constellio.model.services.records.RecordImpl;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
			} else {
				searchID = null;
				resultsViewMode = SearchResultsViewMode.DETAILED;
				saveTemporarySearch(false);
			}
		} else {
			searchExpression = "";
			resultsViewMode = SearchResultsViewMode.DETAILED;
		}
		return this;
	}

	private void setSavedSearch(SavedSearch search) {
		searchExpression = search.getFreeTextSearch();
		facetSelections.putAll(search.getSelectedFacets());
		sortCriterion = search.getSortField();
		sortOrder = SortOrder.valueOf(search.getSortOrder().name());
		pageNumber = search.getPageNumber();
		resultsViewMode = search.getResultsViewMode() != null ? search.getResultsViewMode() : SearchResultsViewMode.DETAILED;
		setSelectedPageLength(search.getPageLength());
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
		for (MetadataSchemaType metadataSchemaType : schemaTypes) {
			for (MetadataVO metadata : getMetadataAllowedInSort(metadataSchemaType)) {
				if (resultCodes.add(metadata.getLocalCode())) {
					result.add(metadata);
				}
			}
		}
		return result;
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
		if (allowedSchemaTypes().isEmpty()) {
			return fromAllSchemasIn(view.getCollection()).returnAll();
		} else {
			return from(allowedSchemaTypes()).returnAll();
		}
	}

	@Override
	protected SavedSearch prepareSavedSearch(SavedSearch search) {
		return search.setSearchType(SimpleSearchView.SEARCH_TYPE)
				.setFreeTextSearch(searchExpression)
				.setPageNumber(pageNumber);
	}

	private List<MetadataSchemaType> allowedSchemaTypes() {
		List<MetadataSchemaType> result = new ArrayList<>();
		for (MetadataSchemaType type : types().getSchemaTypes()) {
			SchemaTypeDisplayConfig config = schemasDisplayManager()
					.getType(view.getSessionContext().getCurrentCollection(), type.getCode());
			if (config.isSimpleSearch() && isVisibleForUser(type, getCurrentUser())) {
				result.add(type);
			}
		}
		return result;
	}

	private boolean isVisibleForUser(MetadataSchemaType type, User currentUser) {
		if(ContainerRecord.SCHEMA_TYPE.equals(type.getCode()) && !currentUser.hasAny(RMPermissionsTo.DISPLAY_CONTAINERS, RMPermissionsTo.MANAGE_CONTAINERS).onSomething()) {
			return false;
		} else if(StorageSpace.SCHEMA_TYPE.equals(type.getCode()) && !currentUser.has(RMPermissionsTo.MANAGE_STORAGE_SPACES).globally()) {
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
			tmpSearchRecord = recordServices().newRecordWithSchema(schema(SavedSearch.DEFAULT_SCHEMA));
		} else {
			tmpSearchRecord = getTemporarySearchRecord();
		}

		SavedSearch search = new SavedSearch(tmpSearchRecord, types())
				.setTitle("temporarySimple")
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
		((RecordImpl) search.getWrappedRecord()).markAsSaved(1, search.getSchema());
		modelLayerFactory.getRecordsCaches().getCache(collection).insert(search.getWrappedRecord());
		//			recordServices().update(search);
		updateUIContext(search);
		if (refreshPage) {
			view.navigate().to().simpleSearchReplay(search.getId());
		}
		return search;
	}
}
