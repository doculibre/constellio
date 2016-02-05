package com.constellio.model.services.search.query.logical;

import org.apache.solr.common.params.SolrParams;

import com.constellio.model.entities.security.global.UserCredential;

public class FreeTextQuery {

	SolrParams solrParams;

	private boolean searchingEvents;

	private UserCredential userFilter;

	public FreeTextQuery(SolrParams solrParams) {
		this.solrParams = solrParams;
	}

	public FreeTextQuery filteredByUser(UserCredential multiCollectionUserFilter) {
		this.userFilter = multiCollectionUserFilter;
		return this;
	}

	public SolrParams getSolrParams() {
		return solrParams;
	}

	public UserCredential getUserFilter() {
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
