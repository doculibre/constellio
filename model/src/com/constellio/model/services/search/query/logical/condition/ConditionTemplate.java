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

import com.constellio.model.entities.schemas.DataStoreField;
import com.constellio.model.services.search.query.logical.LogicalOperator;
import com.constellio.model.services.search.query.logical.LogicalSearchValueCondition;

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
