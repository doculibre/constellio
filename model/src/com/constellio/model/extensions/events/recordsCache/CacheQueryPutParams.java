package com.constellio.model.extensions.events.recordsCache;

import java.util.List;

import com.constellio.model.services.search.query.logical.LogicalSearchQuerySignature;

public class CacheQueryPutParams {

	LogicalSearchQuerySignature signature;
	List<String> ids;
	long duration;

	public CacheQueryPutParams(LogicalSearchQuerySignature signature, List<String> ids, long duration) {
		this.signature = signature;
		this.ids = ids;
		this.duration = duration;
	}

	public LogicalSearchQuerySignature getSignature() {
		return signature;
	}

	public List<String> getIds() {
		return ids;
	}

	public long getDuration() {
		return duration;
	}
}
