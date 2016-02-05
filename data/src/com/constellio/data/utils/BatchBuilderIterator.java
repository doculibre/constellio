package com.constellio.data.utils;

import java.util.ArrayList;
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

		while (nestedIterator.hasNext() && batch.size() < batchSize) {
			batch.add(nestedIterator.next());
		}

		return batch.isEmpty() ? null : batch;
	}
}
