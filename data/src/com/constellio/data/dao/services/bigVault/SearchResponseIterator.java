package com.constellio.data.dao.services.bigVault;

import java.util.Iterator;
import java.util.List;

import com.constellio.data.utils.BatchBuilderIterator;

public interface SearchResponseIterator<T> extends Iterator<T> {

	long getNumFound();

	SearchResponseIterator<List<T>> inBatches();
}
