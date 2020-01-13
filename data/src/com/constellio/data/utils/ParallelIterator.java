package com.constellio.data.utils;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public class ParallelIterator<T> extends LazyIterator<T> implements Closeable {

	LinkedBlockingQueue<List<T>> queue = new LinkedBlockingQueue<>(5);
	Iterator<List<T>> parallelyConsumedBatchIterator;
	Iterator<T> localIterator;
	Thread thread;
	boolean running = true;

	public ParallelIterator(Iterator<List<T>> batchIterator) {
		this.parallelyConsumedBatchIterator = batchIterator;

		thread = new Thread() {
			@Override
			public void run() {
				try {
					while (running) {
						if (parallelyConsumedBatchIterator.hasNext()) {

							queue.put(parallelyConsumedBatchIterator.next());

						} else {
							running = false;
						}
					}

					queue.put(new ArrayList<>());
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}

		};
		thread.start();
	}

	public ParallelIterator(Iterator<T> batchIterator, int batchSize) {
		this(new BatchBuilderIterator<>(batchIterator, batchSize));
	}


	@Override
	protected T getNextOrNull() {
		if (localIterator == null || !localIterator.hasNext()) {
			List<T> list = null;
			try {
				list = queue.take();
			} catch (InterruptedException e) {
				return null;
			}
			if (list.isEmpty()) {
				return null;
			} else {
				localIterator = list.iterator();
			}
		}
		return localIterator.next();
	}

	@Override
	public void close() throws IOException {
		running = false;
		queue.poll();
	}
}
