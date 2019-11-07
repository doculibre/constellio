package com.constellio.data.utils;

import java.util.Iterator;
import java.util.List;

public class BatchConsumerIterator<T> extends LazyIterator<T> {

	private Iterator<List<T>> nestedIterator;

	private Iterator<T> current;

	public BatchConsumerIterator(Iterator<List<T>> nestedIterator) {
		this.nestedIterator = nestedIterator;
	}

	@Override
	protected T getNextOrNull() {
		while (current == null || !current.hasNext()) {
			if (nestedIterator.hasNext()) {
				current = nestedIterator.next().iterator();
			} else {
				return null;
			}
		}

		return current.next();
	}

}
