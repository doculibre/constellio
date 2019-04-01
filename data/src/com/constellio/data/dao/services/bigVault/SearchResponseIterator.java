package com.constellio.data.dao.services.bigVault;

import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface SearchResponseIterator<T> extends Iterator<T> {

	long getNumFound();

	SearchResponseIterator<List<T>> inBatches();

	default Spliterator<T> spliterator() {
		return Spliterators.spliterator(this, getNumFound(), 0);
	}

	default Stream<T> stream() {
		return StreamSupport.stream(spliterator(), false);
	}

	default Stream<T> parallelStream() {
		return StreamSupport.stream(spliterator(), true);
	}
}
