package com.constellio.model.entities.records.wrappers;

import com.constellio.app.ui.pages.search.criteria.Criterion;
import com.constellio.app.ui.pages.search.criteria.FacetSelections;
import com.constellio.model.entities.EnumWithSmallCode;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SavedSearch extends RecordWrapper {
	public static final String SCHEMA_TYPE = "savedSearch";
	public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";

	public static final String USER = "user";
	public static final String PUBLIC = "public";
	public static final String SORT_FIELD = "sortField";
	public static final String FACET_SELECTIONS = "facetSelections";
	public static final String FREE_TEXT_SEARCH = "freeTextSearch";
	public static final String ADVANCED_SEARCH = "advancedSearch";
	public static final String SCHEMA_FILTER = "schemaFilter";
	public static final String SCHEMA_CODE_FILTER = "schemaCodeFilter";
	public static final String SEARCH_TYPE = "searchType";
	public static final String SORT_ORDER = "sortOrder";
	public static final String TEMPORARY = "temporary";
	public static final String PAGE_NUMBER = "pageNumber";
	public static final String RESULTS_VIEW_MODE = "resultsViewMode";
	public static final String PAGE_LENGTH = "pageLength";
	public static final String SHARED_USERS = "sharedUsers";
	public static final String SHARED_GROUPS = "sharedGroups";
	public static final String RESTRICTED = "restricted";

	//testing only
	public static final String CRITERION = "criterions";

	public SavedSearch(Record record,
					   MetadataSchemaTypes types) {
		super(record, types, SCHEMA_TYPE);
	}

	public SavedSearch setTitle(String title) {
		super.setTitle(title);
		return this;
	}

	public String getUser() {
		return get(USER);
	}

	public SavedSearch setUser(String user) {
		set(USER, user);
		return this;
	}

	public String getResultsViewMode() {
		return get(RESULTS_VIEW_MODE);
	}

	public SavedSearch setResultsViewMode(String resultsViewMode) {
		set(RESULTS_VIEW_MODE, resultsViewMode);
		return this;
	}

	public int getPageLength() {
		Integer pageLengthInteger = getInteger(PAGE_LENGTH);
		return pageLengthInteger != null ? pageLengthInteger : 0;
	}

	public SavedSearch setPageLength(int pageLength) {
		set(PAGE_LENGTH, pageLength);
		return this;
	}

	public boolean isPublic() {
		return get(PUBLIC);
	}

	public SavedSearch setPublic(boolean publicSearch) {
		set(PUBLIC, publicSearch);
		return this;
	}

	public String getSortField() {
		return get(SORT_FIELD);
	}

	public SavedSearch setSortField(String sortField) {
		set(SORT_FIELD, sortField);
		return this;
	}

	public Map<String, Set<String>> getSelectedFacets() {
		List<FacetSelections> selections = getList(FACET_SELECTIONS);
		Map<String, Set<String>> result = new HashMap<>();
		for (FacetSelections each : selections) {
			result.put(each.getFacetField(), each.getSelectedValues());
		}
		return result;
	}

	public SavedSearch setSelectedFacets(Map<String, Set<String>> facetSelections) {
		List<FacetSelections> selections = new ArrayList<>();
		for (Map.Entry<String, Set<String>> each : facetSelections.entrySet()) {
			selections.add(new FacetSelections(each.getKey(), each.getValue()));
		}
		set(FACET_SELECTIONS, selections);
		return this;
	}

	public String getFreeTextSearch() {
		return get(FREE_TEXT_SEARCH);
	}

	public SavedSearch setFreeTextSearch(String freeTextSearch) {
		set(FREE_TEXT_SEARCH, freeTextSearch);
		return this;
	}

	public List<Criterion> getAdvancedSearch() {
		return getList(ADVANCED_SEARCH);
	}

	public SavedSearch setAdvancedSearch(List<Criterion> advancedSearch) {
		set(ADVANCED_SEARCH, advancedSearch);
		return this;
	}

	public SortOrder getSortOrder() {
		return get(SORT_ORDER);
	}

	public SavedSearch setSortOrder(SortOrder sortOrder) {
		set(SORT_ORDER, sortOrder);
		return this;
	}

	public String getSchemaFilter() {
		return get(SCHEMA_FILTER);
	}

	public SavedSearch setSchemaFilter(String schemaFilter) {
		set(SCHEMA_FILTER, schemaFilter);
		return this;
	}

	public String getSchemaCodeFilter() {
		return get(SCHEMA_CODE_FILTER);
	}

	public SavedSearch setSchemaCodeFilter(String schemaCode) {
		set(SCHEMA_CODE_FILTER, schemaCode);
		return this;
	}

	public String getSearchType() {
		return get(SEARCH_TYPE);
	}

	public SavedSearch setSearchType(String searchType) {
		set(SEARCH_TYPE, searchType);
		return this;
	}

	public Boolean getTemporary() {
		return get(TEMPORARY);
	}

	public boolean isTemporary() {
		return getBooleanWithDefaultValue(TEMPORARY, false);
	}

	public SavedSearch setTemporary(Boolean temporary) {
		set(TEMPORARY, temporary);
		return this;
	}

	public SavedSearch setPageNumber(int pageNumber) {
		set(PAGE_NUMBER, pageNumber);
		return this;
	}

	public int getPageNumber() {
		return getInteger(PAGE_NUMBER);
	}

	public enum SortOrder implements EnumWithSmallCode {
		ASCENDING("a"), DESCENDING("d");

		private String code;

		SortOrder(String code) {
			this.code = code;
		}

		public String getCode() {
			return code;
		}
	}

	public SavedSearch setSharedUsers(List<String> userIds) {
		set(SHARED_USERS, userIds);
		return this;
	}

	public List<String> getSharedUsers() {
		return getList(SHARED_USERS);
	}

	public SavedSearch setSharedGroups(List<String> userIds) {
		set(SHARED_GROUPS, userIds);
		return this;
	}

	public List<String> getSharedGroups() {
		return getList(SHARED_GROUPS);
	}
}
