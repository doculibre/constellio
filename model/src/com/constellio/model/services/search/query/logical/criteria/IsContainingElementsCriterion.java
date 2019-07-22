package com.constellio.model.services.search.query.logical.criteria;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.DataStoreField;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.services.search.query.logical.LogicalSearchValueCondition;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.List;

public class IsContainingElementsCriterion extends LogicalSearchValueCondition {

	private final List<Object> elements;

	@SuppressWarnings({"unchecked", "rawtypes"})
	public IsContainingElementsCriterion(List<?> values) {
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
		boolean first = true;
		StringBuilder query = new StringBuilder();
		for (Object element : elements) {
			if (!first) {
				query.append(" AND ");
			}
			String convertedValue = CriteriaUtils.toSolrStringValue(element, dataStoreField);
			query.append(dataStoreField.getDataStoreCode() + ":\"" + convertedValue + "\"");
			first = false;
		}
		return query.toString();
	}

	@Override
	public boolean testConditionOnField(Metadata metadata, Record record) {
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
}
