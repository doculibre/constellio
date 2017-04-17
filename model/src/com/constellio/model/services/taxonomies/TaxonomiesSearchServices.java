package com.constellio.model.services.taxonomies;

import static com.constellio.data.utils.LangUtils.isTrueOrNull;
import static com.constellio.model.entities.schemas.Schemas.LINKABLE;
import static com.constellio.model.entities.schemas.Schemas.PATH_PARTS;
import static com.constellio.model.entities.schemas.Schemas.VISIBLE_IN_TREES;
import static com.constellio.model.services.schemas.SchemaUtils.getSchemaTypeCode;
import static com.constellio.model.services.search.StatusFilter.ACTIVES;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.allConditions;
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
import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.Role;
import com.constellio.model.entities.security.global.AuthorizationDetails;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordUtils;
import com.constellio.model.services.records.utils.RecordCodeComparator;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.search.SPEQueryResponse;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.taxonomies.TaxonomiesSearchServicesRuntimeException.TaxonomiesSearchServicesRuntimeException_CannotFilterNonPrincipalConceptWithWriteOrDeleteAccess;

public class TaxonomiesSearchServices {

	private static final String CHILDREN_QUERY = "children";
	private static final boolean NOT_LINKABLE = false;

	SearchServices searchServices;
	TaxonomiesManager taxonomiesManager;
	MetadataSchemasManager metadataSchemasManager;
	RecordServices recordServices;
	SchemaUtils schemaUtils = new SchemaUtils();
	ConceptNodesTaxonomySearchServices conceptNodesTaxonomySearchServices;

	public TaxonomiesSearchServices(ModelLayerFactory modelLayerFactory) {
		this.searchServices = modelLayerFactory.newSearchServices();
		this.taxonomiesManager = modelLayerFactory.getTaxonomiesManager();
		this.metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
		this.recordServices = modelLayerFactory.newRecordServices();
		this.conceptNodesTaxonomySearchServices = new ConceptNodesTaxonomySearchServices(searchServices, taxonomiesManager,
				metadataSchemasManager);
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

	public LinkableTaxonomySearchResponse getLinkableRootConceptResponse(User user, String collection, String usingTaxonomyCode,
			String selectedType, TaxonomiesSearchOptions options) {

		return getLinkableConceptResponse(user, collection, usingTaxonomyCode, selectedType, null, options);

	}

	public List<TaxonomySearchRecord> getLinkableChildConcept(User user, Record record, String usingTaxonomy, String selectedType,
			TaxonomiesSearchOptions options) {
		return getLinkableChildConceptResponse(user, record, usingTaxonomy, selectedType, options).getRecords();
	}

	public LinkableTaxonomySearchResponse getLinkableChildConceptResponse(User user, Record inRecord, String usingTaxonomy,
			String selectedType, TaxonomiesSearchOptions options) {

		return getLinkableConceptResponse(user, inRecord.getCollection(), usingTaxonomy, selectedType, inRecord, options);

	}

	public List<TaxonomySearchRecord> getVisibleChildConcept(User user, Record record, TaxonomiesSearchOptions options) {
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

	private class GetChildrenContext {
		User user;
		Record record;
		TaxonomiesSearchOptions options;
		MetadataSchemaType forSelectionOfSchemaType;
		Taxonomy taxonomy;

		public GetChildrenContext(User user, Record record, TaxonomiesSearchOptions options,
				MetadataSchemaType forSelectionOfSchemaType) {
			this.user = user;
			this.record = record;
			this.options = options;
			this.forSelectionOfSchemaType = forSelectionOfSchemaType;
			this.taxonomy = getTaxonomyForNavigation(record);
		}

		public GetChildrenContext(User user, Record record, TaxonomiesSearchOptions options,
				MetadataSchemaType forSelectionOfSchemaType, Taxonomy taxonomy) {
			this.user = user;
			this.record = record;
			this.options = options;
			this.forSelectionOfSchemaType = forSelectionOfSchemaType;
			this.taxonomy = taxonomy;
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
				logicalSearchQuery = new LogicalSearchQuery(condition).filteredWithUser(user, options.getRequiredAccess());
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

		public LogicalSearchCondition applyLinkableConceptsCondition(LogicalSearchCondition condition) {
			if (options.getFilter() != null && options.getFilter().getLinkableConceptsCondition() != null) {
				return allConditions(condition, options.getFilter().getLinkableConceptsCondition());
			} else {
				return condition;
			}
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
		List<Record> childrenWithoutAccessToInclude = new ArrayList<>();
		int realRecordsStart = 0;
		SPEQueryResponse nonTaxonomyRecordsResponse = null;
		if (ctx.isSelectingAConcept()) {
			nonTaxonomyRecordsResponse = new SPEQueryResponse(new ArrayList<Record>(),
					new HashMap<Record, Map<Record, Double>>());
		} else {
			childrenWithoutAccessToInclude.addAll(getChildrenRecordsWithoutRequiredAccessLeadingToRecordWithAccess(ctx));
			int realRecordsRows;
			if (ctx.options.getFastContinueInfos() == null) {
				realRecordsStart = 0;
				realRecordsRows = ctx.options.getStartRow() + ctx.options.getRows() - conceptsResponse.records.size()
						+ childrenWithoutAccessToInclude.size();
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
			nonTaxonomyRecordsResponse = getNonTaxonomyRecords(ctx, childrenWithoutAccessToInclude, realRecordsStart,
					realRecordsRows);
			nonNullRecords.addAll(nonTaxonomyRecordsResponse.getRecords());

			for (Record childWithoutAccessToInclude : childrenWithoutAccessToInclude) {
				if (ctx.options.getFastContinueInfos() == null || !ctx.options.getFastContinueInfos()
						.getShownRecordsWithVisibleChildren().contains(childWithoutAccessToInclude.getId())) {
					nonNullRecords.add(childWithoutAccessToInclude);
				}
			}
			Collections.sort(nonNullRecords, new RecordCodeComparator(ctx.taxonomy.getSchemaTypes()));
			records.addAll(nonNullRecords);

		}

		return regroupChildren(ctx, conceptsResponse, childrenWithoutAccessToInclude, nonTaxonomyRecordsResponse, records,
				realRecordsStart);

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
			List<Record> childrenWithoutAccessToInclude, SPEQueryResponse nonTaxonomyRecordsResponse, List<Record> records,
			int recordsStartIndex) {
		Set<String> childrenWithoutAccessToIncludeRecordIds = new RecordUtils().toIdSet(childrenWithoutAccessToInclude);
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
					hasChildren = facetResponse.hasQueryFacetResults(facetQueryFor(ctx.taxonomy, returnedRecord));
				}
				boolean linkable = ctx.hasRequiredAccessOn(returnedRecord) && ctx.forSelectionOfSchemaType != null
						&& ctx.forSelectionOfSchemaType.getCode().equals(returnedRecord.getTypeCode());
				Record record = records.get(nonTaxonomyIndex);
				returnedRecords.add(new TaxonomySearchRecord(record, linkable, hasChildren));

				if (childrenWithoutAccessToIncludeRecordIds.contains(record.getId())) {
					placedChildrenWithoutAccessToIncludeRecordIds.add(record.getId());
				} else {
					recordsStartIndex++;
				}

			}

		}

		FastContinueInfos infos;
		if (conceptsResponse.finishedIteratingOverRecords) {
			infos = new FastContinueInfos(true, recordsStartIndex, placedChildrenWithoutAccessToIncludeRecordIds);
		} else {
			infos = new FastContinueInfos(false, conceptsResponse.continueAtPosition, new ArrayList<String>());
		}

		long numfound;
		numfound = nonTaxonomyRecordsResponse.getNumFound() + childrenWithoutAccessToInclude.size() + concepts.size();

		return new LinkableTaxonomySearchResponse(numfound, infos, returnedRecords);
	}

	private SPEQueryResponse getNonTaxonomyRecords(GetChildrenContext ctx, List<Record> childrenWithoutAccessToInclude,
			int realStart, int realRows) {
		LogicalSearchCondition condition;

		if (ctx.forSelectionOfSchemaType == null
				|| ctx.forSelectionOfSchemaType.getAllReferencesToTaxonomySchemas(asList(ctx.taxonomy)).isEmpty()) {
			condition = fromAllSchemasInCollectionOf(ctx.record)
					.where(directChildOf(ctx.record)).andWhere(visibleInTrees)
					.andWhere(schemaTypeIsNotIn(ctx.taxonomy.getSchemaTypes()));
		} else {
			condition = from(ctx.forSelectionOfSchemaType).where(directChildOf(ctx.record));

			if (!ctx.options.isShowInvisibleRecordsInLinkingMode()) {
				condition = condition.andWhere(VISIBLE_IN_TREES).isTrueOrNull();
			}
		}
		LogicalSearchQuery query = newQuery(condition, ctx.options)
				.setStartRow(realStart).setNumberOfRows(realRows)
				.filteredWithUser(ctx.user, ctx.options.getRequiredAccess());

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

		if (context.options.isHasChildrenFlagCalculated()) {
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
					facetQuery.addQueryFacet("hasChildren", facetQueryFor(context.taxonomy, record));
					facetCounts++;
				}
			}

			if (facetCounts > 0) {
				facetResponse = searchServices.query(facetQuery);
			}
		}
		return facetResponse;
	}

	private static class GetConceptRecordsWithVisibleRecordsResponse {
		List<TaxonomySearchRecord> records;
		boolean finishedIteratingOverRecords;
		int continueAtPosition;
	}

	private GetConceptRecordsWithVisibleRecordsResponse getConceptRecordsWithVisibleRecords(GetChildrenContext context) {

		GetConceptRecordsWithVisibleRecordsResponse methodResponse = new GetConceptRecordsWithVisibleRecordsResponse();
		MetadataSchemaTypes types = metadataSchemasManager.getSchemaTypes(context.getCollection());
		LogicalSearchQuery mainQuery = childConceptsQuery(context.record, context.taxonomy, context.options, types);

		Iterator<List<Record>> iterator;
		int lastIteratedRecordIndex = 0;
		FastContinueInfos continueInfos = context.options.getFastContinueInfos();
		methodResponse.records = new ArrayList<>();
		if (continueInfos != null) {
			lastIteratedRecordIndex = continueInfos.lastReturnRecordIndex;
			for (int i = 0; i < context.options.getStartRow(); i++) {
				methodResponse.records.add(null);
			}
			int batchSize = context.options.getRows() * 2;
			iterator = searchServices.recordsIteratorKeepingOrder(mainQuery, batchSize, continueInfos.getLastReturnRecordIndex())
					.inBatches();

		} else {
			iterator = searchServices.recordsIteratorKeepingOrder(mainQuery, 50).inBatches();
		}

		int consumed = 0;

		while (methodResponse.records.size() < context.options.getEndRow() && iterator.hasNext()) {

			List<Record> batch = iterator.next();
			consumed += batch.size();

			LogicalSearchQuery facetQuery;
			if (context.isSelectingAConcept()) {
				LogicalSearchCondition condition = fromAllSchemasIn(context.taxonomy.getCollection())
						.where(PATH_PARTS).isEqualTo(context.record.getId())
						.andWhere(schemaTypeIsIn(context.taxonomy.getSchemaTypes()));

				boolean selectingAConceptNoMatterTheLinkableStatus =
						context.isSelectingAConcept() && context.options.isAlwaysReturnTaxonomyConceptsWithReadAccess();

				if (!selectingAConceptNoMatterTheLinkableStatus) {
					condition = condition.andWhere(Schemas.LINKABLE).isTrueOrNull();
				}

				condition = context.applyLinkableConceptsCondition(condition);

				facetQuery = newQueryForFacets(condition, null, context.options);

				for (Record record : batch) {
					facetQuery.addQueryFacet(CHILDREN_QUERY, "id:" + record.getId());
				}
			} else {
				LogicalSearchCondition condition = findVisibleNonTaxonomyRecordsInStructure(
						context, context.isHiddenInvisibleInTree());

				facetQuery = newQueryForFacets(condition, context);
			}
			facetQuery.addQueryFacets(CHILDREN_QUERY, facetQueriesFor(context.taxonomy, batch));

			SPEQueryResponse response = searchServices.query(facetQuery);
			for (Record child : batch) {
				boolean hasChildren = response.getQueryFacetCount(facetQueryFor(context.taxonomy, child)) > 0;
				boolean linkable;
				if (context.isSelectingAConcept()) {
					linkable = response.hasQueryFacetResults("id:" + child.getId());
				} else {
					linkable = NOT_LINKABLE;
				}

				if (hasChildren || linkable) {
					if (methodResponse.records.size() < context.options.getEndRow()) {
						lastIteratedRecordIndex++;
					}
					methodResponse.records.add(new TaxonomySearchRecord(child, linkable, hasChildren));

				} else if (context.options.isAlwaysReturnTaxonomyConceptsWithReadAccess()) {
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

		methodResponse.finishedIteratingOverRecords =
				!iterator.hasNext() && methodResponse.records.size() <= context.options.getEndRow();
		methodResponse.continueAtPosition = lastIteratedRecordIndex;
		return methodResponse;

	}

	/**
	 * @param context The call context
	 * @return all children for which the user has no access, but are ancestor of a record for which he has access
	 */
	private List<Record> getChildrenRecordsWithoutRequiredAccessLeadingToRecordWithAccess(GetChildrenContext context) {

		List<Record> records = new ArrayList<>();
		Set<String> returnedRecordIds = new HashSet<>();
		for (String authorizationDetailsId : context.user.getAllUserAuthorizations()) {
			AuthorizationDetails authorizationDetails = context.user.getAuthorizationDetail(authorizationDetailsId);
			if (authorizationDetails.getRoles().contains(context.options.getRequiredAccess())) {

				Record securedRecord = recordServices.getDocumentById(authorizationDetails.getTarget());
				String schemaType = getSchemaTypeCode(securedRecord.getSchemaCode());

				boolean recordExpectedToBeVisibleInTree;
				if (context.forSelectionOfSchemaType == null) {
					recordExpectedToBeVisibleInTree = !context.taxonomy.getSchemaTypes().contains(schemaType)
							&& !Boolean.TRUE.equals(securedRecord.get(Schemas.LOGICALLY_DELETED_STATUS))
							&& !Boolean.FALSE.equals(securedRecord.get(VISIBLE_IN_TREES));
				} else {
					recordExpectedToBeVisibleInTree = context.forSelectionOfSchemaType.getCode().equals(schemaType)
							&& !Boolean.TRUE.equals(securedRecord.get(Schemas.LOGICALLY_DELETED_STATUS))
							&& (context.options.isShowInvisibleRecordsInLinkingMode() ||
							!Boolean.FALSE.equals(securedRecord.get(VISIBLE_IN_TREES)));
				}

				if (recordExpectedToBeVisibleInTree
						&& securedRecord.getList(PATH_PARTS).contains(context.record.getId())
						&& !securedRecord.getList(PATH_PARTS).contains("_LAST_" + context.record.getId())) {

					Record childRecordLeadingToSecuredRecord = null;
					for (String aPath : securedRecord.<String>getList(Schemas.PATH)) {
						int index = aPath.indexOf("/" + context.record.getId() + "/");
						if (index != -1) {
							String pathAfterCurrentRecord = aPath.substring(index + context.record.getId().length() + 2);
							String childLeadingToSecuredRecordId = StringUtils.substringBefore(pathAfterCurrentRecord, "/");
							childRecordLeadingToSecuredRecord = recordServices.getDocumentById(childLeadingToSecuredRecordId);
						}

					}

					if (childRecordLeadingToSecuredRecord != null
							&& !context.hasRequiredAccessOn(childRecordLeadingToSecuredRecord)
							&& !context.isConceptOfNavigatedTaxonomy(childRecordLeadingToSecuredRecord)
							&& !returnedRecordIds.contains(childRecordLeadingToSecuredRecord.getId())) {

						returnedRecordIds.add(childRecordLeadingToSecuredRecord.getId());
						records.add(childRecordLeadingToSecuredRecord);
					}
				}
			}
		}

		return records;
	}

	public LinkableTaxonomySearchResponse getVisibleChildConceptResponse(User user, String taxonomyCode, Record record,
			TaxonomiesSearchOptions options) {
		GetChildrenContext ctx = new GetChildrenContext(user, record, options, null);
		return getVisibleChildrenRecords(ctx);
	}

	public LinkableTaxonomySearchResponse getVisibleRootConceptResponse(User user, String collection, String taxonomyCode,
			TaxonomiesSearchOptions options, String forSelectionOfSchemaType) {
		LogicalSearchQuery mainQuery = conceptNodesTaxonomySearchServices.getRootConceptsQuery(collection, taxonomyCode, options);
		//		SearchResponseIterator<Record> rootIterator = searchServices.recordsIteratorKeepingOrder(
		//				mainQuery.setNumberOfRows(100000).setStartRow(0), 50);

		Taxonomy taxonomy = taxonomiesManager.getEnabledTaxonomyWithCode(collection, taxonomyCode);
		boolean selectingAConcept =
				forSelectionOfSchemaType != null && taxonomy.getSchemaTypes().contains(forSelectionOfSchemaType);

		Iterator<List<Record>> iterator;
		List<TaxonomySearchRecord> visibleRecords = new ArrayList<>();
		int lastIteratedRecordIndex = 0;
		FastContinueInfos continueInfos = options.getFastContinueInfos();
		if (continueInfos != null) {
			lastIteratedRecordIndex = continueInfos.lastReturnRecordIndex;
			for (int i = 0; i < options.getStartRow(); i++) {
				visibleRecords.add(null);
			}
			iterator = searchServices.recordsIteratorKeepingOrder(mainQuery, 50, continueInfos.lastReturnRecordIndex).inBatches();

		} else {
			iterator = searchServices.recordsIteratorKeepingOrder(mainQuery, 50).inBatches();
		}

		int consumed = 0;
		while (visibleRecords.size() < options.getEndRow() + 1 && iterator.hasNext()) {

			LogicalSearchQuery facetQuery;
			if (selectingAConcept) {
				LogicalSearchCondition condition = fromTypeIn(taxonomy)
						.where(VISIBLE_IN_TREES).isTrueOrNull()
						.andWhere(LINKABLE).isTrueOrNull();
				if (options.getFilter() != null && options.getFilter().getLinkableConceptsCondition() != null) {
					condition = allConditions(condition, options.getFilter().getLinkableConceptsCondition());
				}
				facetQuery = newQueryForFacets(condition, null, options);

			} else if (options.isAlwaysReturnTaxonomyConceptsWithReadAccess()) {
				LogicalSearchCondition condition = fromAllSchemasIn(taxonomy.getCollection()).where(VISIBLE_IN_TREES)
						.isTrueOrNull();
				facetQuery = newQueryForFacets(condition, user, options);

			} else {
				LogicalSearchCondition condition = findVisibleNonTaxonomyRecordsInStructure(taxonomy, true);
				facetQuery = newQueryForFacets(condition, user, options);
			}

			List<Record> batch = iterator.next();
			for (Record child : batch) {
				facetQuery.addQueryFacet(CHILDREN_QUERY, facetQueryFor(taxonomy, child));
				if (selectingAConcept) {
					facetQuery.addQueryFacet(CHILDREN_QUERY, "id:" + child.getId());
				}
			}
			SPEQueryResponse response = searchServices.query(facetQuery);

			for (Record child : batch) {
				consumed++;
				if (visibleRecords.size() < options.getEndRow()) {
					lastIteratedRecordIndex++;
				}
				Taxonomy taxonomyOfRecord = taxonomiesManager.getTaxonomyOf(child);
				boolean showEvenIfNoChildren = options.isAlwaysReturnTaxonomyConceptsWithReadAccess()
						&& !taxonomyOfRecord.hasSameCode(taxonomiesManager.getPrincipalTaxonomy(collection));
				boolean hasChildren = response.getQueryFacetCount(facetQueryFor(taxonomy, child)) > 0;

				boolean linkable = NOT_LINKABLE;
				if (selectingAConcept) {
					linkable = response.hasQueryFacetResults("id:" + child.getId());
				}

				if (showEvenIfNoChildren || hasChildren || linkable) {
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

	private LinkableTaxonomySearchResponse getLinkableConceptsForSelectionOfAPrincipalTaxonomyConceptBasedOnAuthorizations(
			User user, Taxonomy usingTaxonomy, Record inRecord, TaxonomiesSearchOptions originalOptions) {

		SPEQueryResponse mainQueryResponse;
		TaxonomiesSearchOptions options = new TaxonomiesSearchOptions(originalOptions);
		options.setRows(10000);
		options.setStartRow(0);
		if (inRecord == null) {
			mainQueryResponse = conceptNodesTaxonomySearchServices.getRootConceptResponse(
					usingTaxonomy.getCollection(), usingTaxonomy.getCode(), options);
		} else {
			mainQueryResponse = conceptNodesTaxonomySearchServices.getChildNodesResponse(inRecord, options);
		}
		List<Record> children = mainQueryResponse.getRecords();

		Taxonomy taxonomy = taxonomiesManager.getEnabledTaxonomyWithCode(usingTaxonomy.getCollection(), usingTaxonomy.getCode());
		LogicalSearchCondition condition = fromAllSchemasIn(taxonomy.getCollection())
				.where(schemaTypeIsIn(taxonomy.getSchemaTypes()));
		LogicalSearchQuery query = new LogicalSearchQuery(condition)
				.filteredWithUser(user, options.getRequiredAccess())
				.filteredByStatus(options.getIncludeStatus())
				.sortAsc(Schemas.CODE).sortAsc(Schemas.TITLE)
				.setReturnedMetadatas(
						conceptNodesTaxonomySearchServices.returnedMetadatasForRecordsIn(usingTaxonomy.getCollection(), options));

		for (Record child : children) {
			query.addQueryFacet("childrens", facetQueryFor(usingTaxonomy, child));
		}
		SPEQueryResponse response = searchServices.query(query);
		List<String> responseRecordIds = new RecordUtils().toIdList(response.getRecords());
		List<TaxonomySearchRecord> resultVisible = new ArrayList<>();
		for (Record child : children) {
			boolean hasVisibleChildren =
					response.getQueryFacetCount(facetQueryFor(taxonomy, child)) > 0;

			boolean readAuthorizationsOnConcept = responseRecordIds.contains(child.getId());
			boolean conceptIsLinkable = isTrueOrNull(child.get(Schemas.LINKABLE));
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

	private LinkableTaxonomySearchResponse getLinkableConceptsForSelectionOfATaxonomyConcept(User user,
			Taxonomy taxonomy, MetadataSchemaType selectedType, Record inRecord, TaxonomiesSearchOptions options) {

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

		for (Record child : mainQueryResponse.getRecords()) {
			hasChildrenQuery.addQueryFacet(CHILDREN_QUERY, facetQueryFor(taxonomy, child));
		}

		SPEQueryResponse response = searchServices.query(hasChildrenQuery);

		List<TaxonomySearchRecord> records = new ArrayList<>();
		for (Record rootConcept : mainQueryResponse.getRecords()) {

			boolean sameType = rootConcept.getSchemaCode().startsWith(selectedType.getCode());
			boolean linkable = isTrueOrNull(rootConcept.get(Schemas.LINKABLE));
			boolean hasChildren =
					response.getQueryFacetCount(facetQueryFor(taxonomy, rootConcept)) > 0;
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
					.setReturnedMetadatas(returnedMetadatasForRecordsIn(ctx));

			Iterator<List<Record>> iterator;

			List<TaxonomySearchRecord> visibleRecords = new ArrayList<>();
			int lastIteratedRecordIndex = 0;
			FastContinueInfos continueInfos = ctx.options.getFastContinueInfos();
			if (continueInfos != null) {
				lastIteratedRecordIndex = continueInfos.lastReturnRecordIndex;
				for (int i = 0; i < ctx.options.getStartRow(); i++) {
					visibleRecords.add(null);
				}
				iterator = searchServices.recordsIteratorKeepingOrder(mainQuery.setStartRow(0), 25,
						continueInfos.lastReturnRecordIndex).inBatches();

			} else {
				iterator = searchServices.recordsIteratorKeepingOrder(mainQuery.setStartRow(0), 25).inBatches();
			}

			while (visibleRecords.size() < ctx.options.getEndRow() + 1 && iterator.hasNext()) {

				List<Record> batch = iterator.next();

				LogicalSearchCondition condition = from(ctx.forSelectionOfSchemaType).returnAll();

				if (!ctx.options.isShowInvisibleRecordsInLinkingMode()) {
					condition = condition.andWhere(VISIBLE_IN_TREES).isTrueOrNull();
				}
				LogicalSearchQuery facetQuery = newQueryForFacets(condition, ctx.user, ctx.options);
				for (Record child : batch) {
					facetQuery.addQueryFacet(CHILDREN_QUERY, facetQueryFor(ctx.taxonomy, child));
				}

				SPEQueryResponse response = searchServices.query(facetQuery);
				for (Record child : batch) {
					if (visibleRecords.size() < ctx.options.getEndRow()) {
						lastIteratedRecordIndex++;
					}
					String schemaType = getSchemaTypeCode(child.getSchemaCode());

					boolean hasVisibleChildren =
							response.getQueryFacetCount(facetQueryFor(ctx.taxonomy, child)) > 0;

					if (schemaType.equals(ctx.forSelectionOfSchemaType.getCode())) {
						boolean hasAccess = ctx.user.hasRequiredAccess(ctx.options.getRequiredAccess()).on(child);
						if (hasAccess || hasVisibleChildren) {
							visibleRecords.add(new TaxonomySearchRecord(child, hasAccess, hasVisibleChildren));
						}

					} else if (hasVisibleChildren) {
						visibleRecords.add(new TaxonomySearchRecord(child, false, true));
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

	private LinkableTaxonomySearchResponse getLinkableConceptResponse(User user, String collection, String usingTaxonomyCode,
			String selectedTypeCode, Record inRecord, TaxonomiesSearchOptions options) {

		long start = new Date().getTime();

		MetadataSchemaType selectedType = metadataSchemasManager.getSchemaTypes(collection).getSchemaType(selectedTypeCode);
		Taxonomy usingTaxonomy = taxonomiesManager.getEnabledTaxonomyWithCode(collection, usingTaxonomyCode);
		Taxonomy principalTaxonomy = taxonomiesManager.getPrincipalTaxonomy(collection);

		LinkableTaxonomySearchResponse response;
		if (principalTaxonomy.getSchemaTypes().contains(selectedType.getCode())) {
			//selecting a record of the principal taxonomy

			if (user == User.GOD || user.hasCollectionAccess(options.getRequiredAccess()) || user
					.has(CorePermissions.MANAGE_SECURITY).globally()) {
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

	LogicalSearchCondition findVisibleNonTaxonomyRecordsInStructure(GetChildrenContext context, boolean onlyVisibleInTrees) {
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

	private LogicalSearchQuery newQueryForFacets(LogicalSearchCondition condition, User user, TaxonomiesSearchOptions options) {
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

	private List<String> facetQueriesFor(Taxonomy taxonomy, List<Record> records) {
		List<String> queries = new ArrayList<>();
		for (Record record : records) {
			queries.add(facetQueryFor(taxonomy, record));
		}
		return queries;
	}

	private String facetQueryFor(Taxonomy taxonomy, Record record) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("pathParts_ss:");
		stringBuilder.append(record.getId());
		return stringBuilder.toString();
	}

	private enum TreeNavigationPurpose {SHOW_RECORDS_WITH_ACCESS, SET_METADATA}
}
