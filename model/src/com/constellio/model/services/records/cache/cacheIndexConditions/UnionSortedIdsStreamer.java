package com.constellio.model.services.records.cache.cacheIndexConditions;

import com.constellio.data.utils.LazyIterator;
import com.constellio.model.services.records.RecordId;
import com.constellio.model.services.records.cache.MetadataIndexCacheDataStore;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class UnionSortedIdsStreamer implements SortedIdsStreamer {


	List<SortedIdsStreamer> streamers;

	public UnionSortedIdsStreamer(
			List<SortedIdsStreamer> streamers) {
		this.streamers = streamers;
	}

	@Override
	public Iterator<RecordId> iterator(MetadataIndexCacheDataStore dataStore) {
		return new RecordIdsListIteratorConsumerLazyIterator(streamers.stream()
				.map(s -> new RecordIdsIteratorConsumer(s.iterator(dataStore)))
				.collect(Collectors.toList()));

	}

	private class RecordIdsListIteratorConsumerLazyIterator extends LazyIterator<RecordId> {

		private List<RecordIdsIteratorConsumer> consumers = new ArrayList<>();

		public RecordIdsListIteratorConsumerLazyIterator(
				List<RecordIdsIteratorConsumer> consumers) {
			this.consumers = consumers;
		}

		protected RecordId getNextOrNull() {

			RecordIdsIteratorConsumer consumerProvidingNext = null;
			RecordId smallestId = null;

			for (RecordIdsIteratorConsumer consumer : consumers) {
				RecordId smallerId = consumer.getIdSmallerThan(smallestId);
				if (smallerId != null) {
					smallestId = smallerId;
					consumerProvidingNext = consumer;
				}
			}

			if (consumerProvidingNext == null) {
				return null;
			} else {
				consumerProvidingNext.markAsRetrieved();
				return smallestId;
			}
		}

	}

	private class RecordIdsIteratorConsumer {

		private Iterator<RecordId> recordIdIterator;

		private RecordId temp;

		public RecordIdsIteratorConsumer(Iterator<RecordId> recordIdIterator) {
			this.recordIdIterator = recordIdIterator;
			if (recordIdIterator.hasNext()) {
				temp = recordIdIterator.next();
			}
		}

		private void markAsRetrieved() {
			if (recordIdIterator.hasNext()) {
				temp = recordIdIterator.next();
			} else {
				temp = null;
			}
		}

		private RecordId getIdSmallerThan(RecordId recordId) {
			if (temp != null) {
				if (recordId != null) {
					if (recordId.lesserThan(temp)) {
						return null;
					} else if (recordId.equals(temp)) {
						//This Id is a duplicate, so this iterator is advanced
						if (recordIdIterator.hasNext()) {
							temp = recordIdIterator.next();
						} else {
							temp = null;
						}
						return null;

					}
				}
				return temp;
			}

			return null;
		}

	}
}
