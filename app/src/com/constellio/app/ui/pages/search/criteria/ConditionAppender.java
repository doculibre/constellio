package com.constellio.app.ui.pages.search.criteria;

import com.constellio.app.ui.pages.search.criteria.Criterion.BooleanOperator;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.search.query.logical.ongoing.OngoingLogicalSearchCondition;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class ConditionAppender {
	private MetadataSchemaType schemaType;
	private String schemaCode;
	private List<LogicalSearchCondition> pending;
	private BooleanOperator pendingOperator;

	public ConditionAppender(MetadataSchemaType schemaType) {
		this(schemaType, "");
	}

	public ConditionAppender(MetadataSchemaType schemaType, String schemaCode) {
		this.schemaType = schemaType;
		this.schemaCode = schemaCode;
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
				return generateFrom().whereAllConditions(pending);
			case OR:
				return generateFrom().whereAnyCondition(pending);
			case AND_NOT:
				ArrayList<LogicalSearchCondition> result = new ArrayList<>();
				result.add(pending.remove(0));
				for (LogicalSearchCondition condition : pending) {
					result.add(condition.negate());
				}
				return generateFrom().whereAllConditions(result);
			default:
				throw new RuntimeException("Unknown operator");
		}
	}

	private OngoingLogicalSearchCondition generateFrom() {
		if (StringUtils.isBlank(schemaCode)) {
			return from(schemaType);
		}

		return from(schemaType.getSchema(schemaCode));
	}
}
