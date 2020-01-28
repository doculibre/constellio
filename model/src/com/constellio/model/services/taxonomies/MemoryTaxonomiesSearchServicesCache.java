package com.constellio.model.services.taxonomies;

import com.constellio.data.utils.LangUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.model.services.records.RecordUtils.sizeOf;

public class MemoryTaxonomiesSearchServicesCache implements TaxonomiesSearchServicesCache {

	Map<String, TaxonomyRecordCache> cache = new HashMap<>();

	@Override
	public synchronized void insert(String username, String recordId, String mode, Boolean value) {

		if (username != null && recordId != null && mode != null && value != null) {
			TaxonomyRecordCache taxonomyRecordCache = cache.get(recordId);
			if (taxonomyRecordCache == null) {
				taxonomyRecordCache = new TaxonomyRecordCache();
				cache.put(recordId, taxonomyRecordCache);
			}
			taxonomyRecordCache.put(username, mode, value);
		}
	}

	@Override
	public synchronized void invalidateAll() {
		cache.clear();
	}

	@Override
	public synchronized void invalidateWithChildren(String recordId) {
		if (recordId != null) {
			TaxonomyRecordCache taxonomyRecordCache = cache.get(recordId);
			if (taxonomyRecordCache != null) {
				taxonomyRecordCache.invalidateWithChildren();
			}
		}
	}

	@Override
	public synchronized void invalidateWithoutChildren(String recordId) {
		if (recordId != null) {
			TaxonomyRecordCache taxonomyRecordCache = cache.get(recordId);
			if (taxonomyRecordCache != null) {
				taxonomyRecordCache.removeWithoutChildren();
			}
		}
	}

	@Override
	public synchronized void invalidateRecord(String recordId) {
		if (recordId != null) {
			cache.remove(recordId);
		}

	}

	@Override
	public synchronized void invalidateUser(String username) {
		if (username != null) {
			for (TaxonomyRecordCache taxonomyRecordCache : cache.values()) {
				taxonomyRecordCache.invalidateUser(username);
			}
		}
	}

	@Override
	public synchronized Boolean getCachedValue(String username, String recordId, String mode) {

		if (username != null && recordId != null && mode != null) {

			TaxonomyRecordCache taxonomyRecordCache = cache.get(recordId);
			return taxonomyRecordCache == null ? null : taxonomyRecordCache.get(username, mode);
		} else {
			return null;
		}
	}

	@Override
	public synchronized long getHeapConsumption() {
		long heapConsumption = 12 + LangUtils.estimatedizeOfMapStructureBasedOnSize(cache);

		for (Map.Entry<String, TaxonomyRecordCache> entry : cache.entrySet()) {
			heapConsumption += sizeOf(entry.getKey());
			heapConsumption += sizeOf(12 + entry.getValue().getHeapConsumption());
		}

		return heapConsumption;
	}

	/**
	 * For test purposes
	 */
	public Map<String, Map<String, Boolean>> getMemoryCache(String id) {
		TaxonomyRecordCache recordCache = cache.get(id);
		return recordCache == null ? new HashMap<String, Map<String, Boolean>>() : recordCache.userModesCache;
	}

	private static class TaxonomyRecordCache {

		Map<String, Map<String, Boolean>> userModesCache = new HashMap<>();

		void put(String username, String mode, Boolean value) {
			Map<String, Boolean> userCache = userModesCache.get(username);
			if (userCache == null) {
				userCache = new HashMap<>();
				userModesCache.put(username, userCache);
			}
			userCache.put(mode, value);
		}

		public Boolean get(String username, String mode) {
			Map<String, Boolean> userCache = userModesCache.get(username);
			if (userCache != null) {
				return userCache.get(mode);
			} else {
				return null;
			}
		}

		void invalidateWithChildren() {

			for (Map<String, Boolean> userCache : userModesCache.values()) {
				List<String> modesToRemove = new ArrayList<>();
				for (Map.Entry<String, Boolean> entry : userCache.entrySet()) {
					if (Boolean.TRUE.equals(entry.getValue())) {
						modesToRemove.add(entry.getKey());
					}
				}

				for (String userId : modesToRemove) {
					userCache.remove(userId);
				}
			}

		}

		void removeWithoutChildren() {

			for (Map<String, Boolean> userCache : userModesCache.values()) {
				List<String> modesToRemove = new ArrayList<>();
				for (Map.Entry<String, Boolean> entry : userCache.entrySet()) {
					if (Boolean.FALSE.equals(entry.getValue())) {
						modesToRemove.add(entry.getKey());
					}
				}

				for (String userId : modesToRemove) {
					userCache.remove(userId);
				}
			}

		}

		public void invalidateUser(String username) {
			userModesCache.remove(username);
		}

		public long getHeapConsumption() {
			//Map<String, Map<String, Boolean>> userModesCache = new HashMap<>();

			long heapConsumption = 12 + LangUtils.estimatedizeOfMapStructureBasedOnSize(userModesCache);
			for (Map.Entry<String, Map<String, Boolean>> entry : userModesCache.entrySet()) {
				heapConsumption += sizeOf(entry.getKey());
				Map<String, Boolean> value = entry.getValue();
				heapConsumption += 12 + LangUtils.estimatedizeOfMapStructureBasedOnSize(value);
				for (Map.Entry<String, Boolean> entry2 : value.entrySet()) {
					heapConsumption += sizeOf(entry2.getKey());
					heapConsumption += 16;
				}
			}


			return heapConsumption;
		}
	}
}
