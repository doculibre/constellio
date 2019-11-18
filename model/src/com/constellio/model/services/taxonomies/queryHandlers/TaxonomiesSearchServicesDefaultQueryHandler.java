package com.constellio.model.services.taxonomies.queryHandlers;

import com.constellio.data.dao.services.records.DataStore;
import com.constellio.data.utils.LangUtils;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.extensions.ModelLayerCollectionExtensions;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.utils.RecordCodeComparator;
import com.constellio.model.services.search.MoreLikeThisRecord;
import com.constellio.model.services.search.SPEQueryResponse;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery.HierarchyUserFilter;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.taxonomies.FastContinueInfos;
import com.constellio.model.services.taxonomies.HasChildrenQueryHandler;
import com.constellio.model.services.taxonomies.LinkableConceptFilter.LinkableConceptFilterParams;
import com.constellio.model.services.taxonomies.LinkableTaxonomySearchResponse;
import com.constellio.model.services.taxonomies.TaxonomiesSearchOptions;
import com.constellio.model.services.taxonomies.TaxonomiesSearchOptions.HasChildrenFlagCalculated;
import com.constellio.model.services.taxonomies.TaxonomySearchRecord;
import com.constellio.model.utils.Lazy;

import java.util.ArrayList;
import java.util.Collections;
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
import static com.constellio.model.services.search.query.logical.QueryExecutionMethod.USE_SOLR;
import static com.constellio.model.services.search.query.logical.valueCondition.ConditionTemplateFactory.schemaTypeIsIn;
import static com.constellio.model.services.search.query.logical.valueCondition.ConditionTemplateFactory.schemaTypeIsNotIn;
import static com.constellio.model.services.taxonomies.ConceptNodesTaxonomySearchServices.directChildOf;
import static com.constellio.model.services.taxonomies.ConceptNodesTaxonomySearchServices.fromTypeIn;
import static com.constellio.model.services.taxonomies.ConceptNodesTaxonomySearchServices.notDirectChildOf;
import static com.constellio.model.services.taxonomies.ConceptNodesTaxonomySearchServices.recordInHierarchyOf;
import static com.constellio.model.services.taxonomies.ConceptNodesTaxonomySearchServices.visibleInTrees;
import static com.constellio.model.services.taxonomies.TaxonomiesSearchOptions.HasChildrenFlagCalculated.NEVER;
import static java.util.Arrays.asList;

public class TaxonomiesSearchServicesDefaultQueryHandler
		extends TaxonomiesSearchServicesBaseQueryHandler implements TaxonomiesSearchServicesQueryHandler {

	public TaxonomiesSearchServicesDefaultQueryHandler(ModelLayerFactory modelLayerFactory) {
		super(modelLayerFactory);
	}

	public boolean findNonTaxonomyRecordsInStructure(Record record, TaxonomiesSearchOptions options) {
		GetChildrenContext context = new GetChildrenContext(User.GOD, record, options, null, modelLayerFactory);
		LogicalSearchCondition condition = findVisibleNonTaxonomyRecordsInStructure(context, false);
		return searchServices.hasResults(new LogicalSearchQuery(condition).filteredByStatus(options.getIncludeStatus()));
	}

	public LinkableTaxonomySearchResponse getLinkableConceptsForSelectionOfAPrincipalTaxonomyConceptBasedOnAuthorizations(
			GetChildrenContext ctx) {
		TaxonomiesSearchOptions options = new TaxonomiesSearchOptions(ctx.options);

		List<Record> children = new ArrayList<>();
		for (Record record : caches.getCache(ctx.getCollection()).getAllValues(ctx.forSelectionOfSchemaType.getCode())) {
			if (LangUtils.isEqual(record.getParentId(), ctx.getRecord() == null ? null : ctx.getRecord().getId())) {
				children.add(record);
			}
		}

		Collections.sort(children, new RecordCodeComparator(ctx.getTaxonomy().getSchemaTypes()));

		List<TaxonomySearchRecord> resultVisible = new ArrayList<>();
		for (final Record child : children) {

			Lazy<Boolean> hasVisibleChildren = new Lazy<Boolean>() {
				@Override
				protected Boolean load() {

					TaxonomiesSearchOptions options = new TaxonomiesSearchOptions(ctx.getOptions());
					options.setHasChildrenFlagCalculated(HasChildrenFlagCalculated.NEVER);
					options.setRows(1);
					options.setStartRow(0);

					GetChildrenContext ctxCopy = ctx.createCopyFor(child);
					ctxCopy.options = options;

					return getLinkableConceptsForSelectionOfAPrincipalTaxonomyConceptBasedOnAuthorizations(ctxCopy).getNumFound() > 0;
				}
			};

			boolean readAuthorizationsOnConcept = ctx.getUser() == null || ctx.getUser().hasRequiredAccess(options.getRequiredAccess()).on(child);
			boolean conceptIsLinkable = isTrueOrNull(child.get(Schemas.LINKABLE));

			if (options.getFilter() != null && options.getFilter().getLinkableConceptsFilter() != null) {
				conceptIsLinkable = options.getFilter().getLinkableConceptsFilter().isLinkable(new LinkableConceptFilterParams(child, ctx.getTaxonomy()));
			}

			if ((readAuthorizationsOnConcept && conceptIsLinkable) || hasVisibleChildren.get()) {
				boolean returnedHasVisibleChildren = options.getHasChildrenFlagCalculated() == HasChildrenFlagCalculated.NEVER ? true : hasVisibleChildren.get();

				resultVisible.add(new TaxonomySearchRecord(child, readAuthorizationsOnConcept && conceptIsLinkable,
						returnedHasVisibleChildren));
			}
		}

		int from = ctx.getOptions().getStartRow();
		int to = ctx.getOptions().getEndRow();
		if (resultVisible.size() < to) {
			to = resultVisible.size();
		}
		return new LinkableTaxonomySearchResponse(resultVisible.size(), resultVisible.subList(from, to));

	}

	public LinkableTaxonomySearchResponse getLinkableConceptsForSelectionOfARecordUsingNonPrincipalTaxonomy(
			GetChildrenContext ctx) {

		if (ctx.record != null) {
			return getVisibleChildrenRecords(ctx);
		} else {

			LogicalSearchQuery mainQuery = conceptNodesTaxonomySearchServices.getRootConceptsQuery(
					ctx.getCollection(), ctx.taxonomy.getCode(), ctx.options);

			mainQuery.filteredByStatus(ctx.options.getIncludeStatus())
					.setName("getRootConcepts")
					.setReturnedMetadatas(returnedMetadatasForRecordsIn(ctx));

			ModelLayerCollectionExtensions collectionExtensions = extensions.forCollectionOf(ctx.taxonomy);
			Metadata[] sortMetadatas = collectionExtensions.getSortMetadatas(ctx.taxonomy);
			if (sortMetadatas != null) {
				for (Metadata sortMetadata : sortMetadatas) {
					mainQuery.sortAsc(sortMetadata);
				}
			}

			List<TaxonomySearchRecord> visibleRecords = new ArrayList<>();
			int lastIteratedRecordIndex = 0;
			Iterator<List<Record>> iterator;
			iterator = searchServices.cachedRecordsIteratorKeepingOrder(mainQuery.setStartRow(0), 25).inBatches();


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


	public List<TaxonomySearchRecord> getVisibleChildConcept(GetChildrenContext ctx) {
		return getVisibleChildrenRecords(ctx).getRecords();
	}


	public LinkableTaxonomySearchResponse getVisibleChildrenRecords(GetChildrenContext ctx) {

		GetConceptRecordsWithVisibleRecordsResponse conceptsResponse = getConceptRecordsWithVisibleRecords(ctx);

		List<Record> records = new ArrayList<>();
		int realRecordsStart = 0;
		SPEQueryResponse nonTaxonomyRecordsResponse = null;
		if (ctx.isSelectingAConcept()) {
			nonTaxonomyRecordsResponse = new SPEQueryResponse(new ArrayList<Record>(), new ArrayList<MoreLikeThisRecord>());
		} else {
			int realRecordsRows;
			realRecordsStart = 0;
			realRecordsRows = ctx.options.getStartRow() + ctx.options.getRows() - conceptsResponse.getRecords().size();

			List<Record> nonNullRecords = new ArrayList<>();
			realRecordsRows = Math.max(0, realRecordsRows);
			nonTaxonomyRecordsResponse = getNonTaxonomyRecords(ctx, realRecordsStart, realRecordsRows);
			nonNullRecords.addAll(nonTaxonomyRecordsResponse.getRecords());

			Collections.sort(nonNullRecords, new RecordCodeComparator(ctx.taxonomy.getSchemaTypes()));
			records.addAll(nonNullRecords);

		}

		return regroupChildren(ctx, conceptsResponse, nonTaxonomyRecordsResponse, records, realRecordsStart);

	}


	protected LinkableTaxonomySearchResponse regroupChildren(
			GetChildrenContext ctx, GetConceptRecordsWithVisibleRecordsResponse conceptsResponse,
			SPEQueryResponse nonTaxonomyRecordsResponse, List<Record> records,
			int recordsStartIndex) {
		List<TaxonomySearchRecord> concepts = conceptsResponse.getRecords();
		Set<String> typesParentOfOtherTypes = metadataSchemasManager.getSchemaTypes(ctx.getCollection())
				.getTypeParentOfOtherTypes();
		List<TaxonomySearchRecord> returnedRecords = new ArrayList<>();
		SPEQueryResponse facetResponse = queryFindingWhichRecordsHasChildren(ctx, concepts.size(), records);

		List<String> placedChildrenWithoutAccessToIncludeRecordIds = new ArrayList<>();
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
		if (conceptsResponse.isFinishedIteratingOverRecords()) {
			infos = new FastContinueInfos(true, recordsStartIndex, placedChildrenWithoutAccessToIncludeRecordIds);
		} else {
			infos = new FastContinueInfos(false, conceptsResponse.getContinueAtPosition(), new ArrayList<String>());
		}

		long numfound;
		numfound = nonTaxonomyRecordsResponse.getNumFound() + concepts.size();

		if (!conceptsResponse.isFinishedIteratingOverRecords()) {
			numfound++;
		}

		return new LinkableTaxonomySearchResponse(numfound, infos, returnedRecords);
	}

	public LinkableTaxonomySearchResponse getVisibleRootConceptResponse(GetChildrenContext ctx) {


		LogicalSearchQuery mainQuery = conceptNodesTaxonomySearchServices.getRootConceptsQuery(ctx.getTaxonomy().getCollection(), ctx.getTaxonomy().getCode(), ctx.getOptions());


		boolean selectingAConcept =
				ctx.getForSelectionOfSchemaType() != null && ctx.getTaxonomy().getSchemaTypes().contains(ctx.getForSelectionOfSchemaType().getCode());

		boolean principalConcept = taxonomiesManager.getPrincipalTaxonomy(ctx.getTaxonomy().getCollection()).hasSameCode(ctx.getTaxonomy());

		Iterator<List<Record>> iterator;
		List<TaxonomySearchRecord> visibleRecords = new ArrayList<>();
		int lastIteratedRecordIndex = 0;


		iterator = searchServices.cachedRecordsIteratorKeepingOrder(mainQuery, 100).inBatches();

		boolean calculateHasChildren = !ctx.getOptions().isAlwaysReturnTaxonomyConceptsWithReadAccessOrLinkable()
									   || ctx.getOptions().getHasChildrenFlagCalculated() != NEVER;
		boolean calculateLinkability = ctx.getOptions().isLinkableFlagCalculated();

		while (visibleRecords.size() < ctx.getOptions().getEndRow() + 1 && iterator.hasNext()) {

			LogicalSearchQuery facetQuery;
			if (selectingAConcept) {
				LogicalSearchCondition condition = fromTypeIn(ctx.getTaxonomy())
						.where(VISIBLE_IN_TREES).isTrueOrNull();

				if (!ctx.getOptions().isAlwaysReturnTaxonomyConceptsWithReadAccessOrLinkable()) {
					condition = condition.andWhere(LINKABLE).isTrueOrNull();
				}

				facetQuery = newQueryForFacets(condition, null, ctx.getOptions());

			} else if (ctx.getOptions().isAlwaysReturnTaxonomyConceptsWithReadAccessOrLinkable()) {
				LogicalSearchCondition condition = fromAllSchemasIn(ctx.getTaxonomy().getCollection()).where(VISIBLE_IN_TREES)
						.isTrueOrNull();
				facetQuery = newQueryForFacets(condition, ctx.getUser(), ctx.getOptions());

			} else {
				LogicalSearchCondition condition = findVisibleNonTaxonomyRecordsInStructure(ctx.getTaxonomy(), true);
				facetQuery = newQueryForFacets(condition, ctx.getUser(), ctx.getOptions());
			}

			List<Record> batch = iterator.next();
			boolean[] hasAccess = new boolean[batch.size()];

			HasChildrenQueryHandler hasChildrenQueryHandler = newHasChildrenQueryHandler(ctx, facetQuery);

			for (int i = 0; i < batch.size(); i++) {
				Record child = batch.get(i);
				hasAccess[i] = !principalConcept || ctx.getUser().has(ctx.getOptions().getRequiredAccess()).on(child);

				if (calculateHasChildren || !hasAccess[i]) {
					hasChildrenQueryHandler.addRecordToCheck(child);
				}
			}

			for (int i = 0; i < batch.size(); i++) {
				Record child = batch.get(i);
				if (visibleRecords.size() < ctx.getOptions().getEndRow()) {
					lastIteratedRecordIndex++;
				}
				Taxonomy taxonomyOfRecord = taxonomiesManager.getTaxonomyOf(child);
				boolean showEvenIfNoChildren = ctx.getOptions().isAlwaysReturnTaxonomyConceptsWithReadAccessOrLinkable()
											   && !taxonomyOfRecord.hasSameCode(taxonomiesManager.getPrincipalTaxonomy(
						ctx.getTaxonomy().getCollection()));

				boolean hasChildren;
				if (calculateHasChildren || !hasAccess[i]) {
					hasChildren = hasChildrenQueryHandler.hasChildren(child);

				} else {
					hasChildren = true;
				}

				boolean linkable = false;
				if (selectingAConcept && calculateLinkability) {
					linkable = isLinkable(child, ctx.getTaxonomy(), ctx.getOptions());
				}

				if (hasChildren
					&& ctx.getOptions().getFilter() != null && ctx.getOptions().getFilter().getLinkableConceptsFilter() != null
					&& !ctx.principalTaxonomy
					&& ctx.isSelectingAConcept()
					&& ctx.taxonomy.getSchemaTypes().size() == 1
					&& (!linkable || ctx.getOptions().getHasChildrenFlagCalculated() != NEVER)) {
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
		int toIndex = Math.min(visibleRecords.size(), ctx.getOptions().getStartRow() + ctx.getOptions().getRows());
		List<TaxonomySearchRecord> returnedRecords = visibleRecords.subList(ctx.getOptions().getStartRow(), toIndex);

		boolean finishedConceptsIteration = !iterator.hasNext();
		FastContinueInfos infos = new FastContinueInfos(finishedConceptsIteration, lastIteratedRecordIndex,
				new ArrayList<String>());

		return new LinkableTaxonomySearchResponse(numFound, infos, returnedRecords);
	}

	protected GetConceptRecordsWithVisibleRecordsResponse getConceptRecordsWithVisibleRecords(
			GetChildrenContext context) {

		GetConceptRecordsWithVisibleRecordsResponse methodResponse = new GetConceptRecordsWithVisibleRecordsResponse();
		MetadataSchemaTypes types = metadataSchemasManager.getSchemaTypes(context.getCollection());

		Iterator<List<Record>> iterator;
		int lastIteratedRecordIndex = 0;
		if (context.isConceptOfNavigatedTaxonomy(context.record)) {
			LogicalSearchQuery mainQuery = conceptNodesTaxonomySearchServices.childConceptsQuery(context.record, context.taxonomy, context.options, types);

			iterator = searchServices.cachedRecordsIteratorKeepingOrder(mainQuery, context.options.getRows()).inBatches();
			lastIteratedRecordIndex = 0;
		} else {
			iterator = new ArrayList<List<Record>>().iterator();
		}

		while (methodResponse.getRecords().size() < context.options.getEndRow() + 1 && iterator.hasNext()) {

			List<Record> batch = iterator.next();

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

				facetQuery = newQueryForFacets(condition, null, context.options);

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
						&& !context.principalTaxonomy
						&& context.isSelectingAConcept()
						&& context.taxonomy.getSchemaTypes().size() == 1) {
						hasChildren = hasLinkableConceptInHierarchy(child, context.taxonomy, context.options);
					}
				}
				boolean linkable;
				if (context.isSelectingAConcept() && calculateLinkability) {
					linkable = isLinkable(child, context.taxonomy, context.options);
				} else {
					linkable = NOT_LINKABLE;
				}

				if (hasChildren || linkable) {
					if (methodResponse.getRecords().size() < context.options.getEndRow()) {
						lastIteratedRecordIndex++;
					}
					methodResponse.getRecords().add(new TaxonomySearchRecord(child, linkable, hasChildren));

				} else if (context.options.isAlwaysReturnTaxonomyConceptsWithReadAccessOrLinkable()) {
					if (!taxonomiesManager.isTypeInPrincipalTaxonomy(context.getCollection(), child.getTypeCode())
						|| context.hasRequiredAccessOn(child)) {
						if (methodResponse.getRecords().size() < context.options.getEndRow()) {
							lastIteratedRecordIndex++;
						}
						methodResponse.getRecords().add(new TaxonomySearchRecord(child, linkable, false));
					}
				}
			}
		}

		if (methodResponse.getRecords().size() > context.options.getEndRow()) {
			methodResponse.setFinishedIteratingOverRecords(false);
			methodResponse.getRecords().remove(methodResponse.getRecords().size() - 1);
		} else {
			methodResponse.setFinishedIteratingOverRecords(true);
		}

		methodResponse.setContinueAtPosition(lastIteratedRecordIndex);
		return methodResponse;

	}


	protected HasChildrenQueryHandler newHasChildrenQueryHandler(GetChildrenContext context,
																 LogicalSearchQuery facetQuery) {
		return new HasChildrenQueryHandler(context.username(), context.getCacheMode(), cache, searchServices, facetQuery);
	}

	protected HasChildrenQueryHandler newHasChildrenQueryHandler(User user, String cacheMode,
																 LogicalSearchQuery facetQuery) {
		return new HasChildrenQueryHandler(user == null ? null : user.getUsername(), cacheMode, cache, searchServices, facetQuery);
	}


	protected ReturnedMetadatasFilter returnedMetadatasForRecordsIn(
			GetChildrenContext context) {
		return conceptNodesTaxonomySearchServices.returnedMetadatasForRecordsIn(context.getCollection(), context.options);
	}


	protected SPEQueryResponse getNonTaxonomyRecords(final GetChildrenContext ctx, int realStart, int realRows) {
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

		query.filteredWith(new HierarchyUserFilter(ctx.user, ctx.options.getRequiredAccess(),
				ctx.forSelectionOfSchemaType, ctx.options.isShowInvisibleRecordsInLinkingMode()));
		query.setName("TaxonomiesSearchServices:getNonTaxonomyRecords(" + ctx.username() + ", " + ctx.record.getId() + ")");
		query.setQueryExecutionMethod(USE_SOLR);
		return searchServices.query(query);
	}


	protected SPEQueryResponse queryFindingWhichRecordsHasChildren(GetChildrenContext context, int visibleConceptsSize,
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


	protected LogicalSearchCondition findVisibleNonTaxonomyRecordsInStructure(GetChildrenContext context,
																			  boolean onlyVisibleInTrees) {
		LogicalSearchCondition condition = fromAllSchemasIn(context.taxonomy.getCollection())
				.where(PATH_PARTS).isEqualTo(context.record.getId())
				.andWhere(schemaTypeIsNotIn(context.taxonomy.getSchemaTypes()));

		if (onlyVisibleInTrees) {
			condition = condition.andWhere(VISIBLE_IN_TREES).isTrueOrNull();
		}

		return condition;
	}

	protected LogicalSearchCondition findVisibleNonTaxonomyRecordsInStructure(Taxonomy taxonomy,
																			  boolean onlyVisibleInTrees) {

		LogicalSearchCondition condition = fromAllSchemasIn(taxonomy.getCollection())
				.where(schemaTypeIsNotIn(taxonomy.getSchemaTypes()));

		if (onlyVisibleInTrees) {
			condition = condition.andWhere(VISIBLE_IN_TREES).isTrueOrNull();
		}

		return condition;
	}

	protected LogicalSearchQuery newQueryForFacets(LogicalSearchCondition condition, GetChildrenContext context) {
		return newQueryForFacets(condition, context.user, context.options);
	}

	protected LogicalSearchQuery newQueryForFacets(LogicalSearchCondition condition, User user,
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

	protected LogicalSearchQuery newQuery(LogicalSearchCondition condition, TaxonomiesSearchOptions options) {
		return new LogicalSearchQuery(condition)
				.filteredByStatus(options.getIncludeStatus())
				.setStartRow(options.getStartRow())
				.setNumberOfRows(options.getRows())
				.setReturnedMetadatas(
						conceptNodesTaxonomySearchServices.returnedMetadatasForRecordsIn(condition.getCollection(), options))
				.sortAsc(Schemas.CODE).sortAsc(Schemas.TITLE);
	}

	protected SPEQueryResponse query(LogicalSearchCondition condition, TaxonomiesSearchOptions options) {
		return searchServices.query(newQuery(condition, options));
	}


	protected boolean hasLinkableConceptInHierarchy(final Record concept, final Taxonomy taxonomy,
													TaxonomiesSearchOptions options) {
		MetadataSchemaType schemaType = metadataSchemasManager.getSchemaTypes(concept.getCollection())
				.getSchemaType(taxonomy.getSchemaTypes().get(0));
		List<Record> records = searchServices.getAllRecords(schemaType);
		for (final Record record : records) {
			if (record.getList(Schemas.PATH_PARTS).contains(concept.getId())) {
				boolean linkableFlag = LangUtils.isTrueOrNull(record.get(Schemas.LINKABLE));
				boolean linkableUsingFilter = options.getFilter().getLinkableConceptsFilter()
						.isLinkable(new LinkableConceptFilterParams(record, taxonomy));

				if (linkableFlag && linkableUsingFilter) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean isLinkable(final Record record, final Taxonomy taxonomy, TaxonomiesSearchOptions options) {

		if (options.isAlwaysReturnTaxonomyConceptsWithReadAccessOrLinkable()) {
			return true;
		}

		boolean linkable = LangUtils.isTrueOrNull(record.<Boolean>get(Schemas.LINKABLE));
		if (linkable && options.getFilter() != null && options.getFilter().getLinkableConceptsFilter() != null) {
			linkable = options.getFilter().getLinkableConceptsFilter().isLinkable(new LinkableConceptFilterParams(record, taxonomy));
		}
		return linkable;
	}


	protected String facetQueryFor(Record record) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("pathParts_ss:");
		stringBuilder.append(record.getId());
		return stringBuilder.toString();
	}

}
