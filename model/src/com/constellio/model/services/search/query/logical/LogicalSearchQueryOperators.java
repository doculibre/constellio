package com.constellio.model.services.search.query.logical;

import java.util.Arrays;
import java.util.List;

import com.constellio.model.entities.schemas.DataStoreField;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.search.query.logical.condition.CollectionFilters;
import com.constellio.model.services.search.query.logical.condition.CompositeLogicalSearchCondition;
import com.constellio.model.services.search.query.logical.condition.DataStoreFieldLogicalSearchCondition;
import com.constellio.model.services.search.query.logical.condition.DataStoreFilters;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.search.query.logical.condition.NegatedLogicalSearchCondition;
import com.constellio.model.services.search.query.logical.condition.SchemaFilters;
import com.constellio.model.services.search.query.logical.condition.SchemaTypesFilters;
import com.constellio.model.services.search.query.logical.criteria.CompositeLogicalSearchValueOperator;
import com.constellio.model.services.search.query.logical.criteria.IsContainingElementsCriterion;
import com.constellio.model.services.search.query.logical.criteria.IsContainingTextCriterion;
import com.constellio.model.services.search.query.logical.criteria.IsEndingWithTextCriterion;
import com.constellio.model.services.search.query.logical.criteria.IsEqualCriterion;
import com.constellio.model.services.search.query.logical.criteria.IsFalseCriterion;
import com.constellio.model.services.search.query.logical.criteria.IsFalseOrNullCriterion;
import com.constellio.model.services.search.query.logical.criteria.IsGreaterOrEqualThanCriterion;
import com.constellio.model.services.search.query.logical.criteria.IsGreaterThanCriterion;
import com.constellio.model.services.search.query.logical.criteria.IsInCriterion;
import com.constellio.model.services.search.query.logical.criteria.IsLessOrEqualThanCriterion;
import com.constellio.model.services.search.query.logical.criteria.IsLessThanCriterion;
import com.constellio.model.services.search.query.logical.criteria.IsNewerThanCriterion;
import com.constellio.model.services.search.query.logical.criteria.IsNotContainingElementsCriterion;
import com.constellio.model.services.search.query.logical.criteria.IsNotEqualCriterion;
import com.constellio.model.services.search.query.logical.criteria.IsNotInCriterion;
import com.constellio.model.services.search.query.logical.criteria.IsNotNullCriterion;
import com.constellio.model.services.search.query.logical.criteria.IsNullCriterion;
import com.constellio.model.services.search.query.logical.criteria.IsOldLikeCriterion;
import com.constellio.model.services.search.query.logical.criteria.IsOlderThanCriterion;
import com.constellio.model.services.search.query.logical.criteria.IsStartingWithTextCriterion;
import com.constellio.model.services.search.query.logical.criteria.IsTrueCriterion;
import com.constellio.model.services.search.query.logical.criteria.IsTrueOrNullCriterion;
import com.constellio.model.services.search.query.logical.criteria.IsValueInRangeCriterion;
import com.constellio.model.services.search.query.logical.criteria.MeasuringUnitTime;
import com.constellio.model.services.search.query.logical.criteria.NotCriterion;
import com.constellio.model.services.search.query.logical.criteria.QueryCriterion;
import com.constellio.model.services.search.query.logical.ongoing.OngoingLogicalSearchCondition;
import com.constellio.model.services.search.query.logical.ongoing.OngoingLogicalSearchConditionWithDataStoreFields;

public class LogicalSearchQueryOperators {

	public static OngoingLogicalSearchCondition from(MetadataSchema schema) {
		return new OngoingLogicalSearchCondition(new SchemaFilters(schema));
	}

	public static OngoingLogicalSearchCondition from(MetadataSchemaType schemaType) {
		return new OngoingLogicalSearchCondition(new SchemaFilters(schemaType));
	}

	public static OngoingLogicalSearchCondition from(List<MetadataSchemaType> schemaTypes) {
		return new OngoingLogicalSearchCondition(new SchemaTypesFilters(schemaTypes));
	}

	public static OngoingLogicalSearchCondition fromAllSchemasExcept(List<MetadataSchemaType> schemaTypes) {
		return new OngoingLogicalSearchCondition(new SchemaTypesFilters(schemaTypes, true));
	}

	public static OngoingLogicalSearchCondition fromAllSchemasIn(String collection) {
		return new OngoingLogicalSearchCondition(new CollectionFilters(collection, false));
	}

	public static OngoingLogicalSearchCondition fromAllSchemasInExceptEvents(String collection) {
		return new OngoingLogicalSearchCondition(new CollectionFilters(collection, true));
	}

	public static OngoingLogicalSearchConditionWithDataStoreFields whereAll(DataStoreField... metadatas) {
		return new OngoingLogicalSearchConditionWithDataStoreFields(new CollectionFilters(metadatas[0].getCollection(), false),
				Arrays.asList(metadatas),
				LogicalOperator.AND);
	}

	public static OngoingLogicalSearchConditionWithDataStoreFields whereAll(List<?> dataStoreFields) {
		List<DataStoreField> fields = (List<DataStoreField>) dataStoreFields;
		return new OngoingLogicalSearchConditionWithDataStoreFields(new CollectionFilters(fields.get(0).getCollection(), false),
				fields,
				LogicalOperator.AND);
	}

	public static OngoingLogicalSearchConditionWithDataStoreFields whereAny(List<?> dataStoreFields) {
		List<DataStoreField> fields = (List<DataStoreField>) dataStoreFields;
		return new OngoingLogicalSearchConditionWithDataStoreFields(new CollectionFilters(fields.get(0).getCollection(), false),
				fields, LogicalOperator.OR);
	}

	public static OngoingLogicalSearchConditionWithDataStoreFields where(DataStoreField metadata) {
		return new OngoingLogicalSearchConditionWithDataStoreFields(new CollectionFilters(metadata.getCollection(), false),
				Arrays.asList(metadata),
				LogicalOperator.AND);
	}

	public static LogicalSearchCondition returnAll() {
		return new DataStoreFieldLogicalSearchCondition(null);
	}

	public static LogicalSearchCondition allConditions(LogicalSearchCondition... otherOperators) {
		return allConditions(Arrays.asList(otherOperators));
	}

	public static LogicalSearchCondition allConditions(List<LogicalSearchCondition> otherOperators) {
		DataStoreFilters filters = otherOperators.get(0).getFilters();
		return new CompositeLogicalSearchCondition(filters, LogicalOperator.AND, otherOperators);
	}

	public static LogicalSearchCondition anyConditions(LogicalSearchCondition... otherOperators) {
		return anyConditions(Arrays.asList(otherOperators));
	}

	public static LogicalSearchCondition not(LogicalSearchCondition otherOperator) {
		return new NegatedLogicalSearchCondition(otherOperator);
	}

	public static LogicalSearchCondition anyConditions(List<LogicalSearchCondition> otherOperators) {
		DataStoreFilters filters = otherOperators.get(0).getFilters();
		return new CompositeLogicalSearchCondition(filters, LogicalOperator.OR, otherOperators);
	}

	public static <T> LogicalSearchValueCondition is(T value) {
		return new IsEqualCriterion(value);
	}

	public static <T> LogicalSearchValueCondition isNotEqual(T value) {
		return new IsNotEqualCriterion(value);
	}

	public static <T> LogicalSearchValueCondition in(List<T> values) {
		return new IsInCriterion(values);
	}

	public static <T> LogicalSearchValueCondition notIn(List<T> values) {
		return new IsNotInCriterion(values);
	}

	public static <T> LogicalSearchValueCondition containing(List<T> values) {
		return new IsContainingElementsCriterion(values);
	}

	public static <T> LogicalSearchValueCondition notContainingElements(List<T> values) {
		return new IsNotContainingElementsCriterion(values);
	}

	public static <T> LogicalSearchValueCondition isNull() {
		return new IsNullCriterion();
	}

	public static <T> LogicalSearchValueCondition isNotNull() {
		return new IsNotNullCriterion();
	}

	public static LogicalSearchValueCondition containingText(String value) {
		return new IsContainingTextCriterion(value);
	}

	public static <T> LogicalSearchValueCondition startingWithText(String value) {
		return new IsStartingWithTextCriterion(value);
	}

	public static <T> LogicalSearchValueCondition endingWithText(String value) {
		return new IsEndingWithTextCriterion(value);
	}

	public static <T> LogicalSearchValueCondition all(LogicalSearchValueCondition... otherOperators) {
		return all(Arrays.asList(otherOperators));
	}

	public static <T> LogicalSearchValueCondition all(List<LogicalSearchValueCondition> otherOperators) {
		return new CompositeLogicalSearchValueOperator(LogicalOperator.AND, otherOperators);
	}

	public static <T> LogicalSearchValueCondition any(List<LogicalSearchValueCondition> otherOperators) {
		return new CompositeLogicalSearchValueOperator(LogicalOperator.OR, otherOperators);
	}

	public static <T> LogicalSearchValueCondition any(LogicalSearchValueCondition... otherOperators) {
		return any(Arrays.asList(otherOperators));
	}

	public static <T> LogicalSearchValueCondition not(LogicalSearchValueCondition otherOperator) {
		return new NotCriterion(otherOperator);
	}

	public static LogicalSearchValueCondition isTrue() {
		return new IsTrueCriterion();
	}

	public static LogicalSearchValueCondition isTrueOrNull() {
		return new IsTrueOrNullCriterion();
	}

	public static LogicalSearchValueCondition isFalse() {
		return new IsFalseCriterion();
	}

	public static LogicalSearchValueCondition isFalseOrNull() {
		return new IsFalseOrNullCriterion();
	}

	public static LogicalSearchValueCondition valueInRange(Object beginIndex, Object endIndex) {
		return new IsValueInRangeCriterion(beginIndex, endIndex);
	}

	public static LogicalSearchValueCondition lessThan(Object index) {
		return new IsLessThanCriterion(index);
	}

	public static LogicalSearchValueCondition greaterThan(Object index) {
		return new IsGreaterThanCriterion(index);
	}

	public static LogicalSearchValueCondition lessOrEqualThan(Object index) {
		return new IsLessOrEqualThanCriterion(index);

	}

	public static LogicalSearchValueCondition newerThan(Object value, MeasuringUnitTime measuringUnitTime) {
		return new IsNewerThanCriterion(value, measuringUnitTime);
	}

	public static LogicalSearchValueCondition olderThan(Object value, MeasuringUnitTime measuringUnitTime) {
		return new IsOlderThanCriterion(value, measuringUnitTime);
	}

	public static LogicalSearchValueCondition oldLike(Object value, MeasuringUnitTime measuringUnitTime) {
		return new IsOldLikeCriterion(value, measuringUnitTime);
	}

	public static LogicalSearchValueCondition greaterOrEqualThan(Object index) {
		return new IsGreaterOrEqualThanCriterion(index);
	}

	public static LogicalSearchValueCondition equal(Object index) {
		return new IsEqualCriterion(index);
	}

	public static LogicalSearchValueCondition notEqual(Object value) {
		return new IsNotEqualCriterion(value);
	}

	public static LogicalSearchValueCondition query(String query) {
		return new QueryCriterion(query);
	}

	public static LogicalSearchCondition impossibleCondition(String collection) {
		return fromAllSchemasIn(collection).where(Schemas.IDENTIFIER).isEqualTo("impossibleID_42");
	}
}
