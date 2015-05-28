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
package com.constellio.model.services.search.query.logical.criteria;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.DataStoreField;
import com.constellio.model.services.search.query.logical.LogicalSearchValueCondition;

public class IsNotEqualCriterion extends LogicalSearchValueCondition {

	private final Object value;

	public IsNotEqualCriterion(Object value) {
		super();
		this.value = value;
	}

	public Object getValue() {
		return value;
	}

	@Override
	public boolean isValidFor(DataStoreField dataStoreField) {
		return true;
	}

	@Override
	public String getSolrQuery(DataStoreField dataStoreField) {
		StringBuilder query = new StringBuilder();
		query.append("(*:* -(");
		if (value instanceof LocalDateTime || value instanceof LocalDate) {
			String dateValue = CriteriaUtils.toSolrStringValue(value, dataStoreField);
			query.append(dataStoreField.getDataStoreCode() + ":\"" + dateValue + "\"");
		} else if (value instanceof Number) {
			query.append(dataStoreField.getDataStoreCode() + ":\"" + value + "\"");
		} else if (value instanceof Boolean) {
			String booleanValue = CriteriaUtils.toSolrStringValue(value, dataStoreField);
			query.append(dataStoreField.getDataStoreCode() + ":\"" + booleanValue + "\"");
		} else {
			if (value != null) {
				String textValue;
				if (value instanceof Record) {
					textValue = ((Record) value).getId();
				} else {
					textValue = value.toString();
				}
				query.append(dataStoreField.getDataStoreCode() + ":\"" + textValue + "\"");
			} else {
				query.append(dataStoreField.getDataStoreCode() + ":\"" + CriteriaUtils.getNullStringValue() + "\"");
			}
		}

		query.append("))");
		return query.toString();
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
	public String toString() {
		return getClass().getSimpleName() + ":" + value;
	}
}
