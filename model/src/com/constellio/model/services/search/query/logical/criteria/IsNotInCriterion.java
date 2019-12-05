package com.constellio.model.services.search.query.logical.criteria;

import com.constellio.model.entities.schemas.DataStoreField;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.services.search.SearchServicesRuntimeException.TooManyElementsInCriterion;
import com.constellio.model.services.search.query.logical.LogicalSearchValueCondition;
import com.constellio.model.services.search.query.logical.condition.TestedQueryRecord;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Arrays;
import java.util.List;

public class IsNotInCriterion extends LogicalSearchValueCondition {

	private final List<Object> values;
	private final List<Object> memoryQueryValues;

	@SuppressWarnings({"unchecked", "rawtypes"})
	public IsNotInCriterion(List<?> values) {
		super();
		this.values = (List) values;
		this.memoryQueryValues = CriteriaUtils.convertToMemoryQueryValues((List) values);
		if (values.size() > 1000) {
			throw new TooManyElementsInCriterion(values.size());
		}
	}

	public List<Object> getValues() {
		return values;
	}

	@Override
	public boolean isValidFor(DataStoreField dataStoreField) {
		return true;
	}

	@Override
	public String getSolrQuery(DataStoreField dataStoreField) {
		StringBuilder solrQuery = new StringBuilder("(*:* ");

		List<Object> queryValues = values;
		if (queryValues.isEmpty()) {
			queryValues = Arrays.asList((Object) "_impossible_value_a38_");
		}

		String nullValue = CriteriaUtils.getNullValueForDataStoreField(dataStoreField);
		for (Object element : queryValues) {

			element = CriteriaUtils.toSolrStringValue(element, dataStoreField);
			solrQuery.append("-");
			solrQuery.append(dataStoreField.getDataStoreCode());
			solrQuery.append(":\"");
			solrQuery.append(element);
			solrQuery.append("\" ");
		}
		solrQuery.append(") AND (");
		solrQuery.append(dataStoreField.getDataStoreCode());
		solrQuery.append(":*)");
		return solrQuery.toString();
	}

	@Override
	public boolean testConditionOnField(Metadata metadata, TestedQueryRecord record) {
		if (memoryQueryValues.isEmpty()) {
			return true;
		}

		Object recordValue = CriteriaUtils.convertMetadataValue(metadata, record);

		for (Object value : CriteriaUtils.getValues(recordValue)) {
			if (CriteriaUtils.useConvertedValues(metadata)) {
				if (!memoryQueryValues.contains(value)) {
					return true;
				}
			} else {
				if (!values.contains(value)) {
					return true;
				}
			}
		}

		return false;
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
		return getClass().getSimpleName() + ":" + values;
	}

	@Override
	public boolean isSupportingMemoryExecution() {
		return true;
	}
}
