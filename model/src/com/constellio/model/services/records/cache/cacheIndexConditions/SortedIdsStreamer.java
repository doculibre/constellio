package com.constellio.model.services.records.cache.cacheIndexConditions;

import com.constellio.data.utils.LangUtils;
import com.constellio.model.services.records.RecordId;
import com.constellio.model.services.records.cache.MetadataIndexCacheDataStore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public interface SortedIdsStreamer {

	Iterator<RecordId> iterator(MetadataIndexCacheDataStore dataStore);

	default boolean hasResults(MetadataIndexCacheDataStore dataStore) {
		return iterator(dataStore).hasNext();
	}

	default int countResults(MetadataIndexCacheDataStore dataStore) {
		Iterator<RecordId> iterator = iterator(dataStore);
		int results = 0;
		while (iterator.hasNext()) {
			iterator.next();
			results++;
		}

		return results;
	}

	default Stream<RecordId> stream(MetadataIndexCacheDataStore dataStore) {
		return LangUtils.stream(iterator(dataStore));
	}

	default SortedIdsStreamer and(SortedIdsStreamer... streamers) {
		return and(Arrays.asList(streamers));
	}

	default SortedIdsStreamer and(List<SortedIdsStreamer> streamers) {
		List<SortedIdsStreamer> conditionsCopy = new ArrayList<>(streamers.size() + 1);
		conditionsCopy.add(this);
		conditionsCopy.addAll(streamers);
		return new IntersectionSortedIdsStreamer(conditionsCopy);
	}

	default SortedIdsStreamer except(SortedIdsStreamer streamer) {
		return new AllExceptSortedIdsStreamer(this, streamer);
	}


	default SortedIdsStreamer or(SortedIdsStreamer... streamers) {
		return or(Arrays.asList(streamers));
	}

	default SortedIdsStreamer or(List<SortedIdsStreamer> streamers) {
		List<SortedIdsStreamer> conditionsCopy = new ArrayList<>(streamers.size() + 1);
		conditionsCopy.add(this);
		conditionsCopy.addAll(streamers);
		return new UnionSortedIdsStreamer(conditionsCopy);
	}

}
