package com.constellio.model.services.search.query.logical;

import com.constellio.model.services.users.SystemWideUserInfos;
import org.apache.solr.common.params.SolrParams;

public class FreeTextQuery {

	SolrParams solrParams;

	private boolean searchingEvents;

	private SystemWideUserInfos userFilter;

	public FreeTextQuery(SolrParams solrParams) {
		this.solrParams = solrParams;
	}

	public FreeTextQuery filteredByUser(SystemWideUserInfos multiCollectionUserFilter) {
		this.userFilter = multiCollectionUserFilter;
		return this;
	}

	public SolrParams getSolrParams() {
		return solrParams;
	}

	public SystemWideUserInfos getUserFilter() {
		return userFilter;
	}

	public boolean isSearchingEvents() {
		return searchingEvents;
	}

	public FreeTextQuery searchEvents() {
		searchingEvents = true;
		return this;
	}
}
