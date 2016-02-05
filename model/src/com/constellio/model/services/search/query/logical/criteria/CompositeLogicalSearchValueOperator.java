package com.constellio.model.services.search.query.logical.criteria;

import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.constellio.model.entities.schemas.DataStoreField;
import com.constellio.model.services.search.query.logical.LogicalOperator;
import com.constellio.model.services.search.query.logical.LogicalSearchValueCondition;

public class CompositeLogicalSearchValueOperator extends LogicalSearchValueCondition {

	LogicalOperator operator;

	List<LogicalSearchValueCondition> conditions;

	public CompositeLogicalSearchValueOperator(LogicalOperator operator, List<LogicalSearchValueCondition> conditions) {
		super();
		this.operator = operator;
		this.conditions = conditions;
	}

	@Override
	public boolean isValidFor(DataStoreField dataStoreField) {
		boolean valid = true;
		for (LogicalSearchValueCondition condition : conditions) {
			valid &= condition.isValidFor(dataStoreField);
		}
		return valid;
	}

	@Override
	public String getSolrQuery(DataStoreField dataStoreField) {
		String query = "(";

		for (int i = 0; i < conditions.size() - 1; i++) {
			query += " " + conditions.get(i).getSolrQuery(dataStoreField) + " " + operator;
		}

		query += " " + conditions.get(conditions.size() - 1).getSolrQuery(dataStoreField);

		query += " )";

		return query;
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
		return operator.name() + conditions;
	}

}
