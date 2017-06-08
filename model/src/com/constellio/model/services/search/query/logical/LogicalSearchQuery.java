package com.constellio.model.services.search.query.logical;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.constellio.data.utils.KeySetMap;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.DataStoreField;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.search.entities.SearchBoost;
import com.constellio.model.services.search.query.FilterUtils;
import com.constellio.model.services.search.query.ResultsProjection;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import com.constellio.model.services.search.query.SearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.search.query.logical.condition.SchemaFilters;
import com.constellio.model.services.search.query.logical.condition.SolrQueryBuilderParams;

//TODO Remove inheritance, rename to LogicalQuery
public class LogicalSearchQuery implements SearchQuery {
	private static final String HIGHLIGHTING_FIELDS = "search_*";

	//This condition will be inserted in Filter Query
	LogicalSearchCondition condition;
	//This condition will be inserted in Query
	private LogicalSearchCondition queryCondition;
	private LogicalSearchQueryFacetFilters facetFilters = new LogicalSearchQueryFacetFilters();
	private String freeTextQuery;
	UserFilter userFilter;
	String filterStatus;

	private int numberOfRows;
	private int startRow;

	private ReturnedMetadatasFilter returnedMetadatasFilter;
	private List<LogicalSearchQuerySort> sortFields = new ArrayList<>();
	private ResultsProjection resultsProjection;

	private KeySetMap<String, String> queryFacets = new KeySetMap<>();
	private List<String> fieldFacets = new ArrayList<>();
	private List<String> statisticFields = new ArrayList<>();
	private List<String> moreLikeThisFields = new ArrayList<>();
	private int fieldFacetLimit;

	private boolean highlighting = false;
	private boolean spellcheck = false;
	private boolean moreLikeThis = false;
	private boolean preferAnalyzedFields = false;

	private List<SearchBoost> fieldBoosts = new ArrayList<>();
	private List<SearchBoost> queryBoosts = new ArrayList<>();

	private Map<String, String[]> overridedQueryParams = new HashMap<>();

	public LogicalSearchQuery() {
		numberOfRows = 100000;
		startRow = 0;
		fieldFacetLimit = 0;
	}

	public LogicalSearchQuery(LogicalSearchCondition condition) {
		this();
		if (condition == null) {
			throw new IllegalArgumentException("Condition must not be null");
		}
		this.condition = condition;
	}

	public LogicalSearchQuery(LogicalSearchQuery query) {
		condition = query.condition;
		queryCondition = query.queryCondition;
		facetFilters = new LogicalSearchQueryFacetFilters(query.facetFilters);
		freeTextQuery = query.freeTextQuery;
		userFilter = query.userFilter;
		filterStatus = query.filterStatus;

		numberOfRows = query.numberOfRows;
		startRow = query.startRow;

		returnedMetadatasFilter = query.returnedMetadatasFilter;
		sortFields = new ArrayList<>(query.sortFields);
		resultsProjection = query.resultsProjection;

		queryFacets = new KeySetMap<>(query.queryFacets);
		fieldFacets = new ArrayList<>(query.fieldFacets);
		statisticFields = new ArrayList<>(query.statisticFields);
		fieldFacetLimit = query.fieldFacetLimit;

		highlighting = query.highlighting;
		spellcheck = query.spellcheck;
		preferAnalyzedFields = query.preferAnalyzedFields;

		fieldBoosts = new ArrayList<>(query.fieldBoosts);
		queryBoosts = new ArrayList<>(query.queryBoosts);
	}

	// The following methods are attribute accessors

	public boolean isPreferAnalyzedFields() {
		return preferAnalyzedFields;
	}

	public LogicalSearchQuery setPreferAnalyzedFields(boolean preferAnalyzedFields) {
		this.preferAnalyzedFields = preferAnalyzedFields;
		return this;
	}

	public LogicalSearchCondition getCondition() {
		return condition;
	}

	public LogicalSearchQuery setCondition(LogicalSearchCondition condition) {
		this.condition = condition;
		return this;
	}

	public String getFreeTextQuery() {
		return freeTextQuery;
	}

	public LogicalSearchQuery setFreeTextQuery(String freeTextQuery) {
		this.freeTextQuery = freeTextQuery;
		return this;
	}

	@Override
	public LogicalSearchQuery filteredWithUser(User user) {
		return filteredWithUser(user, Role.READ);
	}

	@Override
	public LogicalSearchQuery filteredWithUser(User user, String accessOrPermission) {
		if (user == null) {
			throw new IllegalArgumentException("user required");
		}
		if (accessOrPermission == null) {
			throw new IllegalArgumentException("access/permission required");
		}
		userFilter = new UserFilter(user, accessOrPermission);
		return this;
	}

	public LogicalSearchQuery filteredWithUserWrite(User user) {
		return filteredWithUser(user, Role.WRITE);
	}

	public LogicalSearchQuery filteredWithUserDelete(User user) {
		return filteredWithUser(user, Role.DELETE);
	}

	public UserFilter getUserFilter() {
		return userFilter;
	}

	public LogicalSearchQuery filteredByStatus(StatusFilter status) {
		filterStatus = FilterUtils.statusFilter(status);
		return this;
	}

	@Override
	public LogicalSearchQuery computeStatsOnField(DataStoreField field) {
		this.statisticFields.add(field.getDataStoreCode());
		return this;
	}

	@Override
	public int getStartRow() {
		return this.startRow;
	}

	@Override
	public LogicalSearchQuery setStartRow(int row) {
		startRow = row;
		return this;
	}

	@Override
	public int getNumberOfRows() {
		return numberOfRows;
	}

	@Override
	public LogicalSearchQuery setNumberOfRows(int number) {
		numberOfRows = number;
		return this;
	}

	public ReturnedMetadatasFilter getReturnedMetadatas() {
		return returnedMetadatasFilter == null ? ReturnedMetadatasFilter.all() : returnedMetadatasFilter;
	}

	public LogicalSearchQuery setReturnedMetadatas(ReturnedMetadatasFilter filter) {
		this.returnedMetadatasFilter = filter;
		return this;
	}

	public void clearSort() {
		sortFields.clear();
	}

	public LogicalSearchQuery sortAsc(DataStoreField field) {
		if (!field.isMultivalue() && field.getType() != MetadataValueType.TEXT) {
			DataStoreField sortField = field.getSortField();
			if (sortField != null) {
				sortFields.add(new LogicalSearchQuerySort(sortField.getDataStoreCode(), true));
			}
			sortFields.add(new LogicalSearchQuerySort(field.getDataStoreCode(), true));
		}
		return this;
	}

	public LogicalSearchQuery sortDesc(DataStoreField field) {
		if (!field.isMultivalue() && field.getType() != MetadataValueType.TEXT) {
			DataStoreField sortField = field.getSortField();
			if (sortField != null) {
				sortFields.add(new LogicalSearchQuerySort(sortField.getDataStoreCode(), false));
			}
			sortFields.add(new LogicalSearchQuerySort(field.getDataStoreCode(), false));
		}
		return this;
	}

	public ResultsProjection getResultsProjection() {
		return resultsProjection;
	}

	public LogicalSearchQuery setResultsProjection(ResultsProjection resultsProjection) {
		this.resultsProjection = resultsProjection;
		return this;
	}

	public KeySetMap<String, String> getQueryFacets() {
		return queryFacets;
	}

	public LogicalSearchQuery addQueryFacets(String facetGroup, List<String> queryFacets) {
		for (String queryFacet : queryFacets) {
			this.queryFacets.add(facetGroup, queryFacet);
		}
		return this;
	}

	public LogicalSearchQuery addQueryFacet(String facetGroup, String queryFacet) {
		queryFacets.add(facetGroup, queryFacet);
		return this;
	}

	public List<String> getFieldFacets() {
		return fieldFacets;
	}

	public List<String> getStatisticFields() {
		return statisticFields;
	}

	public LogicalSearchQuery addFieldFacet(String fieldFacet) {
		fieldFacets.add(fieldFacet);
		return this;
	}

	public LogicalSearchQueryFacetFilters getFacetFilters() {
		return facetFilters;
	}

	public int getFieldFacetLimit() {
		return fieldFacetLimit;
	}

	public LogicalSearchQuery setFieldFacetLimit(int fieldFacetLimit) {
		this.fieldFacetLimit = fieldFacetLimit;
		return this;
	}

	public LogicalSearchQuery setHighlighting(boolean highlighting) {
		this.highlighting = highlighting;
		return this;
	}

	public boolean isHighlighting() {
		return highlighting;
	}

	public boolean isSpellcheck() {
		return spellcheck;
	}

	public LogicalSearchQuery setSpellcheck(boolean spellcheck) {
		this.spellcheck = spellcheck;
		return this;
	}

	public Map<String, String[]> getOverridedQueryParams() {
		return overridedQueryParams;
	}

	public LogicalSearchQuery setOverridedQueryParams(Map<String, String[]> overridedQueryParams) {
		this.overridedQueryParams = overridedQueryParams;
		return this;
	}

	public List<SearchBoost> getFieldBoosts() {
		return fieldBoosts;
	}

	public LogicalSearchQuery setFieldBoosts(List<SearchBoost> fieldBoosts) {
		this.fieldBoosts = fieldBoosts;
		return this;
	}

	public List<SearchBoost> getQueryBoosts() {
		return queryBoosts;
	}

	public LogicalSearchQuery setQueryBoosts(List<SearchBoost> queryBoosts) {
		this.queryBoosts = queryBoosts;
		return this;
	}

	// The following methods are mainly used by the SPE itself

	@Override
	public String getQuery(String language) {
		SolrQueryBuilderParams params = new SolrQueryBuilderParams(preferAnalyzedFields, language);
		return condition.getSolrQuery(params);
	}

	@Override
	public List<String> getFilterQueries() {
		List<String> filterQueries = new ArrayList<>();

		if (condition != null && condition.getFilters() != null) {
			for (String filterQuery : condition.getFilters().getFilterQueries(userFilter != null)) {
				filterQueries.add(filterQuery);
			}
		}
		if (filterStatus != null) {
			filterQueries.add(filterStatus);
		}

		filterQueries.addAll(facetFilters.toSolrFilterQueries());

		return filterQueries;
	}

	public String getSort() {
		StringBuilder stringBuilder = new StringBuilder();

		for (LogicalSearchQuerySort sort : sortFields) {
			if (stringBuilder.length() > 0) {
				stringBuilder.append(", ");
			}
			String sorFieldName = sortFieldName(sort);
			stringBuilder.append(sorFieldName);
			stringBuilder.append(" ");
			stringBuilder.append(sort.isAscending() ? "asc" : "desc");
		}

		return stringBuilder.toString();
	}

	private String sortFieldName(LogicalSearchQuerySort sort) {
		String fieldName = sort.getFieldName();
		if (fieldName != null && fieldName.endsWith("_s")) {
			return fieldName.substring(0, fieldName.length() - 2) + "_fs-s";
		}
		return fieldName;
	}

	@Deprecated
	public MetadataSchema getSchemaCondition() {
		return ((SchemaFilters) condition.getFilters()).getSchema();
	}

	public String getHighlightingFields() {
		return HIGHLIGHTING_FIELDS;
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	public static LogicalSearchQuery returningNoResults() {
		return new LogicalSearchQuery(LogicalSearchQueryOperators.fromAllSchemasIn("inexistentCollection42").returnAll());
	}

	public void setMoreLikeThis(boolean moreLikeThis) {
		this.moreLikeThis = moreLikeThis;
	}

	public void addMoreLikeThisField(DataStoreField... fields) {
		for (DataStoreField field : fields) {
			//			String dataStoreType;
			//			switch (field.getDataStoreType()) {
			//			case "ss":
			//				dataStoreType = "txt";
			//				break;
			//			case "s":
			//				dataStoreType = "t";
			//				break;
			//			default:
			//				dataStoreType = field.getDataStoreType();
			//				break;
			//			}

			for (String lang : new String[] { "en", "fr", "ar" }) {
				moreLikeThisFields.add(field.getAnalyzedField(lang).getDataStoreCode());
			}
		}
	}

	public List<String> getMoreLikeThisFields() {
		return moreLikeThisFields;
	}

	public boolean isMoreLikeThis() {
		return moreLikeThis;
	}

	public void setQueryCondition(LogicalSearchCondition queryCondition) {
		this.queryCondition = queryCondition;
	}

	public LogicalSearchCondition getQueryCondition() {
		return queryCondition;
	}

	public static class UserFilter {
		private final User user;
		private final String access;

		public UserFilter(User user, String access) {
			this.user = user;
			this.access = access;
		}

		public User getUser() {
			return user;
		}

		public String getAccess() {
			return access;
		}
	}

	public static LogicalSearchQuery query(LogicalSearchCondition condition) {
		return new LogicalSearchQuery(condition);
	}
}
