package com.constellio.model.services.search.query.logical.criteria;

import com.constellio.model.entities.schemas.DataStoreField;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.services.search.query.logical.LogicalSearchValueCondition;
import com.constellio.model.services.search.query.logical.condition.TestedQueryRecord;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

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
	public boolean testConditionOnField(Metadata metadata, TestedQueryRecord record) {
		Object recordValue = CriteriaUtils.convertMetadataValue(metadata, record);

		throw new NotImplementedException("Not implemented yet");
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
