package com.constellio.model.services.search.query.logical;

import java.util.Arrays;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.constellio.model.entities.schemas.DataStoreField;
import com.constellio.model.services.search.query.logical.criteria.CompositeLogicalSearchValueOperator;

public abstract class LogicalSearchValueCondition {

	public LogicalSearchValueCondition and(LogicalSearchValueCondition condition) {

		return new CompositeLogicalSearchValueOperator(LogicalOperator.AND, Arrays.asList(this, condition));
	}

	public LogicalSearchValueCondition or(LogicalSearchValueCondition condition) {
		return new CompositeLogicalSearchValueOperator(LogicalOperator.OR, Arrays.asList(this, condition));
	}

	public abstract boolean isValidFor(DataStoreField metadata);

	public abstract String getSolrQuery(DataStoreField metadata);

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

}
