package com.constellio.data.utils;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public class ParallelQueuingIterator<T> extends LazyIterator<T> implements Iterator<T> {

	Iterator<T> nestedIterator;

	int batchSize;
	int currentBatchIndex = 0;
	List<T> currentBatch = null;
	LinkedBlockingQueue<List<T>> nextBatches;

	Thread thread;

	public ParallelQueuingIterator(Iterator<T> nestedIterator, int batchSize, int nbBatches) {
		this.nestedIterator = nestedIterator;
		this.nextBatches = new LinkedBlockingQueue<>(nbBatches);
		this.batchSize = batchSize;
		this.thread = new Thread() {
			@Override
			public void run() {

				boolean closed = false;
				while (nestedIterator.hasNext()) {
					List<T> newBatch = new ArrayList<>(batchSize);
					while (nestedIterator.hasNext() && newBatch.size() < batchSize) {
						newBatch.add(nestedIterator.next());
					}

					try {
						nextBatches.put(newBatch);
						closed = newBatch.isEmpty();
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
				}

				if (!closed) {
					try {
						nextBatches.put(Collections.emptyList());
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
				}
			}
		};
		this.thread.start();
	}

	@Override
	protected T getNextOrNull() {

		if (currentBatch == null || currentBatchIndex >= currentBatch.size()) {
			retrieveNewBatch();
			if (currentBatch.size() == 0) {
				return null;
			}
		}

		return (T) currentBatch.get(currentBatchIndex++);
	}

	private void retrieveNewBatch() {
		try {
			currentBatch = nextBatches.take();
			currentBatchIndex = 0;
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
}
