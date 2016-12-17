package com.constellio.model.services.search.query.logical.condition;

import java.util.Arrays;
import java.util.List;

import com.constellio.model.entities.schemas.DataStoreField;
import com.constellio.model.services.search.query.logical.LogicalOperator;
import com.constellio.model.services.search.query.logical.LogicalSearchValueCondition;

@Deprecated
/**
 * Use LogicalSearchConditionBuilder instead
 */
public class ConditionTemplate {

	LogicalOperator operator;

	List<?> fields;

	LogicalSearchValueCondition condition;

	private ConditionTemplate(List<?> fields,
			LogicalSearchValueCondition condition, LogicalOperator operator) {
		this.fields = fields;
		this.condition = condition;
		this.operator = operator;
	}

	public List<?> getFields() {
		return fields;
	}

	public LogicalSearchValueCondition getCondition() {
		return condition;
	}

	public LogicalOperator getOperator() {
		return operator;
	}

	public static ConditionTemplate field(DataStoreField field, LogicalSearchValueCondition condition) {
		return new ConditionTemplate(Arrays.asList(field), condition, LogicalOperator.AND);
	}

	public static ConditionTemplate allFields(List<?> fields, LogicalSearchValueCondition condition) {
		return new ConditionTemplate(fields, condition, LogicalOperator.AND);

	}

	public static ConditionTemplate anyField(List<?> fields, LogicalSearchValueCondition condition) {
		return new ConditionTemplate(fields, condition, LogicalOperator.OR);
	}
}
