package com.constellio.model.services.search.query.logical.criteria;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.constellio.model.entities.schemas.DataStoreField;
import com.constellio.model.services.search.query.logical.LogicalSearchValueCondition;

public class IsEqualCriterion extends LogicalSearchValueCondition {

	private final Object value;

	public IsEqualCriterion(Object value) {
		super();
		this.value = value;
	}

	public Object getIndex() {
		return value;
	}

	@Override
	public boolean isValidFor(DataStoreField dataStoreField) {
		return true;
	}

	@Override
	public String getSolrQuery(DataStoreField dataStoreField) {
		String convertedValue = CriteriaUtils.toSolrStringValue(value, dataStoreField);
		return dataStoreField.getDataStoreCode() + ":\"" + convertedValue + "\"";
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
