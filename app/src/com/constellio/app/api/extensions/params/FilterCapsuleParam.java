package com.constellio.app.api.extensions.params;

import com.constellio.model.entities.records.wrappers.Capsule;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import org.apache.solr.common.params.ModifiableSolrParams;

public class FilterCapsuleParam {
	
	private Capsule capsule;
	private ModifiableSolrParams solrParams;
	private LogicalSearchQuery query;

	public FilterCapsuleParam(Capsule capsule, LogicalSearchQuery query) {
		this.capsule = capsule;
		this.query = query;
	}

	public FilterCapsuleParam(Capsule capsule, ModifiableSolrParams solrParams) {
		this.capsule = capsule;
		this.solrParams = solrParams;
	}

	public Capsule getCapsule() {
		return capsule;
	}
	
	public LogicalSearchQuery getQuery() {
		return query;
	}

	public ModifiableSolrParams getSolrParams() {
		return solrParams;
	}
}
