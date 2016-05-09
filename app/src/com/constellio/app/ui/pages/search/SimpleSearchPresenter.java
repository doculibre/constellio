package com.constellio.app.ui.pages.search;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.entities.schemasDisplay.SchemaTypeDisplayConfig;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.SavedSearch;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemasRuntimeException.NoSuchMetadataWithAtomicCode;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

public class SimpleSearchPresenter extends SearchPresenter<SimpleSearchView> {
	private static final Logger LOGGER = LoggerFactory.getLogger(SimpleSearchPresenter.class);

	private int pageNumber;
	private String searchExpression;

	public SimpleSearchPresenter(SimpleSearchView view) {
		super(view);
	}

	@Override
	public SimpleSearchPresenter forRequestParameters(String params) {
		if (StringUtils.isNotBlank(params)) {
			String[] parts = params.split("/", 3);
			pageNumber = parts.length == 3 ? Integer.parseInt(parts[2]) : 1;
			if ("s".equals(parts[0])) {
				SavedSearch search = getSavedSearch(parts[1]);
				setSavedSearch(search);
			} else {
				searchExpression = parts[1];
			}
		} else {
			searchExpression = "";
		}
		return this;
	}

	private void setSavedSearch(SavedSearch search) {
		searchExpression = search.getFreeTextSearch();
		facetSelections.putAll(search.getSelectedFacets());
		sortCriterion = search.getSortField();
		sortOrder = SortOrder.valueOf(search.getSortOrder().name());
		pageNumber = search.getPageNumber();
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

	private List<MetadataVO> getCommonMetadataAllowedInSort(List<MetadataSchemaType> schemaTypes) {
		List<MetadataVO> result = new ArrayList<>();
		for (MetadataVO metadata : getMetadataAllowedInSort(schemaTypes.get(0))) {
			String localCode = MetadataVO.getCodeWithoutPrefix(metadata.getCode());
			if (isMetadataInAllTypes(localCode, schemaTypes)) {
				result.add(metadata);
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
			if (config.isSimpleSearch()) {
				result.add(type);
			}
		}
		return result;
	}

	public Record getTemporarySearchRecord() {
		MetadataSchema schema = schema(SavedSearch.DEFAULT_SCHEMA);
		try {
			return searchServices().searchSingleResult(from(schema).where(schema.getMetadata(SavedSearch.USER))
					.isEqualTo(getCurrentUser())
					.andWhere(schema.getMetadata(SavedSearch.TEMPORARY)).isEqualTo(true)
					.andWhere(schema.getMetadata(SavedSearch.SEARCH_TYPE)).isEqualTo(SimpleSearchView.SEARCH_TYPE));
		} catch (Exception e) {
			//TODO exception
			e.printStackTrace();
		}

		return null;
	}

	protected void saveTemporarySearch(boolean refreshPage) {
		Record tmpSearchRecord = getTemporarySearchRecord();
		if (tmpSearchRecord == null) {
			tmpSearchRecord = recordServices().newRecordWithSchema(schema(SavedSearch.DEFAULT_SCHEMA));
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
				.setPageNumber(pageNumber);
		try {
			recordServices().update(search);
			if (refreshPage) {
				view.navigate().to().simpleSearchReplay(search.getId());
			}
		} catch (RecordServicesException e) {
			LOGGER.info("TEMPORARY SAVE ERROR", e);
		}
	}
}
