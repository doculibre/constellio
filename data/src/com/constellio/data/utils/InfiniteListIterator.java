package com.constellio.data.utils;

import java.util.Iterator;
import java.util.List;

public class InfiniteListIterator<V> extends LazyIterator<V> {

	List<V> values;
	Iterator<V> currentIterator;

	public InfiniteListIterator(List<V> values) {
		this.values = values;
	}

	@Override
	protected synchronized V getNextOrNull() {
		if (currentIterator == null || !currentIterator.hasNext()) {
			currentIterator = values.iterator();
		}
		return currentIterator.next();
	}

	public static <V> Iterator<V> infinitelyIteratingOverList(List<V> values) {
		return new InfiniteListIterator<>(values);
	}
}