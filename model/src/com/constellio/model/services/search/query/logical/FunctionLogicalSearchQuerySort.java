package com.constellio.model.services.search.query.logical;

public class FunctionLogicalSearchQuerySort implements LogicalSearchQuerySort {

	private String function;

	private boolean ascending;

	public FunctionLogicalSearchQuerySort(String function, boolean ascending) {
		this.function = function;
		this.ascending = ascending;
	}

	public String getFunction() {
		return function;
	}

	public boolean isAscending() {
		return ascending;
	}
}
