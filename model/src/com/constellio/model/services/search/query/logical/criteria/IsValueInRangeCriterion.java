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

import com.constellio.model.entities.schemas.DataStoreField;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.search.query.logical.LogicalSearchValueCondition;

public class IsValueInRangeCriterion extends LogicalSearchValueCondition {

	private final Object beginValue;
	private final Object endValue;

	public IsValueInRangeCriterion(Object beginIndex, Object endIndex) {
		super();
		this.beginValue = beginIndex;
		this.endValue = endIndex;
	}

	public Object getBeginIndex() {
		return beginValue;
	}

	public Object getEndIndex() {
		return endValue;
	}

	@Override
	public boolean isValidFor(DataStoreField dataStoreField) {
		return !dataStoreField.getType().equals(MetadataValueType.BOOLEAN);
	}

	@Override
	public String getSolrQuery(DataStoreField dataStoreField) {
		if (beginValue instanceof Number && endValue instanceof Number) {
			return dataStoreField.getDataStoreCode() + ":[" + beginValue + " TO " + endValue + "]";
		} else if (beginValue instanceof LocalDateTime && endValue instanceof LocalDateTime) {
			return correctDate(dataStoreField);
		} else if (beginValue instanceof LocalDate && endValue instanceof LocalDate) {
			return correctDate(dataStoreField);
		} else {
			//FIXME Date?
			return dataStoreField.getDataStoreCode() + ":[\"" + beginValue + "\" TO \"" + endValue + "\"] AND -"
					+ dataStoreField.getDataStoreCode() + ":\"__NULL__\"";
		}
	}

	private String correctDate(DataStoreField dataStoreField) {
		String begin = CriteriaUtils.toSolrStringValue(beginValue, dataStoreField);
		String end = CriteriaUtils.toSolrStringValue(endValue, dataStoreField);

		return dataStoreField.getDataStoreCode() + ":[" + begin + " TO " + end + "] AND -" + dataStoreField.getDataStoreCode()
				+ ":\"" + CriteriaUtils.getNullDateValue() + "\"";
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
		return getClass().getSimpleName() + ":[" + beginValue + " TO " + endValue + "]  AND -" + getClass().getSimpleName()
				+ ":\"__NULL__\"";
	}
}
