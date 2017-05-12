package com.constellio.data.utils;

import java.util.Iterator;

public class InfiniteIteratorIterator<V> extends LazyIterator<V> {

	Factory<Iterator<V>> iteratorFactory;
	Iterator<V> currentIterator;

	public InfiniteIteratorIterator(Factory<Iterator<V>> iteratorFactory) {
		this.iteratorFactory = iteratorFactory;
	}

	@Override
	protected synchronized V getNextOrNull() {
		if (currentIterator == null || !currentIterator.hasNext()) {
			currentIterator = iteratorFactory.get();
		}
		return currentIterator.next();
	}

	public static <V> Iterator<V> infinitelyIteratingOverIteratorFactory(Factory<Iterator<V>> iteratorFactory) {
		return new InfiniteIteratorIterator<>(iteratorFactory);
	}
}