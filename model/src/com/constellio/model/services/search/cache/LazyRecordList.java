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
package com.constellio.model.services.search.cache;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.constellio.model.entities.records.Record;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.cache.RecordsCaches;
import com.constellio.model.services.search.SPEQueryResponse;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

public class LazyRecordList extends AbstractList<Record> {

	private int batchSize;

	private SerializableSearchCache cache;

	private RecordsCaches recordsCaches;

	private LogicalSearchQuery query;

	private RecordServices recordServices;

	private SearchServices searchServices;

	LazyRecordList(int batchSize, SerializableSearchCache cache, ModelLayerFactory modelLayerFactory,
			LogicalSearchQuery query) {
		this.batchSize = batchSize;
		this.recordsCaches = modelLayerFactory.getRecordsCaches();
		this.cache = cache;
		this.recordServices = modelLayerFactory.newRecordServices();
		this.searchServices = modelLayerFactory.newSearchServices();
		this.query = new LogicalSearchQuery(query);
	}

	@Override
	public Record get(int index) {
		String recordId = cache.getCachedId(index);
		if (recordId == null) {
			return fetchIndex(index);

		} else {
			return recordServices.getDocumentById(recordId);

		}
	}

	private Record fetchIndex(int index) {
		SPEQueryResponse speQueryResponse = searchServices.query(query.setNumberOfRows(batchSize).setStartRow(index));
		cache.setSize((int) speQueryResponse.getNumFound());

		List<Record> recordsToInsert = new ArrayList<>();
		Record returnedRecord = null;
		for (int i = 0; i < batchSize; i++) {
			if (i < speQueryResponse.getRecords().size()) {
				Record record = speQueryResponse.getRecords().get(i);
				cache.setRecordId(index + i, record.getId());
				recordsToInsert.add(record);
				if (returnedRecord == null) {
					returnedRecord = record;
				}
				Map<String, List<String>> recordHighlighting = speQueryResponse.getHighlighting(record.getId());
				cache.setRecordHighLighting(record.getId(), recordHighlighting);
			}
		}
		if (!recordsToInsert.isEmpty()) {
			recordsCaches.insert(recordsToInsert.get(0).getCollection(), recordsToInsert);

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
}
