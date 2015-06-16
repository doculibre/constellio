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

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.DisMaxParams;
import org.apache.solr.common.params.FacetParams;
import org.apache.solr.common.params.HighlightParams;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.ShardParams;

import com.constellio.data.dao.dto.records.FacetValue;
import com.constellio.data.dao.dto.records.QueryResponseDTO;
import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.services.bigVault.LazyResultsIterator;
import com.constellio.data.dao.services.records.RecordDao;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.DataStoreField;
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

	public Iterator<Record> recordsIterator(LogicalSearchQuery query) {
		return recordsIterator(query, 10);
	}

	public Iterator<Record> recordsIterator(LogicalSearchQuery query, int batchSize) {
		ModifiableSolrParams params = addSolrModifiableParams(query);
		return new LazyResultsIterator<Record>(recordDao, params, batchSize) {

			@Override
			public Record convert(RecordDTO recordDTO) {
				return recordServices.toRecord(recordDTO);
			}
		};
	}

	public long getResultsCount(LogicalSearchCondition condition) {
		return getResultsCount(new LogicalSearchQuery(condition));
	}

	public long getResultsCount(LogicalSearchQuery query) {
		query.setNumberOfRows(0);
		ModifiableSolrParams params = addSolrModifiableParams(query);
		return recordDao.query(params).getNumFound();
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
			for (DataStoreField field : query.getFieldFacets()) {
				params.add(FacetParams.FACET_FIELD, "{!ex=" + field.getDataStoreCode() + "}" + field.getDataStoreCode());
			}
			if (query.getFieldFacetLimit() != 0) {
				params.add(FacetParams.FACET_LIMIT, "" + query.getFieldFacetLimit());
			}
		}
		if (!query.getQueryFacets().isEmpty()) {
			for (String facetQuery : query.getQueryFacets()) {
				params.add(FacetParams.FACET_QUERY, facetQuery);
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

		List<Record> records = recordServices.toRecords(recordDTOs);
		Map<DataStoreField, List<FacetValue>> fieldFacetValues = buildFacets(query.getFieldFacets(),
				queryResponseDTO.getFieldFacetValues());
		Map<String, Integer> queryFacetValues = queryResponseDTO.getQueryFacetValues();

		SPEQueryResponse response = new SPEQueryResponse(fieldFacetValues, queryFacetValues, queryResponseDTO.getQtime(),
				queryResponseDTO.getNumFound(), records, queryResponseDTO.getHighlights(),
				queryResponseDTO.isCorrectlySpelt(), queryResponseDTO.getSpellCheckerSuggestions());

		if (query.getResultsProjection() != null) {
			return query.getResultsProjection().project(query, response);
		} else {
			return response;
		}
	}

	private Map<DataStoreField, List<FacetValue>> buildFacets(
			List<DataStoreField> fields, Map<String, List<FacetValue>> facetValues) {
		Map<DataStoreField, List<FacetValue>> result = new HashMap<>();
		for (DataStoreField field : fields) {
			List<FacetValue> values = facetValues.get(field.getDataStoreCode());
			if (values != null) {
				result.put(field, values);
			}
		}
		return result;
	}
}
