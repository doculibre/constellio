package com.constellio.model.services.search.query.logical.condition;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.DataStoreField;
import com.constellio.model.services.search.query.logical.LogicalOperator;
import com.constellio.model.services.search.query.logical.LogicalSearchValueCondition;
import com.constellio.model.services.search.query.logical.ongoing.OngoingLogicalSearchConditionWithDataStoreFields;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public abstract class LogicalSearchCondition implements Predicate<Record> {

	protected final DataStoreFilters filters;

	public LogicalSearchCondition(DataStoreFilters filters) {
		super();
		this.filters = filters;
	}

	public abstract void validate();

	public LogicalSearchCondition or(List<LogicalSearchValueCondition> conditions) {
		return this.withOrValueConditions(conditions);
	}

	public LogicalSearchCondition or(LogicalSearchValueCondition conditions) {
		return this.withOrValueConditions(Arrays.asList(conditions));
	}

	public LogicalSearchCondition and(List<LogicalSearchValueCondition> conditions) {
		return this.withAndValueConditions(conditions);
	}

	public LogicalSearchCondition and(LogicalSearchValueCondition conditions) {
		return this.withAndValueConditions(Arrays.asList(conditions));
	}

	public OngoingLogicalSearchConditionWithDataStoreFields orWhere(DataStoreField dataStoreField) {
		return orWhereAll(Arrays.asList(dataStoreField));
	}

	public OngoingLogicalSearchConditionWithDataStoreFields orWhereAll(List<DataStoreField> dataStoreFields) {
		return new OngoingLogicalSearchConditionWithDataStoreFields(filters, dataStoreFields,
				LogicalOperator.AND, this, LogicalOperator.OR);
	}

	public OngoingLogicalSearchConditionWithDataStoreFields orWhereAny(List<?> dataStoreFields) {
		return new OngoingLogicalSearchConditionWithDataStoreFields(filters, (List) dataStoreFields,
				LogicalOperator.OR,
				this, LogicalOperator.OR);
	}

	public LogicalSearchCondition andWhere(DataStoreFieldLogicalSearchCondition condition) {
		if (condition.getMetadataLogicalOperator() == LogicalOperator.AND) {
			return this.andWhereAll(condition.getDataStoreFields()).is(condition.getValueCondition());
		} else {
			return this.andWhereAny(condition.getDataStoreFields()).is(condition.getValueCondition());
		}
	}

	public LogicalSearchCondition orWhere(DataStoreFieldLogicalSearchCondition condition) {
		if (condition.getMetadataLogicalOperator() == LogicalOperator.AND) {
			return this.orWhereAll(condition.getDataStoreFields()).is(condition.getValueCondition());
		} else {
			return this.orWhereAny(condition.getDataStoreFields()).is(condition.getValueCondition());
		}
	}

	public LogicalSearchCondition andWhere(ConditionTemplate template) {
		if (template.getOperator() == LogicalOperator.AND) {
			return andWhereAll((List<DataStoreField>) template.getFields()).is(template.getCondition());
		} else {
			return andWhereAny((List<DataStoreField>) template.getFields()).is(template.getCondition());
		}
	}

	public LogicalSearchCondition orWhere(ConditionTemplate template) {
		if (template.getOperator() == LogicalOperator.AND) {
			return orWhereAll((List<DataStoreField>) template.getFields()).is(template.getCondition());
		} else {
			return orWhereAny((List<DataStoreField>) template.getFields()).is(template.getCondition());
		}
	}

	public OngoingLogicalSearchConditionWithDataStoreFields andWhere(DataStoreField dataStoreField) {
		return andWhereAll(Arrays.asList(dataStoreField));
	}

	public OngoingLogicalSearchConditionWithDataStoreFields andWhereAll(List<DataStoreField> dataStoreFields) {
		return new OngoingLogicalSearchConditionWithDataStoreFields(filters, dataStoreFields,
				LogicalOperator.AND, this, LogicalOperator.AND);
	}

	public OngoingLogicalSearchConditionWithDataStoreFields andWhereAny(
			List<? extends DataStoreField> dataStoreFields) {
		return new OngoingLogicalSearchConditionWithDataStoreFields(filters, (List<DataStoreField>) dataStoreFields,
				LogicalOperator.OR,
				this, LogicalOperator.AND);
	}

	public LogicalSearchCondition negate() {
		return new NegatedLogicalSearchCondition(this);
	}


	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	public abstract LogicalSearchCondition withFilters(DataStoreFilters filters);

	public abstract LogicalSearchCondition withOrValueConditions(List<LogicalSearchValueCondition> conditions);

	public abstract LogicalSearchCondition withAndValueConditions(List<LogicalSearchValueCondition> conditions);

	public abstract String getSolrQuery(SolrQueryBuilderContext params);

	public DataStoreFilters getFilters() {
		return filters;
	}

	public boolean isCollectionSearch() {
		return filters != null && filters.getCollection() != null;
	}

	public List<String> getFilterSchemaTypesCodes() {
		if (filters instanceof SchemaTypesFilters) {
			return ((SchemaTypesFilters) filters).getSchemaTypesCodes();

		} else if (filters instanceof SchemaFilters) {
			return Collections.singletonList(((SchemaFilters) filters).getSchemaType());

		} else {
			return Collections.emptyList();
		}
	}

	public String getCollection() {
		return filters != null ? filters.getCollection() : null;
	}

	public abstract boolean isSupportingMemoryExecution(boolean queryingTypesInSummaryCache,
														boolean requiringExecutionMethod);
}
