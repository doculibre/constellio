/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.model.services.search.query.logical.condition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.constellio.model.entities.schemas.DataStoreField;
import com.constellio.model.services.search.query.logical.LogicalOperator;
import com.constellio.model.services.search.query.logical.LogicalSearchConditionRuntimeException;
import com.constellio.model.services.search.query.logical.LogicalSearchConditionRuntimeException.MetadatasRequired;
import com.constellio.model.services.search.query.logical.LogicalSearchValueCondition;
import com.constellio.model.services.search.query.logical.criteria.CompositeLogicalSearchValueOperator;

public class DataStoreFieldLogicalSearchCondition extends LogicalSearchCondition {

	final List<DataStoreField> dataStoreFields;

	final LogicalOperator metadataLogicalOperator;

	final LogicalSearchValueCondition valueCondition;

	public DataStoreFieldLogicalSearchCondition(DataStoreFilters filters,
			List<?> dataStoreFields, LogicalOperator metadataLogicalOperator,
			LogicalSearchValueCondition valueCondition) {
		super(filters);
		if (dataStoreFields == null) {
			throw new MetadatasRequired();
		}
		this.dataStoreFields = Collections.unmodifiableList((List<DataStoreField>) dataStoreFields);
		this.metadataLogicalOperator = metadataLogicalOperator;
		this.valueCondition = valueCondition;
	}

	public DataStoreFieldLogicalSearchCondition(DataStoreFilters filters) {
		super(filters);
		this.dataStoreFields = null;
		this.metadataLogicalOperator = null;
		this.valueCondition = null;
	}

	public List<DataStoreField> getDataStoreFields() {
		return dataStoreFields;
	}

	public LogicalOperator getMetadataLogicalOperator() {
		return metadataLogicalOperator;
	}

	public LogicalSearchValueCondition getValueCondition() {
		return valueCondition;
	}

	@Override
	public LogicalSearchCondition withFilters(DataStoreFilters filters) {
		return new DataStoreFieldLogicalSearchCondition(filters, dataStoreFields, metadataLogicalOperator,
				valueCondition);
	}

	@Override
	public LogicalSearchCondition withOrValueConditions(List<LogicalSearchValueCondition> conditions) {
		List<LogicalSearchValueCondition> zeConditions = new ArrayList<>();
		zeConditions.add(valueCondition);
		zeConditions.addAll(conditions);

		LogicalSearchValueCondition newValueCondition = new CompositeLogicalSearchValueOperator(LogicalOperator.OR, zeConditions);
		return new DataStoreFieldLogicalSearchCondition(filters, dataStoreFields, metadataLogicalOperator,
				newValueCondition);
	}

	@Override
	public LogicalSearchCondition withAndValueConditions(List<LogicalSearchValueCondition> conditions) {
		List<LogicalSearchValueCondition> zeConditions = new ArrayList<>();
		zeConditions.add(valueCondition);
		zeConditions.addAll(conditions);

		LogicalSearchValueCondition newValueCondition = new CompositeLogicalSearchValueOperator(LogicalOperator.AND,
				zeConditions);
		return new DataStoreFieldLogicalSearchCondition(filters, dataStoreFields,
				metadataLogicalOperator, newValueCondition);
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
		StringBuilder sb = new StringBuilder();

		if (dataStoreFields == null) {
			sb.append(" *:* ");
		} else {
			sb.append(filters + ":");
			for (DataStoreField dataStoreField : dataStoreFields) {
				if (sb.length() > 0) {
					sb.append(" " + metadataLogicalOperator + " ");
				}
				sb.append(dataStoreField.getDataStoreCode());
			}
			sb.append(":");
			sb.append(valueCondition);
		}

		return sb.toString();
	}

	@Override
	public void validate() {
		for (DataStoreField dataStoreField : this.dataStoreFields) {
			if (!valueCondition.isValidFor(dataStoreField)) {
				throw new LogicalSearchConditionRuntimeException.UnsupportedConditionForMetadata(dataStoreField);
			}
		}
	}

	@Override
	public String getSolrQuery() {
		String query = "(";

		if (dataStoreFields == null) {
			query += " *:*";
		} else {
			for (int i = 0; i < dataStoreFields.size() - 1; i++) {
				query += " " + valueCondition.getSolrQuery(dataStoreFields.get(i)) + " " + metadataLogicalOperator;
			}

			DataStoreField metadata = dataStoreFields.get(dataStoreFields.size() - 1);
			String solrQuery = valueCondition.getSolrQuery(metadata);
			query += " " + solrQuery;
		}

		query += " )";

		return query;
	}

	public DataStoreFieldLogicalSearchCondition replacingValueConditionWith(LogicalSearchValueCondition newValueCondition) {
		return new DataStoreFieldLogicalSearchCondition(filters, dataStoreFields, metadataLogicalOperator,
				newValueCondition);
	}
}
