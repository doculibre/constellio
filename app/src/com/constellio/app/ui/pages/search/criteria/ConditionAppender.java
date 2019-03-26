package com.constellio.app.ui.pages.search.criteria;

import com.constellio.app.ui.pages.search.criteria.Criterion.BooleanOperator;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class ConditionAppender {
	private MetadataSchemaType schemaType;
	private List<LogicalSearchCondition> pending;
	private BooleanOperator pendingOperator;

	public ConditionAppender(MetadataSchemaType schemaType) {
		this.schemaType = schemaType;
		pending = new ArrayList<>();
		pendingOperator = BooleanOperator.AND;
	}

	public LogicalSearchCondition build() {
		return conflatePending();
	}

	public void append(LogicalSearchCondition clause, BooleanOperator operator) {
		if (!operator.equals(pendingOperator)) {
			if (!pending.isEmpty()) {
				LogicalSearchCondition condition = conflatePending();
				pending = new ArrayList<>();
				pending.add(condition);
			} else {
				pending = new ArrayList<>();
			}
			pendingOperator = operator;
		}
		pending.add(clause);
	}

	private LogicalSearchCondition conflatePending() {
		if (pending.size() == 1) {
			return pending.get(0);
		}
		switch (pendingOperator) {
			case AND:
				return from(schemaType).whereAllConditions(pending);
			case OR:
				return from(schemaType).whereAnyCondition(pending);
			case AND_NOT:
				ArrayList<LogicalSearchCondition> result = new ArrayList<>();
				result.add(pending.remove(0));
				for (LogicalSearchCondition condition : pending) {
					result.add(condition.negate());
				}
				return from(schemaType).whereAllConditions(result);
			default:
				throw new RuntimeException("Unknown operator");
		}
	}
}
