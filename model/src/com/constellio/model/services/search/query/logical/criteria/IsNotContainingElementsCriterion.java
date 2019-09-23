package com.constellio.model.services.search.query.logical.criteria;

import com.constellio.model.entities.schemas.DataStoreField;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.services.search.query.logical.LogicalSearchValueCondition;
import com.constellio.model.services.search.query.logical.condition.TestedQueryRecord;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.List;

public class IsNotContainingElementsCriterion extends LogicalSearchValueCondition {

	private final List<Object> elements;

	@SuppressWarnings({"unchecked", "rawtypes"})
	public IsNotContainingElementsCriterion(List<?> values) {
		super();
		this.elements = (List) values;
	}

	public List<Object> getElements() {
		return elements;
	}

	@Override
	public boolean isValidFor(DataStoreField dataStoreField) {
		return dataStoreField.isMultivalue();
	}

	@Override
	public String getSolrQuery(DataStoreField dataStoreField) {
		String solrQuery = "(*:* -(";
		for (int i = 0; i < elements.size(); i++) {
			Object item = elements.get(i);
			String convertedValue = CriteriaUtils.toSolrStringValue(item, dataStoreField);
			if (i > 0) {
				solrQuery += " AND ";
			}
			solrQuery += dataStoreField.getDataStoreCode() + ":\"" + convertedValue + "\"";
		}
		return solrQuery + "))";
	}

	@Override
	public boolean testConditionOnField(Metadata metadata, TestedQueryRecord record) {
		Object recordValue = CriteriaUtils.convertMetadataValue(metadata, record);

		throw new NotImplementedException("Not implemented yet");
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
		return getClass().getSimpleName() + ":" + elements;
	}
}
