package com.constellio.data.utils;

import java.util.Iterator;

import com.constellio.data.utils.LazyIteratorRuntimeException.LazyIteratorRuntimeException_IncorrectUsage;
import com.constellio.data.utils.LazyIteratorRuntimeException.LazyIteratorRuntimeException_RemoveNotAvailable;

public abstract class LazyIterator<T> implements Iterator<T> {

	boolean consumed = true;
	T next;

	@Override
	public final boolean hasNext() {
		if (consumed) {
			next = getNextOrNull();
			consumed = false;
		}
		return next != null;
	}

	@Override
	public final T next() {
		if (consumed) {
			next = getNextOrNull();
			consumed = false;
		}
		if (next == null) {
			throw new LazyIteratorRuntimeException_IncorrectUsage();
		}

		consumed = true;
		return next;
	}

	@Override
	public final void remove() {
		throw new LazyIteratorRuntimeException_RemoveNotAvailable();
	}

	protected abstract T getNextOrNull();
}
