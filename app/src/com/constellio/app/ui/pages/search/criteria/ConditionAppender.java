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
package com.constellio.app.ui.pages.search.criteria;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import java.util.ArrayList;
import java.util.List;

import com.constellio.app.ui.pages.search.criteria.Criterion.BooleanOperator;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

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
				result.add(condition.negated());
			}
			return from(schemaType).whereAllConditions(result);
		default:
			throw new RuntimeException("Unknown operator");
		}
	}
}
