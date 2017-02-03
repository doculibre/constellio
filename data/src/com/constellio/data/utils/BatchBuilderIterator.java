package com.constellio.data.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class BatchBuilderIterator<T> extends LazyIterator<List<T>> {

	private Iterator<T> nestedIterator;

	private int batchSize;

	public BatchBuilderIterator(Iterator<T> nestedIterator, int batchSize) {
		this.nestedIterator = nestedIterator;
		this.batchSize = batchSize;
	}

	@Override
	protected List<T> getNextOrNull() {
		List<T> batch = new ArrayList<>();

		while (batch.size() < batchSize && nestedIterator.hasNext()) {
			batch.add(nestedIterator.next());
		}

		return batch.isEmpty() ? null : batch;
	}

	public static <T> BatchBuilderIterator<T> forListIterator(final Iterator<List<T>> iterator, int batchSize) {

		Iterator<T> allElements = new LazyIterator<T>() {

			Iterator<T> currentIterator = null;

			@Override
			protected T getNextOrNull() {
				if (currentIterator == null || !currentIterator.hasNext()) {
					if (iterator.hasNext()) {
						Collection<T> nextCollection = iterator.next();
						if (nextCollection != null) {
							currentIterator = nextCollection.iterator();
						}
						return getNextOrNull();
					} else {
						return null;
					}
				} else {
					return currentIterator.next();
				}

			}
		};

		return new BatchBuilderIterator<>(allElements, batchSize);
	}


}
