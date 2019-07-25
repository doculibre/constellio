package com.constellio.model.services.search.query.logical.criteria;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.DataStoreField;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.search.query.logical.LogicalSearchValueCondition;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

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
		if(beginValue == null && endValue == null) {
			return dataStoreField.getDataStoreCode() + ":*";
		} else if(beginValue == null) {
			return new IsLessOrEqualThanCriterion(endValue).getSolrQuery(dataStoreField);
		} else if(endValue == null) {
			return new IsGreaterOrEqualThanCriterion(beginValue).getSolrQuery(dataStoreField);
		} else if (beginValue instanceof Number && endValue instanceof Number) {
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

	@Override
	public String toString() {
		return getClass().getSimpleName() + ":[" + beginValue + " TO " + endValue + "]  AND -" + getClass().getSimpleName()
			   + ":\"__NULL__\"";
	}
}
