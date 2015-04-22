/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
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
