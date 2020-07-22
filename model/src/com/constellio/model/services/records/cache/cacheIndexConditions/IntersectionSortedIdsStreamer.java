package com.constellio.model.services.records.cache.cacheIndexConditions;

import com.constellio.data.dao.dto.records.RecordId;
import com.constellio.data.utils.LazyIterator;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class IntersectionSortedIdsStreamer implements SortedIdsStreamer {

	List<SortedIdsStreamer> streamers;

	public IntersectionSortedIdsStreamer(
			List<SortedIdsStreamer> streamers) {
		this.streamers = streamers;
	}

	static int counter;

	@Override
	public Iterator<RecordId> iterator() {

		if (streamers.isEmpty()) {
			return Collections.emptyIterator();
		}

		final Iterator<RecordId> mainIterator = streamers.get(0).iterator();
		final RecordIdsListIteratorConsumerAgregator otherIteratorsAgregator = new RecordIdsListIteratorConsumerAgregator();
		for (int i = 1; i < streamers.size(); i++) {
			SortedIdsStreamer streamer = streamers.get(i);
			otherIteratorsAgregator.add(streamer.iterator());
		}

		return new IntersectionIterator(mainIterator, otherIteratorsAgregator);

	}

	private static class IntersectionIterator extends LazyIterator<RecordId> {

		private final Iterator<RecordId> mainIterator;
		private final RecordIdsListIteratorConsumerAgregator otherIteratorsAgregator;

		public IntersectionIterator(Iterator<RecordId> mainIterator,
									RecordIdsListIteratorConsumerAgregator otherIteratorsAgregator) {
			this.mainIterator = mainIterator;
			this.otherIteratorsAgregator = otherIteratorsAgregator;
		}

		@Override
		protected RecordId getNextOrNull() {
			counter++;
			//				if (counter % 1_000_000 == 0) {
			//					System.out.println((counter / 1_000_000) + "M");
			//				}
			return compute();
		}

		@Nullable
		private RecordId compute() {
			while (mainIterator.hasNext() && !otherIteratorsAgregator.anyFullyConsumed()) {
				RecordId recordId = mainIterator.next();
				if (otherIteratorsAgregator.existsInAll(recordId)) {
					return recordId;
				}

			}

			return null;
		}
	}

	private class RecordIdsListIteratorConsumerAgregator {

		private boolean anyFullyConsumed;
		private List<RecordExistingIdsIteratorConsumer> consumers = new ArrayList<>();

		private void add(Iterator<RecordId> recordIdIterator) {
			consumers.add(new RecordExistingIdsIteratorConsumer(recordIdIterator) {
				@Override
				protected void onIteratorFullyConsumed() {
					anyFullyConsumed = true;
				}
			});
		}

		private boolean existsInAll(RecordId recordId) {
			for (int i = 0; i < consumers.size(); i++) {
				RecordExistingIdsIteratorConsumer consumer = consumers.get(i);
				if (!consumer.exists(recordId)) {
					return false;
				}
			}

			return true;
		}

		private boolean anyFullyConsumed() {
			return anyFullyConsumed;
		}

	}

}
