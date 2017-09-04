package com.constellio.model.services.taxonomies;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MemoryTaxonomiesSearchServicesCache implements TaxonomiesSearchServicesCache {

	Map<String, TaxonomyRecordCache> cache = new HashMap<>();

	@Override
	public synchronized void insert(String username, String recordId, Boolean value) {
		TaxonomyRecordCache taxonomyRecordCache = cache.get(recordId);
		if (taxonomyRecordCache == null) {
			taxonomyRecordCache = new TaxonomyRecordCache();
			cache.put(recordId, taxonomyRecordCache);
		}
		taxonomyRecordCache.usersCache.put(username, value);
	}

	@Override
	public synchronized void invalidateAll() {
		cache.clear();
	}

	@Override
	public synchronized void invalidateWithChildren(String recordId) {
		TaxonomyRecordCache taxonomyRecordCache = cache.get(recordId);
		if (taxonomyRecordCache != null) {
			taxonomyRecordCache.invalidateWithChildren();
		}
	}

	@Override
	public synchronized void invalidateWithoutChildren(String recordId) {
		TaxonomyRecordCache taxonomyRecordCache = cache.get(recordId);
		if (taxonomyRecordCache != null) {
			taxonomyRecordCache.removeWithoutChildren();
		}
	}

	@Override
	public synchronized void invalidateRecord(String recordId) {
		cache.remove(recordId);

	}

	@Override
	public synchronized void invalidateUser(String username) {
		for (TaxonomyRecordCache taxonomyRecordCache : cache.values()) {
			taxonomyRecordCache.usersCache.remove(username);
		}
	}

	@Override
	public synchronized Boolean getCachedValue(String username, String recordId) {
		TaxonomyRecordCache taxonomyRecordCache = cache.get(recordId);
		return taxonomyRecordCache == null ? null : taxonomyRecordCache.usersCache.get(username);
	}

	private static class TaxonomyRecordCache {

		Map<String, Boolean> usersCache = new HashMap<>();

		void invalidateWithChildren() {

			List<String> userIdsToRemove = new ArrayList<>();
			for (Map.Entry<String, Boolean> entry : usersCache.entrySet()) {
				if (Boolean.TRUE.equals(entry.getValue())) {
					userIdsToRemove.add(entry.getKey());
				}
			}

			for (String userId : userIdsToRemove) {
				usersCache.remove(userId);
			}

		}

		void removeWithoutChildren() {

			List<String> userIdsToRemove = new ArrayList<>();
			for (Map.Entry<String, Boolean> entry : usersCache.entrySet()) {
				if (Boolean.TRUE.equals(entry.getValue())) {
					userIdsToRemove.add(entry.getKey());
				}
			}

			for (String userId : userIdsToRemove) {
				usersCache.remove(userId);
			}
		}

	}
}
