package com.constellio.model.services.search.entities;

import java.util.List;

public class ResultsElevation {

	String query;

	List<String> ids;

	public ResultsElevation(String query, List<String> ids) {
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
