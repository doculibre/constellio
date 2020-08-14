package com.constellio.model.services.search.query.logical.ongoing;

import com.constellio.model.entities.schemas.DataStoreField;
import com.constellio.model.services.search.query.logical.LogicalOperator;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.search.query.logical.LogicalSearchValueCondition;
import com.constellio.model.services.search.query.logical.condition.CompositeLogicalSearchCondition;
import com.constellio.model.services.search.query.logical.condition.DataStoreFieldLogicalSearchCondition;
import com.constellio.model.services.search.query.logical.condition.DataStoreFilters;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.search.query.logical.criteria.MeasuringUnitTime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OngoingLogicalSearchConditionWithDataStoreFields {

	private final LogicalSearchCondition otherCondition;

	private final LogicalOperator otherConditionLogicalOperator;

	private final DataStoreFilters filters;

	private final List<DataStoreField> dataStoreFields;

	private final LogicalOperator metadataLogicalOperator;

	public OngoingLogicalSearchConditionWithDataStoreFields(DataStoreFilters filters,
															List<DataStoreField> dataStoreFields,
															LogicalOperator metadataLogicalOperator) {
		super();
		this.filters = filters;
		this.dataStoreFields = dataStoreFields;
		this.metadataLogicalOperator = metadataLogicalOperator;
		this.otherCondition = null;
		this.otherConditionLogicalOperator = null;
	}

	public OngoingLogicalSearchConditionWithDataStoreFields(DataStoreFilters filters,
															List<DataStoreField> dataStoreFields,
															LogicalOperator metadataLogicalOperator,
															LogicalSearchCondition otherCondition,
															LogicalOperator otherConditionLogicalOperator) {
		super();
		this.filters = filters;
		this.dataStoreFields = dataStoreFields;
		this.metadataLogicalOperator = metadataLogicalOperator;
		this.otherCondition = otherCondition;
		this.otherConditionLogicalOperator = otherConditionLogicalOperator;
	}

	public <T> LogicalSearchCondition is(T value) {
		return is(LogicalSearchQueryOperators.is(value));
	}

	public <T> LogicalSearchCondition isNotEqual(T value) {
		return is(LogicalSearchQueryOperators.isNotEqual(value));
	}

	public <T> LogicalSearchCondition isNot(LogicalSearchValueCondition otherOperator) {
		return is(LogicalSearchQueryOperators.not(otherOperator));
	}

	public LogicalSearchCondition isContainingText(String value) {
		return is(LogicalSearchQueryOperators.containingText(value));
	}

	public LogicalSearchCondition isStartingWithText(String value) {
		return is(LogicalSearchQueryOperators.startingWithText(value));
	}

	public LogicalSearchCondition isStartingWithTextFromAny(List<String> values) {
		List<LogicalSearchValueCondition> logicalSearchValueConditionList = new ArrayList<>();
		for (String value : values) {
			logicalSearchValueConditionList.add(LogicalSearchQueryOperators.startingWithText(value));
		}
		return isAny(logicalSearchValueConditionList);
	}

	public LogicalSearchCondition isContainingTextFromAny(List<String> values) {
		List<LogicalSearchValueCondition> logicalSearchValueConditionList = new ArrayList<>();
		for (String value : values) {
			logicalSearchValueConditionList.add(LogicalSearchQueryOperators.containingText(value));
		}
		return isAny(logicalSearchValueConditionList);
	}

	public LogicalSearchCondition isEndingWithText(String value) {
		return is(LogicalSearchQueryOperators.endingWithText(value));
	}

	public <T> LogicalSearchCondition isIn(List<T> values) {
		return is(LogicalSearchQueryOperators.in(values));
	}

	public <T> LogicalSearchCondition isNotIn(List<T> values) {
		return is(LogicalSearchQueryOperators.notIn(values));
	}

	// public <T> LogicalSearchCondition isIn(LogicalSearchCondition condition) {
	// return is(LogicalSearchQueryOperators.in(condition));
	// }

	// public <T> LogicalSearchCondition isNotIn(LogicalSearchCondition condition) {
	// return is(LogicalSearchQueryOperators.notIn(condition));
	// }

	public <T> LogicalSearchCondition isContaining(List<T> values) {
		return is(LogicalSearchQueryOperators.containing(values));
	}

	public <T> LogicalSearchCondition isNotContainingElements(List<T> values) {
		return is(LogicalSearchQueryOperators.notContainingElements(values));
	}

	public <T> LogicalSearchCondition isNull() {
		return is(LogicalSearchQueryOperators.isNull());
	}

	public <T> LogicalSearchCondition isNotNull() {
		return is(LogicalSearchQueryOperators.isNotNull());
	}

	public <T> LogicalSearchCondition isAll(List<LogicalSearchValueCondition> otherOperators) {
		return is(LogicalSearchQueryOperators.all(otherOperators));
	}

	public <T> LogicalSearchCondition isAny(List<LogicalSearchValueCondition> otherOperators) {
		return is(LogicalSearchQueryOperators.any(otherOperators));
	}

	public <T> LogicalSearchCondition isAny(LogicalSearchValueCondition... otherOperators) {
		return is(LogicalSearchQueryOperators.any(Arrays.asList(otherOperators)));
	}

	public <T> LogicalSearchCondition is(LogicalSearchValueCondition otherOperator) {
		LogicalSearchCondition condition = new DataStoreFieldLogicalSearchCondition(filters, dataStoreFields,
				metadataLogicalOperator, otherOperator);
		condition.validate();
		if (otherCondition != null) {
			condition = new CompositeLogicalSearchCondition(filters, otherConditionLogicalOperator,
					Arrays.asList(otherCondition, condition));
		}
		return condition;
	}

	public LogicalSearchCondition isTrue() {
		return is(LogicalSearchQueryOperators.isTrue());
	}

	public LogicalSearchCondition isTrueOrNull() {
		return is(LogicalSearchQueryOperators.isTrueOrNull());
	}

	public LogicalSearchCondition isFalse() {
		return is(LogicalSearchQueryOperators.isFalse());
	}

	public LogicalSearchCondition isFalseOrNull() {
		return is(LogicalSearchQueryOperators.isFalseOrNull());
	}

	public LogicalSearchCondition isValueInRange(Object beginValue, Object endValue) {
		return is(LogicalSearchQueryOperators.valueInRange(beginValue, endValue));
	}

	public LogicalSearchCondition isLessThan(Object value) {
		return is(LogicalSearchQueryOperators.lessThan(value));
	}

	public LogicalSearchCondition isGreaterThan(Object value) {
		return is(LogicalSearchQueryOperators.greaterThan(value));
	}

	public LogicalSearchCondition isLessOrEqualThan(Object value) {
		return is(LogicalSearchQueryOperators.lessOrEqualThan(value));
	}

	public LogicalSearchCondition isGreaterOrEqualThan(Object value) {
		return is(LogicalSearchQueryOperators.greaterOrEqualThan(value));
	}

	public LogicalSearchCondition isNewerThan(Object value, MeasuringUnitTime measuringUnitTime) {
		return is(LogicalSearchQueryOperators.newerThan(value, measuringUnitTime));
	}

	public LogicalSearchCondition isOlderThan(Object value, MeasuringUnitTime measuringUnitTime) {
		return is(LogicalSearchQueryOperators.olderThan(value, measuringUnitTime));
	}

	public LogicalSearchCondition isOldLike(Object value, MeasuringUnitTime measuringUnitTime) {
		return is(LogicalSearchQueryOperators.oldLike(value, measuringUnitTime));
	}

	public LogicalSearchCondition isEqualTo(Object value) {
		return is(LogicalSearchQueryOperators.equal(value));
	}

	public LogicalSearchCondition query(String query) {
		return is(LogicalSearchQueryOperators.query(query));
	}
}
