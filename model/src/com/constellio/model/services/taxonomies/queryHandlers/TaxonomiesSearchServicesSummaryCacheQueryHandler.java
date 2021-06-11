package com.constellio.model.services.taxonomies.queryHandlers;

import com.constellio.data.utils.LangUtils;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.QueryExecutionMethod;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.taxonomies.LinkableConceptFilter.LinkableConceptFilterParams;
import com.constellio.model.services.taxonomies.LinkableTaxonomySearchResponse;
import com.constellio.model.services.taxonomies.TaxonomiesSearchOptions;
import com.constellio.model.services.taxonomies.TaxonomySearchRecord;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.constellio.model.entities.schemas.Schemas.LINKABLE;
import static com.constellio.model.entities.schemas.Schemas.LOGICALLY_DELETED_STATUS;
import static com.constellio.model.entities.schemas.Schemas.VISIBLE_IN_TREES;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.taxonomies.TaxonomiesSearchOptions.HasChildrenFlagCalculated.NEVER;
import static java.util.stream.Collectors.toList;

public class TaxonomiesSearchServicesSummaryCacheQueryHandler
		extends TaxonomiesSearchServicesBaseQueryHandler implements TaxonomiesSearchServicesQueryHandler {

	private static final int LIMIT_OF_RECORDS_IN_A_NODE_FOR_USING_CACHE = 100000;

	public TaxonomiesSearchServicesSummaryCacheQueryHandler(ModelLayerFactory modelLayerFactory) {
		super(modelLayerFactory);
	}

	public LinkableTaxonomySearchResponse getNodes(GetChildrenContext ctx) {
		boolean childrenOfTaxonomyRecords = ctx.record == null || ctx.isConceptOfNavigatedTaxonomy(ctx.record);
		List<TaxonomySearchRecord> returnedRecords = new ArrayList<>();
		if (childrenOfTaxonomyRecords) {
			returnedRecords.addAll(findVisibleChildrenOfTaxonomyRecord(ctx));
		}
		//We try to load rows+1 records

		List<MetadataSchemaType> classifiedSchemaTypes = ctx.getClassifiedSchemaTypes();
		if (ctx.record != null) {
			for (int i = 0; returnedRecords.size() <= ctx.getOptions().getEndRow() && i < classifiedSchemaTypes.size(); i++) {
				MetadataSchemaType schemaType = classifiedSchemaTypes.get(i);
				Metadata refMetadata = ctx.getTaxonomyClassificationMetadata(schemaType);
				if (refMetadata != null) {

					//TODO Retirer cette passe de l'ours
					if (ctx.isShowingAllVisibleOrSelectingSchemaType(schemaType)
						|| (ctx.isShowingAllVisibleOrSelectingSchemaType("document") && schemaType.getCode().equals("folder"))) {

						int rows = ctx.getOptions().getEndRow() - returnedRecords.size() + 1;
						if (shouldUseCacheToFindChildrensOfType(ctx, schemaType, refMetadata)) {
							returnedRecords.addAll(findClassifiedChildrenUsingCache(ctx, schemaType, refMetadata, rows));
						} else {
							returnedRecords.addAll(findChildrenUsingSolr(ctx, schemaType, refMetadata, rows));
						}
					}
				}

			}
		}

		int numFound = returnedRecords.size();

		//Removing first records
		for (int i = 0; i < ctx.getOptions().getStartRow() & !returnedRecords.isEmpty(); i++) {
			returnedRecords.remove(0);
		}


		while (returnedRecords.size() > ctx.getOptions().getRows()) {
			returnedRecords.remove(returnedRecords.size() - 1);
		}

		return new LinkableTaxonomySearchResponse(numFound, null, returnedRecords);
	}

	private List<TaxonomySearchRecord> findChildrenUsingSolr(
			GetChildrenContext ctx, MetadataSchemaType classifiedType, Metadata classificationMetadata, int rows) {
		LogicalSearchQuery query = findClassifiedChildrenQuery(ctx, classifiedType, classificationMetadata);
		query.setQueryExecutionMethod(QueryExecutionMethod.USE_SOLR);
		query.setNumberOfRows(rows);

		List<Record> records = searchServices.search(query);

		return toTaxonomySearchRecords(ctx, records);
	}

	private List<TaxonomySearchRecord> findClassifiedChildrenUsingCache(
			GetChildrenContext ctx, MetadataSchemaType classifiedType, Metadata classificationMetadata, int rows) {


		LogicalSearchQuery query = findClassifiedChildrenQuery(ctx, classifiedType, classificationMetadata);
		query.setQueryExecutionMethod(QueryExecutionMethod.ENSURE_INDEXED_METADATA_USED);
		query.setNumberOfRows(rows);

		List<Record> records = searchServices.search(query);

		return toTaxonomySearchRecords(ctx, records);
	}

	@NotNull
	private List<TaxonomySearchRecord> toTaxonomySearchRecords(GetChildrenContext ctx, List<Record> records) {
		MetadataSchemaTypes types = metadataSchemasManager.getSchemaTypes(ctx.getCollection());
		return records.stream().map(r -> {
			boolean linkable = false;

			if (ctx.options.isForceLinkableCalculation() || ( ctx.getForSelectionOfSchemaType() != null
				&& ctx.getForSelectionOfSchemaType().getCode().equals(r.getTypeCode()))) {

				linkable = LangUtils.isTrueOrNull(r.get(LINKABLE)) && LangUtils.isFalseOrNull(r.get(LOGICALLY_DELETED_STATUS)) && ctx.hasRequiredAccessOn(r);
			}
			return new TaxonomySearchRecord(r, linkable, !types.getClassifiedSchemaTypesIn(r.getTypeCode()).isEmpty());
		}).collect(toList());
	}

	@NotNull
	private LogicalSearchQuery findClassifiedChildrenQuery(GetChildrenContext ctx, MetadataSchemaType classifiedType,
														   Metadata classificationMetadata) {
		LogicalSearchCondition condition = from(classifiedType).where(classificationMetadata).isEqualTo(ctx.record);

		if (ctx.forSelectionOfSchemaType == null && ctx.isHiddenInvisibleInTree()) {
			condition = condition.andWhere(VISIBLE_IN_TREES).isTrueOrNull();
		}

		if (!ctx.getOptions().isShowInvisibleRecords(ctx.forSelectionOfSchemaType != null)) {
			condition = condition.andWhere(VISIBLE_IN_TREES).isTrueOrNull();
		}

		LogicalSearchQuery query = new LogicalSearchQuery(condition);
		query.setReturnedMetadatas(ReturnedMetadatasFilter.onlySummaryFields());
		query.filteredByStatus(ctx.getOptions().getIncludeStatus());
		query.sortAsc(classifiedType.getMainSortMetadata());
		if (ctx.getOptions().getRequiredAccess() != null) {
			query.filteredWithUserHierarchy(ctx.user, ctx.getOptions().getRequiredAccess(),
					null, !ctx.isHiddenInvisibleInTree());
		}
		return query;
	}

	private boolean shouldUseCacheToFindChildrensOfType(GetChildrenContext ctx, MetadataSchemaType classifiedType,
														Metadata classificationMetadata) {
		if (classificationMetadata != null && classificationMetadata.isCacheIndex()
			&& classifiedType.getCacheType().hasPermanentCache()
			&& modelLayerFactory.getRecordsCaches().isCacheInitialized(classifiedType)) {

			int estimatedSize = caches.estimateMaxResultSizeUsingIndexedMetadata(
					classifiedType, classificationMetadata, ctx.record.getId());
			return estimatedSize < LIMIT_OF_RECORDS_IN_A_NODE_FOR_USING_CACHE;
		}
		return false;
	}


	private List<TaxonomySearchRecord> findVisibleChildrenOfTaxonomyRecord(GetChildrenContext ctx) {

		boolean selectingAConcept =
				ctx.getForSelectionOfSchemaType() != null && ctx.getTaxonomy().getSchemaTypes().contains(ctx.getForSelectionOfSchemaType().getCode());
		boolean calculateLinkability = ctx.getOptions().isLinkableFlagCalculated();

		LogicalSearchQuery query = queryReturningChildrenConcepts(ctx.getFromType(), ctx.getRecord());
		Iterator<Record> conceptsIterator = searchServices.search(query).iterator();

		List<TaxonomySearchRecord> returnedConcepts = new ArrayList<>();
		while (returnedConcepts.size() < ctx.getOptions().getEndRow() + 1 && conceptsIterator.hasNext()) {
			Record concept = conceptsIterator.next();

			boolean showEvenIfNoChild = false;
			if (ctx.options.isAlwaysReturnTaxonomyConceptsWithReadAccessOrLinkable()
				|| ctx.isSelectingAConcept()) {
				showEvenIfNoChild = hasAccessToConceptNode(ctx, concept);
			}

			boolean linkable = false;
			if ((selectingAConcept && calculateLinkability) || ctx.getOptions().isForceLinkableCalculation()) {
				linkable = isLinkable(concept, ctx.taxonomy, ctx.options) && ctx.hasRequiredAccessOn(concept);
			}

			boolean hasChildren = true;
			boolean calculateHasClassifiedChildren;
			if (ctx.getOptions().getHasChildrenFlagCalculated() == NEVER) {
				calculateHasClassifiedChildren = !(linkable || (showEvenIfNoChild && !selectingAConcept));

			} else if (ctx.isSelectingAConcept()) {
				//ctx.options.isAlwaysReturnTaxonomyConceptsWithReadAccessOrLinkable() && showEvenIfNoChild &&
				calculateHasClassifiedChildren = false;
				hasChildren = hasAccessToChildConceptInHierarchy(ctx, concept);

			} else {
				calculateHasClassifiedChildren = true;
			}


			if (calculateHasClassifiedChildren) {
				hasChildren = ctx.hasUserAccessToSomethingInConcept(concept)
							  || (ctx.options.isAlwaysReturnTaxonomyConceptsWithReadAccessOrLinkable() && hasAccessToChildConceptInHierarchy(ctx, concept));
			}


			if (hasChildren || linkable || (showEvenIfNoChild && !selectingAConcept)) {
				returnedConcepts.add(new TaxonomySearchRecord(concept, linkable, hasChildren));
			}
		}

		return returnedConcepts;
	}

	@NotNull
	private LogicalSearchQuery queryReturningChildrenConcepts(MetadataSchemaType conceptSchemaType,
															  Record nullableConcept) {
		List<Metadata> allParentReferencesTo = conceptSchemaType.getAllParentReferencesTo(conceptSchemaType.getCode());
		if (allParentReferencesTo.isEmpty()) {
			return LogicalSearchQuery.returningNoResults();
		}
		Metadata parentMetadata = allParentReferencesTo.get(0);

		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setQueryExecutionMethod(QueryExecutionMethod.USE_CACHE);
		query.sortAsc(conceptSchemaType.getMainSortMetadata());
		if (nullableConcept == null) {
			query.setCondition(from(conceptSchemaType).where(parentMetadata).isNull());
		} else {
			query.setCondition(from(conceptSchemaType).where(parentMetadata).isEqualTo(nullableConcept));
		}
		return query;
	}

	private boolean hasAccessToConceptNode(GetChildrenContext ctx, Record concept) {
		return !ctx.isPrincipalTaxonomy() || ctx.hasRequiredAccessOn(concept);
	}

	private boolean hasAccessToChildConceptInHierarchy(GetChildrenContext ctx, Record concept) {

		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setQueryExecutionMethod(QueryExecutionMethod.USE_CACHE);
		//TODO Use better cache query
		query.setCondition(from(ctx.getFromType()).where(Schemas.PATH_PARTS).isEqualTo(concept.getId()));


		if (ctx.isSelectingAConcept() && !ctx.getOptions().isAlwaysReturnTaxonomyConceptsWithReadAccessOrLinkable()) {
			query.setCondition(query.getCondition().andWhere(LINKABLE).isTrueOrNull());
		}

		if (ctx.isPrincipalTaxonomy()) {
			query.filteredWithUserRead(ctx.getUser(), ctx.getOptions().getRequiredAccess());
		}

		if (ctx.getOptions().getFilter() != null && ctx.getOptions().getFilter().getLinkableConceptsFilter() != null) {
			boolean hasLinkableConcept = false;
			for (Record childConcept : searchServices.search(query)) {
				hasLinkableConcept |= isLinkable(childConcept, ctx.getTaxonomy(), ctx.getOptions());
			}
			return hasLinkableConcept;

		} else {
			return searchServices.hasResults(query);
		}

	}

	public boolean isLinkable(final Record record, final Taxonomy taxonomy, TaxonomiesSearchOptions options) {

		if (options.isAlwaysReturnTaxonomyConceptsWithReadAccessOrLinkable()) {
			return true;
		}

		boolean linkable = LangUtils.isTrueOrNull(record.<Boolean>get(LINKABLE))
				&& LangUtils.isFalseOrNull(record.<Boolean>get(LOGICALLY_DELETED_STATUS));
		if (linkable && options.getFilter() != null && options.getFilter().getLinkableConceptsFilter() != null) {
			linkable = options.getFilter().getLinkableConceptsFilter().isLinkable(new LinkableConceptFilterParams(record, taxonomy));
		}

		return linkable;
	}


	/*
	 * Entering Garbage Area...
	 *
	 * Following methods are for interface compliance
	 */
	public List<TaxonomySearchRecord> getVisibleChildConcept(GetChildrenContext ctx) {
		return getVisibleChildrenRecords(ctx).getRecords();
	}

	public boolean findNonTaxonomyRecordsInStructure(Record record, TaxonomiesSearchOptions options) {
		throw new UnsupportedOperationException("Unsupported deprecated operation");
	}

	public LinkableTaxonomySearchResponse getVisibleChildrenRecords(GetChildrenContext ctx) {
		return getNodes(ctx);

	}

	public LinkableTaxonomySearchResponse getVisibleRootConceptResponse(GetChildrenContext ctx) {
		return getNodes(ctx);
	}

	public LinkableTaxonomySearchResponse getLinkableConceptsForSelectionOfAPrincipalTaxonomyConceptBasedOnAuthorizations
			(
					GetChildrenContext ctx) {
		return getNodes(ctx);
	}

	public LinkableTaxonomySearchResponse getLinkableConceptsForSelectionOfARecordUsingNonPrincipalTaxonomy(
			GetChildrenContext ctx) {
		return getNodes(ctx);
	}


}
