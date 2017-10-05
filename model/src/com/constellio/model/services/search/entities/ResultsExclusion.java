package com.constellio.model.services.search.entities;

import java.util.List;

public class ResultsExclusion {

	String query;

	List<String> ids;

	public ResultsExclusion(String query, List<String> ids) {
		this.query = query;
		this.ids = ids;
	}

	public String getQuery() {
		return query;
	}

	public List<String> getIds() {
		return ids;
	}
}
