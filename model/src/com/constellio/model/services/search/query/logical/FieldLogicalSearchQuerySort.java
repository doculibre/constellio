package com.constellio.model.services.search.query.logical;

import com.constellio.model.entities.schemas.DataStoreField;

public class FieldLogicalSearchQuerySort implements LogicalSearchQuerySort {

	private DataStoreField field;

	private boolean ascending;

	public FieldLogicalSearchQuerySort(DataStoreField field, boolean ascending) {
		this.field = field;
		this.ascending = ascending;
	}

	public DataStoreField getField() {
		return field;
	}

	public boolean isAscending() {
		return ascending;
	}
}
