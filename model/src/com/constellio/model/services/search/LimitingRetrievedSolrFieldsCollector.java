package com.constellio.model.services.search;

import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

import java.util.stream.Collector;

public interface LimitingRetrievedSolrFieldsCollector<T, A, R> extends Collector<T, A, R> {

	void filterFields(LogicalSearchQuery query);

}
