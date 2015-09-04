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
package com.constellio.model.services.records.cache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.constellio.model.entities.records.Record;

public class RecordsCaches {

	Map<String, RecordsCache> collectionsCache = new HashMap<>();

	public RecordsCache getCache(String collection) {

		//This method is called whenever a service is created
		//Since a synchronize block is slow, we try to use it only when necessary

		RecordsCache cache = collectionsCache.get(collection);

		if (cache == null) {
			return getORCreateCache(collection);
		} else {
			return cache;
		}
	}

	private synchronized RecordsCache getORCreateCache(String collection) {
		RecordsCache cache = collectionsCache.get(collection);

		if (cache == null) {
			cache = new RecordsCacheImpl();
			collectionsCache.put(collection, cache);
		}
		return cache;
	}

	public boolean isCached(String id) {
		for (RecordsCache cache : collectionsCache.values()) {
			if (cache.isCached(id)) {
				return true;
			}
		}
		return false;
	}

	public void insert(String collection, List<Record> records) {
		RecordsCache cache = getCache(collection);
		cache.insert(records);
	}

	public void insert(Record record) {
		RecordsCache cache = getCache(record.getCollection());
		cache.insert(record);
	}

	public Record getRecord(String id) {
		for (RecordsCache cache : collectionsCache.values()) {
			Record record = cache.get(id);
			if (record != null) {
				return record;
			}
		}
		return null;
	}

	public void invalidateAll() {
		for (RecordsCache cache : collectionsCache.values()) {
			cache.invalidateAll();
		}
	}
}
