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
import org.joda.time.LocalTime;

import com.constellio.model.entities.schemas.DataStoreField;
import com.constellio.model.services.search.query.logical.LogicalSearchValueCondition;

public class IsLessThanCriterion extends LogicalSearchValueCondition {

	private final Object index;

	public IsLessThanCriterion(Object index) {
		super();
		this.index = index;
	}

	public Object getIndex() {
		return index;
	}

	@Override
	public boolean isValidFor(DataStoreField dataStoreField) {
		switch (dataStoreField.getType()) {
		case BOOLEAN:
			return false;
		default:
			return true;
		}
	}

	@Override
	public String getSolrQuery(DataStoreField dataStoreField) {
		StringBuilder query = new StringBuilder();
		if (index instanceof Number) {
			query.append(
					dataStoreField.getDataStoreCode() + ":{* TO " + index + "} AND (*:* -(" + dataStoreField.getDataStoreCode()
							+ ":\""
							+ Integer.MIN_VALUE + "\"))");
		} else if (index instanceof LocalDateTime || index instanceof LocalDate) {

			String value = CriteriaUtils.toSolrStringValue(index, dataStoreField);
			String excludedValue;
			LocalDateTime dateValue;
			if (index instanceof LocalDateTime) {
				excludedValue = value;
			} else {
				LocalDateTime localDateTime = ((LocalDate) index).toLocalDateTime(LocalTime.MIDNIGHT);
				excludedValue = CriteriaUtils.toSolrStringValue(localDateTime, dataStoreField);
			}

			String code = dataStoreField.getDataStoreCode();
			String between = code + ":{* TO " + value + "}";
			String notNull = "(*:* -(" + code + ":\"" + CriteriaUtils.getNullDateValue() + "\")) ";
			query.append(between + " AND " + notNull);
		} else {
			query.append(dataStoreField.getDataStoreCode() + ":{* TO \"" + index + "\"} AND (*:* -(" + dataStoreField
					.getDataStoreCode()
					+ ":\"__NULL__\"))");
		}
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
}
