package com.constellio.model.services.search.query.logical.criteria;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.constellio.model.entities.schemas.DataStoreField;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.search.query.logical.LogicalSearchValueCondition;

public class IsEndingWithTextCriterion extends LogicalSearchValueCondition {

	private final String text;

	public IsEndingWithTextCriterion(String text) {
		super();
		this.text = text;
	}

	public String getText() {
		return text;
	}

	@Override
	public boolean isValidFor(DataStoreField dataStoreField) {
		return dataStoreField.getType() == MetadataValueType.STRING;
	}

	@Override
	public String getSolrQuery(DataStoreField dataStoreField) {
		String correctedText = CriteriaUtils.toSolrStringValue(text, dataStoreField);
		return dataStoreField.getDataStoreCode() + ":*" + correctedText;
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
		return getClass().getSimpleName() + ":" + text;
	}
}
