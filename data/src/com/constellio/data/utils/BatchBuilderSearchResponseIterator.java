package com.constellio.data.utils;

import com.constellio.data.dao.services.bigVault.SearchResponseIterator;

import java.util.Iterator;
import java.util.List;

public abstract class BatchBuilderSearchResponseIterator<T> extends BatchBuilderIterator<T>
		implements SearchResponseIterator<List<T>> {

	public BatchBuilderSearchResponseIterator(Iterator<T> nestedIterator, int batchSize) {
		super(nestedIterator, batchSize);
	}

	@Override
	public SearchResponseIterator<List<List<T>>> inBatches() {
		throw new UnsupportedOperationException("Already a batch iterator");
	}
}
