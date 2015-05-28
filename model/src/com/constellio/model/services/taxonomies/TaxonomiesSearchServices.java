/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.model.services.taxonomies;

import static com.constellio.data.utils.LangUtils.isTrueOrNull;
import static com.constellio.model.entities.schemas.Schemas.PARENT_PATH;
import static com.constellio.model.entities.schemas.Schemas.PATH;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;
import static com.constellio.model.services.search.query.logical.valueCondition.ConditionTemplateFactory.schemaTypeIs;
import static com.constellio.model.services.search.query.logical.valueCondition.ConditionTemplateFactory.schemaTypeIsIn;
import static com.constellio.model.services.search.query.logical.valueCondition.ConditionTemplateFactory.schemaTypeIsNotIn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordUtils;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.search.SPEQueryResponse;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

public class TaxonomiesSearchServices {

	private static final Logger LOGGER = LoggerFactory.getLogger(TaxonomiesSearchServices.class);

	SearchServices searchServices;

	TaxonomiesManager taxonomiesManager;

	MetadataSchemasManager metadataSchemasManager;

	public TaxonomiesSearchServices(SearchServices searchServices, TaxonomiesManager taxonomiesManager,
			MetadataSchemasManager metadataSchemasManager) {
		this.searchServices = searchServices;
		this.taxonomiesManager = taxonomiesManager;
		this.metadataSchemasManager = metadataSchemasManager;
	}

	public List<Record> getRootConcept(String collection, String taxonomyCode, TaxonomiesSearchOptions taxonomiesSearchOptions) {
		return getRootConceptResponse(collection, taxonomyCode, taxonomiesSearchOptions).getRecords();
	}

	public SPEQueryResponse getRootConceptResponse(String collection, String taxonomyCode,
			TaxonomiesSearchOptions taxonomiesSearchOptions) {
		LogicalSearchQuery query = getRootConceptsQuery(collection, taxonomyCode, taxonomiesSearchOptions);
		return searchServices.query(query);
	}

	public LogicalSearchQuery getRootConceptsQuery(String collection, String taxonomyCode,
			TaxonomiesSearchOptions taxonomiesSearchOptions) {
		LogicalSearchCondition condition = fromAllSchemasIn(collection).where(PARENT_PATH).isEqualTo("/" + taxonomyCode);
		LogicalSearchQuery query = new LogicalSearchQuery(condition);
		query.filteredByStatus(taxonomiesSearchOptions.getIncludeStatus());
		query.setStartRow(taxonomiesSearchOptions.getStartRow());
		query.setNumberOfRows(taxonomiesSearchOptions.getRows());
		query.setReturnedMetadatas(taxonomiesSearchOptions.getReturnedMetadatasFilter());
		query.sortAsc(Schemas.CODE).sortAsc(Schemas.IDENTIFIER);
		return query;
	}

	public List<Record> getChildConcept(Record record, TaxonomiesSearchOptions taxonomiesSearchOptions) {
		return getChildConceptResponse(record, taxonomiesSearchOptions).getRecords();
	}

	public SPEQueryResponse getChildConceptResponse(Record record, TaxonomiesSearchOptions taxonomiesSearchOptions) {
		LogicalSearchQuery query = getChildConceptsQuery(record, taxonomiesSearchOptions);
		return searchServices.query(query);
	}

	public LogicalSearchQuery getChildConceptsQuery(Record record, TaxonomiesSearchOptions taxonomiesSearchOptions) {
		List<String> paths = record.getList(PATH);
		LogicalSearchCondition condition = fromAllSchemasIn(record.getCollection())
				.where(PARENT_PATH).isIn(paths).andWhere(Schemas.VISIBLE_IN_TREES).isTrueOrNull();

		return new LogicalSearchQuery(condition)
				.filteredByStatus(taxonomiesSearchOptions.getIncludeStatus())
				.setStartRow(taxonomiesSearchOptions.getStartRow())
				.setNumberOfRows(taxonomiesSearchOptions.getRows())
				.sortAsc(Schemas.CODE).sortAsc(Schemas.IDENTIFIER)
				.setReturnedMetadatas(taxonomiesSearchOptions.getReturnedMetadatasFilter());
	}

	public List<Record> getVisibleRootConcept(User user, String collection, String taxonomyCode,
			TaxonomiesSearchOptions taxonomiesSearchOptions) {
		return getVisibleRootConceptResponse(user, collection, taxonomyCode, taxonomiesSearchOptions).getRecords();
	}

	public SPEQueryResponse getVisibleRootConceptResponse(User user, String collection, String taxonomyCode,
			TaxonomiesSearchOptions taxonomiesSearchOptions) {
		SPEQueryResponse childrenResponse = getRootConceptResponse(collection, taxonomyCode, taxonomiesSearchOptions);
		List<Record> children = childrenResponse.getRecords();

		//		if (user == User.GOD || user.hasCollectionReadAccess() || user.hasCollectionWriteAccess() || user
		//				.hasCollectionDeleteAccess()) {
		//			return childrenResponse;
		//		}

		Taxonomy taxonomy = taxonomiesManager.getEnabledTaxonomyWithCode(collection, taxonomyCode);
		LogicalSearchCondition condition = findVisibleNonTaxonomyRecordsInStructure(null, taxonomy);
		LogicalSearchQuery query = new LogicalSearchQuery(condition)
				.filteredWithUser(user)
				.filteredByStatus(taxonomiesSearchOptions.getIncludeStatus())
				.setReturnedMetadatas(ReturnedMetadatasFilter.idVersionSchema())
				.setNumberOfRows(0);

		for (Record child : children) {
			query.addQueryFacet(facetQueryFor(taxonomy, 0, child, true));
		}
		SPEQueryResponse response = searchServices.query(query);

		List<Record> resultVisible = new ArrayList<>();
		for (Record child : children) {
			if (response.getQueryFacetCount(facetQueryFor(taxonomy, 0, child, true)) > 0) {
				resultVisible.add(child);
			}
		}

		Collections.sort(resultVisible, new RecordsComparator());
		return childrenResponse.withModifiedRecordList(resultVisible).withNumFound(resultVisible.size());
	}

	private String facetQueryFor(Taxonomy taxonomy, int level, Record child, boolean onlyVisibleInTrees) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("(pathParts_ss:");
		stringBuilder.append(taxonomy.getCode());
		stringBuilder.append("_");
		stringBuilder.append(level);
		stringBuilder.append("_");
		stringBuilder.append(child.getId());
		if (onlyVisibleInTrees) {
			stringBuilder.append(" AND (visibleInTrees_s:__TRUE__ OR visibleInTrees_s:__NULL__)");
		}
		stringBuilder.append(")");
		return stringBuilder.toString();
	}

	public List<TaxonomySearchRecord> getLinkableRootConcept(User user, String collection, String taxonomyCode,
			String selectedType, TaxonomiesSearchOptions taxonomiesSearchOptions) {
		return getLinkableRootConceptResponse(user, collection, taxonomyCode, selectedType, taxonomiesSearchOptions).getRecords();
	}

	private LinkableTaxonomySearchResponse getLinkableConceptsForSelectionOfAPrincipalTaxonomyConceptBasedOnAuthorizations(
			User user, Taxonomy usingTaxonomy, Record inRecord,
			TaxonomiesSearchOptions options) {

		SPEQueryResponse mainQueryResponse;
		if (inRecord == null) {
			mainQueryResponse = getRootConceptResponse(usingTaxonomy.getCollection(), usingTaxonomy.getCode(), options);
		} else {
			mainQueryResponse = getChildConceptResponse(inRecord, options);
		}
		List<Record> children = mainQueryResponse.getRecords();
		int level = inRecord == null ? 0 : getRecordLevelInTaxonomy(inRecord, usingTaxonomy);

		Taxonomy taxonomy = taxonomiesManager.getEnabledTaxonomyWithCode(usingTaxonomy.getCollection(), usingTaxonomy.getCode());
		LogicalSearchCondition condition = fromAllSchemasIn(taxonomy.getCollection())
				.where(schemaTypeIsIn(taxonomy.getSchemaTypes()));
		LogicalSearchQuery query = new LogicalSearchQuery(condition)
				.filteredWithUser(user)
				.filteredByStatus(options.getIncludeStatus())
				.setReturnedMetadatas(ReturnedMetadatasFilter.onlyFields(Schemas.LINKABLE));

		for (Record child : children) {
			query.addQueryFacet(facetQueryFor(usingTaxonomy, level, child, false));
		}
		SPEQueryResponse response = searchServices.query(query);
		List<String> responseRecordIds = new RecordUtils().toIdList(response.getRecords());
		List<TaxonomySearchRecord> resultVisible = new ArrayList<>();
		for (Record child : children) {
			if (response.getQueryFacetCount(facetQueryFor(taxonomy, level, child, false)) > 0) {
				boolean readAuthorizationsOnConcept = responseRecordIds.contains(child.getId());
				boolean conceptIsLinkable = isTrueOrNull(child.get(Schemas.LINKABLE));
				resultVisible.add(new TaxonomySearchRecord(child, readAuthorizationsOnConcept && conceptIsLinkable));
			}
		}
		Collections.sort(resultVisible, new TaxonomySearchRecordsComparator());
		return new LinkableTaxonomySearchResponse(response.getNumFound(), resultVisible);

	}

	private SPEQueryResponse queryWithOptionsExceptStartAndRows(LogicalSearchCondition condition,
			TaxonomiesSearchOptions options) {
		LogicalSearchQuery query = new LogicalSearchQuery(condition)
				.filteredByStatus(options.getIncludeStatus())
				.setReturnedMetadatas(options.getReturnedMetadatasFilter());

		return searchServices.query(query);
	}

	private SPEQueryResponse queryWithOptions(LogicalSearchCondition condition, TaxonomiesSearchOptions options) {
		LogicalSearchQuery query = new LogicalSearchQuery(condition)
				.filteredByStatus(options.getIncludeStatus())
				.setStartRow(options.getStartRow())
				.setNumberOfRows(options.getRows())
				.setReturnedMetadatas(options.getReturnedMetadatasFilter());

		return searchServices.query(query);
	}

	private LinkableTaxonomySearchResponse getLinkableConceptsForSelectionOfATaxonomyConcept(User user,
			Taxonomy taxonomy, MetadataSchemaType selectedType, Record inRecord, TaxonomiesSearchOptions options) {

		options = options.cloneAddingReturnedField(Schemas.LINKABLE);

		SPEQueryResponse mainQueryResponse;
		if (inRecord == null) {
			mainQueryResponse = getRootConceptResponse(taxonomy.getCollection(), taxonomy.getCode(), options);
		} else {
			List<String> paths = inRecord.getList(PATH);

			LogicalSearchCondition condition = fromAllSchemasIn(inRecord.getCollection())
					.where(PARENT_PATH).isIn(paths).andWhere(schemaTypeIsIn(taxonomy.getSchemaTypes()));

			mainQueryResponse = queryWithOptions(condition, options);
		}

		List<TaxonomySearchRecord> records = new ArrayList<>();
		for (Record rootConcept : mainQueryResponse.getRecords()) {
			boolean sameType = rootConcept.getSchemaCode().startsWith(selectedType.getCode());
			boolean linkable = isTrueOrNull(rootConcept.get(Schemas.LINKABLE));
			records.add(new TaxonomySearchRecord(rootConcept, sameType && linkable));
		}
		Collections.sort(records, new TaxonomySearchRecordsComparator());
		return new LinkableTaxonomySearchResponse(mainQueryResponse.getNumFound(), records);
	}

	private int getRecordLevelInTaxonomy(Record record, Taxonomy taxonomy) {
		List<String> paths = record.getList(PATH);
		for (String path : paths) {
			if (path.startsWith("/" + taxonomy.getCode())) {
				return path.split("/").length - 2;
			}
		}
		return 0;
	}

	private LinkableTaxonomySearchResponse getLinkableConceptsForSelectionOfARecordUsingNonPrincipalTaxonomy(User user,
			Taxonomy taxonomy, MetadataSchemaType selectedType, Record inRecord, TaxonomiesSearchOptions options) {

		SPEQueryResponse mainQueryResponse;

		if (inRecord == null) {
			mainQueryResponse = getRootConceptResponse(taxonomy.getCollection(), taxonomy.getCode(), options);
		} else {
			Object path = inRecord.getList(PATH).get(0);
			LogicalSearchCondition condition = fromAllSchemasIn(inRecord.getCollection()).where(PARENT_PATH).isEqualTo(path);
			mainQueryResponse = queryWithOptionsExceptStartAndRows(condition, options);
		}

		int level = inRecord == null ? 0 : getRecordLevelInTaxonomy(inRecord, taxonomy);

		LogicalSearchQuery query = new LogicalSearchQuery()
				.setCondition(fromAllSchemasIn(taxonomy.getCollection()).where(schemaTypeIs(selectedType)))
				.filteredWithUser(user)
				.filteredByStatus(options.getIncludeStatus())
				.setReturnedMetadatas(ReturnedMetadatasFilter.idVersionSchema());

		for (Record child : mainQueryResponse.getRecords()) {
			query.addQueryFacet(facetQueryFor(taxonomy, level, child, false));
		}

		SPEQueryResponse response = searchServices.query(query);
		List<String> childrenIds = new RecordUtils().toIdList(response.getRecords());

		List<TaxonomySearchRecord> visibleRecords = new ArrayList<>();
		for (Record child : mainQueryResponse.getRecords()) {

			if (childrenIds.contains(child.getId())) {
				visibleRecords.add(new TaxonomySearchRecord(child, true));

			} else if (response.getQueryFacetCount(facetQueryFor(taxonomy, level, child, false)) > 0) {
				visibleRecords.add(new TaxonomySearchRecord(child, false));
			}
		}
		Collections.sort(visibleRecords, new TaxonomySearchRecordsComparator());
		return new LinkableTaxonomySearchResponse(visibleRecords.size(), visibleRecords);
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

			if (user == User.GOD || user.hasCollectionReadWriteOrDeleteAccess()) {
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

	public LinkableTaxonomySearchResponse getLinkableRootConceptResponse(User user, String collection, String usingTaxonomyCode,
			String selectedType, TaxonomiesSearchOptions options) {

		return getLinkableConceptResponse(user, collection, usingTaxonomyCode, selectedType, null, options);

	}

	public List<TaxonomySearchRecord> getLinkableChildConcept(User user, Record record, String usingTaxonomy, String selectedType,
			TaxonomiesSearchOptions taxonomiesSearchOptions) {
		return getLinkableChildConceptResponse(user, record, usingTaxonomy, selectedType, taxonomiesSearchOptions).getRecords();
	}

	public LinkableTaxonomySearchResponse getLinkableChildConceptResponse(User user, Record inRecord, String usingTaxonomy,
			String selectedType, TaxonomiesSearchOptions options) {

		return getLinkableConceptResponse(user, inRecord.getCollection(), usingTaxonomy, selectedType, inRecord, options);

	}

	public List<Record> getVisibleChildConcept(User user, String taxonomyCode, Record record,
			TaxonomiesSearchOptions taxonomiesSearchOptions) {
		return getVisibleChildConceptResponse(user, taxonomyCode, record, taxonomiesSearchOptions).getRecords();
	}

	public SPEQueryResponse getVisibleChildConceptResponse(User user, String taxonomyCode, Record record,
			TaxonomiesSearchOptions taxonomiesSearchOptions) {

		SPEQueryResponse mainQueryResponse = getChildConceptResponse(record, taxonomiesSearchOptions);
		List<Record> childs = mainQueryResponse.getRecords();

		//		if (user == User.GOD || user.hasCollectionReadAccess() || user.hasCollectionWriteAccess() || user
		//				.hasCollectionDeleteAccess()) {
		//			return mainQueryResponse;
		//		}

		Taxonomy taxonomy = taxonomiesManager.getEnabledTaxonomyWithCode(record.getCollection(), taxonomyCode);
		//String path = record.getList(PATH).get(0) + "/";
		String path = null;
		for (String candidate : record.<String>getList(PATH)) {
			if (candidate.startsWith("/" + taxonomy.getCode())) {
				path = candidate + "/";
				break;
			}
		}

		LogicalSearchCondition condition = findVisibleNonTaxonomyRecordsInStructure(path,
				taxonomy);
		LogicalSearchQuery query = new LogicalSearchQuery(condition)
				.filteredWithUser(user)
				.filteredByStatus(taxonomiesSearchOptions.getIncludeStatus())
				.setReturnedMetadatas(ReturnedMetadatasFilter.idVersionSchema())
				.setNumberOfRows(0);

		int level = path.split("/").length - 2;
		for (Record child : childs) {
			query.addQueryFacet(facetQueryFor(taxonomy, level, child, true));
		}

		SPEQueryResponse response = searchServices.query(query);

		List<Record> resultVisible = new ArrayList<>();
		for (Record child : childs) {
			if (response.getQueryFacetCount(facetQueryFor(taxonomy, level, child, true)) > 0) {
				resultVisible.add(child);
			}
		}

		Collections.sort(resultVisible, new RecordsComparator());
		return mainQueryResponse.withModifiedRecordList(resultVisible).withNumFound(resultVisible.size());
	}

	public boolean findNonTaxonomyRecordsInStructure(Record record,
			TaxonomiesSearchOptions taxonomiesSearchOptions) {
		String schemaType = new SchemaUtils().getSchemaTypeCode(record.getSchemaCode());
		Taxonomy taxonomy = taxonomiesManager.getTaxonomyFor(record.getCollection(), schemaType);
		String path = record.getList(PATH).get(0) + "/";
		return findNonTaxonomyRecordsInStructure(record.getCollection(), path, taxonomy, taxonomiesSearchOptions);
	}

	public boolean findNonTaxonomyRecordsInStructure(String collection, String path, Taxonomy taxonomy,
			TaxonomiesSearchOptions options) {

		LogicalSearchCondition condition = findVisibleNonTaxonomyRecordsInStructure(path, taxonomy);
		return searchServices.hasResults(new LogicalSearchQuery(condition).filteredByStatus(options.getIncludeStatus()));
	}

	LogicalSearchCondition findVisibleNonTaxonomyRecordsInStructure(String path, Taxonomy taxonomy) {

		if (path == null) {
			return fromAllSchemasIn(taxonomy.getCollection())
					.where(schemaTypeIsNotIn(taxonomy.getSchemaTypes()));
		} else {

			return fromAllSchemasIn(taxonomy.getCollection())
					.where(PATH).isStartingWithText(path)
					.andWhere(schemaTypeIsNotIn(taxonomy.getSchemaTypes()));
		}
	}

	LogicalSearchCondition findVisibleTaxonomyRecordsInStructure(String collection, String path, Taxonomy taxonomy) {

		if (path == null) {
			return fromAllSchemasIn(collection)
					.where(schemaTypeIsIn(taxonomy.getSchemaTypes()));
		} else {

			return fromAllSchemasIn(collection)
					.where(PATH).isStartingWithText(path)
					.andWhere(schemaTypeIsIn(taxonomy.getSchemaTypes()));
		}
	}

	private LogicalSearchCondition whereTypeIn(Taxonomy taxonomy) {
		return fromAllSchemasIn(taxonomy.getCollection())
				.where(schemaTypeIsIn(taxonomy.getSchemaTypes()));
	}

	public LogicalSearchCondition getAllConceptHierarchyOfCondition(Taxonomy taxonomy, Record record) {

		if (record == null) {
			return whereTypeIn(taxonomy).andWhere(Schemas.PATH).isStartingWithText("/" + taxonomy.getCode());

		} else {
			List<String> paths = record.getList(Schemas.PATH);
			return whereTypeIn(taxonomy).andWhere(Schemas.PATH).isStartingWithText(paths.get(0));
		}
	}

	public List<String> getAllConceptIdsHierarchyOf(Taxonomy taxonomy, Record record) {
		return searchServices.searchRecordIds(new LogicalSearchQuery(getAllConceptHierarchyOfCondition(taxonomy, record)));
	}

	public List<Record> getAllConceptHierarchyOf(Taxonomy taxonomy, Record record) {
		return searchServices.search(new LogicalSearchQuery(getAllConceptHierarchyOfCondition(taxonomy, record)));
	}

	public List<String> getAllPrincipalConceptIdsAvailableTo(Taxonomy taxonomy, User user, StatusFilter statusFilter) {
		return searchServices.searchRecordIds(new LogicalSearchQuery()
				.setCondition(getAllConceptHierarchyOfCondition(taxonomy, null))
				.filteredWithUser(user).filteredByStatus(statusFilter));
	}

	public List<String> getAllPrincipalConceptIdsAvailableTo(Taxonomy taxonomy, User user) {
		return getAllPrincipalConceptIdsAvailableTo(taxonomy, user, StatusFilter.ALL);
	}

	public List<Record> getAllPrincipalConceptsAvailableTo(Taxonomy taxonomy, User user) {
		return searchServices.search(new LogicalSearchQuery()
				.setCondition(getAllConceptHierarchyOfCondition(taxonomy, null))
				.filteredWithUser(user));
	}

	private static class TaxonomySearchRecordsComparator implements Comparator<TaxonomySearchRecord> {

		RecordsComparator comparator = new RecordsComparator();

		@Override
		public int compare(TaxonomySearchRecord o1, TaxonomySearchRecord o2) {
			return comparator.compare(o1.getRecord(), o2.getRecord());
		}
	}

	private static class RecordsComparator implements Comparator<Record> {

		@Override
		public int compare(Record o1, Record o2) {

			String code1 = o1.get(Schemas.CODE);
			String code2 = o2.get(Schemas.CODE);

			int result = compare(code1, code2);

			if (result == 0) {
				String title1 = o1.get(Schemas.CODE);
				String title2 = o2.get(Schemas.CODE);
				result = compare(title1, title2);
			}

			return result;
		}

		private int compare(String s1, String s2) {
			if (s1 != null && s2 != null) {
				return s1.compareTo(s2);

			} else if (s1 != null) {
				return 1;

			} else if (s2 != null) {
				return -1;
			} else {
				return 0;
			}
		}

	}

}
