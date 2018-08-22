package com.constellio.model.services.search.query.logical;

import com.constellio.model.entities.schemas.DataStoreField;

public class ScoreLogicalSearchQuerySort implements LogicalSearchQuerySort {

	static final private String SCORE_FIELD = "score";
	private boolean ascending;

	public ScoreLogicalSearchQuerySort(boolean ascending) {
		this.ascending = ascending;
	}

	public String getField() {
		return SCORE_FIELD;
	}

	public boolean isAscending() {
		return ascending;
	}
}
