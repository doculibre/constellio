package com.constellio.model.services.search.query.logical;

public class LogicalSearchQuerySort {

	private String fieldName;

	private boolean ascending;

	public LogicalSearchQuerySort(String fieldName, boolean ascending) {
		this.fieldName = fieldName;
		this.ascending = ascending;
	}

	public String getFieldName() {
		return fieldName;
	}

	public boolean isAscending() {
		return ascending;
	}
}
