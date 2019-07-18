package com.constellio.model.extensions.events.recordsCache;

import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

public class CacheQueryParams {

	LogicalSearchQuery query;

	long duration;

	public CacheQueryParams(LogicalSearchQuery query, long duration) {
		this.query = query;
		this.duration = duration;
	}

	public LogicalSearchQuery getQuery() {
		return query;
	}

	public long getDuration() {
		return duration;
	}
}
