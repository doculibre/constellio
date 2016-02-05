package com.constellio.model.services.search.query.logical.criteria;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.constellio.model.entities.schemas.DataStoreField;
import com.constellio.model.services.search.query.logical.LogicalSearchValueCondition;

public class NotCriterion extends LogicalSearchValueCondition {

	private final LogicalSearchValueCondition nestedValueCondition;

	public NotCriterion(LogicalSearchValueCondition nestedValueCondition) {
		super();
		this.nestedValueCondition = nestedValueCondition;
	}

	public LogicalSearchValueCondition getNestedValueCondition() {
		return nestedValueCondition;
	}

	@Override
	public boolean isValidFor(DataStoreField dataStoreField) {
		return true;
	}

	@Override
	public String getSolrQuery(DataStoreField dataStoreField) {
		return "(*:* -(" + nestedValueCondition.getSolrQuery(dataStoreField) + ") )";
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
		return getClass().getSimpleName() + "[" + nestedValueCondition + "]";
	}
}
