package com.constellio.model.services.search.query.logical.condition;

import java.util.List;

import com.constellio.model.services.search.query.logical.LogicalSearchValueCondition;

public class NegatedLogicalSearchCondition extends LogicalSearchCondition {
	private final LogicalSearchCondition negated;

	public NegatedLogicalSearchCondition(LogicalSearchCondition negated) {
		super(negated.getFilters());
		this.negated = negated;
	}

	@Override
	public void validate() {
		negated.validate();
	}

	@Override
	public LogicalSearchCondition withFilters(DataStoreFilters filters) {
		return new NegatedLogicalSearchCondition(negated.withFilters(filters));
	}

	@Override
	public LogicalSearchCondition withOrValueConditions(List<LogicalSearchValueCondition> conditions) {
		throw new UnsupportedOperationException("Cannot add value conditions on a negated condition");
	}

	@Override
	public LogicalSearchCondition withAndValueConditions(List<LogicalSearchValueCondition> conditions) {
		throw new UnsupportedOperationException("Cannot add value conditions on a negated condition");
	}

	@Override
	public LogicalSearchCondition negated() {
		return negated;
	}

	@Override
	public String getSolrQuery(SolrQueryBuilderParams params) {
		return "( *:* -" + negated.getSolrQuery(params) + " )";
	}
}
