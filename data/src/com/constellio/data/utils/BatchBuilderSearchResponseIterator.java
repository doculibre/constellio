package com.constellio.data.utils;

import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.formula.functions.T;

import com.constellio.data.dao.services.bigVault.SearchResponseIterator;

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
