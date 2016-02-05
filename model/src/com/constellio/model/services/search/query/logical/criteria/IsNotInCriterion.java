package com.constellio.model.services.search.query.logical.criteria;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.constellio.model.entities.schemas.DataStoreField;
import com.constellio.model.services.search.SearchServicesRuntimeException.TooManyElementsInCriterion;
import com.constellio.model.services.search.query.logical.LogicalSearchValueCondition;

public class IsNotInCriterion extends LogicalSearchValueCondition {

	private final List<Object> elements;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public IsNotInCriterion(List<?> values) {
		super();
		this.elements = (List) values;
		if (values.size() > 1000) {
			throw new TooManyElementsInCriterion(values.size());
		}
	}

	public List<Object> getValues() {
		return elements;
	}

	@Override
	public boolean isValidFor(DataStoreField dataStoreField) {
		return true;
	}

	@Override
	public String getSolrQuery(DataStoreField dataStoreField) {
		StringBuilder solrQuery = new StringBuilder("(*:* ");

		List<Object> queryValues = elements;
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
