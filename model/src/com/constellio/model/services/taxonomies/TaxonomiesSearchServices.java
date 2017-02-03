package com.constellio.model.services.taxonomies;

import static com.constellio.data.utils.LangUtils.isTrueOrNull;
import static com.constellio.model.entities.schemas.Schemas.ALL_REMOVED_AUTHS;
import static com.constellio.model.entities.schemas.Schemas.ATTACHED_ANCESTORS;
import static com.constellio.model.entities.schemas.Schemas.PATH_PARTS;
import static com.constellio.model.entities.schemas.Schemas.TOKENS;
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
import static com.constellio.model.services.taxonomies.ConceptNodesTaxonomySearchServices.notDirectChildOf;
import static com.constellio.model.services.taxonomies.ConceptNodesTaxonomySearchServices.recordInHierarchyOf;
import static com.constellio.model.services.taxonomies.ConceptNodesTaxonomySearchServices.visibleInTrees;
import static com.constellio.model.services.taxonomies.ConceptNodesTaxonomySearchServices.whereTypeIn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.constellio.data.dao.services.bigVault.SearchResponseIterator;
import com.constellio.data.utils.AccentApostropheCleaner;
import com.constellio.data.utils.BatchBuilderIterator;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaType;
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
		return getVisibleRootConceptResponse(user, collection, taxonomyCode, options).getRecords();
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

		return getVisibleChildrenRecords(user, record, options, null).getRecords();
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

		public TaxonomiesSearchOptions clonedOptionsWithoutRange() {
			return new TaxonomiesSearchOptions(options).setRows(10000).setStartRow(0);
		}

		public boolean hasRequiredAccessOn(Record record) {
			return user.hasRequiredAccess(options.getRequiredAccess()).on(record);
		}

		public boolean isConceptOfNavigatedTaxonomy(Record childRecordLeadingToSecuredRecord) {
			return taxonomy.getSchemaTypes().contains(childRecordLeadingToSecuredRecord.getTypeCode());
		}

		public String getCollection() {
			return record.getCollection();
		}

		public LogicalSearchQuery newQueryWithUserFilter(LogicalSearchCondition condition) {
			return new LogicalSearchQuery(condition).filteredWithUser(user, options.getRequiredAccess());
		}
	}

	private LinkableTaxonomySearchResponse getVisibleChildrenRecords(User user, Record record, TaxonomiesSearchOptions options,
			MetadataSchemaType forSelectionOfSchemaType) {
		TaxonomiesSearchOptions clonedOptionsWithoutRange = new TaxonomiesSearchOptions(options).setRows(10000).setStartRow(0);
		GetChildrenContext ctx = new GetChildrenContext(user, record, clonedOptionsWithoutRange, forSelectionOfSchemaType);
		Set<String> typesParentOfOtherTypes = metadataSchemasManager.getSchemaTypes(record).getTypeParentOfOtherTypes();
		List<TaxonomySearchRecord> concepts = getConceptRecordsWithVisibleRecords(ctx);

		List<Record> childrenWithoutAccessToInclude = getChildrenRecordsWithoutRequiredAccessLeadingToRecordWithAccess(ctx);
		Set<String> childrenIdsWithoutAccessToInclude = new RecordUtils().toIdSet(childrenWithoutAccessToInclude);

		int pessimisticStartRow = Math.max(0, ctx.options.getStartRow() - childrenWithoutAccessToInclude.size());
		LogicalSearchCondition condition;
		if (forSelectionOfSchemaType == null) {
			condition = fromAllSchemasInCollectionOf(ctx.record)
					.where(directChildOf(ctx.record)).andWhere(visibleInTrees)
					.andWhere(schemaTypeIsNotIn(ctx.taxonomy.getSchemaTypes()));
		} else {
			//TODO seulement linkable
			condition = from(forSelectionOfSchemaType)
					.where(directChildOf(ctx.record));
		}
		LogicalSearchQuery query = newQuery(condition, ctx.options)
				.setStartRow(pessimisticStartRow)
				.setNumberOfRows(ctx.options.getRows() + childrenWithoutAccessToInclude.size())
				.filteredWithUser(ctx.user, ctx.options.getRequiredAccess());

		SPEQueryResponse response = searchServices.query(query);

		List<Record> records = new ArrayList<>();
		records.addAll(response.getRecords());
		records.addAll(childrenWithoutAccessToInclude);
		Collections.sort(records, new NodeComparator(ctx.taxonomy.getSchemaTypes()));

		List<TaxonomySearchRecord> returnedRecords = new ArrayList<>();
		SPEQueryResponse facetResponse = queryFindingWhichRecordsHasChildren(ctx, concepts.size(), records);

		for (int i = options.getStartRow(); i < options.getEndRow() && i - concepts.size() < records.size(); i++) {
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
				boolean linkable = ctx.hasRequiredAccessOn(returnedRecord) && forSelectionOfSchemaType != null
						&& forSelectionOfSchemaType.getCode().equals(returnedRecord.getTypeCode());
				returnedRecords.add(new TaxonomySearchRecord(records.get(nonTaxonomyIndex), linkable, hasChildren));
			}

		}

		long numfound = response.getNumFound() + childrenIdsWithoutAccessToInclude.size() + concepts.size();
		return new LinkableTaxonomySearchResponse(numfound, returnedRecords);

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
			}

			LogicalSearchQuery facetQuery = context.newQueryWithUserFilter(queryCondition)
					.filteredByStatus(ACTIVES).setStartRow(0).setNumberOfRows(0);

			boolean hasFacetsToCompute = false;
			for (int i = context.options.getStartRow(); i < context.options.getEndRow(); i++) {
				if (i >= visibleConceptsSize) {
					int nonTaxonomyIndex = i - visibleConceptsSize;
					if (nonTaxonomyIndex < records.size()) {
						facetQuery.addQueryFacet("hasChildren", facetQueryFor(context.taxonomy, records.get(nonTaxonomyIndex)));
						hasFacetsToCompute = true;
					} else {
						break;
					}
				}
			}
			if (hasFacetsToCompute) {
				facetResponse = searchServices.query(facetQuery);
			}
		}
		return facetResponse;
	}

	private List<TaxonomySearchRecord> getConceptRecordsWithVisibleRecords(GetChildrenContext context) {

		SearchResponseIterator<List<Record>> childsIterator = searchServices
				.recordsIteratorKeepingOrder(childConceptsQuery(context.record, context.taxonomy, context.options), 25)
				.inBatches();

		int consumed = 0;
		List<TaxonomySearchRecord> resultVisible = new ArrayList<>();
		while (resultVisible.size() < context.options.getEndRow() && childsIterator.hasNext()) {

			List<Record> batch = childsIterator.next();
			consumed += batch.size();
			LogicalSearchQuery facetQuery = newQueryForFacets(findVisibleNonTaxonomyRecordsInStructure(context, true), context);
			facetQuery.addQueryFacets(CHILDREN_QUERY, facetQueriesFor(context.taxonomy, batch));

			SPEQueryResponse response = searchServices.query(facetQuery);
			for (Record child : batch) {
				boolean hasChildren = response.getQueryFacetCount(facetQueryFor(context.taxonomy, child)) > 0;
				if (hasChildren) {
					resultVisible.add(new TaxonomySearchRecord(child, NOT_LINKABLE, true));

				} else if (context.options.isAlwaysReturnTaxonomyConceptsWithReadAccess()) {
					if (!taxonomiesManager.isTypeInPrincipalTaxonomy(context.getCollection(), child.getTypeCode())
							|| context.hasRequiredAccessOn(child)) {
						resultVisible.add(new TaxonomySearchRecord(child, NOT_LINKABLE, false));
					}
				}
			}
		}

		long numFound = childsIterator.getNumFound() - consumed + resultVisible.size();
		int toIndex = Math.min(resultVisible.size(), context.options.getEndRow());
		return resultVisible.subList(context.options.getStartRow(), toIndex);

	}

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
					//TODO ne devrait pas être visible si logiquement supprimé ou si non affichable
					recordExpectedToBeVisibleInTree = !context.taxonomy.getSchemaTypes().contains(schemaType);
				} else {
					//TODO ne devrait pas être visible si logiquement supprimé ou si non-sélectionnable
					recordExpectedToBeVisibleInTree = context.forSelectionOfSchemaType.getCode().equals(schemaType);
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

	private static class NodeComparator implements Comparator<Record> {

		List<String> taxonomySchemaTypes;

		public NodeComparator(List<String> taxonomySchemaTypes) {
			this.taxonomySchemaTypes = taxonomySchemaTypes;
		}

		@Override
		public int compare(Record o1, Record o2) {
			int type1 = taxonomySchemaTypes.contains(o1.getTypeCode()) ? 0 : 1;
			int type2 = taxonomySchemaTypes.contains(o2.getTypeCode()) ? 0 : 1;

			int value = new Integer(type1).compareTo(type2);
			if (value == 0) {

				String s1 = convert(o1.<String>get(Schemas.CODE));
				if (s1 == null) {
					s1 = convert(o1.<String>get(Schemas.TITLE));
				}

				String s2 = convert(o2.<String>get(Schemas.CODE));
				if (s2 == null) {
					s2 = convert(o2.<String>get(Schemas.TITLE));
				}

				value = s1.compareTo(s2);
			}

			return value;
		}

		private String convert(String value) {
			return value == null ? null : AccentApostropheCleaner.removeAccents(value);
		}
	}

	public LinkableTaxonomySearchResponse getVisibleChildConceptResponse(User user, String taxonomyCode, Record record,
			TaxonomiesSearchOptions options) {
		return getVisibleChildrenRecords(user, record, options, null);
		//		LogicalSearchQuery mainQuery = conceptNodesTaxonomySearchServices.getChildNodesQuery(record, options).setStartRow(0);
		//
		//		SearchResponseIterator<List<Record>> childsIterator = searchServices.recordsIteratorKeepingOrder(mainQuery, 25)
		//				.inBatches();
		//		Taxonomy taxonomy = taxonomiesManager.getEnabledTaxonomyWithCode(record.getCollection(), taxonomyCode);
		//
		//		int consumed = 0;
		//		List<TaxonomySearchRecord> resultVisible = new ArrayList<>();
		//		MetadataSchemaTypes types = metadataSchemasManager.getSchemaTypes(record.getCollection());
		//		while (resultVisible.size() < options.getStartRow() + options.getRows() && childsIterator.hasNext()) {
		//
		//			LogicalSearchCondition condition = findVisibleNonTaxonomyRecordsInStructure(record, taxonomy, false, options);
		//			LogicalSearchQuery facetQuery = newQueryForFacets(condition, user, options);
		//
		//			List<Record> batch = childsIterator.next();
		//
		//			boolean[] isTaxonomyRecord = new boolean[batch.size()];
		//			boolean[] hasChildrenFlagCalculated = new boolean[batch.size()];
		//			boolean[] recordMayBeParent = new boolean[batch.size()];
		//			boolean[] readAccess = new boolean[batch.size()];
		//
		//			for (int i = 0; i < batch.size(); i++) {
		//				Record child = batch.get(i);
		//				readAccess[i] = user == User.GOD || user.hasReadAccess().on(child);
		//				isTaxonomyRecord[i] = taxonomy.getSchemaTypes().contains(child.getTypeCode());
		//				recordMayBeParent[i] = types.getTypeParentOfOtherTypes().contains(child.getTypeCode());
		//				hasChildrenFlagCalculated[i] = isTaxonomyRecord[i]
		//						|| (options.isHasChildrenFlagCalculated() && recordMayBeParent[i])
		//						|| (!readAccess[i]);
		//
		//				if (hasChildrenFlagCalculated[i]) {
		//					facetQuery.addQueryFacet(CHILDREN_QUERY, facetQueryFor(taxonomy, child));
		//				}
		//			}
		//
		//			SPEQueryResponse response = searchServices.query(facetQuery);
		//
		//			for (int i = 0; i < batch.size(); i++) {
		//				Record child = batch.get(i);
		//				consumed++;
		//
		//				boolean hasChildren;
		//				if (hasChildrenFlagCalculated[i]) {
		//					hasChildren = response.getQueryFacetCount(facetQueryFor(taxonomy, child)) > 0;
		//				} else {
		//					hasChildren = recordMayBeParent[i];
		//				}
		//
		//				boolean showEvenIfNoChildren = false;
		//				if (options.isAlwaysReturnTaxonomyConceptsWithReadAccess()) {
		//					Taxonomy taxonomyOfRecord = taxonomiesManager.getTaxonomyOf(child);
		//					if (taxonomyOfRecord != null) {
		//						if (taxonomyOfRecord.hasSameCode(taxonomiesManager.getPrincipalTaxonomy(record.getCollection()))) {
		//							showEvenIfNoChildren = user.hasReadAccess().on(child);
		//						} else {
		//							showEvenIfNoChildren = true;
		//						}
		//
		//						LogicalSearchQuery hasVisibleConceptsQuery = new LogicalSearchQuery(whereTypeIn(taxonomy)
		//								.andWhere(PATH_PARTS).isEqualTo(child.getId()));
		//						hasVisibleConceptsQuery.filteredWithUser(user, options.getRequiredAccess());
		//						hasChildren |= searchServices.hasResults(hasVisibleConceptsQuery);
		//					}
		//
		//				}
		//
		//				if ((!isTaxonomyRecord[i] && readAccess[i]) || hasChildren || showEvenIfNoChildren) {
		//					resultVisible.add(new TaxonomySearchRecord(child, NOT_LINKABLE, hasChildren));
		//				}
		//			}
		//		}
		//		long numFound = childsIterator.getNumFound() - consumed + resultVisible.size();
		//		int toIndex = Math.min(resultVisible.size(), options.getStartRow() + options.getRows());
		//		List<TaxonomySearchRecord> returnedRecords = resultVisible.subList(options.getStartRow(), toIndex);
		//		return new LinkableTaxonomySearchResponse(numFound, returnedRecords);
	}

	public LinkableTaxonomySearchResponse getVisibleRootConceptResponse(User user, String collection, String taxonomyCode,
			TaxonomiesSearchOptions options) {
		LogicalSearchQuery mainQuery = conceptNodesTaxonomySearchServices.getRootConceptsQuery(collection, taxonomyCode, options);
		SearchResponseIterator<Record> rootIterator = searchServices.recordsIteratorKeepingOrder(
				mainQuery.setNumberOfRows(100000).setStartRow(0), 25);
		Iterator<List<Record>> batchIterators = new BatchBuilderIterator<>(rootIterator, 25);

		Taxonomy taxonomy = taxonomiesManager.getEnabledTaxonomyWithCode(collection, taxonomyCode);

		int consumed = 0;
		List<TaxonomySearchRecord> resultVisible = new ArrayList<>();
		while (resultVisible.size() < options.getEndRow() && batchIterators.hasNext()) {

			LogicalSearchCondition condition = findVisibleNonTaxonomyRecordsInStructure(taxonomy, true);
			LogicalSearchQuery facetQuery = newQueryForFacets(condition, user, options);

			List<Record> batch = batchIterators.next();
			for (Record child : batch) {
				facetQuery.addQueryFacet(CHILDREN_QUERY, facetQueryFor(taxonomy, child));
			}
			SPEQueryResponse response = searchServices.query(facetQuery);

			for (Record child : batch) {
				consumed++;
				boolean showEvenIfNoChildren = false;
				boolean hasChildren = response.getQueryFacetCount(facetQueryFor(taxonomy, child)) > 0;
				if (options.isAlwaysReturnTaxonomyConceptsWithReadAccess()) {
					Taxonomy taxonomyOfRecord = taxonomiesManager.getTaxonomyOf(child);
					if (taxonomyOfRecord != null) {
						if (taxonomyOfRecord.hasSameCode(taxonomiesManager.getPrincipalTaxonomy(collection))) {
							showEvenIfNoChildren = user.hasReadAccess().on(child);
						} else {
							showEvenIfNoChildren = true;
						}

						LogicalSearchQuery hasVisibleConceptsQuery = new LogicalSearchQuery(whereTypeIn(taxonomy)
								.andWhere(PATH_PARTS).isEqualTo(child.getId()));
						hasVisibleConceptsQuery.filteredWithUser(user, options.getRequiredAccess());
						hasChildren |= searchServices.hasResults(hasVisibleConceptsQuery);
					}
				}
				if (showEvenIfNoChildren || hasChildren) {
					resultVisible.add(new TaxonomySearchRecord(child, NOT_LINKABLE, hasChildren));
				}
			}
		}

		long numFound = rootIterator.getNumFound() - consumed + resultVisible.size();
		int toIndex = Math.min(resultVisible.size(), options.getStartRow() + options.getRows());
		List<TaxonomySearchRecord> returnedRecords = resultVisible.subList(options.getStartRow(), toIndex);
		return new LinkableTaxonomySearchResponse(numFound, returnedRecords);
	}

	private LinkableTaxonomySearchResponse getLinkableConceptsForSelectionOfAPrincipalTaxonomyConceptBasedOnAuthorizations(
			User user, Taxonomy usingTaxonomy, Record inRecord, TaxonomiesSearchOptions options) {

		SPEQueryResponse mainQueryResponse;
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
				.setReturnedMetadatas(ReturnedMetadatasFilter.onlyMetadatas(Schemas.LINKABLE));

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
		return new LinkableTaxonomySearchResponse(resultVisible.size(), resultVisible);

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

		LogicalSearchCondition condition = whereTypeIn(taxonomy).andWhere(Schemas.VISIBLE_IN_TREES).isTrueOrNull();
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

	private LinkableTaxonomySearchResponse getLinkableConceptsForSelectionOfARecordUsingNonPrincipalTaxonomy(User user,
			Taxonomy taxonomy, MetadataSchemaType selectedType, Record inRecord, TaxonomiesSearchOptions options) {

		if (inRecord != null) {
			return getVisibleChildrenRecords(user, inRecord, options, selectedType);
		} else {

			LogicalSearchQuery mainQuery;
			if (inRecord == null) {
				mainQuery = conceptNodesTaxonomySearchServices.getRootConceptsQuery(
						taxonomy.getCollection(), taxonomy.getCode(), options);
			} else {
				LogicalSearchCondition condition = fromAllSchemasIn(inRecord.getCollection())
						.where(PATH_PARTS).isEqualTo(("_LAST_" + inRecord.getId()));
				mainQuery = new LogicalSearchQuery(condition);

			}

			mainQuery.filteredByStatus(options.getIncludeStatus())
					.sortAsc(Schemas.CODE).sortAsc(Schemas.TITLE)
					.setReturnedMetadatas(options.getReturnedMetadatasFilter()
							.withIncludedMetadatas(TOKENS, ATTACHED_ANCESTORS, ALL_REMOVED_AUTHS));

			Iterator<List<Record>> iterator = searchServices.recordsIteratorKeepingOrder(mainQuery.setStartRow(0), 25)
					.inBatches();
			List<TaxonomySearchRecord> visibleRecords = new ArrayList<>();
			while (visibleRecords.size() < options.getEndRow() + 1 && iterator.hasNext()) {
				List<Record> batch = iterator.next();

				LogicalSearchQuery facetQuery = newQueryForFacets(from(selectedType).returnAll(), user, options);
				for (Record child : batch) {
					facetQuery.addQueryFacet(CHILDREN_QUERY, facetQueryFor(taxonomy, child));
				}

				SPEQueryResponse response = searchServices.query(facetQuery);
				for (Record child : batch) {
					String schemaType = getSchemaTypeCode(child.getSchemaCode());

					boolean hasVisibleChildren =
							response.getQueryFacetCount(facetQueryFor(taxonomy, child)) > 0;

					if (schemaType.equals(selectedType.getCode())) {
						boolean hasAccess = user.hasRequiredAccess(options.getRequiredAccess()).on(child);
						if (hasAccess || hasVisibleChildren) {
							visibleRecords.add(new TaxonomySearchRecord(child, hasAccess, hasVisibleChildren));
						}

					} else if (hasVisibleChildren) {
						visibleRecords.add(new TaxonomySearchRecord(child, false, true));
					}

				}
			}
			int numFound = visibleRecords.size();
			int toIndex = Math.min(visibleRecords.size(), options.getEndRow());
			List<TaxonomySearchRecord> returnedRecords = visibleRecords.subList(options.getStartRow(), toIndex);
			return new LinkableTaxonomySearchResponse(numFound, returnedRecords);
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

			response = getLinkableConceptsForSelectionOfATaxonomyConcept(user, usingTaxonomy, selectedType, inRecord,
					options);

		} else {
			//selecting a non-taxonomy record using a taxonomy
			response = getLinkableConceptsForSelectionOfARecordUsingNonPrincipalTaxonomy(
					user, usingTaxonomy, selectedType, inRecord, options);

		}

		long duration = new Date().getTime() - start;
		return response.withQTime(duration);
	}

	LogicalSearchCondition findVisibleNonTaxonomyRecordsInStructure(GetChildrenContext context, boolean onlyVisibleInTrees) {
		LogicalSearchCondition condition = fromAllSchemasIn(context.taxonomy.getCollection())
				.where(PATH_PARTS).isEqualTo(context.record.getId())
				.andWhere(schemaTypeIsNotIn(context.taxonomy.getSchemaTypes()));

		if (onlyVisibleInTrees) {
			condition = condition.andWhere(Schemas.VISIBLE_IN_TREES).isTrueOrNull();
		}

		return condition;
	}

	LogicalSearchCondition findVisibleNonTaxonomyRecordsInStructure(Taxonomy taxonomy, boolean onlyVisibleInTrees) {

		LogicalSearchCondition condition = fromAllSchemasIn(taxonomy.getCollection())
				.where(schemaTypeIsNotIn(taxonomy.getSchemaTypes()));

		if (onlyVisibleInTrees) {
			condition = condition.andWhere(Schemas.VISIBLE_IN_TREES).isTrueOrNull();
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
				.setReturnedMetadatas(options.getReturnedMetadatasFilter()
						.withIncludedMetadatas(TOKENS, ATTACHED_ANCESTORS, ALL_REMOVED_AUTHS))
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
