package com.constellio.model.services.search.cache;

import com.constellio.model.entities.records.Record;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.cache.RecordsCaches;
import com.constellio.model.services.search.SPEQueryResponse;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.constellio.data.dao.services.cache.InsertionReason.WAS_OBTAINED;

public class LazyRecordList extends AbstractList<Record> {

	private int batchSize;

	private SerializableSearchCache cache;

	private RecordsCaches recordsCaches;

	private LogicalSearchQuery query;

	private RecordServices recordServices;

	private SearchServices searchServices;

	private boolean serializeRecords;

	LazyRecordList(int batchSize, SerializableSearchCache cache, ModelLayerFactory modelLayerFactory,
				   LogicalSearchQuery query, boolean serializeRecords) {
		this.batchSize = batchSize;
		this.recordsCaches = modelLayerFactory.getRecordsCaches();
		this.cache = cache;
		this.recordServices = modelLayerFactory.newRecordServices();
		this.searchServices = modelLayerFactory.newSearchServices();
		this.query = new LogicalSearchQuery(query);
		this.serializeRecords = serializeRecords;
	}

	@Override
	public Record get(int index) {
		String recordId = cache.getCachedId(index);
		if (recordId == null) {
			return fetchIndex(index);

		} else {
			if (serializeRecords) {
				return cache.getCachedRecord(index);

			} else {
				return recordServices.getDocumentById(recordId);
			}

		}
	}

	private Record fetchIndex(int index) {

		LogicalSearchQuery fetchIndexQuery = new LogicalSearchQuery(query);
		fetchIndexQuery.setNumberOfRows(batchSize).setStartRow(index);

		boolean facetComputedInThisQuery = !cache.isFacetsComputed();
		if (!facetComputedInThisQuery) {
			fetchIndexQuery.clearFacets();
		}

		SPEQueryResponse speQueryResponse = searchServices.query(fetchIndexQuery);
		cache.incrementQTime((int) speQueryResponse.getQtime());
		cache.setSize((int) speQueryResponse.getNumFound());

		List<Record> recordsToInsert = new ArrayList<>();
		Record returnedRecord = null;
		for (int i = 0; i < batchSize; i++) {
			if (i < speQueryResponse.getRecords().size()) {
				Record record = speQueryResponse.getRecords().get(i);
				cache.setRecordId(index + i, record.getId());
				if (serializeRecords) {
					cache.setRecord(index + i, record);
				}
				recordsToInsert.add(record);
				if (returnedRecord == null) {
					returnedRecord = record;
				}
				Map<String, List<String>> recordHighlighting = speQueryResponse.getHighlighting(record.getId());
				cache.setRecordHighLighting(record.getId(), recordHighlighting);
			}
		}
		if (!recordsToInsert.isEmpty()) {
			recordsCaches.insert(recordsToInsert.get(0).getCollection(), recordsToInsert, WAS_OBTAINED);

		}

		if (facetComputedInThisQuery) {
			cache.setFieldFacetValues(speQueryResponse.getFieldFacetValues());
			cache.setQueryFacetsValues(speQueryResponse.getQueryFacetsValues());
			cache.setFacetsComputed(true);
		}
		return returnedRecord;
	}

	@Override
	public int size() {
		int size = cache.getSize();
		if (size == -1) {
			fetchIndex(0);
			size = cache.getSize();
		}
		return size;
	}

	@NotNull
	public List<Record> subList(int fromIndex, int toIndex) {
		List<Record> returnedValues = new ArrayList<>();
		int previousBatchSize = this.batchSize;


		for (int i = fromIndex; i < toIndex; i++) {
			this.batchSize = Math.min(toIndex - i, 250);
			Record record = get(i);
			if (record == null) {
				throw new IllegalStateException("No record at index '" + i + "'");
			}
			returnedValues.add(record);
		}

		this.batchSize = previousBatchSize;
		return returnedValues;
	}

}
