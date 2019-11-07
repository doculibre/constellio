package com.constellio.model.services.search.query.logical.criteria;

import com.constellio.data.utils.LangUtils;
import com.constellio.model.entities.schemas.DataStoreField;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.search.query.logical.LogicalSearchValueCondition;
import com.constellio.model.services.search.query.logical.condition.TestedQueryRecord;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class IsTrueOrNullCriterion extends LogicalSearchValueCondition {

	public IsTrueOrNullCriterion() {
		super();
	}

	@Override
	public boolean isValidFor(DataStoreField dataStoreField) {
		return dataStoreField.getType() == MetadataValueType.BOOLEAN;
	}

	@Override
	public String getSolrQuery(DataStoreField dataStoreField) {
		return "(*:* -" + dataStoreField.getDataStoreCode() + ":" + CriteriaUtils.getBooleanStringValue(false) + ")";
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
	public boolean testConditionOnField(Metadata metadata, TestedQueryRecord record) {
		return LangUtils.isTrueOrNull(record.getRecord().get(metadata));
	}

	@Override
	public boolean isSupportingMemoryExecution() {
		return true;
	}
}
