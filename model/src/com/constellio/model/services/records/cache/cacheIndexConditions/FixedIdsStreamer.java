package com.constellio.model.services.records.cache.cacheIndexConditions;

import com.constellio.model.services.records.RecordId;
import com.constellio.model.services.records.cache.MetadataIndexCacheDataStore;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class FixedIdsStreamer implements SortedIdsStreamer {

	private List<RecordId> ids;

	private FixedIdsStreamer(List<RecordId> ids) {
		this.ids = ids;
	}

	public static FixedIdsStreamer createFromStringValues(List<String> ids) {
		return new FixedIdsStreamer(ids.stream().map((s) -> RecordId.toId(s)).sorted().collect(Collectors.toList()));
	}

	public static FixedIdsStreamer createFromRecordIds(List<RecordId> ids) {
		return new FixedIdsStreamer(ids.stream().sorted().collect(Collectors.toList()));
	}

	@Override
	public boolean hasResults(MetadataIndexCacheDataStore dataStore) {
		return !ids.isEmpty();
	}

	@Override
	public int countResults(MetadataIndexCacheDataStore dataStore) {
		return ids.size();
	}

	@Override
	public Iterator<RecordId> iterator(MetadataIndexCacheDataStore dataStore) {
		return ids.iterator();
	}
}
