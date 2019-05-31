package com.constellio.model.services.search.query.logical.condition;

import com.constellio.model.entities.records.Record;
import com.constellio.model.services.search.query.logical.LogicalSearchValueCondition;

import java.util.List;

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
		throw new UnsupportedOperationException("Cannot add value conditions on a negate condition");
	}

	@Override
	public LogicalSearchCondition withAndValueConditions(List<LogicalSearchValueCondition> conditions) {
		throw new UnsupportedOperationException("Cannot add value conditions on a negate condition");
	}

	@Override
	public LogicalSearchCondition negate() {
		return negated;
	}

	@Override
	public String getSolrQuery(SolrQueryBuilderContext params) {
		return "( *:* -" + negated.getSolrQuery(params) + " )";
	}

	@Override
	public boolean isSupportingMemoryExecution() {
		return negated.isSupportingMemoryExecution();
	}


	@Override
	public boolean test(Record record) {
		return !negated.test(record);
	}
}
