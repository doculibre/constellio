package com.constellio.model.services.search.query.logical.criteria;

import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.DataStoreField;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.services.search.query.logical.LogicalSearchValueCondition;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.List;

public class IsNotNullCriterion extends LogicalSearchValueCondition {

	public IsNotNullCriterion() {
		super();
	}

	@Override
	public boolean isValidFor(DataStoreField dataStoreField) {
		return true;
	}

	@Override
	public String getSolrQuery(DataStoreField dataStoreField) {
		if (Toggle.MIGRATING_LEGACY_SAVESTATE.isEnabled()) {
			String nullValue = CriteriaUtils.getNullValueForDataStoreField(dataStoreField);

			return "(" + dataStoreField.getDataStoreCode() + ":*) AND -(" + dataStoreField.getDataStoreCode() + ":\"" + nullValue + "\")";
		} else {
			return "(" + dataStoreField.getDataStoreCode() + ":*)";
		}


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

	@Override
	public boolean testConditionOnField(Metadata metadata, Record record) {

		Object recordValue = CriteriaUtils.convertMetadataValue(metadata, record);

		if (recordValue instanceof List) {
			return !((List) recordValue).isEmpty();
		} else {
			return recordValue != null;
		}

	}

	@Override
	public boolean isSupportingMemoryExecution() {
		return true;
	}
}
