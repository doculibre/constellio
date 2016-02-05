package com.constellio.model.services.search.query.logical.criteria;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import com.constellio.model.entities.schemas.DataStoreField;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.search.query.logical.LogicalSearchValueCondition;

public class IsGreaterOrEqualThanCriterion extends LogicalSearchValueCondition {

	private final Object index;

	public IsGreaterOrEqualThanCriterion(Object index) {
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
			query.append(dataStoreField.getDataStoreCode() + ":[" + index + " TO *]");
		} else if (index instanceof LocalDateTime || index instanceof LocalDate) {
			String time = CriteriaUtils.toSolrStringValue(index, dataStoreField);
			query.append(dataStoreField.getDataStoreCode() + ":[" + time + " TO *]");
			query.append(" AND (*:* -(" + dataStoreField.getDataStoreCode() + ":\"" + CriteriaUtils.getNullDateValue() + "\")) ");
		} else {
			query.append(dataStoreField.getDataStoreCode() + ":[\"" + index + "\" TO *] AND (*:* -(" + dataStoreField
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
