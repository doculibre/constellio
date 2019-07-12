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
import org.joda.time.LocalTime;

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
