package com.constellio.model.services.taxonomies;

import com.constellio.data.dao.services.records.DataStore;
import com.constellio.data.utils.LangUtils;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Authorization;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordUtils;
import com.constellio.model.services.records.cache.CacheConfig;
import com.constellio.model.services.records.cache.RecordsCaches;
import com.constellio.model.services.records.utils.RecordCodeComparator;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.search.MoreLikeThisRecord;
import com.constellio.model.services.search.SPEQueryResponse;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.FilterUtils;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery.UserFilter;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.security.SecurityTokenManager;
import com.constellio.model.services.taxonomies.LinkableConceptFilter.LinkableConceptFilterParams;
import com.constellio.model.services.taxonomies.TaxonomiesSearchOptions.HasChildrenFlagCalculated;
import com.constellio.model.services.taxonomies.TaxonomiesSearchServicesRuntimeException.TaxonomiesSearchServicesRuntimeException_CannotFilterNonPrincipalConceptWithWriteOrDeleteAccess;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static com.constellio.data.utils.LangUtils.isTrueOrNull;
import static com.constellio.model.entities.schemas.Schemas.LINKABLE;
import static com.constellio.model.entities.schemas.Schemas.PATH_PARTS;
import static com.constellio.model.entities.schemas.Schemas.VISIBLE_IN_TREES;
import static com.constellio.model.services.schemas.SchemaUtils.getSchemaTypeCode;
import static com.constellio.model.services.search.StatusFilter.ACTIVES;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasInCollectionOf;
import static com.constellio.model.services.search.query.logical.valueCondition.ConditionTemplateFactory.schemaTypeIsIn;
import static com.constellio.model.services.search.query.logical.valueCondition.ConditionTemplateFactory.schemaTypeIsNotIn;
import static com.constellio.model.services.taxonomies.ConceptNodesTaxonomySearchServices.childConceptsQuery;
import static com.constellio.model.services.taxonomies.ConceptNodesTaxonomySearchServices.childrenCondition;
import static com.constellio.model.services.taxonomies.ConceptNodesTaxonomySearchServices.directChildOf;
import static com.constellio.model.services.taxonomies.ConceptNodesTaxonomySearchServices.fromTypeIn;
import static com.constellio.model.services.taxonomies.ConceptNodesTaxonomySearchServices.notDirectChildOf;
import static com.constellio.model.services.taxonomies.ConceptNodesTaxonomySearchServices.recordInHierarchyOf;
import static com.constellio.model.services.taxonomies.ConceptNodesTaxonomySearchServices.visibleInTrees;
import static com.constellio.model.services.taxonomies.TaxonomiesSearchOptions.HasChildrenFlagCalculated.NEVER;
import static java.util.Arrays.asList;

public class TaxonomiesSearchServicesBasedOnHierarchyTokensImpl implements TaxonomiesSearchServices {

	//private static final String CHILDREN_QUERY = "children";
	private static final boolean NOT_LINKABLE = false;

	SearchServices searchServices;
	TaxonomiesManager taxonomiesManager;
	MetadataSchemasManager metadataSchemasManager;
	RecordServices recordServices;
	SchemaUtils schemaUtils = new SchemaUtils();
	ConceptNodesTaxonomySearchServices conceptNodesTaxonomySearchServices;
	RecordsCaches caches;
	TaxonomiesSearchServicesCache cache;

	public TaxonomiesSearchServicesBasedOnHierarchyTokensImpl(ModelLayerFactory modelLayerFactory) {
		this.searchServices = modelLayerFactory.newSearchServices();
		this.taxonomiesManager = modelLayerFactory.getTaxonomiesManager();
		this.metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
		this.recordServices = modelLayerFactory.newRecordServices();
		this.conceptNodesTaxonomySearchServices = new ConceptNodesTaxonomySearchServices(modelLayerFactory);
		this.caches = modelLayerFactory.getRecordsCaches();
		this.cache = modelLayerFactory.getTaxonomiesSearchServicesCache();
	}

	public List<TaxonomySearchRecord> getVisibleRootConcept(User user, String collection, String taxonomyCode,
															TaxonomiesSearchOptions options) {
		return getVisibleRootConceptResponse(user, collection, taxonomyCode, options, null).getRecords();
	}

	public List<TaxonomySearchRecord> getVisibleChildConcept(User user, String taxonomyCode, Record record,
															 TaxonomiesSearchOptions options) {
		return getVisibleChildConceptResponse(user, taxonomyCode, record, options).getRecords();
	}

	public boolean findNonTaxonomyRecordsInStructure(Record record, TaxonomiesSearchOptions options) {
		String schemaType = new SchemaUtils().getSchemaTypeCode(record.getSchemaCode());
		GetChildrenContext context = new GetChildrenContext(User.GOD, record, options, null);
		LogicalSearchCondition condition = findVisibleNonTaxonomyRecordsInStructure(context, false);
		return searchServices.hasResults(new LogicalSearchQuery(condition).filteredByStatus(options.getIncludeStatus()));
	}

	public List<TaxonomySearchRecord> getLinkableRootConcept(User user, String collection, String taxonomyCode,
															 String selectedType, TaxonomiesSearchOptions options) {
		return getLinkableRootConceptResponse(user, collection, taxonomyCode, selectedType, options).getRecords();
	}

	public LinkableTaxonomySearchResponse getLinkableRootConceptResponse(User user, String collection,
																		 String usingTaxonomyCode,
																		 String selectedType,
																		 TaxonomiesSearchOptions options) {

		return getLinkableConceptResponse(user, collection, usingTaxonomyCode, selectedType, null, options);

	}

	public List<TaxonomySearchRecord> getLinkableChildConcept(User user, Record record, String usingTaxonomy,
															  String selectedType,
															  TaxonomiesSearchOptions options) {
		return getLinkableChildConceptResponse(user, record, usingTaxonomy, selectedType, options).getRecords();
	}

	public LinkableTaxonomySearchResponse getLinkableChildConceptResponse(User user, Record inRecord,
																		  String usingTaxonomy,
																		  String selectedType,
																		  TaxonomiesSearchOptions options) {

		return getLinkableConceptResponse(user, inRecord.getCollection(), usingTaxonomy, selectedType, inRecord, options);

	}

	public List<TaxonomySearchRecord> getVisibleChildConcept(User user, Record record,
															 TaxonomiesSearchOptions options) {
		Taxonomy taxonomy = taxonomiesManager.getTaxonomyOf(record);
		String taxonomyCode = null;
		if (taxonomy == null) {

			taxonomyCode = taxonomiesManager.getPrincipalTaxonomy(record.getCollection()).getCode();
		} else {
			taxonomyCode = taxonomy.getCode();
		}

		GetChildrenContext ctx = new GetChildrenContext(user, record, options, null);
		return getVisibleChildrenRecords(ctx).getRecords();
	}

	private HasChildrenQueryHandler newHasChildrenQueryHandler(GetChildrenContext context,
															   LogicalSearchQuery facetQuery) {
		return new HasChildrenQueryHandler(context.username(), context.getCacheMode(), this, searchServices, facetQuery);
	}

	private HasChildrenQueryHandler newHasChildrenQueryHandler(User user, String cacheMode,
															   LogicalSearchQuery facetQuery) {
		return new HasChildrenQueryHandler(user == null ? null : user.getUsername(), cacheMode, this, searchServices, facetQuery);
	}

	private class GetChildrenContext {
		User user;
		Record record;
		TaxonomiesSearchOptions options;
		MetadataSchemaType forSelectionOfSchemaType;
		Taxonomy taxonomy;
		boolean hasPermanentCache;
		boolean principalTaxonomy;

		public GetChildrenContext(User user, Record record, TaxonomiesSearchOptions options,
								  MetadataSchemaType forSelectionOfSchemaType) {
			this.user = user;
			this.record = record;
			this.options = options;
			this.forSelectionOfSchemaType = forSelectionOfSchemaType;
			this.taxonomy = getTaxonomyForNavigation(record);

			if (taxonomy.getSchemaTypes().size() == 1) {
				CacheConfig cacheConfig = caches.getCache(getCollection()).getCacheConfigOf(taxonomy.getSchemaTypes().get(0));
				hasPermanentCache = cacheConfig != null && cacheConfig.isPermanent();
			}
			principalTaxonomy = taxonomiesManager.getPrincipalTaxonomy(getCollection()).hasSameCode(taxonomy);
		}

		public GetChildrenContext(User user, Record record, TaxonomiesSearchOptions options,
								  MetadataSchemaType forSelectionOfSchemaType, Taxonomy taxonomy) {
			this.user = user;
			this.record = record;
			this.options = options;
			this.forSelectionOfSchemaType = forSelectionOfSchemaType;
			this.taxonomy = taxonomy;

			if (taxonomy.getSchemaTypes().size() == 1) {
				CacheConfig cacheConfig = caches.getCache(getCollection()).getCacheConfigOf(taxonomy.getSchemaTypes().get(0));
				hasPermanentCache = cacheConfig != null && cacheConfig.isPermanent();
			}
			principalTaxonomy = taxonomiesManager.getPrincipalTaxonomy(getCollection()).hasSameCode(taxonomy);
		}

		public boolean hasRequiredAccessOn(Record record) {
			return user.hasRequiredAccess(options.getRequiredAccess()).on(record);
		}

		public boolean isConceptOfNavigatedTaxonomy(Record childRecordLeadingToSecuredRecord) {
			return taxonomy.getSchemaTypes().contains(childRecordLeadingToSecuredRecord.getTypeCode());
		}

		public String getCollection() {
			return record == null ? taxonomy.getCollection() : record.getCollection();
		}

		public LogicalSearchQuery newQueryWithUserFilter(LogicalSearchCondition condition) {
			LogicalSearchQuery logicalSearchQuery;
			if (user != null) {
				logicalSearchQuery = new LogicalSearchQuery(condition).filteredWith(new UserFilter() {
					@Override
					public String buildFQ(SecurityTokenManager securityTokenManager) {
						return FilterUtils.userHierarchyFilter(user, securityTokenManager, options.getRequiredAccess(),
								forSelectionOfSchemaType, options.isShowInvisibleRecordsInLinkingMode());
					}

					@Override
					public User getUser() {
						return user;
					}
				});

			} else {
				logicalSearchQuery = new LogicalSearchQuery(condition);
			}
			return logicalSearchQuery;
		}

		public boolean isHiddenInvisibleInTree() {
			return forSelectionOfSchemaType == null ? true : !options.isShowInvisibleRecordsInLinkingMode();
		}

		public boolean isSelectingAConcept() {
			return forSelectionOfSchemaType != null && taxonomy.getSchemaTypes().contains(forSelectionOfSchemaType.getCode());
		}

		//		public LogicalSearchCondition applyLinkableConceptsCondition(LogicalSearchCondition condition) {
		//			if (options.getFilter() != null && options.getFilter().getLinkableConceptsCondition() != null) {
		//				return allConditions(condition, options.getFilter().getLinkableConceptsCondition());
		//			} else {
		//				return condition;
		//			}
		//		}

		public boolean isNonSecurableTaxonomyRecord(Record record) {
			return isConceptOfNavigatedTaxonomy(record) && !principalTaxonomy;
		}

		public String getCacheMode() {
			return HasChildrenQueryHandler.getCacheMode(forSelectionOfSchemaType, options.getRequiredAccess(),
					options.isShowInvisibleRecordsInLinkingMode(),
					options.isAlwaysReturnTaxonomyConceptsWithReadAccessOrLinkable());

		}

		public String username() {
			return user == null ? null : user.getUsername();

		}

		public GetChildrenContext createCopyFor(Record child) {
			return new GetChildrenContext(user, child, options, forSelectionOfSchemaType, taxonomy);

		}
	}

	private ReturnedMetadatasFilter returnedMetadatasForRecordsIn(GetChildrenContext context) {
		return conceptNodesTaxonomySearchServices.returnedMetadatasForRecordsIn(context.getCollection(), context.options);
	}

	private LinkableTaxonomySearchResponse getVisibleChildrenRecords(GetChildrenContext ctx) {

		GetConceptRecordsWithVisibleRecordsResponse conceptsResponse;
		if (ctx.options.getFastContinueInfos() == null || !ctx.options.getFastContinueInfos().isFinishedConceptsIteration()) {
			conceptsResponse = getConceptRecordsWithVisibleRecords(ctx);
		} else {
			//Since we know that all concepts has been checked and the quantity of returned concepts, we place nulls
			// in the list instead of records. Since these values are outside the returned range, it cause no problem
			conceptsResponse = createConceptsResponseWithNullValues(ctx);
		}

		List<Record> records = new ArrayList<>();
		int realRecordsStart = 0;
		SPEQueryResponse nonTaxonomyRecordsResponse = null;
		if (ctx.isSelectingAConcept()) {
			nonTaxonomyRecordsResponse = new SPEQueryResponse(new ArrayList<Record>(), new ArrayList<MoreLikeThisRecord>());
		} else {
			int realRecordsRows;
			if (ctx.options.getFastContinueInfos() == null) {
				realRecordsStart = 0;
				realRecordsRows = ctx.options.getStartRow() + ctx.options.getRows() - conceptsResponse.records.size();
			} else {
				if (ctx.options.getFastContinueInfos().isFinishedConceptsIteration()) {
					realRecordsStart = ctx.options.getFastContinueInfos().lastReturnRecordIndex;
				} else {
					realRecordsStart = 0;
				}
				realRecordsRows = ctx.options.getRows();

				if (ctx.options.getFastContinueInfos().isFinishedConceptsIteration()) {
					int qtyRecords = ctx.options.getFastContinueInfos().getLastReturnRecordIndex()
									 + ctx.options.getFastContinueInfos().getShownRecordsWithVisibleChildren().size();
					for (int i = 0; i < qtyRecords; i++) {
						records.add(null);
					}
				}
			}

			List<Record> nonNullRecords = new ArrayList<>();
			realRecordsRows = Math.max(0, realRecordsRows);
			nonTaxonomyRecordsResponse = getNonTaxonomyRecords(ctx, realRecordsStart, realRecordsRows);
			nonNullRecords.addAll(nonTaxonomyRecordsResponse.getRecords());

			Collections.sort(nonNullRecords, new RecordCodeComparator(ctx.taxonomy.getSchemaTypes()));
			records.addAll(nonNullRecords);

		}

		return regroupChildren(ctx, conceptsResponse, nonTaxonomyRecordsResponse, records, realRecordsStart);

	}

	private GetConceptRecordsWithVisibleRecordsResponse createConceptsResponseWithNullValues(GetChildrenContext ctx) {
		GetConceptRecordsWithVisibleRecordsResponse conceptsResponse;
		conceptsResponse = new GetConceptRecordsWithVisibleRecordsResponse();
		conceptsResponse.finishedIteratingOverRecords = true;
		conceptsResponse.records = new ArrayList<>();
		int qtyConcepts = ctx.options.getStartRow()
						  - ctx.options.getFastContinueInfos().getLastReturnRecordIndex()
						  - ctx.options.getFastContinueInfos().getShownRecordsWithVisibleChildren().size();
		for (int i = 0; i < qtyConcepts; i++) {
			conceptsResponse.records.add(null);
		}
		return conceptsResponse;
	}

	private LinkableTaxonomySearchResponse regroupChildren(
			GetChildrenContext ctx, GetConceptRecordsWithVisibleRecordsResponse conceptsResponse,
			SPEQueryResponse nonTaxonomyRecordsResponse, List<Record> records,
			int recordsStartIndex) {
		List<TaxonomySearchRecord> concepts = conceptsResponse.records;
		Set<String> typesParentOfOtherTypes = metadataSchemasManager.getSchemaTypes(ctx.getCollection())
				.getTypeParentOfOtherTypes();
		List<TaxonomySearchRecord> returnedRecords = new ArrayList<>();
		SPEQueryResponse facetResponse = queryFindingWhichRecordsHasChildren(ctx, concepts.size(), records);

		List<String> placedChildrenWithoutAccessToIncludeRecordIds = new ArrayList<>();
		if (ctx.options.getFastContinueInfos() != null) {
			placedChildrenWithoutAccessToIncludeRecordIds
					.addAll(ctx.options.getFastContinueInfos().getShownRecordsWithVisibleChildren());
		}
		int lastRow = recordsStartIndex;
		for (int i = ctx.options.getStartRow(); i < ctx.options.getEndRow() && i - concepts.size() < records.size(); i++) {
			if (i < concepts.size()) {
				returnedRecords.add(concepts.get(i));

			} else {
				int nonTaxonomyIndex = i - concepts.size();
				Record returnedRecord = records.get(nonTaxonomyIndex);
				boolean hasChildren;
				if (facetResponse == null) {
					hasChildren = typesParentOfOtherTypes.contains(returnedRecord.getTypeCode());
				} else {
					hasChildren = facetResponse.hasQueryFacetResults(facetQueryFor(returnedRecord));
				}
				boolean linkable = ctx.hasRequiredAccessOn(returnedRecord) && ctx.forSelectionOfSchemaType != null
								   && ctx.forSelectionOfSchemaType.getCode().equals(returnedRecord.getTypeCode());
				Record record = records.get(nonTaxonomyIndex);
				returnedRecords.add(new TaxonomySearchRecord(record, linkable, hasChildren));

				recordsStartIndex++;

			}

		}

		FastContinueInfos infos;
		if (conceptsResponse.finishedIteratingOverRecords) {
			infos = new FastContinueInfos(true, recordsStartIndex, placedChildrenWithoutAccessToIncludeRecordIds);
		} else {
			infos = new FastContinueInfos(false, conceptsResponse.continueAtPosition, new ArrayList<String>());
		}

		long numfound;
		numfound = nonTaxonomyRecordsResponse.getNumFound() + concepts.size();

		if (!conceptsResponse.finishedIteratingOverRecords) {
			numfound++;
		}

		return new LinkableTaxonomySearchResponse(numfound, infos, returnedRecords);
	}

	private SPEQueryResponse getNonTaxonomyRecords(final GetChildrenContext ctx, int realStart, int realRows) {
		LogicalSearchCondition condition;

		if (ctx.forSelectionOfSchemaType == null
			|| ctx.forSelectionOfSchemaType.getAllReferencesToTaxonomySchemas(asList(ctx.taxonomy)).isEmpty()) {
			condition = fromAllSchemasInCollectionOf(ctx.record, DataStore.RECORDS)
					.where(directChildOf(ctx.record)).andWhere(visibleInTrees)
					.andWhere(schemaTypeIsNotIn(ctx.taxonomy.getSchemaTypes()));
		} else {
			condition = from(ctx.forSelectionOfSchemaType).where(directChildOf(ctx.record));

			if (!ctx.options.isShowInvisibleRecordsInLinkingMode()) {
				condition = condition.andWhere(VISIBLE_IN_TREES).isTrueOrNull();
			}
		}
		LogicalSearchQuery query = newQuery(condition, ctx.options)
				.setStartRow(realStart).setNumberOfRows(realRows);

		query.filteredWith(new UserFilter() {

			@Override
			public String buildFQ(SecurityTokenManager securityTokenManager) {

				return FilterUtils.userHierarchyFilter(ctx.user, securityTokenManager, ctx.options.getRequiredAccess(),
						ctx.forSelectionOfSchemaType, ctx.options.isShowInvisibleRecordsInLinkingMode());
			}

			@Override
			public User getUser() {
				return ctx.user;
			}
		});
		query.setName("TaxonomiesSearchServices:getNonTaxonomyRecords(" + ctx.username() + ", " + ctx.record.getId() + ")");
		return searchServices.query(query);
	}

	private Taxonomy getTaxonomyForNavigation(Record record) {
		Taxonomy taxonomy = taxonomiesManager.getTaxonomyOf(record);
		if (taxonomy == null) {
			taxonomy = taxonomiesManager.getPrincipalTaxonomy(record.getCollection());
		}
		return taxonomy;
	}

	private SPEQueryResponse queryFindingWhichRecordsHasChildren(GetChildrenContext context, int visibleConceptsSize,
																 List<Record> records) {
		SPEQueryResponse facetResponse = null;

		if (context.options.getHasChildrenFlagCalculated() == HasChildrenFlagCalculated.ALWAYS) {
			LogicalSearchCondition queryCondition;

			if (context.forSelectionOfSchemaType == null) {
				queryCondition = fromAllSchemasIn(context.getCollection())
						.where(recordInHierarchyOf(context.record))
						.andWhere(notDirectChildOf(context.record))
						.andWhere(visibleInTrees)
						.andWhere(schemaTypeIsNotIn(context.taxonomy.getSchemaTypes()));
			} else {
				queryCondition = from(context.forSelectionOfSchemaType)
						.where(recordInHierarchyOf(context.record))
						.andWhere(notDirectChildOf(context.record))
						.andWhere(Schemas.LINKABLE).isTrueOrNull();

				if (!context.options.isShowInvisibleRecordsInLinkingMode()) {
					queryCondition = queryCondition.andWhere(visibleInTrees);
				}

			}

			LogicalSearchQuery facetQuery = context.newQueryWithUserFilter(queryCondition)
					.filteredByStatus(ACTIVES).setStartRow(0).setNumberOfRows(0);

			int facetCounts = 0;
			for (int i = visibleConceptsSize;
				 i - visibleConceptsSize < records.size() && facetCounts < context.options.getRows(); i++) {
				int nonTaxonomyIndex = i - visibleConceptsSize;
				Record record = records.get(nonTaxonomyIndex);
				if (record != null) {
					facetQuery.addQueryFacet("hasChildren", facetQueryFor(record));
					facetCounts++;
				}
			}

			if (facetCounts > 0) {
				facetQuery.setName(
						"TaxonomiesSearchServices:hasChildrenQuery(" + context.username() + ", " + context.record.getId() + ")");
				facetResponse = searchServices.query(facetQuery);
			}
		}
		return facetResponse;
	}

	public String facetQueryFor(Record record) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("pathParts_ss:");
		stringBuilder.append(record.getId());
		return stringBuilder.toString();
	}

	private static class GetConceptRecordsWithVisibleRecordsResponse {
		List<TaxonomySearchRecord> records;
		boolean finishedIteratingOverRecords;
		int continueAtPosition;
	}

	private GetConceptRecordsWithVisibleRecordsResponse getConceptRecordsWithVisibleRecords(
			GetChildrenContext context) {

		GetConceptRecordsWithVisibleRecordsResponse methodResponse = new GetConceptRecordsWithVisibleRecordsResponse();
		MetadataSchemaTypes types = metadataSchemasManager.getSchemaTypes(context.getCollection());

		Iterator<List<Record>> iterator;
		int lastIteratedRecordIndex = 0;
		methodResponse.records = new ArrayList<>();
		if (context.isConceptOfNavigatedTaxonomy(context.record)) {
			LogicalSearchQuery mainQuery = childConceptsQuery(context.record, context.taxonomy, context.options, types);

			FastContinueInfos continueInfos = context.options.getFastContinueInfos();
			if (continueInfos != null) {
				lastIteratedRecordIndex = continueInfos.lastReturnRecordIndex;
			}

			if (context.hasPermanentCache) {
				iterator = searchServices.cachedRecordsIteratorKeepingOrder(mainQuery, context.options.getRows()).inBatches();
				lastIteratedRecordIndex = 0;
			} else {

				if (continueInfos != null) {
					for (int i = 0; i < context.options.getStartRow(); i++) {
						methodResponse.records.add(null);
					}
					int batchSize = context.options.getRows() * 2;
					iterator = searchServices
							.recordsIteratorKeepingOrder(mainQuery, batchSize, continueInfos.getLastReturnRecordIndex())
							.inBatches();

				} else {
					iterator = searchServices.recordsIteratorKeepingOrder(mainQuery, context.options.getRows()).inBatches();
				}
			}
		} else {
			iterator = new ArrayList<List<Record>>().iterator();
		}

		int consumed = 0;

		while (methodResponse.records.size() < context.options.getEndRow() + 1 && iterator.hasNext()) {

			List<Record> batch = iterator.next();
			consumed += batch.size();

			boolean calculateHasChildren;

			calculateHasChildren = context.options.getHasChildrenFlagCalculated() != NEVER
								   || !context.options.isAlwaysReturnTaxonomyConceptsWithReadAccessOrLinkable();
			boolean calculateLinkability = context.options.isLinkableFlagCalculated();

			LogicalSearchQuery facetQuery;
			if (context.isSelectingAConcept()) {
				LogicalSearchCondition condition = fromAllSchemasIn(context.taxonomy.getCollection())
						.where(PATH_PARTS).isEqualTo(context.record.getId())
						.andWhere(schemaTypeIsIn(context.taxonomy.getSchemaTypes()));

				boolean selectingAConceptNoMatterTheLinkableStatus =
						context.isSelectingAConcept() && context.options.isAlwaysReturnTaxonomyConceptsWithReadAccessOrLinkable();

				if (!selectingAConceptNoMatterTheLinkableStatus) {
					condition = condition.andWhere(Schemas.LINKABLE).isTrueOrNull();
				}

				//condition = context.applyLinkableConceptsCondition(condition);

				facetQuery = newQueryForFacets(condition, null, context.options);

				//				for (Record record : batch) {
				//					facetQuery.addQueryFacet(CHILDREN_QUERY, "id:" + record.getId());
				//				}
			} else {
				LogicalSearchCondition condition = findVisibleNonTaxonomyRecordsInStructure(
						context, context.isHiddenInvisibleInTree());

				facetQuery = newQueryForFacets(condition, context);
			}

			boolean[] hasAccess = new boolean[batch.size()];

			HasChildrenQueryHandler hasChildrenQueryHandler = newHasChildrenQueryHandler(context, facetQuery);
			for (int i = 0; i < batch.size(); i++) {
				Record child = batch.get(i);

				hasAccess[i] = context.isNonSecurableTaxonomyRecord(child) || context.hasRequiredAccessOn(child);

				if (calculateHasChildren || !hasAccess[i]) {
					hasChildrenQueryHandler.addRecordToCheck(child);
				}
			}

			for (int i = 0; i < batch.size(); i++) {
				Record child = batch.get(i);
				boolean hasChildren = true;
				if (calculateHasChildren || !hasAccess[i]) {
					hasChildren = hasChildrenQueryHandler.hasChildren(child);
					if (hasChildren
						&& context.options.getFilter() != null
						&& context.options.getFilter().getLinkableConceptsFilter() != null
						&& context.hasPermanentCache
						&& !context.principalTaxonomy
						&& context.isSelectingAConcept()
						&& context.taxonomy.getSchemaTypes().size() == 1) {
						hasChildren = hasLinkableConceptInHierarchy(child, context.taxonomy, context.options);
					}
				}
				boolean linkable;
				if (context.isSelectingAConcept() && calculateLinkability) {
					linkable = isLinkable(child, context.taxonomy, context.options);
					//response.hasQueryFacetResults("id:" + child.getId());
				} else {
					linkable = NOT_LINKABLE;
				}

				if (hasChildren || linkable) {
					if (methodResponse.records.size() < context.options.getEndRow()) {
						lastIteratedRecordIndex++;
					}
					methodResponse.records.add(new TaxonomySearchRecord(child, linkable, hasChildren));

				} else if (context.options.isAlwaysReturnTaxonomyConceptsWithReadAccessOrLinkable()) {
					if (!taxonomiesManager.isTypeInPrincipalTaxonomy(context.getCollection(), child.getTypeCode())
						|| context.hasRequiredAccessOn(child)) {
						if (methodResponse.records.size() < context.options.getEndRow()) {
							lastIteratedRecordIndex++;
						}
						methodResponse.records.add(new TaxonomySearchRecord(child, linkable, false));
					}
				}
			}
		}

		if (methodResponse.records.size() > context.options.getEndRow()) {
			methodResponse.finishedIteratingOverRecords = false;
			methodResponse.records.remove(methodResponse.records.size() - 1);
		} else {
			methodResponse.finishedIteratingOverRecords = true;
		}

		methodResponse.continueAtPosition = lastIteratedRecordIndex;
		return methodResponse;

	}

	private boolean isAuthGivingRequiredAccess(Authorization authorizationDetails, String requiredAccess) {

		if (Role.READ.equals(requiredAccess)) {
			return authorizationDetails.getRoles().contains(Role.READ)
				   || authorizationDetails.getRoles().contains(Role.WRITE)
				   || authorizationDetails.getRoles().contains(Role.DELETE);
		} else {
			return authorizationDetails.getRoles().contains(requiredAccess);
		}

	}

	public LinkableTaxonomySearchResponse getVisibleChildConceptResponse(User user, String taxonomyCode, Record record,
																		 TaxonomiesSearchOptions options) {
		GetChildrenContext ctx = new GetChildrenContext(user, record, options, null);
		return getVisibleChildrenRecords(ctx);
	}

	public LinkableTaxonomySearchResponse getVisibleRootConceptResponse(User user, String collection,
																		String taxonomyCode,
																		TaxonomiesSearchOptions options,
																		String forSelectionOfSchemaType) {
		LogicalSearchQuery mainQuery = conceptNodesTaxonomySearchServices.getRootConceptsQuery(collection, taxonomyCode, options);
		//		SearchResponseIterator<Record> rootIterator = searchServices.recordsIteratorKeepingOrder(
		//				mainQuery.setNumberOfRows(100000).setStartRow(0), 50);

		Taxonomy taxonomy = taxonomiesManager.getEnabledTaxonomyWithCode(collection, taxonomyCode);
		boolean selectingAConcept =
				forSelectionOfSchemaType != null && taxonomy.getSchemaTypes().contains(forSelectionOfSchemaType);

		boolean principalConcept = taxonomiesManager.getPrincipalTaxonomy(collection).hasSameCode(taxonomy);

		Iterator<List<Record>> iterator;
		List<TaxonomySearchRecord> visibleRecords = new ArrayList<>();
		int lastIteratedRecordIndex = 0;
		FastContinueInfos continueInfos = options.getFastContinueInfos();

		MetadataSchemaType schemaType = forSelectionOfSchemaType == null ? null :
										metadataSchemasManager.getSchemaTypes(collection).getSchemaType(forSelectionOfSchemaType);
		GetChildrenContext ctx = new GetChildrenContext(user, null, options, schemaType, taxonomy);

		if (ctx.hasPermanentCache) {
			if (continueInfos != null) {
				lastIteratedRecordIndex = continueInfos.lastReturnRecordIndex;
				for (int i = 0; i < options.getStartRow(); i++) {
					visibleRecords.add(null);
				}
				iterator = searchServices.cachedRecordsIteratorKeepingOrder(mainQuery, 100, continueInfos.lastReturnRecordIndex)
						.inBatches();

			} else {
				iterator = searchServices.cachedRecordsIteratorKeepingOrder(mainQuery, 100).inBatches();
			}
		} else {

			if (continueInfos != null) {
				lastIteratedRecordIndex = continueInfos.lastReturnRecordIndex;
				for (int i = 0; i < options.getStartRow(); i++) {
					visibleRecords.add(null);
				}
				iterator = searchServices.recordsIteratorKeepingOrder(mainQuery, 100, continueInfos.lastReturnRecordIndex)
						.inBatches();

			} else {
				iterator = searchServices.recordsIteratorKeepingOrder(mainQuery, 100).inBatches();
			}

		}

		boolean calculateHasChildren = !options.isAlwaysReturnTaxonomyConceptsWithReadAccessOrLinkable()
									   || options.getHasChildrenFlagCalculated() != NEVER;
		boolean calculateLinkability = options.isLinkableFlagCalculated();

		int consumed = 0;
		while (visibleRecords.size() < options.getEndRow() + 1 && iterator.hasNext()) {

			LogicalSearchQuery facetQuery;
			if (selectingAConcept) {
				LogicalSearchCondition condition = fromTypeIn(taxonomy)
						.where(VISIBLE_IN_TREES).isTrueOrNull();

				if (!options.isAlwaysReturnTaxonomyConceptsWithReadAccessOrLinkable()) {
					condition = condition.andWhere(LINKABLE).isTrueOrNull();
				}

				//				if (options.getFilter() != null && options.getFilter().getLinkableConceptsCondition() != null) {
				//					condition = allConditions(condition, options.getFilter().getLinkableConceptsCondition());
				//				}
				facetQuery = newQueryForFacets(condition, null, options);

			} else if (options.isAlwaysReturnTaxonomyConceptsWithReadAccessOrLinkable()) {
				LogicalSearchCondition condition = fromAllSchemasIn(taxonomy.getCollection()).where(VISIBLE_IN_TREES)
						.isTrueOrNull();
				facetQuery = newQueryForFacets(condition, user, options);

			} else {
				LogicalSearchCondition condition = findVisibleNonTaxonomyRecordsInStructure(taxonomy, true);
				facetQuery = newQueryForFacets(condition, user, options);
			}

			List<Record> batch = iterator.next();
			boolean[] hasAccess = new boolean[batch.size()];

			HasChildrenQueryHandler hasChildrenQueryHandler = newHasChildrenQueryHandler(ctx, facetQuery);

			for (int i = 0; i < batch.size(); i++) {
				Record child = batch.get(i);
				hasAccess[i] = !principalConcept || user.has(options.getRequiredAccess()).on(child);

				if (calculateHasChildren || !hasAccess[i]) {
					hasChildrenQueryHandler.addRecordToCheck(child);
				}
				//if (selectingAConcept && calculateLinkability) {
				//	facetQuery.addQueryFacet(CHILDREN_QUERY, "id:" + child.getId());
				//}
			}

			for (int i = 0; i < batch.size(); i++) {
				Record child = batch.get(i);
				consumed++;
				if (visibleRecords.size() < options.getEndRow()) {
					lastIteratedRecordIndex++;
				}
				Taxonomy taxonomyOfRecord = taxonomiesManager.getTaxonomyOf(child);
				boolean showEvenIfNoChildren = options.isAlwaysReturnTaxonomyConceptsWithReadAccessOrLinkable()
											   && !taxonomyOfRecord.hasSameCode(taxonomiesManager.getPrincipalTaxonomy(collection));

				boolean hasChildren;
				if (calculateHasChildren || !hasAccess[i]) {
					hasChildren = hasChildrenQueryHandler.hasChildren(child);

				} else {
					hasChildren = true;
				}

				boolean linkable = false;
				if (selectingAConcept && calculateLinkability) {
					linkable = isLinkable(child, taxonomy, options);
					//response.hasQueryFacetResults("id:" + child.getId());
				}

				if (hasChildren
					&& options.getFilter() != null && options.getFilter().getLinkableConceptsFilter() != null
					&& ctx.hasPermanentCache
					&& !ctx.principalTaxonomy
					&& ctx.isSelectingAConcept()
					&& ctx.taxonomy.getSchemaTypes().size() == 1
					&& (!linkable || options.getHasChildrenFlagCalculated() != NEVER)) {
					TaxonomiesSearchOptions childrenOptions = new TaxonomiesSearchOptions(ctx.options)
							.setHasChildrenFlagCalculated(HasChildrenFlagCalculated.NEVER);
					hasChildren = hasLinkableConceptInHierarchy(child, ctx.taxonomy, ctx.options);

				}
				if (showEvenIfNoChildren || linkable || hasChildren) {
					visibleRecords.add(new TaxonomySearchRecord(child, linkable, hasChildren));
				}
			}
		}

		int numFound = visibleRecords.size();
		int toIndex = Math.min(visibleRecords.size(), options.getStartRow() + options.getRows());
		List<TaxonomySearchRecord> returnedRecords = visibleRecords.subList(options.getStartRow(), toIndex);

		boolean finishedConceptsIteration = !iterator.hasNext();
		FastContinueInfos infos = new FastContinueInfos(finishedConceptsIteration, lastIteratedRecordIndex,
				new ArrayList<String>());

		return new LinkableTaxonomySearchResponse(numFound, infos, returnedRecords);
	}

	public boolean isLinkable(final Record record, final Taxonomy taxonomy, TaxonomiesSearchOptions options) {

		if (options.isAlwaysReturnTaxonomyConceptsWithReadAccessOrLinkable()) {
			return true;
		}

		boolean linkable = LangUtils.isTrueOrNull(record.<Boolean>get(Schemas.LINKABLE));
		if (linkable && options.getFilter() != null && options.getFilter().getLinkableConceptsFilter() != null) {
			linkable = options.getFilter().getLinkableConceptsFilter().isLinkable(new LinkableConceptFilterParams() {
				@Override
				public Record getRecord() {
					return record;
				}

				@Override
				public Taxonomy getTaxonomy() {
					return taxonomy;
				}

			});
		}
		return linkable;
	}

	private LinkableTaxonomySearchResponse getLinkableConceptsForSelectionOfAPrincipalTaxonomyConceptBasedOnAuthorizations(
			User user, Taxonomy usingTaxonomy, Record inRecord, TaxonomiesSearchOptions originalOptions) {

		TaxonomiesSearchOptions options = new TaxonomiesSearchOptions(originalOptions);
		final Taxonomy taxonomy = taxonomiesManager.getEnabledTaxonomyWithCode(usingTaxonomy.getCollection(), usingTaxonomy.getCode());
		MetadataSchemaType schemaType = metadataSchemasManager.getSchemaTypes(taxonomy.getCollection())
				.getSchemaType(usingTaxonomy.getSchemaTypes().get(0));

		GetChildrenContext ctx = new GetChildrenContext(user, inRecord, originalOptions, schemaType, taxonomy);

		if (ctx.hasPermanentCache) {

			String cacheMode = HasChildrenQueryHandler
					.getCacheMode(usingTaxonomy.getSchemaTypes().get(0), originalOptions.getRequiredAccess(),
							originalOptions.isShowInvisibleRecordsInLinkingMode(),
							originalOptions.isAlwaysReturnTaxonomyConceptsWithReadAccessOrLinkable());
			SPEQueryResponse mainQueryResponse;
			options.setRows(10000);
			options.setStartRow(0);
			if (inRecord == null) {
				mainQueryResponse = conceptNodesTaxonomySearchServices.getRootConceptResponse(
						usingTaxonomy.getCollection(), usingTaxonomy.getCode(), options);
			} else {
				mainQueryResponse = conceptNodesTaxonomySearchServices.getChildNodesResponse(inRecord, options);
			}
			List<Record> children = mainQueryResponse.getRecords();


			LogicalSearchCondition condition = fromAllSchemasIn(taxonomy.getCollection())
					.where(schemaTypeIsIn(taxonomy.getSchemaTypes()));
			LogicalSearchQuery query = new LogicalSearchQuery(condition)
					.filteredWithUser(user, options.getRequiredAccess())
					.filteredByStatus(options.getIncludeStatus())
					.sortAsc(Schemas.CODE).sortAsc(Schemas.TITLE)
					.setReturnedMetadatas(
							conceptNodesTaxonomySearchServices.returnedMetadatasForRecordsIn(usingTaxonomy.getCollection(), options));

			HasChildrenQueryHandler hasChildrenQueryHandler = newHasChildrenQueryHandler(user, cacheMode, query);

			for (Record child : children) {
				hasChildrenQueryHandler.addRecordToCheck(child);
			}

			SPEQueryResponse response = hasChildrenQueryHandler.query();
			List<String> responseRecordIds = new RecordUtils().toIdList(response.getRecords());
			List<TaxonomySearchRecord> resultVisible = new ArrayList<>();
			for (final Record child : children) {

				boolean hasVisibleChildren = hasChildrenQueryHandler.hasChildren(child);

				boolean readAuthorizationsOnConcept = responseRecordIds.contains(child.getId());
				boolean conceptIsLinkable = isTrueOrNull(child.get(Schemas.LINKABLE));


				if (options.getFilter() != null && options.getFilter().getLinkableConceptsFilter() != null) {
					conceptIsLinkable = options.getFilter().getLinkableConceptsFilter().isLinkable(new LinkableConceptFilterParams() {
						@Override
						public Record getRecord() {
							return child;
						}

						@Override
						public Taxonomy getTaxonomy() {
							return taxonomy;
						}
					});
				}

				if (hasVisibleChildren || (readAuthorizationsOnConcept && conceptIsLinkable)) {
					resultVisible.add(new TaxonomySearchRecord(child, readAuthorizationsOnConcept && conceptIsLinkable,
							hasVisibleChildren));
				}
			}

			int from = originalOptions.getStartRow();
			int to = originalOptions.getEndRow();
			if (resultVisible.size() < to) {
				to = resultVisible.size();
			}

			return new LinkableTaxonomySearchResponse(resultVisible.size(), resultVisible.subList(from, to));
		} else {

			List<Record> records = new ArrayList<>();
			if (records.is)

			if (inRecord == null) {
				mainQueryResponse = conceptNodesTaxonomySearchServices.getRootConceptResponse(
						usingTaxonomy.getCollection(), usingTaxonomy.getCode(), options);
			} else {
				mainQueryResponse = conceptNodesTaxonomySearchServices.getChildNodesResponse(inRecord, options);
			}
			List<Record> children = mainQueryResponse.getRecords();


			LogicalSearchCondition condition = fromAllSchemasIn(taxonomy.getCollection())
					.where(schemaTypeIsIn(taxonomy.getSchemaTypes()));
			LogicalSearchQuery query = new LogicalSearchQuery(condition)
					.filteredWithUser(user, options.getRequiredAccess())
					.filteredByStatus(options.getIncludeStatus())
					.sortAsc(Schemas.CODE).sortAsc(Schemas.TITLE)
					.setReturnedMetadatas(
							conceptNodesTaxonomySearchServices.returnedMetadatasForRecordsIn(usingTaxonomy.getCollection(), options));

			HasChildrenQueryHandler hasChildrenQueryHandler = newHasChildrenQueryHandler(user, cacheMode, query);

			for (Record child : children) {
				hasChildrenQueryHandler.addRecordToCheck(child);
			}

			SPEQueryResponse response = hasChildrenQueryHandler.query();
			List<String> responseRecordIds = new RecordUtils().toIdList(response.getRecords());
			List<TaxonomySearchRecord> resultVisible = new ArrayList<>();
			for (final Record child : children) {

				boolean hasVisibleChildren = hasChildrenQueryHandler.hasChildren(child);

				boolean readAuthorizationsOnConcept = responseRecordIds.contains(child.getId());
				boolean conceptIsLinkable = isTrueOrNull(child.get(Schemas.LINKABLE));


				if (options.getFilter() != null && options.getFilter().getLinkableConceptsFilter() != null) {
					conceptIsLinkable = options.getFilter().getLinkableConceptsFilter().isLinkable(new LinkableConceptFilterParams() {
						@Override
						public Record getRecord() {
							return child;
						}

						@Override
						public Taxonomy getTaxonomy() {
							return taxonomy;
						}
					});
				}

				if (hasVisibleChildren || (readAuthorizationsOnConcept && conceptIsLinkable)) {
					resultVisible.add(new TaxonomySearchRecord(child, readAuthorizationsOnConcept && conceptIsLinkable,
							hasVisibleChildren));
				}
			}

			int from = originalOptions.getStartRow();
			int to = originalOptions.getEndRow();
			if (resultVisible.size() < to) {
				to = resultVisible.size();
			}

			return new LinkableTaxonomySearchResponse(resultVisible.size(), resultVisible.subList(from, to));
		}

	}

	private LinkableTaxonomySearchResponse getLinkableConceptsForSelectionOfATaxonomyConcept(User user,
																							 Taxonomy taxonomy,
																							 MetadataSchemaType selectedType,
																							 Record inRecord,
																							 TaxonomiesSearchOptions options) {
		String cacheMode = HasChildrenQueryHandler
				.getCacheMode(selectedType, options.getRequiredAccess(), options.isShowInvisibleRecordsInLinkingMode(),
						options.isAlwaysReturnTaxonomyConceptsWithReadAccessOrLinkable());
		options = options.cloneAddingReturnedField(Schemas.LINKABLE).cloneAddingReturnedField(Schemas.DESCRIPTION_STRING)
				.cloneAddingReturnedField(Schemas.DESCRIPTION_TEXT);

		SPEQueryResponse mainQueryResponse;
		if (inRecord == null) {
			mainQueryResponse = conceptNodesTaxonomySearchServices.getRootConceptResponse(
					taxonomy.getCollection(), taxonomy.getCode(), options);
		} else {
			mainQueryResponse = query(childrenCondition(taxonomy, inRecord), options);
		}

		LogicalSearchCondition condition = fromTypeIn(taxonomy).where(VISIBLE_IN_TREES).isTrueOrNull();
		LogicalSearchQuery hasChildrenQuery = newQueryForFacets(condition, User.GOD, options);

		HasChildrenQueryHandler hasChildrenQueryHandler = newHasChildrenQueryHandler(user, cacheMode, hasChildrenQuery);
		for (Record child : mainQueryResponse.getRecords()) {
			hasChildrenQueryHandler.addRecordToCheck(child);
		}

		List<TaxonomySearchRecord> records = new ArrayList<>();
		for (Record rootConcept : mainQueryResponse.getRecords()) {

			boolean sameType = rootConcept.getSchemaCode().startsWith(selectedType.getCode());
			boolean linkable = isTrueOrNull(rootConcept.get(Schemas.LINKABLE));
			boolean hasChildren = hasChildrenQueryHandler.hasChildren(rootConcept);
			records.add(new TaxonomySearchRecord(rootConcept, sameType && linkable, hasChildren));
		}
		return new LinkableTaxonomySearchResponse(mainQueryResponse.getNumFound(), records);
	}

	private LinkableTaxonomySearchResponse getLinkableConceptsForSelectionOfARecordUsingNonPrincipalTaxonomy(
			GetChildrenContext ctx) {

		if (ctx.record != null) {
			return getVisibleChildrenRecords(ctx);
		} else {

			LogicalSearchQuery mainQuery = conceptNodesTaxonomySearchServices.getRootConceptsQuery(
					ctx.getCollection(), ctx.taxonomy.getCode(), ctx.options);

			mainQuery.filteredByStatus(ctx.options.getIncludeStatus())
					.sortAsc(Schemas.CODE).sortAsc(Schemas.TITLE)
					.setName("getRootConcepts")
					.setReturnedMetadatas(returnedMetadatasForRecordsIn(ctx));

			List<TaxonomySearchRecord> visibleRecords = new ArrayList<>();
			int lastIteratedRecordIndex = 0;
			FastContinueInfos continueInfos = ctx.options.getFastContinueInfos();
			Iterator<List<Record>> iterator;
			if (!ctx.hasPermanentCache) {

				if (continueInfos != null) {
					lastIteratedRecordIndex = continueInfos.lastReturnRecordIndex;
					iterator = searchServices.recordsIteratorKeepingOrder(mainQuery.setStartRow(0), 25,
							continueInfos.lastReturnRecordIndex).inBatches();
					for (int i = 0; i < ctx.options.getStartRow(); i++) {
						visibleRecords.add(null);
					}

				} else {
					iterator = searchServices.recordsIteratorKeepingOrder(mainQuery.setStartRow(0), 25).inBatches();
				}

			} else {

				if (continueInfos != null) {
					lastIteratedRecordIndex = continueInfos.lastReturnRecordIndex;
					iterator = searchServices.cachedRecordsIteratorKeepingOrder(mainQuery.setStartRow(0), 25,
							continueInfos.lastReturnRecordIndex).inBatches();
					for (int i = 0; i < ctx.options.getStartRow(); i++) {
						visibleRecords.add(null);
					}

				} else {
					iterator = searchServices.cachedRecordsIteratorKeepingOrder(mainQuery.setStartRow(0), 25).inBatches();
				}
			}
			Taxonomy principalTaxonomy = taxonomiesManager.getPrincipalTaxonomy(ctx.getCollection());
			while (visibleRecords.size() < ctx.options.getEndRow() + 1 && iterator.hasNext()) {

				List<Record> batch = iterator.next();
				boolean navigatingUsingPrincipalTaxonomy = principalTaxonomy != null
														   && principalTaxonomy.getCode().equals(ctx.taxonomy.getCode());

				List<String> schemaTypes = new ArrayList<>();
				schemaTypes.add(ctx.forSelectionOfSchemaType.getCode());

				if (ctx.options.isAlwaysReturnTaxonomyConceptsWithReadAccessOrLinkable() && navigatingUsingPrincipalTaxonomy) {
					schemaTypes.addAll(ctx.taxonomy.getSchemaTypes());
				}
				LogicalSearchCondition condition = from(schemaTypes, ctx.getCollection()).returnAll();

				if (!ctx.options.isShowInvisibleRecordsInLinkingMode()) {
					condition = condition.andWhere(VISIBLE_IN_TREES).isTrueOrNull();
				}
				LogicalSearchQuery facetQuery = newQueryForFacets(condition, ctx.user, ctx.options);
				HasChildrenQueryHandler hasChildrenQueryHandler = newHasChildrenQueryHandler(ctx, facetQuery);
				for (Record child : batch) {
					hasChildrenQueryHandler.addRecordToCheck(child);
				}

				for (Record child : batch) {
					if (visibleRecords.size() < ctx.options.getEndRow()) {
						lastIteratedRecordIndex++;
					}
					String schemaType = getSchemaTypeCode(child.getSchemaCode());

					boolean hasVisibleChildren = hasChildrenQueryHandler.hasChildren(child);
					Taxonomy taxonomy = taxonomiesManager.getTaxonomyOf(child);
					boolean visibleEvenIfEmpty = false;
					if (taxonomy != null && ctx.options.isAlwaysReturnTaxonomyConceptsWithReadAccessOrLinkable()) {
						if (principalTaxonomy != null && taxonomy.getCode().equals(principalTaxonomy.getCode())) {
							visibleEvenIfEmpty = ctx.hasRequiredAccessOn(child);
						} else {
							visibleEvenIfEmpty = true;
						}
					}

					if (schemaType.equals(ctx.forSelectionOfSchemaType.getCode())) {
						boolean hasAccess = ctx.user.hasRequiredAccess(ctx.options.getRequiredAccess()).on(child);
						if (hasAccess || hasVisibleChildren || visibleEvenIfEmpty) {
							visibleRecords.add(new TaxonomySearchRecord(child, hasAccess, hasVisibleChildren));
						}

					} else if (hasVisibleChildren || visibleEvenIfEmpty) {
						visibleRecords.add(new TaxonomySearchRecord(child, false, hasVisibleChildren));
					}

				}
			}

			int numFound = visibleRecords.size();
			int toIndex = Math.min(visibleRecords.size(), ctx.options.getEndRow());
			List<TaxonomySearchRecord> returnedRecords = visibleRecords.subList(ctx.options.getStartRow(), toIndex);

			boolean finishedConceptsIteration = !iterator.hasNext();
			FastContinueInfos infos = new FastContinueInfos(finishedConceptsIteration, lastIteratedRecordIndex,
					new ArrayList<String>());

			return new LinkableTaxonomySearchResponse(numFound, infos, returnedRecords);
		}
	}

	private LinkableTaxonomySearchResponse getLinkableConceptResponse(User user, String collection,
																	  String usingTaxonomyCode,
																	  String selectedTypeCode, Record inRecord,
																	  TaxonomiesSearchOptions options) {

		long start = new Date().getTime();

		MetadataSchemaType selectedType = metadataSchemasManager.getSchemaTypes(collection).getSchemaType(selectedTypeCode);
		Taxonomy usingTaxonomy = taxonomiesManager.getEnabledTaxonomyWithCode(collection, usingTaxonomyCode);
		Taxonomy principalTaxonomy = taxonomiesManager.getPrincipalTaxonomy(collection);

		LinkableTaxonomySearchResponse response;
		if (principalTaxonomy.getSchemaTypes().contains(selectedType.getCode())) {
			//selecting a record of the principal taxonomy

			//FIXME
			if (user == User.GOD || user.hasCollectionAccess(options.getRequiredAccess()) || (user
																									  .has(CorePermissions.MANAGE_SECURITY).globally() && options.isShowAllIfHasAccessToManageSecurity())) {
				//No security, the whole tree is visible
				response = getLinkableConceptsForSelectionOfATaxonomyConcept(user, usingTaxonomy, selectedType, inRecord,
						options);

			} else {
				//Security, only authorized concepts are visible (and their parents which are not selectable)
				response = getLinkableConceptsForSelectionOfAPrincipalTaxonomyConceptBasedOnAuthorizations(
						user, usingTaxonomy, inRecord, options);
			}
		} else if (usingTaxonomy.getSchemaTypes().contains(selectedType.getCode())) {
			//selecting a record of a non-principal taxonomy
			if (Role.WRITE.equals(options.getRequiredAccess()) || Role.DELETE.equals(options.getRequiredAccess())) {
				throw new TaxonomiesSearchServicesRuntimeException_CannotFilterNonPrincipalConceptWithWriteOrDeleteAccess();
			}

			if (inRecord == null) {
				response = getVisibleRootConceptResponse(user, collection, usingTaxonomyCode, options,
						selectedTypeCode);
			} else {

				GetChildrenContext ctx = new GetChildrenContext(user, inRecord, options, selectedType, usingTaxonomy);
				response = getVisibleChildrenRecords(ctx);
			}

		} else {
			//selecting a non-taxonomy record using a taxonomy
			GetChildrenContext ctx = new GetChildrenContext(user, inRecord, options, selectedType, usingTaxonomy);
			response = getLinkableConceptsForSelectionOfARecordUsingNonPrincipalTaxonomy(ctx);

		}

		long duration = new Date().getTime() - start;
		return response.withQTime(duration);
	}

	LogicalSearchCondition findVisibleNonTaxonomyRecordsInStructure(GetChildrenContext context,
																	boolean onlyVisibleInTrees) {
		LogicalSearchCondition condition = fromAllSchemasIn(context.taxonomy.getCollection())
				.where(PATH_PARTS).isEqualTo(context.record.getId())
				.andWhere(schemaTypeIsNotIn(context.taxonomy.getSchemaTypes()));

		if (onlyVisibleInTrees) {
			condition = condition.andWhere(VISIBLE_IN_TREES).isTrueOrNull();
		}

		return condition;
	}

	LogicalSearchCondition findVisibleNonTaxonomyRecordsInStructure(Taxonomy taxonomy, boolean onlyVisibleInTrees) {

		LogicalSearchCondition condition = fromAllSchemasIn(taxonomy.getCollection())
				.where(schemaTypeIsNotIn(taxonomy.getSchemaTypes()));

		if (onlyVisibleInTrees) {
			condition = condition.andWhere(VISIBLE_IN_TREES).isTrueOrNull();
		}

		return condition;
	}

	private LogicalSearchQuery newQueryForFacets(LogicalSearchCondition condition, GetChildrenContext context) {
		return newQueryForFacets(condition, context.user, context.options);
	}

	private LogicalSearchQuery newQueryForFacets(LogicalSearchCondition condition, User user,
												 TaxonomiesSearchOptions options) {
		LogicalSearchQuery query = new LogicalSearchQuery(condition)
				.filteredByStatus(options.getIncludeStatus())
				.setNumberOfRows(0)
				.setReturnedMetadatas(ReturnedMetadatasFilter.idVersionSchema());

		if (user != null) {
			query.filteredWithUser(user, options.getRequiredAccess());
		}
		return query;
	}

	private LogicalSearchQuery newQuery(LogicalSearchCondition condition, TaxonomiesSearchOptions options) {
		return new LogicalSearchQuery(condition)
				.filteredByStatus(options.getIncludeStatus())
				.setStartRow(options.getStartRow())
				.setNumberOfRows(options.getRows())
				.setReturnedMetadatas(
						conceptNodesTaxonomySearchServices.returnedMetadatasForRecordsIn(condition.getCollection(), options))
				.sortAsc(Schemas.CODE).sortAsc(Schemas.TITLE);
	}

	private SPEQueryResponse query(LogicalSearchCondition condition, TaxonomiesSearchOptions options) {
		return searchServices.query(newQuery(condition, options));
	}

	private static class TaxonomySearchRecordsComparator implements Comparator<TaxonomySearchRecord> {
		@Override
		public int compare(TaxonomySearchRecord o1, TaxonomySearchRecord o2) {
			return RecordCodeComparator.compareRecords(o1.getRecord(), o2.getRecord());
		}

	}

	private enum TreeNavigationPurpose {SHOW_RECORDS_WITH_ACCESS, SET_METADATA}

	public boolean hasLinkableConceptInHierarchy(final Record concept, final Taxonomy taxonomy,
												 TaxonomiesSearchOptions options) {
		MetadataSchemaType schemaType = metadataSchemasManager.getSchemaTypes(concept.getCollection())
				.getSchemaType(taxonomy.getSchemaTypes().get(0));
		List<Record> records = searchServices.getAllRecords(schemaType);
		for (final Record record : records) {
			if (record.getList(Schemas.PATH_PARTS).contains(concept.getId())) {
				boolean linkableFlag = LangUtils.isTrueOrNull(record.get(Schemas.LINKABLE));
				boolean linkableUsingFilter = options.getFilter().getLinkableConceptsFilter()
						.isLinkable(new LinkableConceptFilterParams() {
							@Override
							public Record getRecord() {
								return record;
							}

							@Override
							public Taxonomy getTaxonomy() {
								return taxonomy;
							}
						});

				if (linkableFlag && linkableUsingFilter) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public TaxonomiesSearchServicesCache getCache() {
		return cache;
	}
}
