package com.constellio.model.services.search.query.logical.criteria;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.constellio.model.entities.schemas.DataStoreField;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.search.query.logical.LogicalSearchValueCondition;

public class IsFalseOrNullCriterion extends LogicalSearchValueCondition {

	public IsFalseOrNullCriterion() {
		super();
	}

	@Override
	public boolean isValidFor(DataStoreField dataStoreField) {
		return dataStoreField.getType() == MetadataValueType.BOOLEAN;
	}

	@Override
	public String getSolrQuery(DataStoreField dataStoreField) {
		return "(*:* -" + dataStoreField.getDataStoreCode() + ":" + CriteriaUtils.getBooleanStringValue(true) + ")";
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
		return getClass().getSimpleName();
	}
}
