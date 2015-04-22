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

import java.util.List;

import com.constellio.model.services.search.query.logical.LogicalSearchValueCondition;

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
		throw new UnsupportedOperationException("Cannot add value conditions on a negated condition");
	}

	@Override
	public LogicalSearchCondition withAndValueConditions(List<LogicalSearchValueCondition> conditions) {
		throw new UnsupportedOperationException("Cannot add value conditions on a negated condition");
	}

	@Override
	public LogicalSearchCondition negated() {
		return negated;
	}

	@Override
	public String getSolrQuery() {
		return "( *:* -" + negated.getSolrQuery() + " )";
	}
}
