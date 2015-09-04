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
package com.constellio.model.services.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.DisMaxParams;
import org.apache.solr.common.params.FacetParams;
import org.apache.solr.common.params.HighlightParams;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.ShardParams;
import org.apache.solr.common.params.StatsParams;

import com.constellio.data.dao.dto.records.FacetValue;
import com.constellio.data.dao.dto.records.QueryResponseDTO;
import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.services.bigVault.LazyResultsIterator;
import com.constellio.data.dao.services.records.RecordDao;
import com.constellio.data.utils.BatchBuilderIterator;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.iterators.OptimizedLogicalSearchIterator;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

public class SearchServices {
	RecordDao recordDao;
	RecordServices recordServices;

	public SearchServices(RecordDao recordDao, RecordServices recordServices) {
		this.recordDao = recordDao;
		this.recordServices = recordServices;
	}

	public SPEQueryResponse query(LogicalSearchQuery query) {
		ModifiableSolrParams params = addSolrModifiableParams(query);
		return buildResponse(params, query);
	}

	public List<Record> search(LogicalSearchQuery query) {
		return query(query).getRecords();
	}

	public Record searchSingleResult(LogicalSearchCondition condition) {
		SPEQueryResponse response = query(new LogicalSearchQuery(condition).setNumberOfRows(1));
		if (response.getNumFound() > 1) {
			throw new SearchServicesRuntimeException.TooManyRecordsInSingleSearchResult(condition.getSolrQuery());
		}
		return response.getNumFound() == 1 ? response.getRecords().get(0) : null;
	}

	public Iterator<List<Record>> recordsBatchIterator(int batch, LogicalSearchQuery query) {
		Iterator<Record> recordsIterator = recordsIterator(query, batch);
		return new BatchBuilderIterator<>(recordsIterator, batch);
	}

	public Iterator<List<Record>> recordsBatchIterator(LogicalSearchQuery query) {
		return recordsBatchIterator(100, query);
	}

	public Iterator<Record> recordsIterator(LogicalSearchQuery query) {
		return recordsIterator(query, 100);
	}

	public Iterator<Record> recordsIterator(LogicalSearchQuery query, int batchSize) {
		ModifiableSolrParams params = addSolrModifiableParams(query);
		final boolean fullyLoaded = query.getReturnedMetadatas().isFullyLoaded();
		return new LazyResultsIterator<Record>(recordDao, params, batchSize) {

			@Override
			public Record convert(RecordDTO recordDTO) {
				return recordServices.toRecord(recordDTO, fullyLoaded);
			}
		};
	}

	public long getResultsCount(LogicalSearchCondition condition) {
		return getResultsCount(new LogicalSearchQuery(condition));
	}

	public long getResultsCount(LogicalSearchQuery query) {
		int oldNumberOfRows = query.getNumberOfRows();
		query.setNumberOfRows(0);
		ModifiableSolrParams params = addSolrModifiableParams(query);
		long result = recordDao.query(params).getNumFound();
		query.setNumberOfRows(oldNumberOfRows);
		return result;
	}

	public List<String> searchRecordIds(LogicalSearchQuery query) {
		query.setReturnedMetadatas(ReturnedMetadatasFilter.idVersionSchema());
		ModifiableSolrParams params = addSolrModifiableParams(query);

		List<String> ids = new ArrayList<>();
		for (Record record : buildResponse(params, query).getRecords()) {
			ids.add(record.getId());
		}
		return ids;
	}

	public Iterator<String> recordsIdsIterator(LogicalSearchQuery query) {
		ModifiableSolrParams params = addSolrModifiableParams(query);
		return new LazyResultsIterator<String>(recordDao, params, 10000) {

			@Override
			public String convert(RecordDTO recordDTO) {
				return recordDTO.getId();
			}
		};
	}

	public boolean hasResults(LogicalSearchQuery query) {
		return getResultsCount(query) != 0;
	}

	public boolean hasResults(LogicalSearchCondition condition) {
		return getResultsCount(condition) != 0;
	}

	public Iterator<Record> optimizedRecordsIterator(LogicalSearchQuery query, int batchSize) {
		return new OptimizedLogicalSearchIterator<Record>(query, this, batchSize) {
			@Override
			protected Record convert(Record record) {
				return record;
			}
		};
	}

	public Iterator<String> optimizedRecordsIdsIterator(LogicalSearchQuery query, int batchSize) {
		query.setReturnedMetadatas(ReturnedMetadatasFilter.idVersionSchema());
		return new OptimizedLogicalSearchIterator<String>(query, this, batchSize) {
			@Override
			protected String convert(Record record) {
				return record.getId();
			}
		};
	}

	private ModifiableSolrParams addSolrModifiableParams(LogicalSearchQuery query) {
		ModifiableSolrParams params = new ModifiableSolrParams();

		for (String filterQuery : query.getFilterQueries()) {
			params.add(CommonParams.FQ, filterQuery);
		}
		params.add(CommonParams.FQ, "" + query.getQuery());

		params.add(CommonParams.QT, "/spell");
		params.add(ShardParams.SHARDS_QT, "/spell");

		if (query.getFreeTextQuery() != null) {
			String qf = Schemas.FRENCH_SEARCH_FIELD.getLocalCode() + "_" + Schemas.FRENCH_SEARCH_FIELD.getDataStoreType() + " "
					+ Schemas.ENGLISH_SEARCH_FIELD.getLocalCode() + "_" + Schemas.ENGLISH_SEARCH_FIELD.getDataStoreType();
			params.add(DisMaxParams.QF, qf);
			params.add(DisMaxParams.MM, "2<66%");
			params.add("defType", "edismax");
		}
		params.add(CommonParams.Q, StringUtils.defaultString(query.getFreeTextQuery(), "*:*"));

		params.add(CommonParams.ROWS, "" + query.getNumberOfRows());
		params.add(CommonParams.START, "" + query.getStartRow());

		if (!query.getFieldFacets().isEmpty() || !query.getQueryFacets().isEmpty()) {
			params.add(FacetParams.FACET, "true");
			params.add(FacetParams.FACET_SORT, FacetParams.FACET_SORT_COUNT);
		}
		if (!query.getFieldFacets().isEmpty()) {
			params.add(FacetParams.FACET_MINCOUNT, "1");
			for (String field : query.getFieldFacets()) {
				params.add(FacetParams.FACET_FIELD, "{!ex=" + field + "}" + field);
			}
			if (query.getFieldFacetLimit() != 0) {
				params.add(FacetParams.FACET_LIMIT, "" + query.getFieldFacetLimit());
			}
		}
		if (!query.getStatisticFields().isEmpty()) {
			params.set(StatsParams.STATS, "true");
			for (String field : query.getStatisticFields()) {
				params.add(StatsParams.STATS_FIELD, field);
			}
		}
		if (!query.getQueryFacets().isEmpty()) {
			for (Entry<String, Set<String>> facetQuery : query.getQueryFacets().getMapEntries()) {
				for (String aQuery : facetQuery.getValue()) {
					params.add(FacetParams.FACET_QUERY, "{!ex=f" + facetQuery.getKey() + "}" + aQuery);
				}
			}
		}

		String sort = query.getSort();
		if (!sort.isEmpty()) {
			params.add(CommonParams.SORT, sort);
		}

		if (query.getReturnedMetadatas() != null) {
			List<String> fields = new ArrayList<>();
			fields.add("id");
			fields.add("schema_s");
			fields.add("_version_");
			fields.add("collection_s");
			List<Metadata> acceptedFields = query.getReturnedMetadatas().getAcceptedFields();
			if (acceptedFields != null) {
				for (Metadata acceptedField : acceptedFields) {
					fields.add(acceptedField.getDataStoreCode());
				}
				params.set(CommonParams.FL, StringUtils.join(fields.toArray(), ","));
			}

		}

		if (query.isHighlighting()) {
			params.add(HighlightParams.HIGHLIGHT, "true");
			params.add(HighlightParams.FIELDS, query.getHighlightingFields());
			params.add(HighlightParams.SNIPPETS, "2");
			params.add(HighlightParams.FRAGSIZE, "70");
			params.add(HighlightParams.MERGE_CONTIGUOUS_FRAGMENTS, "true");
		}

		if (query.isSpellcheck()) {
			params.add("spellcheck", "on");
		}

		return params;
	}

	private SPEQueryResponse buildResponse(ModifiableSolrParams params, LogicalSearchQuery query) {
		QueryResponseDTO queryResponseDTO = recordDao.query(params);
		List<RecordDTO> recordDTOs = queryResponseDTO.getResults();

		List<Record> records = recordServices.toRecords(recordDTOs, query.getReturnedMetadatas().isFullyLoaded());
		Map<String, List<FacetValue>> fieldFacetValues = buildFacets(query.getFieldFacets(),
				queryResponseDTO.getFieldFacetValues());
		Map<String, Integer> queryFacetValues = withRemoveExclusions(queryResponseDTO.getQueryFacetValues());

		Map<String, Map<String, Object>> statisticsValues = buildStats(query.getStatisticFields(),
				queryResponseDTO.getFieldsStatistics());
		SPEQueryResponse response = new SPEQueryResponse(fieldFacetValues, statisticsValues, queryFacetValues,
				queryResponseDTO.getQtime(),
				queryResponseDTO.getNumFound(), records, queryResponseDTO.getHighlights(),
				queryResponseDTO.isCorrectlySpelt(), queryResponseDTO.getSpellCheckerSuggestions());

		if (query.getResultsProjection() != null) {
			return query.getResultsProjection().project(query, response);
		} else {
			return response;
		}
	}

	private Map<String, Integer> withRemoveExclusions(Map<String, Integer> queryFacetValues) {
		if (queryFacetValues == null) {
			return null;
		}
		Map<String, Integer> withRemovedExclusions = new HashMap<>();
		for (Map.Entry<String, Integer> queryEntry : queryFacetValues.entrySet()) {
			String query = queryEntry.getKey();
			query = query.substring(query.indexOf("}") + 1);
			withRemovedExclusions.put(query, queryEntry.getValue());
		}
		return withRemovedExclusions;
	}

	private Map<String, List<FacetValue>> buildFacets(
			List<String> fields, Map<String, List<FacetValue>> facetValues) {
		Map<String, List<FacetValue>> result = new HashMap<>();
		for (String field : fields) {
			List<FacetValue> values = facetValues.get(field);
			if (values != null) {
				result.put(field, values);
			}
		}
		return result;
	}

	private Map<String, Map<String, Object>> buildStats(
			List<String> fields, Map<String, Map<String, Object>> fieldStatsValues) {
		Map<String, Map<String, Object>> result = new HashMap<>();
		for (String field : fields) {
			Map<String, Object> values = fieldStatsValues.get(field);
			if (values != null) {
				result.put(field, values);
			}
		}
		return result;
	}
}
