package com.constellio.model.services.search.query.logical;

import com.constellio.data.dao.services.records.DataStore;
import com.constellio.data.utils.KeySetMap;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.DataStoreField;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.security.Role;
import com.constellio.model.entities.security.SecurityModel;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.search.VisibilityStatusFilter;
import com.constellio.model.services.search.entities.SearchBoost;
import com.constellio.model.services.search.query.FilterUtils;
import com.constellio.model.services.search.query.ResultsProjection;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import com.constellio.model.services.search.query.SearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.search.query.logical.condition.SchemaFilters;
import com.constellio.model.services.security.SecurityTokenManager;
import com.constellio.model.services.security.SecurityTokenManager.UserTokens;
import lombok.Getter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.constellio.model.entities.schemas.Schemas.TOKENS;
import static com.constellio.model.entities.schemas.Schemas.TOKENS_OF_HIERARCHY;
import static com.constellio.model.entities.security.SecurityModelUtils.hasNegativeAccessOnSecurisedRecord;
import static com.constellio.model.services.search.VisibilityStatusFilter.VISIBLES;
import static java.util.Arrays.asList;

//TODO Remove inheritance, rename to LogicalQuery
public class LogicalSearchQuery implements SearchQuery {

	public static final int DEFAULT_NUMBER_OF_ROWS = 100000;

	private static final String HIGHLIGHTING_FIELDS = "search_*";
	public static final String INEXISTENT_COLLECTION_42 = "inexistentCollection42";

	//This condition will be inserted in Filter Query
	LogicalSearchCondition condition;
	//This condition will be inserted in Query
	private LogicalSearchQueryFacetFilters facetFilters = new LogicalSearchQueryFacetFilters();
	private String freeTextQuery;
	List<UserFilter> userFilters;
	VisibilityStatusFilter visibilityStatusFilter = VISIBLES;
	StatusFilter statusFilter = StatusFilter.ALL;

	private int numberOfRows;
	private int startRow;

	private ReturnedMetadatasFilter returnedMetadatasFilter;
	private List<LogicalSearchQuerySort> sortFields = new ArrayList<>();
	private ResultsProjection resultsProjection;

	private KeySetMap<String, String> queryFacets = new KeySetMap<>();
	private List<String> fieldFacets = new ArrayList<>();
	private List<String> fieldPivotFacets = new ArrayList<>();
	private List<String> statisticFields = new ArrayList<>();
	private List<String> moreLikeThisFields = new ArrayList<>();
	private int fieldFacetLimit;

	private boolean highlighting = false;
	private boolean spellcheck = false;
	private String moreLikeThisRecord = null;
	private boolean preferAnalyzedFields = false;
	private boolean loadTransientValues = true;

	private List<SearchBoost> fieldBoosts = new ArrayList<>();
	private List<SearchBoost> queryBoosts = new ArrayList<>();

	private Map<String, String[]> overridedQueryParams = new HashMap<>();

	private String name;

	private String language;

	private QueryExecutionMethod queryExecutionMethod = QueryExecutionMethod.DEFAULT;

	public LogicalSearchQuery() {
		numberOfRows = DEFAULT_NUMBER_OF_ROWS;
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
		name = query.name;
		condition = query.condition;
		facetFilters = new LogicalSearchQueryFacetFilters(query.facetFilters);
		freeTextQuery = query.freeTextQuery;
		userFilters = query.userFilters;
		visibilityStatusFilter = query.visibilityStatusFilter;
		statusFilter = query.statusFilter;

		numberOfRows = query.numberOfRows;
		startRow = query.startRow;

		returnedMetadatasFilter = query.returnedMetadatasFilter;
		sortFields = new ArrayList<>(query.sortFields);
		resultsProjection = query.resultsProjection;

		queryFacets = new KeySetMap<>(query.queryFacets);
		fieldFacets = new ArrayList<>(query.fieldFacets);
		fieldPivotFacets = new ArrayList<>(query.fieldPivotFacets);
		statisticFields = new ArrayList<>(query.statisticFields);
		fieldFacetLimit = query.fieldFacetLimit;

		highlighting = query.highlighting;
		spellcheck = query.spellcheck;
		preferAnalyzedFields = query.preferAnalyzedFields;

		fieldBoosts = new ArrayList<>(query.fieldBoosts);
		queryBoosts = new ArrayList<>(query.queryBoosts);

		moreLikeThisFields = query.moreLikeThisFields;
		language = query.language;
		loadTransientValues = query.loadTransientValues;
		queryExecutionMethod = query.queryExecutionMethod;
	}

	// The following methods are attribute accessors


	public boolean isLoadTransientValues() {
		return loadTransientValues;
	}

	public LogicalSearchQuery setLoadTransientValues(boolean loadTransientValues) {
		this.loadTransientValues = loadTransientValues;
		return this;
	}

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
	public LogicalSearchQuery filteredWith(UserFilter userFilter) {
		userFilters = asList(userFilter);
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
		userFilters = asList((UserFilter) new DefaultUserFilter(user, accessOrPermission));
		return this;
	}

	@Override
	public LogicalSearchQuery filteredWithUser(User user, List<String> accessOrPermissions) {
		if (user == null) {
			throw new IllegalArgumentException("user required");
		}
		if (accessOrPermissions == null || accessOrPermissions.isEmpty()) {
			throw new IllegalArgumentException("access/permission required");
		}

		userFilters = new ArrayList<>();
		for (String accessOrPermission : accessOrPermissions) {
			userFilters.add(new DefaultUserFilter(user, accessOrPermission));
		}

		return this;
	}

	public LogicalSearchQuery filteredWithUserWrite(User user) {
		return filteredWithUser(user, Role.WRITE);
	}

	public LogicalSearchQuery filteredWithUserDelete(User user) {
		return filteredWithUser(user, Role.DELETE);
	}


	public LogicalSearchQuery filteredWithUserHierarchyRead(User user) {
		if (user == null) {
			throw new IllegalArgumentException("user required");
		}
		userFilters = asList((UserFilter) new HierarchyUserFilter(user, Role.READ, null, false));
		return this;
	}


	public LogicalSearchQuery filteredWithUserHierarchyWrite(User user) {
		if (user == null) {
			throw new IllegalArgumentException("user required");
		}
		userFilters = asList((UserFilter) new HierarchyUserFilter(user, Role.WRITE, null, false));
		return this;
	}

	public LogicalSearchQuery filteredWithUserHierarchyRead(User user, MetadataSchemaType selectedType,
															boolean includeInvisible) {
		if (user == null) {
			throw new IllegalArgumentException("user required");
		}
		userFilters = asList((UserFilter) new HierarchyUserFilter(user, Role.READ, selectedType, includeInvisible));
		return this;
	}


	public LogicalSearchQuery filteredWithUserHierarchyWrite(User user, MetadataSchemaType selectedType,
															 boolean includeInvisible) {
		if (user == null) {
			throw new IllegalArgumentException("user required");
		}
		userFilters = asList((UserFilter) new HierarchyUserFilter(user, Role.WRITE, selectedType, includeInvisible));
		return this;
	}


	public LogicalSearchQuery filteredWithUserHierarchy(User user, String access, MetadataSchemaType selectedType,
														boolean includeInvisible) {
		if (user == null) {
			throw new IllegalArgumentException("user required");
		}
		userFilters = asList((UserFilter) new HierarchyUserFilter(user, access, selectedType, includeInvisible));
		return this;
	}

	public List<UserFilter> getUserFilters() {
		return userFilters;
	}

	public LogicalSearchQuery filteredByStatus(StatusFilter status) {
		statusFilter = status;
		return this;
	}

	public LogicalSearchQuery filteredByVisibilityStatus(VisibilityStatusFilter status) {
		visibilityStatusFilter = status;
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

	public void clearFacets() {
		fieldFacets.clear();
		fieldPivotFacets.clear();
		queryFacets.clear();
	}

	public LogicalSearchQuery sortAsc(DataStoreField field) {
		if (!field.isMultivalue() && field.getType() != MetadataValueType.TEXT) {
			DataStoreField sortField = field.getSortField();
			sortFields.add(new FieldLogicalSearchQuerySort(field, true));
		}
		return this;
	}

	public LogicalSearchQuery sortOn(LogicalSearchQuerySort logicalSearchQuerySort) {
		sortFields.add(logicalSearchQuerySort);
		return this;
	}

	public LogicalSearchQuery sortFirstOn(LogicalSearchQuerySort logicalSearchQuerySort) {
		if (logicalSearchQuerySort != null) {
			sortFields.add(0, logicalSearchQuerySort);
		}
		return this;
	}

	public LogicalSearchQuery sortDesc(DataStoreField field) {
		if (!field.isMultivalue() && field.getType() != MetadataValueType.TEXT) {
			DataStoreField sortField = field.getSortField();
			sortFields.add(new FieldLogicalSearchQuerySort(field, false));
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

	public List<String> getFieldPivotFacets() {
		return fieldPivotFacets;
	}

	public List<String> getStatisticFields() {
		return statisticFields;
	}

	public LogicalSearchQuery addFieldFacet(String fieldFacet) {
		fieldFacets.add(fieldFacet);
		return this;
	}

	public LogicalSearchQuery setFieldPivotFacets(List<String> fieldPivotFacets) {
		this.fieldPivotFacets = fieldPivotFacets;
		return this;
	}

	public LogicalSearchQuery setFieldPivotFacets(String... fieldPivotFacet) {
		fieldPivotFacets = asList(fieldPivotFacet);
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
	public List<String> getFilterQueries() {
		List<String> filterQueries = new ArrayList<>();

		if (condition != null && condition.getFilters() != null) {
			for (String filterQuery : condition.getFilters().getFilterQueries(userFilters != null)) {
				filterQueries.add(filterQuery);
			}
		}
		if (statusFilter != null && statusFilter != StatusFilter.ALL) {
			filterQueries.add(FilterUtils.statusFilter(statusFilter));
		}

		if (visibilityStatusFilter != null && visibilityStatusFilter != VisibilityStatusFilter.ALL) {
			filterQueries.add(FilterUtils.visibilityStatusFilter(visibilityStatusFilter));
		}

		filterQueries.addAll(facetFilters.toSolrFilterQueries());

		return filterQueries;
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
		return new LogicalSearchQuery(LogicalSearchQueryOperators.fromAllSchemasIn(INEXISTENT_COLLECTION_42).returnAll());
	}

	public String getMoreLikeThisRecordId() {
		return moreLikeThisRecord;
	}

	public LogicalSearchQuery setMoreLikeThisRecordId(String recordId) {
		this.moreLikeThisRecord = recordId;
		return this;
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

			for (String lang : new String[]{"en", "fr", "ar"}) {
				moreLikeThisFields.add(field.getAnalyzedField(lang).getDataStoreCode());
			}
		}
	}

	public List<String> getMoreLikeThisFields() {
		return moreLikeThisFields;
	}

	public boolean isMoreLikeThis() {
		return moreLikeThisRecord != null;
	}

	public String getName() {
		return name;
	}

	public LogicalSearchQuery setName(String name) {
		this.name = name;
		return this;
	}

	public String getDataStore() {
		if (condition == null || condition.getFilters() == null) {
			return DataStore.RECORDS;
		} else {
			return condition.getFilters().getDataStore();
		}
	}

	public String getLanguage() {
		return language;
	}

	public LogicalSearchQuery setLanguage(String language) {
		this.language = language;
		return this;
	}

	public LogicalSearchQuery setLanguage(Locale locale) {
		this.language = locale.getLanguage();
		return this;
	}


	public interface UserFilter {
		String buildFQ(SecurityTokenManager securityTokenManager, LogicalSearchQuery query);

		boolean isExecutableInCache();

		boolean hasUserAccessToRecord(Record record);

		User getUser();
	}

	public static class DefaultUserFilter implements UserFilter {
		private final User user;
		@Getter
		private final String access;

		public DefaultUserFilter(User user, String access) {
			this.user = user;
			this.access = access;
		}

		public User getUser() {
			return user;
		}

		public String getAccess() {
			return access;
		}

		public String buildFQ(SecurityTokenManager securityTokenManager, LogicalSearchQuery query) {
			List<String> from = null;
			if (query != null && query.getCondition() != null) {
				from = query.getCondition().getFilterSchemaTypesCodes();
			}

			String filter;
			switch (access) {
				case Role.READ:
					filter = FilterUtils.userReadFilter(user, securityTokenManager, from);
					break;
				case Role.WRITE:
					filter = FilterUtils.userWriteFilter(user, securityTokenManager, from);
					break;
				case Role.DELETE:
					filter = FilterUtils.userDeleteFilter(user, securityTokenManager);
					break;
				default:
					filter = FilterUtils.permissionFilter(user, access);
			}

			return filter;
		}

		@Override
		public boolean isExecutableInCache() {
			switch (access) {
				case Role.READ:
				case Role.WRITE:
				case Role.DELETE:
					return true;
				default:
					return false;
			}
		}

		@Override
		public boolean hasUserAccessToRecord(Record record) {
			switch (access) {
				case Role.READ:
					return user.hasReadAccess().on(record);
				case Role.WRITE:
					return user.hasWriteAccess().on(record);
				case Role.DELETE:
					return user.hasDeleteAccess().on(record);
				default:
					return user.has(access).on(record);
			}
		}
	}

	public static class HierarchyUserFilter implements UserFilter {

		User user;

		String access;

		MetadataSchemaType forSelectionOfSchemaType;

		boolean showInvisibleRecordsInLinkingMode;

		public HierarchyUserFilter(User user, String access,
								   MetadataSchemaType forSelectionOfSchemaType,
								   boolean showInvisibleRecordsInLinkingMode) {
			this.user = user;
			this.access = access;
			this.forSelectionOfSchemaType = forSelectionOfSchemaType;
			this.showInvisibleRecordsInLinkingMode = showInvisibleRecordsInLinkingMode;
		}

		@Override
		public String buildFQ(SecurityTokenManager securityTokenManager, LogicalSearchQuery query) {
			return FilterUtils.userHierarchyFilter(user, securityTokenManager, access,
					forSelectionOfSchemaType, showInvisibleRecordsInLinkingMode);
		}


		@Override
		public boolean isExecutableInCache() {
			SecurityTokenManager securityTokenManager = user.getRolesDetails().getSchemasRecordsServices()
					.getModelLayerFactory().getSecurityTokenManager();
			UserTokens tokens = securityTokenManager.getTokens(user);
			return forSelectionOfSchemaType == null && tokens.getAllowTokens().isEmpty() && tokens.getShareAllowTokens().isEmpty();
		}

		@Override
		public boolean hasUserAccessToRecord(Record record) {
			SecurityModel securityModel = user.getRolesDetails().getSchemasRecordsServices().getModelLayerFactory()
					.newRecordServices().getSecurityModel(user.getCollection());

			//			if (!this.showInvisibleRecordsInLinkingMode && Boolean.FALSE.equals(record.get(Schemas.VISIBLE_IN_TREES))) {
			//				return false;
			//			}

			String selectedTypeSmallCode = null;
			if (forSelectionOfSchemaType != null) {
				selectedTypeSmallCode = forSelectionOfSchemaType.getSmallCode();
				if (selectedTypeSmallCode == null) {
					selectedTypeSmallCode = forSelectionOfSchemaType.getCode();
				}
			}

			String tokenPrefix;
			if (Role.READ.equals(this.access)) {
				if (forSelectionOfSchemaType == null) {
					tokenPrefix = "r";
				} else {
					tokenPrefix = "r" + selectedTypeSmallCode;
				}
			} else {
				if (forSelectionOfSchemaType == null) {
					tokenPrefix = "w";
				} else {
					tokenPrefix = "w" + selectedTypeSmallCode;
				}
			}

			ModelLayerFactory modelLayerFactory = user.getRolesDetails().getSchemasRecordsServices()
					.getModelLayerFactory();
			SecurityTokenManager securityTokenManager = modelLayerFactory.getSecurityTokenManager();

			MetadataSchema schema = modelLayerFactory.getMetadataSchemasManager().getSchemaTypeOf(record).getDefaultSchema();

			List<String> recordTokens = record.getList(schema.getMetadata(TOKENS.getLocalCode()));
			List<String> tokensHierarchy = record.getList(schema.getMetadata(TOKENS_OF_HIERARCHY.getLocalCode()));

			if (user.isActiveUser() && !user.hasCollectionAccess(this.access == null ? Role.READ : this.access)) {

				if (hasNegativeAccessOnSecurisedRecord(securityModel.getAuthorizationsToPrincipal(user.getId(), false))) {

					if (tokensHierarchy.contains("nr_" + user.getId())) {
						return false;
					}
				}

				for (String aGroup : user.getUserGroups()) {
					if (user.getRolesDetails().getSchemasRecordsServices().isGroupActive(aGroup)
						&& hasNegativeAccessOnSecurisedRecord(securityModel.getAuthorizationsToPrincipal(aGroup, true))) {

						if (tokensHierarchy.contains("nr_" + aGroup)) {
							return false;
						}
					}
				}
			}


			if (user.isActiveUser()) {

				if (user.hasCollectionReadAccess() || user.hasCollectionDeleteAccess() || user.hasCollectionWriteAccess()) {
					if (record.getCollection().equals(user.getCollection())) {
						return true;
					}
				}

				if (tokensHierarchy.contains(tokenPrefix + "_" + user.getId())) {
					return true;
				}

				if (this.showInvisibleRecordsInLinkingMode && tokensHierarchy.contains("z" + tokenPrefix + "_" + user.getId())) {
					return true;
				}

				for (String schemaType : securityTokenManager.getGlobalPermissionSecurableSchemaTypesVisibleBy(user, Role.READ)) {
					if (record.getTypeCode().equals(schemaType)) {
						return true;
					}
				}

				for (String aGroup : user.getUserGroups()) {
					if (user.getRolesDetails().getSchemasRecordsServices().isGroupActive(aGroup)) {

						if (tokensHierarchy.contains(tokenPrefix + "_" + aGroup)) {
							return true;
						}

						if (this.showInvisibleRecordsInLinkingMode && tokensHierarchy.contains("z" + tokenPrefix + "_" + aGroup)) {
							return true;
						}
					}
				}

				if (user.hasRequiredAccess(this.access).on(record)) {
					return true;
				}

				for (String schemaType : securityTokenManager.getSchemaTypesWithoutSecurity()) {
					if (record.getTypeCode().equals(schemaType)) {
						return true;
					}
				}


				if (tokensHierarchy.contains(Record.PUBLIC_TOKEN)) {
					return true;
				}

			}
			return false;
		}

		@Override
		public User getUser() {
			return user;
		}
	}


	public VisibilityStatusFilter getVisibilityStatusFilter() {
		return visibilityStatusFilter;
	}

	public StatusFilter getStatusFilter() {
		return statusFilter;
	}

	public List<LogicalSearchQuerySort> getSortFields() {
		return sortFields;
	}

	public static LogicalSearchQuery query(LogicalSearchCondition condition) {
		return new LogicalSearchQuery(condition);
	}

	public QueryExecutionMethod getQueryExecutionMethod() {
		return queryExecutionMethod;
	}

	public LogicalSearchQuery setQueryExecutionMethod(
			QueryExecutionMethod queryExecutionMethod) {
		this.queryExecutionMethod = queryExecutionMethod;
		return this;
	}
}
