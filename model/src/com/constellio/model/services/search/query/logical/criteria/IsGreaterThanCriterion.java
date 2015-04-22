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
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.search.query.logical.LogicalSearchValueCondition;

public class IsGreaterThanCriterion extends LogicalSearchValueCondition {

	private final Object index;

	public IsGreaterThanCriterion(Object index) {
		super();
		this.index = index;
	}

	public Object getIndex() {
		return index;
	}

	@Override
	public boolean isValidFor(DataStoreField dataStoreField) {
		return !dataStoreField.getType().equals(MetadataValueType.BOOLEAN);
	}

	@Override
	public String getSolrQuery(DataStoreField dataStoreField) {
		StringBuilder query = new StringBuilder();
		if (index instanceof Number) {
			query.append(dataStoreField.getDataStoreCode() + ":{" + index + " TO *}");
		} else if (index instanceof LocalDateTime || index instanceof LocalDate) {
			LocalDateTime dateValue;
			if (index instanceof LocalDateTime) {
				dateValue = (LocalDateTime) index;
			} else {
				dateValue = ((LocalDate) index).toLocalDateTime(LocalTime.MIDNIGHT).plusSeconds(1);
			}

			String value = CriteriaUtils.toSolrStringValue(dateValue, dataStoreField);

			query.append(dataStoreField.getDataStoreCode() + ":{" + value + " TO *}");
			query.append(" AND (*:* -(" + dataStoreField.getDataStoreCode() + ":\"" + CriteriaUtils.getNullDateValue() + "\")) ");
		} else {
			query.append(dataStoreField.getDataStoreCode() + ":{\"" + index + "\" TO *} AND (*:* -(" + dataStoreField
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
