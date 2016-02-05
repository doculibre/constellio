package com.constellio.model.services.search.query.logical.criteria;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.constellio.model.entities.schemas.DataStoreField;
import com.constellio.model.services.search.query.logical.LogicalSearchValueCondition;

public class QueryCriterion extends LogicalSearchValueCondition {

	private final String query;

	public QueryCriterion(String value) {
		super();
		this.query = value;
	}

	public String getValue() {
		return query;
	}

	@Override
	public boolean isValidFor(DataStoreField dataStoreField) {
		return true;
	}

	@Override
	public String getSolrQuery(DataStoreField dataStoreField) {
		String correctedText = correctString();
		return dataStoreField.getDataStoreCode() + ":" + correctedText;
	}

	private String correctString() {
		if (!query.startsWith("[") && !query.startsWith("{")) {
			return query.replaceAll(" ", "\\\\ ");
		} else {
			return query;
		}

	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + ":" + query;
	}

}
