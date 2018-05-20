package com.constellio.app.api.extensions.params;

import com.constellio.model.entities.records.wrappers.Capsule;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

public class FilterCapsuleParam {
	
	private Capsule capsule;
	
	private LogicalSearchQuery query;

	public FilterCapsuleParam(Capsule capsule, LogicalSearchQuery query) {
		this.capsule = capsule;
		this.query = query;
	}
	
	public Capsule getCapsule() {
		return capsule;
	}
	
	public LogicalSearchQuery getQuery() {
		return query;
	}

}
