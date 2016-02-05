package com.constellio.model.services.search.query.logical.criteria;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.constellio.model.entities.schemas.DataStoreField;
import com.constellio.model.services.search.query.logical.LogicalSearchValueCondition;

public class IsNotEqualCriterion extends LogicalSearchValueCondition {

	private final Object value;

	public IsNotEqualCriterion(Object value) {
		super();
		this.value = value;
	}

	public Object getValue() {
		return value;
	}

	@Override
	public boolean isValidFor(DataStoreField dataStoreField) {
		return true;
	}

	@Override
	public String getSolrQuery(DataStoreField dataStoreField) {
		StringBuilder query = new StringBuilder();
		query.append("(*:* -(");
		query.append(dataStoreField.getDataStoreCode());
		query.append(":\"");
		query.append(CriteriaUtils.toSolrStringValue(value, dataStoreField));
		query.append("\"))");
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

	@Override
	public String toString() {
		return getClass().getSimpleName() + ":" + value;
	}
}
