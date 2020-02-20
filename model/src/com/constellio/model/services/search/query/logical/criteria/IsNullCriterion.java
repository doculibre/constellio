package com.constellio.model.services.search.query.logical.criteria;

import com.constellio.model.entities.schemas.DataStoreField;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.services.search.query.logical.LogicalSearchValueCondition;
import com.constellio.model.services.search.query.logical.condition.TestedQueryRecord;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.jetbrains.annotations.NotNull;

public class IsNullCriterion extends LogicalSearchValueCondition {

	public IsNullCriterion() {
		super();
	}

	@Override
	public boolean isValidFor(DataStoreField dataStoreField) {
		return true;
	}

	@Override
	public String getSolrQuery(DataStoreField dataStoreField) {
		return toIsNullSolrQuery(dataStoreField);
	}

	@NotNull
	public static String toIsNullSolrQuery(DataStoreField dataStoreField) {
		String query = "(*:* -" + dataStoreField.getDataStoreCode() + ":*)";
		if (dataStoreField.isMultivalue()) {
			query = "(" + query + " OR " + dataStoreField.getDataStoreCode() + ":" + CriteriaUtils.getNullValueForDataStoreField(dataStoreField) + ")";
		}
		return query;
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

				return !record.getRecord().getRecordDTO().getFields().containsKey(metadata.getDataStoreCode());

//		Object recordValue = CriteriaUtils.convertMetadataValue(metadata, record);
		//
		//		if (recordValue instanceof List) {
		//			return ((List) recordValue).isEmpty();
		//		} else {
		//			return recordValue == null;
		//		}

	}

	@Override
	public boolean isSupportingMemoryExecution() {
		return true;
	}
}
