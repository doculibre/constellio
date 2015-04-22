/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.model.services.search.query.logical.condition;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.constellio.model.entities.schemas.DataStoreField;
import com.constellio.model.services.search.query.logical.LogicalOperator;
import com.constellio.model.services.search.query.logical.LogicalSearchValueCondition;
import com.constellio.model.services.search.query.logical.ongoing.OngoingLogicalSearchConditionWithDataStoreFields;

public abstract class LogicalSearchCondition {

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

	public OngoingLogicalSearchConditionWithDataStoreFields orWhereAny(List<DataStoreField> dataStoreFields) {
		return new OngoingLogicalSearchConditionWithDataStoreFields(filters, dataStoreFields,
				LogicalOperator.OR,
				this, LogicalOperator.OR);
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

	public OngoingLogicalSearchConditionWithDataStoreFields andWhereAny(List<DataStoreField> dataStoreFields) {
		return new OngoingLogicalSearchConditionWithDataStoreFields(filters, dataStoreFields,
				LogicalOperator.OR,
				this, LogicalOperator.AND);
	}

	public LogicalSearchCondition negated() {
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

	public abstract String getSolrQuery();

	public DataStoreFilters getFilters() {
		return filters;
	}

	public String getCollection() {
		return ((CollectionFilters) filters).getCollection();
	}
}
