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
package com.constellio.model.services.records;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;

public class RecordProvider {

	RecordServices recordServices;

	Map<String, Record> memoryList;

	public RecordProvider(RecordServices recordServices, RecordProvider recordProvider, List<Record> records,
			Transaction transaction) {
		this.recordServices = recordServices;
		this.memoryList = new HashMap<>();

		if (transaction != null) {
			memoryList.putAll(transaction.getReferencedRecords());
		}

		if (recordProvider != null) {
			this.memoryList.putAll(recordProvider.memoryList);
		}
		if (records != null) {
			for (Record record : records) {
				this.memoryList.put(record.getId(), record);
			}
		}

	}

	public Record getRecord(String id) {
		Record record = memoryList.get(id);
		if (record == null) {
			record = recordServices.getDocumentById(id);
			memoryList.put(id, record);
		}
		return record;
	}

	public boolean hasRecordInMemoryList(Object referenceValue) {
		if (referenceValue instanceof String) {
			return memoryList.containsKey((String) referenceValue);
		} else if (referenceValue instanceof List) {
			for (String referenceItem : (List<String>) referenceValue) {
				if (memoryList.containsKey(referenceItem)) {
					return true;
				}
			}
		}
		return false;
	}
}
