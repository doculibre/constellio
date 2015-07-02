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

import java.util.List;
import java.util.ListIterator;

import org.joda.time.LocalDateTime;

import com.constellio.app.ui.pages.search.criteria.ConditionException.ConditionException_EmptyCondition;
import com.constellio.app.ui.pages.search.criteria.ConditionException.ConditionException_TooManyClosedParentheses;
import com.constellio.app.ui.pages.search.criteria.ConditionException.ConditionException_UnclosedParentheses;
import com.constellio.app.ui.pages.search.criteria.Criterion.BooleanOperator;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

public class ConditionBuilder {
	private MetadataSchemaType schemaType;

	public ConditionBuilder(MetadataSchemaType schemaType) {
		this.schemaType = schemaType;
	}

	public LogicalSearchCondition build(List<Criterion> criteria)
			throws ConditionException {
		if (criteria.isEmpty()) {
			throw new ConditionException_EmptyCondition();
		}
		return buildOuterCondition(criteria.listIterator());
	}

	private LogicalSearchCondition buildOuterCondition(ListIterator<Criterion> criteria)
			throws ConditionException {
		ConditionAppender appender = new ConditionAppender(schemaType);
		BooleanOperator operator = BooleanOperator.AND;
		while (criteria.hasNext()) {
			Criterion criterion = criteria.next();
			if (criterion.isLeftParens()) {
				appender.append(buildInnerCondition(criterion, criteria), operator);
				operator = criteria.previous().getBooleanOperator();
				criteria.next();
			} else if (criterion.isRightParens()) {
				throw new ConditionException_TooManyClosedParentheses();
			} else {
				appender.append(buildClause(criterion), operator);
				operator = criterion.getBooleanOperator();
			}
		}
		return appender.build();
	}

	private LogicalSearchCondition buildInnerCondition(Criterion first, ListIterator<Criterion> criteria)
			throws ConditionException {
		ConditionAppender appender = new ConditionAppender(schemaType);
		appender.append(buildClause(first), BooleanOperator.AND);
		BooleanOperator operator = first.getBooleanOperator();
		while (criteria.hasNext()) {
			Criterion criterion = criteria.next();
			if (criterion.isLeftParens()) {
				appender.append(buildInnerCondition(criterion, criteria), operator);
				operator = criteria.previous().getBooleanOperator();
				criteria.next();
			} else {
				appender.append(buildClause(criterion), operator);
				operator = criterion.getBooleanOperator();
			}
			if (criterion.isRightParens()) {
				return appender.build();
			}
		}
		throw new ConditionException_UnclosedParentheses();
	}

	private LogicalSearchCondition buildClause(Criterion criterion) {
		Metadata metadata = schemaType.getMetadata(criterion.getMetadataCode());
		switch (criterion.getSearchOperator()) {
		case EQUALS:
			if (metadata.getType() == MetadataValueType.DATE_TIME) {
				LocalDateTime start = (LocalDateTime) criterion.getValue();
				return from(schemaType).where(metadata).isValueInRange(start, start.withTime(23, 59, 59, 999));
			}
			return from(schemaType).where(metadata).isEqualTo(criterion.getValue());
		case CONTAINS_TEXT:
			String value = (String) criterion.getValue();
			return metadata.getType() == MetadataValueType.STRING ?
					from(schemaType).where(metadata).isContainingText(value) :
					from(schemaType).where(metadata).query(value);
		case LESSER_THAN:
			return from(schemaType).where(metadata).isLessThan(criterion.getValue());
		case GREATER_THAN:
			return from(schemaType).where(metadata).isGreaterThan(criterion.getValue());
		case BETWEEN:
			return from(schemaType).where(metadata).isValueInRange(criterion.getValue(), criterion.getEndValue());
		case IS_TRUE:
			return from(schemaType).where(metadata).isTrue();
		case IS_FALSE:
			return from(schemaType).where(metadata).isFalseOrNull();
		case IN_HIERARCHY:
			return from(schemaType).where(Schemas.PATH).isContainingText("/" + criterion.getValue() + "/");
		default:
			throw new RuntimeException("Unsupported search operator");
		}
	}
}
