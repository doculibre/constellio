package com.constellio.data.utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class LazyMergingIterator<T> extends LazyIterator<T> {

	private int index = 0;
	private Iterator<T> currentIterator;

	private List<Iterator<T>> iterators;
	private Set<String> returnedKeys = new HashSet<>();


	public LazyMergingIterator(List<Iterator<T>> iterators) {
		this.iterators = iterators;
	}

	public LazyMergingIterator(Iterator<T>... iterators) {
		this.iterators = Arrays.asList(iterators);
	}

	@Override
	protected final T getNextOrNull() {

		T next = getNextUsingIterators();

		String uniqueKey = toUniqueKey(next);
		if (uniqueKey == null) {
			return next;

		} else if (!returnedKeys.contains(uniqueKey)) {
			returnedKeys.add(uniqueKey);
			return next;

		} else {
			return getNextOrNull();
		}
	}

	protected String toUniqueKey(T element) {
		return null;
	}

	private T getNextUsingIterators() {
		if (currentIterator != null && currentIterator.hasNext()) {
			return currentIterator.next();
		}

		if (index >= iterators.size()) {
			return null;

		} else {
			currentIterator = iterators.get(index);
			index++;
			return getNextUsingIterators();
		}
	}

}
