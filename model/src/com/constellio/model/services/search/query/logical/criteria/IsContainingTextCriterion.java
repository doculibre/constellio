package com.constellio.model.services.search.query.logical.criteria;

import static com.constellio.model.entities.schemas.MetadataValueType.CONTENT;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static com.constellio.model.entities.schemas.MetadataValueType.STRUCTURE;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.constellio.model.entities.schemas.DataStoreField;
import com.constellio.model.services.search.query.logical.LogicalSearchValueCondition;

public class IsContainingTextCriterion extends LogicalSearchValueCondition {

	private final String text;

	public IsContainingTextCriterion(String text) {
		super();
		this.text = text;
	}

	public String getText() {
		return text;
	}

	@Override
	public boolean isValidFor(DataStoreField dataStoreField) {

		return dataStoreField.getType() == STRING || dataStoreField.getType() == CONTENT || dataStoreField.getType() == STRUCTURE;
	}

	@Override
	public String getSolrQuery(DataStoreField dataStoreField) {
		String correctedText = CriteriaUtils.toSolrStringValue(text, dataStoreField);
		return dataStoreField.getDataStoreCode() + ":*" + correctedText + "*";
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
