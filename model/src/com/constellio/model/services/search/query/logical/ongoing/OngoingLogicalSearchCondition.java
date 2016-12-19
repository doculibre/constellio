package com.constellio.model.services.search.query.logical.ongoing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.constellio.model.entities.schemas.DataStoreField;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.services.search.query.logical.LogicalOperator;
import com.constellio.model.services.search.query.logical.condition.CompositeLogicalSearchCondition;
import com.constellio.model.services.search.query.logical.condition.ConditionTemplate;
import com.constellio.model.services.search.query.logical.condition.DataStoreFieldLogicalSearchCondition;
import com.constellio.model.services.search.query.logical.condition.DataStoreFilters;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchConditionBuilder;

public class OngoingLogicalSearchCondition {

	private final DataStoreFilters filters;

	public OngoingLogicalSearchCondition(DataStoreFilters filters) {
		super();
		this.filters = filters;
	}

	public OngoingLogicalSearchConditionWithDataStoreFields where(DataStoreField dataStoreField) {
		return new OngoingLogicalSearchConditionWithDataStoreFields(filters, Arrays.asList(dataStoreField),
				LogicalOperator.AND);
	}

	public LogicalSearchCondition where(LogicalSearchConditionBuilder builder) {
		return builder.build(this);
	}

	public LogicalSearchCondition where(ConditionTemplate template) {
		if (template.getOperator() == LogicalOperator.AND) {
			return whereAll(template.getFields()).is(template.getCondition());
		} else {
			return whereAny(template.getFields()).is(template.getCondition());
		}
	}

	public OngoingLogicalSearchConditionWithDataStoreFields whereAll(Metadata... metadatas) {
		return new OngoingLogicalSearchConditionWithDataStoreFields(filters, asFieldList(metadatas),
				LogicalOperator.AND);
	}

	public OngoingLogicalSearchConditionWithDataStoreFields whereAll(DataStoreField... dataStoreFields) {
		return new OngoingLogicalSearchConditionWithDataStoreFields(filters,
				Arrays.asList(dataStoreFields), LogicalOperator.AND);
	}

	public OngoingLogicalSearchConditionWithDataStoreFields whereAll(List<?> dataStoreFields) {
		return new OngoingLogicalSearchConditionWithDataStoreFields(filters,
				(List<DataStoreField>) dataStoreFields, LogicalOperator.AND);
	}

	public OngoingLogicalSearchConditionWithDataStoreFields whereAny(Metadata... dataStoreFields) {

		return new OngoingLogicalSearchConditionWithDataStoreFields(filters,
				asFieldList(dataStoreFields), LogicalOperator.OR);
	}

	public OngoingLogicalSearchConditionWithDataStoreFields whereAny(DataStoreField... dataStoreFields) {
		return new OngoingLogicalSearchConditionWithDataStoreFields(filters,
				Arrays.asList(dataStoreFields),
				LogicalOperator.OR);
	}

	public OngoingLogicalSearchConditionWithDataStoreFields whereAny(List<?> dataStoreFields) {
		return new OngoingLogicalSearchConditionWithDataStoreFields(filters,
				(List<DataStoreField>) dataStoreFields,
				LogicalOperator.OR);
	}

	public LogicalSearchCondition where(LogicalSearchCondition whereClause) {
		return whereClause.withFilters(filters);
	}

	public LogicalSearchCondition whereAllConditions(LogicalSearchCondition... whereClauses) {
		return whereAllConditions(Arrays.asList(whereClauses));
	}

	public LogicalSearchCondition whereAllConditions(List<LogicalSearchCondition> whereClauses) {
		return new CompositeLogicalSearchCondition(filters, LogicalOperator.AND, withSchema(whereClauses));
	}

	public LogicalSearchCondition whereAnyCondition(LogicalSearchCondition... whereClauses) {
		return whereAnyCondition(Arrays.asList(whereClauses));
	}

	public LogicalSearchCondition whereAnyCondition(List<LogicalSearchCondition> whereClauses) {
		return new CompositeLogicalSearchCondition(filters, LogicalOperator.OR, withSchema(whereClauses));
	}

	private List<LogicalSearchCondition> withSchema(List<LogicalSearchCondition> whereClauses) {
		List<LogicalSearchCondition> searchConditions = new ArrayList<>();
		for (LogicalSearchCondition condition : whereClauses) {
			searchConditions.add(condition.withFilters(filters));
		}
		return searchConditions;
	}

	;

	public LogicalSearchCondition returnAll() {
		return new DataStoreFieldLogicalSearchCondition(filters);
	}

	private List<DataStoreField> asFieldList(Metadata... metadatas) {
		List<DataStoreField> fields = new ArrayList<>();

		for (Metadata metadata : metadatas) {
			fields.add(metadata);
		}

		return fields;
	}
}
