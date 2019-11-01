package com.constellio.model.services.records.cache.cacheIndexHook;

import com.constellio.model.services.records.cache.cacheIndexConditions.FixedIdsStreamer;
import com.constellio.model.services.records.cache.cacheIndexConditions.IntersectionSortedIdsStreamer;
import com.constellio.model.services.records.cache.cacheIndexConditions.SortedIdsStreamer;
import com.constellio.model.services.records.cache.cacheIndexConditions.UnionSortedIdsStreamer;
import com.constellio.model.services.records.cache.offHeapCollections.SortedIdsList;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RecordIdsHookDataIndexRetriever<K> {

	Map<K, SortedIdsList> map;

	public RecordIdsHookDataIndexRetriever(
			Map<K, SortedIdsList> map) {
		this.map = map;
	}

	public SortedIdsStreamer recordIdsStreamerWithKey(K key) {
		SortedIdsList idsList = map.get(key);
		if (idsList == null) {
			return FixedIdsStreamer.EMPTY;
		} else {
			return FixedIdsStreamer.createFromRecordIds(idsList.getValuesId());
		}
	}

	public SortedIdsStreamer recordIdsStreamerWithAnyKey(List<K> keys) {
		return new UnionSortedIdsStreamer(recordIdsStreamersWithKeys(keys));
	}

	public SortedIdsStreamer recordIdsStreamerWithAllKeys(List<K> keys) {
		return new IntersectionSortedIdsStreamer(recordIdsStreamersWithKeys(keys));
	}

	@NotNull
	private List<SortedIdsStreamer> recordIdsStreamersWithKeys(List<K> keys) {
		List<SortedIdsStreamer> streamers = new ArrayList<>();
		for (K key : keys) {
			streamers.add(recordIdsStreamerWithKey(key));
		}
		return streamers;
	}
}
