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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.constellio.model.services.search.query.logical.LogicalOperator;
import com.constellio.model.services.search.query.logical.LogicalSearchValueCondition;

public class CompositeLogicalSearchCondition extends LogicalSearchCondition {

	final LogicalOperator logicalOperator;

	final List<LogicalSearchCondition> nestedSearchConditions;

	public CompositeLogicalSearchCondition(DataStoreFilters filters, LogicalOperator logicalOperator,
			List<LogicalSearchCondition> nestedSearchConditions) {
		super(filters);
		this.logicalOperator = logicalOperator;
		this.nestedSearchConditions = Collections.unmodifiableList(nestedSearchConditions);
	}

	public LogicalOperator getLogicalOperator() {
		return logicalOperator;
	}

	public List<LogicalSearchCondition> getNestedSearchConditions() {
		return nestedSearchConditions;
	}

	@Override
	public String toString() {
		return filters + ":" + logicalOperator.name() + nestedSearchConditions;
	}

	@Override
	public LogicalSearchCondition withFilters(DataStoreFilters filters) {
		List<LogicalSearchCondition> searchConditionsWithSchema = new ArrayList<>();
		for (LogicalSearchCondition condition : nestedSearchConditions) {
			searchConditionsWithSchema.add(condition.withFilters(filters));
		}
		return new CompositeLogicalSearchCondition(filters, logicalOperator, searchConditionsWithSchema);
	}

	@Override
	public LogicalSearchCondition withOrValueConditions(List<LogicalSearchValueCondition> conditions) {
		throw new UnsupportedOperationException("Cannot add value conditions on a compocollection");
	}

	@Override
	public LogicalSearchCondition withAndValueConditions(List<LogicalSearchValueCondition> conditions) {
		throw new UnsupportedOperationException("Cannot add value conditions on a compocollection");
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	@Override
	public void validate() {
	}

	@Override
	public String getSolrQuery() {

		if (nestedSearchConditions.isEmpty()) {
			throw new IllegalStateException("No conditions");
		}

		String query = "(";

		for (int i = 0; i < nestedSearchConditions.size() - 1; i++) {
			query += " " + nestedSearchConditions.get(i).getSolrQuery() + " " + logicalOperator;
		}

		query += " " + nestedSearchConditions.get(nestedSearchConditions.size() - 1).getSolrQuery();

		query += " )";

		return query;
	}

}
