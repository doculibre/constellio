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

import static com.constellio.model.entities.schemas.Schemas.VISIBLE_IN_TREES;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.taxonomies.TaxonomiesSearchOptions.HasChildrenFlagCalculated.NEVER;
import static java.util.stream.Collectors.toList;

public class TaxonomiesSearchServicesRewrittenQueryHandler
		extends TaxonomiesSearchServicesBaseQueryHandler implements TaxonomiesSearchServicesQueryHandler {

	public TaxonomiesSearchServicesRewrittenQueryHandler(ModelLayerFactory modelLayerFactory) {
		super(modelLayerFactory);
	}

	public LinkableTaxonomySearchResponse getVisibleNodesResponse(GetChildrenContext ctx) {
		boolean childrenOfTaxonomyRecords = ctx.record == null || ctx.isConceptOfNavigatedTaxonomy(ctx.record);
		List<TaxonomySearchRecord> returnedRecords = new ArrayList<>();
		if (childrenOfTaxonomyRecords) {
			returnedRecords.addAll(findVisibleChildrenOfTaxonomyRecord(ctx));
		}
		//We try to load rows+1 records

		List<MetadataSchemaType> classifiedSchemaTypes = ctx.getClassifiedSchemaTypes();
		if (ctx.record != null) {
			for (int i = 0; returnedRecords.size() <= ctx.getOptions().getRows() && i < classifiedSchemaTypes.size(); i++) {
				MetadataSchemaType schemaType = classifiedSchemaTypes.get(i);
				Metadata refMetadata = ctx.getTaxonomyClassificationMetadata(schemaType);
				if (refMetadata != null) {

					//TODO Retirer cette passe de l'ours
					if (ctx.isShowingAllVisibleOrSelectingSchemaType(schemaType)
						|| (ctx.isShowingAllVisibleOrSelectingSchemaType("document") && schemaType.getCode().equals("folder"))) {

						int rows = ctx.getOptions().getRows() - returnedRecords.size() + 1;
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

			if (ctx.getForSelectionOfSchemaType() != null
				&& ctx.getForSelectionOfSchemaType().getCode().equals(r.getTypeCode())) {

				linkable = !Boolean.FALSE.equals(r.get(Schemas.LINKABLE)) && ctx.hasRequiredAccessOn(r);
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

		if (ctx.forSelectionOfSchemaType != null && !ctx.getOptions().isShowInvisibleRecordsInLinkingMode()) {
			condition = condition.andWhere(VISIBLE_IN_TREES).isTrueOrNull();
		}

		LogicalSearchQuery query = new LogicalSearchQuery(condition);
		query.setReturnedMetadatas(ReturnedMetadatasFilter.onlySummaryFields());
		query.filteredByStatus(ctx.getOptions().getIncludeStatus());
		query.sortAsc(Schemas.TITLE);
		if (ctx.getOptions().getRequiredAccess() != null) {
			query.filteredWithUserHierarchy(ctx.user, ctx.getOptions().getRequiredAccess(),
					null, !ctx.isHiddenInvisibleInTree());
		}
		return query;
	}

	private static int LIMIT_OF_RECORDS_IN_A_NODE_FOR_USING_CACHE = 2000;

	private boolean shouldUseCacheToFindChildrensOfType(GetChildrenContext ctx, MetadataSchemaType classifiedType,
														Metadata classificationMetadata) {
		if (classificationMetadata != null && classificationMetadata.isCacheIndex()) {
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
			if (selectingAConcept && calculateLinkability) {
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
				hasChildren = ctx.hasUserAccessToSomethingInConcept(concept);
			}


			if (hasChildren || linkable || (showEvenIfNoChild && !selectingAConcept)) {


				//				boolean linkable = false;
				//
				//				if (ctx.getForSelectionOfSchemaType() != null
				//					&& ctx.getForSelectionOfSchemaType().getCode().equals(concept.getTypeCode())) {
				//
				//
				//					if (ctx.getOptions().getFilter() != null && ctx.getOptions().getFilter().getLinkableConceptsFilter() != null) {
				//						linkable = ctx.getOptions().getFilter().getLinkableConceptsFilter().isLinkable(new LinkableConceptFilterParams() {
				//							@Override
				//							public Record getRecord() {
				//								return concept;
				//							}
				//
				//							@Override
				//							public Taxonomy getTaxonomy() {
				//								return ctx.taxonomy;
				//							}
				//						});
				//					} else {
				//						linkable = !Boolean.FALSE.equals(concept.get(Schemas.LINKABLE))
				//								   && ctx.hasRequiredAccessOn(concept);
				//					}
				//				}


				returnedConcepts.add(new TaxonomySearchRecord(concept, linkable, hasChildren));
			}
		}

		return returnedConcepts;
	}

	@NotNull
	private LogicalSearchQuery queryReturningChildrenConcepts(MetadataSchemaType conceptSchemaType,
															  Record nullableConcept) {
		Metadata parentMetadata = conceptSchemaType.getAllParentReferencesTo(conceptSchemaType.getCode()).get(0);

		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setQueryExecutionMethod(QueryExecutionMethod.USE_CACHE);
		query.sortAsc(Schemas.CODE).sortAsc(Schemas.TITLE);
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

		//LogicalSearchQuery query = queryReturningChildrenConcepts(ctx.getFromType(), concept);
		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setQueryExecutionMethod(QueryExecutionMethod.USE_CACHE);
		//TODO Use better cache query
		query.setCondition(from(ctx.getFromType()).where(Schemas.PATH_PARTS).isEqualTo(concept.getId()));


		if (ctx.isSelectingAConcept() && !ctx.getOptions().isAlwaysReturnTaxonomyConceptsWithReadAccessOrLinkable()) {
			query.setCondition(query.getCondition().andWhere(Schemas.LINKABLE).isTrueOrNull());
		}

		if (ctx.isPrincipalTaxonomy()) {
			query.filteredWithUser(ctx.getUser(), ctx.getOptions().getRequiredAccess());
		}

		if (ctx.getOptions().getFilter() != null && ctx.getOptions().getFilter().getLinkableConceptsFilter() != null) {
			boolean hasLinkableConcept = false;
			for (Record childConcept : searchServices.search(query)) {
				hasLinkableConcept |= LangUtils.isTrueOrNull(childConcept.<Boolean>get(Schemas.LINKABLE)) && ctx.getOptions().getFilter().getLinkableConceptsFilter().isLinkable(new LinkableConceptFilterParams() {
					@Override
					public Record getRecord() {
						return childConcept;
					}

					@Override
					public Taxonomy getTaxonomy() {
						return ctx.getTaxonomy();
					}
				});
			}
			return hasLinkableConcept;

		} else {
			return searchServices.hasResults(query);
		}

	}

	/*
	 * Entering Unsure Area...
	 *
	 * Following methods are to be determined if they are necessary
	 */

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


	/*
	 * Entering Garbage Area...
	 *
	 * Following methods are for interface compliance and old code to remove
	 */


	public List<TaxonomySearchRecord> getVisibleChildConcept(GetChildrenContext ctx) {
		return getVisibleChildrenRecords(ctx).getRecords();
	}

	public boolean findNonTaxonomyRecordsInStructure(Record record, TaxonomiesSearchOptions options) {
		throw new UnsupportedOperationException("Unsupported deprecated operation");
	}

	public LinkableTaxonomySearchResponse getVisibleChildrenRecords(GetChildrenContext ctx) {
		return getVisibleNodesResponse(ctx);

	}

	public LinkableTaxonomySearchResponse getVisibleRootConceptResponse(GetChildrenContext ctx) {
		return getVisibleNodesResponse(ctx);
	}


	public LinkableTaxonomySearchResponse getLinkableConceptsForSelectionOfAPrincipalTaxonomyConceptBasedOnAuthorizations
			(
					GetChildrenContext ctx) {
		return getVisibleNodesResponse(ctx);
		//		TaxonomiesSearchOptions options = new TaxonomiesSearchOptions(ctx.options);
		//
		//		List<Record> children = new ArrayList<>();
		//		for (Record record : caches.getCache(ctx.getCollection()).getAllValues(ctx.forSelectionOfSchemaType.getCode())) {
		//			if (LangUtils.isEqual(record.getParentId(), ctx.getRecord() == null ? null : ctx.getRecord().getId())) {
		//				children.add(record);
		//			}
		//		}
		//
		//		Collections.sort(children, new RecordCodeComparator(ctx.getTaxonomy().getSchemaTypes()));
		//
		//		List<TaxonomySearchRecord> resultVisible = new ArrayList<>();
		//		for (final Record child : children) {
		//
		//			Lazy<Boolean> hasVisibleChildren = new Lazy<Boolean>() {
		//				@Override
		//				protected Boolean load() {
		//
		//					TaxonomiesSearchOptions options = new TaxonomiesSearchOptions(ctx.getOptions());
		//					options.setHasChildrenFlagCalculated(HasChildrenFlagCalculated.NEVER);
		//					options.setRows(1);
		//					options.setStartRow(0);
		//
		//					GetChildrenContext ctxCopy = ctx.createCopyFor(child);
		//					ctxCopy.options = options;
		//
		//					return getLinkableConceptsForSelectionOfAPrincipalTaxonomyConceptBasedOnAuthorizations(ctxCopy).getNumFound() > 0;
		//				}
		//			};
		//
		//			boolean readAuthorizationsOnConcept = ctx.getUser() == null || ctx.getUser().hasRequiredAccess(options.getRequiredAccess()).on(child);
		//			boolean conceptIsLinkable = isTrueOrNull(child.get(Schemas.LINKABLE));
		//
		//			if (options.getFilter() != null && options.getFilter().getLinkableConceptsFilter() != null) {
		//				conceptIsLinkable = options.getFilter().getLinkableConceptsFilter().isLinkable(new LinkableConceptFilterParams() {
		//					@Override
		//					public Record getRecord() {
		//						return child;
		//					}
		//
		//					@Override
		//					public Taxonomy getTaxonomy() {
		//						return ctx.getTaxonomy();
		//					}
		//				});
		//			}
		//
		//			if ((readAuthorizationsOnConcept && conceptIsLinkable) || hasVisibleChildren.get()) {
		//				boolean returnedHasVisibleChildren = options.getHasChildrenFlagCalculated() == HasChildrenFlagCalculated.NEVER ? true : hasVisibleChildren.get();
		//
		//				resultVisible.add(new TaxonomySearchRecord(child, readAuthorizationsOnConcept && conceptIsLinkable,
		//						returnedHasVisibleChildren));
		//			}
		//		}
		//
		//		int from = ctx.getOptions().getStartRow();
		//		int to = ctx.getOptions().getEndRow();
		//		if (resultVisible.size() < to) {
		//			to = resultVisible.size();
		//		}
		//		return new LinkableTaxonomySearchResponse(resultVisible.size(), resultVisible.subList(from, to));

	}

	public LinkableTaxonomySearchResponse getLinkableConceptsForSelectionOfARecordUsingNonPrincipalTaxonomy(
			GetChildrenContext ctx) {

		return getVisibleNodesResponse(ctx);
		//		if (ctx.record != null) {
		//			return getVisibleChildrenRecords(ctx);
		//		} else {
		//
		//			LogicalSearchQuery mainQuery = conceptNodesTaxonomySearchServices.getRootConceptsQuery(
		//					ctx.getCollection(), ctx.taxonomy.getCode(), ctx.options);
		//
		//			mainQuery.filteredByStatus(ctx.options.getIncludeStatus())
		//					.setName("getRootConcepts")
		//					.setReturnedMetadatas(returnedMetadatasForRecordsIn(ctx));
		//
		//			ModelLayerCollectionExtensions collectionExtensions = extensions.forCollectionOf(ctx.taxonomy);
		//			Metadata[] sortMetadatas = collectionExtensions.getSortMetadatas(ctx.taxonomy);
		//			if (sortMetadatas != null) {
		//				for (Metadata sortMetadata : sortMetadatas) {
		//					mainQuery.sortAsc(sortMetadata);
		//				}
		//			}
		//
		//			List<TaxonomySearchRecord> visibleRecords = new ArrayList<>();
		//			int lastIteratedRecordIndex = 0;
		//			Iterator<List<Record>> iterator;
		//			iterator = searchServices.cachedRecordsIteratorKeepingOrder(mainQuery.setStartRow(0), 25).inBatches();
		//
		//
		//			Taxonomy principalTaxonomy = taxonomiesManager.getPrincipalTaxonomy(ctx.getCollection());
		//			while (visibleRecords.size() < ctx.options.getEndRow() + 1 && iterator.hasNext()) {
		//
		//				List<Record> batch = iterator.next();
		//				boolean navigatingUsingPrincipalTaxonomy = principalTaxonomy != null
		//														   && principalTaxonomy.getCode().equals(ctx.taxonomy.getCode());
		//
		//				List<String> schemaTypes = new ArrayList<>();
		//				schemaTypes.add(ctx.forSelectionOfSchemaType.getCode());
		//
		//				if (ctx.options.isAlwaysReturnTaxonomyConceptsWithReadAccessOrLinkable() && navigatingUsingPrincipalTaxonomy) {
		//					schemaTypes.addAll(ctx.taxonomy.getSchemaTypes());
		//				}
		//				LogicalSearchCondition condition = from(schemaTypes, ctx.getCollection()).returnAll();
		//
		//				if (!ctx.options.isShowInvisibleRecordsInLinkingMode()) {
		//					condition = condition.andWhere(VISIBLE_IN_TREES).isTrueOrNull();
		//				}
		//				LogicalSearchQuery facetQuery = newQueryForFacets(condition, ctx.user, ctx.options);
		//				HasChildrenQueryHandler hasChildrenQueryHandler = newHasChildrenQueryHandler(ctx, facetQuery);
		//				for (Record child : batch) {
		//					hasChildrenQueryHandler.addRecordToCheck(child);
		//				}
		//
		//				for (Record child : batch) {
		//					if (visibleRecords.size() < ctx.options.getEndRow()) {
		//						lastIteratedRecordIndex++;
		//					}
		//					String schemaType = getSchemaTypeCode(child.getSchemaCode());
		//
		//					boolean hasVisibleChildren = hasChildrenQueryHandler.hasChildren(child);
		//					Taxonomy taxonomy = taxonomiesManager.getTaxonomyOf(child);
		//					boolean visibleEvenIfEmpty = false;
		//					if (taxonomy != null && ctx.options.isAlwaysReturnTaxonomyConceptsWithReadAccessOrLinkable()) {
		//						if (principalTaxonomy != null && taxonomy.getCode().equals(principalTaxonomy.getCode())) {
		//							visibleEvenIfEmpty = ctx.hasRequiredAccessOn(child);
		//						} else {
		//							visibleEvenIfEmpty = true;
		//						}
		//					}
		//
		//					if (schemaType.equals(ctx.forSelectionOfSchemaType.getCode())) {
		//						boolean hasAccess = ctx.user.hasRequiredAccess(ctx.options.getRequiredAccess()).on(child);
		//						if (hasAccess || hasVisibleChildren || visibleEvenIfEmpty) {
		//							visibleRecords.add(new TaxonomySearchRecord(child, hasAccess, hasVisibleChildren));
		//						}
		//
		//					} else if (hasVisibleChildren || visibleEvenIfEmpty) {
		//						visibleRecords.add(new TaxonomySearchRecord(child, false, hasVisibleChildren));
		//					}
		//
		//				}
		//			}
		//
		//			int numFound = visibleRecords.size();
		//			int toIndex = Math.min(visibleRecords.size(), ctx.options.getEndRow());
		//			List<TaxonomySearchRecord> returnedRecords = visibleRecords.subList(ctx.options.getStartRow(), toIndex);
		//
		//			boolean finishedConceptsIteration = !iterator.hasNext();
		//			FastContinueInfos infos = new FastContinueInfos(finishedConceptsIteration, lastIteratedRecordIndex,
		//					new ArrayList<String>());
		//
		//			return new LinkableTaxonomySearchResponse(numFound, infos, returnedRecords);
		//		}
	}


	//	protected String facetQueryFor(Record record) {
	//		StringBuilder stringBuilder = new StringBuilder();
	//		stringBuilder.append("pathParts_ss:");
	//		stringBuilder.append(record.getId());
	//		return stringBuilder.toString();
	//	}


	//
	//	protected SPEQueryResponse getNonTaxonomyRecords(final GetChildrenContext ctx, int start, int rows) {
	//
	//		LinkedList<Record> records = new LinkedList<>();
	//		for (MetadataSchemaType schemaType : ctx.getClassifiedSchemaTypes()) {
	//
	//
	//			List<Metadata> taxonomyReferences = schemaType.getAllReferencesToTaxonomySchemas(asList(ctx.taxonomy));
	//
	//			LogicalSearchCondition condition = from(schemaType).whereAny(taxonomyReferences).isEqualTo(ctx.record);
	//			if (!ctx.options.isShowInvisibleRecordsInLinkingMode()) {
	//				condition = condition.andWhere(VISIBLE_IN_TREES).isTrueOrNull();
	//			}
	//			LogicalSearchQuery query = new LogicalSearchQuery(condition);
	//			query.filteredWith(new HierarchyUserFilter(ctx.user, ctx.options.getRequiredAccess(),
	//					ctx.forSelectionOfSchemaType, ctx.options.isShowInvisibleRecordsInLinkingMode()));
	//			query.setName("TaxonomiesSearchServices:getNonTaxonomyRecords(" + ctx.username() + ", " + ctx.record.getId() + ")");
	//			query.sortAsc(Schemas.TITLE);
	//			query.setQueryExecutionMethod(QueryExecutionMethod.ENSURE_INDEXED_METADATA_USED);
	//			query.setReturnedMetadatas(ReturnedMetadatasFilter.onlySummaryFields());
	//			records.addAll(searchServices.search(query));
	//
	//			//			if (ctx.forSelectionOfSchemaType == null
	//			//				|| ctx.forSelectionOfSchemaType.getAllReferencesToTaxonomySchemas(asList(ctx.taxonomy)).isEmpty()) {
	//			//				condition = fromAllSchemasInCollectionOf(ctx.record, DataStore.RECORDS)
	//			//						.where(directChildOf(ctx.record)).andWhere(visibleInTrees)
	//			//						.andWhere(schemaTypeIsNotIn(ctx.taxonomy.getSchemaTypes()));
	//			//			} else {
	//			//				condition = from(ctx.forSelectionOfSchemaType).where(directChildOf(ctx.record));
	//			//
	//			//				if (!ctx.options.isShowInvisibleRecordsInLinkingMode()) {
	//			//					condition = condition.andWhere(VISIBLE_IN_TREES).isTrueOrNull();
	//			//				}
	//			//			}
	//			//			LogicalSearchQuery query = newQuery(condition, ctx.options)
	//			//					.setStartRow(realStart).setNumberOfRows(realRows);
	//
	//			//			query.filteredWith(new HierarchyUserFilter(ctx.user, ctx.options.getRequiredAccess(),
	//			//					ctx.forSelectionOfSchemaType, ctx.options.isShowInvisibleRecordsInLinkingMode()));
	//			//			query.setName("TaxonomiesSearchServices:getNonTaxonomyRecords(" + ctx.username() + ", " + ctx.record.getId() + ")");
	//			//
	//			//			//TODO : Use Solr if more than 500 records
	//			//			query.setQueryExecutionMethod(USE_CACHE);
	//		}
	//
	//		long numfound = records.size();
	//		for (int i = 0; i < start && !records.isEmpty(); i++) {
	//			records.removeFirst();
	//		}
	//
	//		while (records.size() > rows) {
	//			records.removeLast();
	//		}
	//
	//		return new SPEQueryResponse(records, numfound);
	//	}
	//
	//
	//	protected SPEQueryResponse queryFindingWhichRecordsHasChildren(GetChildrenContext context, int visibleConceptsSize,
	//																   List<Record> records) {
	//		SPEQueryResponse facetResponse = null;
	//
	//		if (context.options.getHasChildrenFlagCalculated() == HasChildrenFlagCalculated.ALWAYS) {
	//			LogicalSearchCondition queryCondition;
	//
	//			if (context.forSelectionOfSchemaType == null) {
	//				queryCondition = fromAllSchemasIn(context.getCollection())
	//						.where(recordInHierarchyOf(context.record))
	//						.andWhere(notDirectChildOf(context.record))
	//						.andWhere(visibleInTrees)
	//						.andWhere(schemaTypeIsNotIn(context.taxonomy.getSchemaTypes()));
	//			} else {
	//				queryCondition = from(context.forSelectionOfSchemaType)
	//						.where(recordInHierarchyOf(context.record))
	//						.andWhere(notDirectChildOf(context.record))
	//						.andWhere(Schemas.LINKABLE).isTrueOrNull();
	//
	//				if (!context.options.isShowInvisibleRecordsInLinkingMode()) {
	//					queryCondition = queryCondition.andWhere(visibleInTrees);
	//				}
	//
	//			}
	//
	//			LogicalSearchQuery facetQuery = context.newQueryWithUserFilter(queryCondition)
	//					.filteredByStatus(ACTIVES).setStartRow(0).setNumberOfRows(0);
	//
	//			int facetCounts = 0;
	//			for (int i = visibleConceptsSize;
	//				 i - visibleConceptsSize < records.size() && facetCounts < context.options.getRows(); i++) {
	//				int nonTaxonomyIndex = i - visibleConceptsSize;
	//				Record record = records.get(nonTaxonomyIndex);
	//				if (record != null) {
	//					facetQuery.addQueryFacet("hasChildren", facetQueryFor(record));
	//					facetCounts++;
	//				}
	//			}
	//
	//			if (facetCounts > 0) {
	//				facetQuery.setName(
	//						"TaxonomiesSearchServices:hasChildrenQuery(" + context.username() + ", " + context.record.getId() + ")");
	//				facetResponse = searchServices.query(facetQuery);
	//			}
	//		}
	//		return facetResponse;
	//	}


	//	protected LogicalSearchCondition findVisibleNonTaxonomyRecordsInStructure(Taxonomy taxonomy,
	//																			  boolean onlyVisibleInTrees) {
	//
	//		LogicalSearchCondition condition = fromAllSchemasIn(taxonomy.getCollection())
	//				.where(schemaTypeIsNotIn(taxonomy.getSchemaTypes()));
	//
	//		if (onlyVisibleInTrees) {
	//			condition = condition.andWhere(VISIBLE_IN_TREES).isTrueOrNull();
	//		}
	//
	//		return condition;
	//	}


	//	protected HasChildrenQueryHandler newHasChildrenQueryHandler(User user, String cacheMode,
	//																 LogicalSearchQuery facetQuery) {
	//		return new HasChildrenQueryHandler(user == null ? null : user.getUsername(), cacheMode, cache, searchServices, facetQuery);
	//	}
	//
	//
	//	protected ReturnedMetadatasFilter returnedMetadatasForRecordsIn(
	//			GetChildrenContext context) {
	//		return conceptNodesTaxonomySearchServices.returnedMetadatasForRecordsIn(context.getCollection(), context.options);
	//	}
	//
	//
	//	protected GetConceptRecordsWithVisibleRecordsResponse getConceptRecordsWithVisibleRecords(
	//			GetChildrenContext context) {
	//
	//		GetConceptRecordsWithVisibleRecordsResponse methodResponse = new GetConceptRecordsWithVisibleRecordsResponse();
	//		MetadataSchemaTypes types = metadataSchemasManager.getSchemaTypes(context.getCollection());
	//
	//		Iterator<List<Record>> iterator;
	//		int lastIteratedRecordIndex = 0;
	//		if (context.isConceptOfNavigatedTaxonomy(context.record)) {
	//			LogicalSearchQuery mainQuery = conceptNodesTaxonomySearchServices.childConceptsQuery(context.record, context.taxonomy, context.options, types);
	//
	//			iterator = searchServices.cachedRecordsIteratorKeepingOrder(mainQuery, context.options.getRows()).inBatches();
	//			lastIteratedRecordIndex = 0;
	//		} else {
	//			iterator = new ArrayList<List<Record>>().iterator();
	//		}
	//
	//		while (methodResponse.getRecords().size() < context.options.getEndRow() + 1 && iterator.hasNext()) {
	//
	//			List<Record> batch = iterator.next();
	//
	//			boolean calculateHasChildren;
	//
	//			calculateHasChildren = context.options.getHasChildrenFlagCalculated() != NEVER
	//								   || !context.options.isAlwaysReturnTaxonomyConceptsWithReadAccessOrLinkable();
	//			boolean calculateLinkability = context.options.isLinkableFlagCalculated();
	//
	//			LogicalSearchQuery facetQuery;
	//			if (context.isSelectingAConcept()) {
	//				LogicalSearchCondition condition = fromAllSchemasIn(context.taxonomy.getCollection())
	//						.where(PATH_PARTS).isEqualTo(context.record.getId())
	//						.andWhere(schemaTypeIsIn(context.taxonomy.getSchemaTypes()));
	//
	//				boolean selectingAConceptNoMatterTheLinkableStatus =
	//						context.isSelectingAConcept() && context.options.isAlwaysReturnTaxonomyConceptsWithReadAccessOrLinkable();
	//
	//				if (!selectingAConceptNoMatterTheLinkableStatus) {
	//					condition = condition.andWhere(Schemas.LINKABLE).isTrueOrNull();
	//				}
	//
	//				facetQuery = newQueryForFacets(condition, null, context.options);
	//
	//			} else {
	//				LogicalSearchCondition condition = findVisibleNonTaxonomyRecordsInStructure(
	//						context, context.isHiddenInvisibleInTree());
	//
	//				facetQuery = newQueryForFacets(condition, context);
	//			}
	//
	//			boolean[] hasAccess = new boolean[batch.size()];
	//
	//			HasChildrenQueryHandler hasChildrenQueryHandler = newHasChildrenQueryHandler(context, facetQuery);
	//			for (int i = 0; i < batch.size(); i++) {
	//				Record child = batch.get(i);
	//
	//				hasAccess[i] = context.isNonSecurableTaxonomyRecord(child) || context.hasRequiredAccessOn(child);
	//
	//				if (calculateHasChildren || !hasAccess[i]) {
	//					hasChildrenQueryHandler.addRecordToCheck(child);
	//				}
	//			}
	//
	//			for (int i = 0; i < batch.size(); i++) {
	//				Record child = batch.get(i);
	//				boolean hasChildren = true;
	//				if (calculateHasChildren || !hasAccess[i]) {
	//					hasChildren = hasChildrenQueryHandler.hasChildren(child);
	//					if (hasChildren
	//						&& context.options.getFilter() != null
	//						&& context.options.getFilter().getLinkableConceptsFilter() != null
	//						&& !context.principalTaxonomy
	//						&& context.isSelectingAConcept()
	//						&& context.taxonomy.getSchemaTypes().size() == 1) {
	//						hasChildren = hasLinkableConceptInHierarchy(child, context.taxonomy, context.options);
	//					}
	//				}
	//				boolean linkable;
	//				if (context.isSelectingAConcept() && calculateLinkability) {
	//					linkable = isLinkable(child, context.taxonomy, context.options);
	//				} else {
	//					linkable = NOT_LINKABLE;
	//				}
	//
	//				if (hasChildren || linkable) {
	//					if (methodResponse.getRecords().size() < context.options.getEndRow()) {
	//						lastIteratedRecordIndex++;
	//					}
	//					methodResponse.getRecords().add(new TaxonomySearchRecord(child, linkable, hasChildren));
	//
	//				} else if (context.options.isAlwaysReturnTaxonomyConceptsWithReadAccessOrLinkable()) {
	//					if (!taxonomiesManager.isTypeInPrincipalTaxonomy(context.getCollection(), child.getTypeCode())
	//						|| context.hasRequiredAccessOn(child)) {
	//						if (methodResponse.getRecords().size() < context.options.getEndRow()) {
	//							lastIteratedRecordIndex++;
	//						}
	//						methodResponse.getRecords().add(new TaxonomySearchRecord(child, linkable, false));
	//					}
	//				}
	//			}
	//		}
	//
	//		if (methodResponse.getRecords().size() > context.options.getEndRow()) {
	//			methodResponse.setFinishedIteratingOverRecords(false);
	//			methodResponse.getRecords().remove(methodResponse.getRecords().size() - 1);
	//		} else {
	//			methodResponse.setFinishedIteratingOverRecords(true);
	//		}
	//
	//		methodResponse.setContinueAtPosition(lastIteratedRecordIndex);
	//		return methodResponse;
	//
	//	}

	//
	//	protected boolean hasLinkableConceptInHierarchy(final Record concept, final Taxonomy taxonomy,
	//													TaxonomiesSearchOptions options) {
	//		MetadataSchemaType schemaType = metadataSchemasManager.getSchemaTypes(concept.getCollection())
	//				.getSchemaType(taxonomy.getSchemaTypes().get(0));
	//		List<Record> records = searchServices.getAllRecords(schemaType);
	//		for (final Record record : records) {
	//			if (record.getList(Schemas.PATH_PARTS).contains(concept.getId())) {
	//				boolean linkableFlag = LangUtils.isTrueOrNull(record.get(Schemas.LINKABLE));
	//				boolean linkableUsingFilter = options.getFilter().getLinkableConceptsFilter()
	//						.isLinkable(new LinkableConceptFilterParams() {
	//							@Override
	//							public Record getRecord() {
	//								return record;
	//							}
	//
	//							@Override
	//							public Taxonomy getTaxonomy() {
	//								return taxonomy;
	//							}
	//						});
	//
	//				if (linkableFlag && linkableUsingFilter) {
	//					return true;
	//				}
	//			}
	//		}
	//		return false;
	//	}
	//
	//	protected HasChildrenQueryHandler newHasChildrenQueryHandler(GetChildrenContext context,
	//																 LogicalSearchQuery facetQuery) {
	//		return new HasChildrenQueryHandler(context.username(), context.getCacheMode(), cache, searchServices, facetQuery);
	//	}
	//	protected LogicalSearchQuery newQueryForFacets(LogicalSearchCondition condition, GetChildrenContext context) {
	//		return newQueryForFacets(condition, context.user, context.options);
	//	}
	//
	//	protected LogicalSearchQuery newQueryForFacets(LogicalSearchCondition condition, User user,
	//												   TaxonomiesSearchOptions options) {
	//		LogicalSearchQuery query = new LogicalSearchQuery(condition)
	//				.filteredByStatus(options.getIncludeStatus())
	//				.setNumberOfRows(0)
	//				.setReturnedMetadatas(ReturnedMetadatasFilter.idVersionSchema());
	//
	//		if (user != null) {
	//			query.filteredWithUser(user, options.getRequiredAccess());
	//		}
	//		return query;
	//	}

	//
	//	protected LogicalSearchCondition findVisibleNonTaxonomyRecordsInStructure(GetChildrenContext context,
	//																			  boolean onlyVisibleInTrees) {
	//		LogicalSearchCondition condition = fromAllSchemasIn(context.taxonomy.getCollection())
	//				.where(PATH_PARTS).isEqualTo(context.record.getId())
	//				.andWhere(schemaTypeIsNotIn(context.taxonomy.getSchemaTypes()));
	//
	//		if (onlyVisibleInTrees) {
	//			condition = condition.andWhere(VISIBLE_IN_TREES).isTrueOrNull();
	//		}
	//
	//		return condition;
	//	}

	//	protected LogicalSearchQuery newQuery(LogicalSearchCondition condition, TaxonomiesSearchOptions options) {
	//		return new LogicalSearchQuery(condition)
	//				.filteredByStatus(options.getIncludeStatus())
	//				.setStartRow(options.getStartRow())
	//				.setNumberOfRows(options.getRows())
	//				.setReturnedMetadatas(
	//						conceptNodesTaxonomySearchServices.returnedMetadatasForRecordsIn(condition.getCollection(), options))
	//				.sortAsc(Schemas.CODE).sortAsc(Schemas.TITLE);
	//	}

}
