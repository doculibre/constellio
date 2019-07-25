package com.constellio.model.services.search.query.logical.criteria;

import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.DataStoreField;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.search.query.logical.LogicalSearchValueCondition;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.joda.time.LocalDateTime;

public class IsNewerThanCriterion extends LogicalSearchValueCondition {

	private final Object value;
	private final MeasuringUnitTime measuringUnitTime;

	public IsNewerThanCriterion(Object value, MeasuringUnitTime measuringUnitTime) {
		super();
		this.value = value;
		this.measuringUnitTime = measuringUnitTime;
	}

	public Object getValue() {
		return value;
	}

	@Override
	public boolean isValidFor(DataStoreField dataStoreField) {
		return dataStoreField.getType().equals(MetadataValueType.DATE) || dataStoreField.getType()
				.equals(MetadataValueType.DATE_TIME);
	}

	@Override
	public String getSolrQuery(DataStoreField dataStoreField) {
		//
		LocalDateTime now = TimeProvider.getLocalDateTime();
		LocalDateTime dateTimeValue;
		int intValue = ((Double) value).intValue();
		if (measuringUnitTime == MeasuringUnitTime.DAYS) {
			dateTimeValue = now.minusDays(intValue);
		} else {
			dateTimeValue = now.minusYears(intValue);
		}
		String stringValue;
		if (dataStoreField.getType().equals(MetadataValueType.DATE)) {
			stringValue = CriteriaUtils.toSolrStringValue(dateTimeValue.toLocalDate(), dataStoreField);
		} else {
			stringValue = CriteriaUtils.toSolrStringValue(dateTimeValue, dataStoreField);
		}
		StringBuilder query = new StringBuilder();
		query.append(dataStoreField.getDataStoreCode() + ":{" + stringValue + " TO *}");
		query.append(" AND (*:* -(" + dataStoreField.getDataStoreCode() + ":\"" + CriteriaUtils.getNullDateValue() + "\"))");
		return query.toString();
	}

	@Override
	public boolean testConditionOnField(Metadata metadata, Record record) {
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
