package com.constellio.model.services.search.query.logical.criteria;

import com.constellio.model.entities.schemas.DataStoreField;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.services.search.query.logical.LogicalSearchValueCondition;
import com.constellio.model.services.search.query.logical.condition.TestedQueryRecord;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.List;

public class IsStartingWithTextCriterion extends LogicalSearchValueCondition {

	private final String text;

	public IsStartingWithTextCriterion(String text) {
		super();
		this.text = text;
	}

	public String getText() {
		return text;
	}

	@Override
	public boolean isValidFor(DataStoreField dataStoreField) {
		return dataStoreField.getType().isStringOrText();
	}

	@Override
	public String getSolrQuery(DataStoreField dataStoreField) {
		String correctedText = CriteriaUtils.toSolrStringValue(text, dataStoreField);
		return dataStoreField.getDataStoreCode() + ":" + correctedText + "* OR " + dataStoreField.getDataStoreCode() + ":" + correctedText;
	}

	@Override
	public boolean testConditionOnField(Metadata metadata, TestedQueryRecord record) {
		Object recordValue = CriteriaUtils.convertMetadataValue(metadata, record);

		if (metadata.isMultivalue()) {
			for (String item : (List<String>) recordValue) {
				if (item.startsWith(text)) {
					return true;
				}
			}
		} else {
			if (recordValue != null && ((String) recordValue).startsWith(text)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean isSupportingMemoryExecution() {
		return true;
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
