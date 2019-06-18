package com.constellio.model.services.search.query.logical.condition;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.DataStoreField;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.services.search.query.logical.LogicalOperator;
import com.constellio.model.services.search.query.logical.LogicalSearchConditionRuntimeException;
import com.constellio.model.services.search.query.logical.LogicalSearchValueCondition;
import com.constellio.model.services.search.query.logical.criteria.CompositeLogicalSearchValueOperator;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalOperator.AND;

public class DataStoreFieldLogicalSearchCondition extends LogicalSearchCondition {

	final List<DataStoreField> dataStoreFields;

	final LogicalOperator metadataLogicalOperator;

	final LogicalSearchValueCondition valueCondition;

	private Boolean preferAnalyzedField;

	public DataStoreFieldLogicalSearchCondition(DataStoreFilters filters,
												List<?> dataStoreFields, LogicalOperator metadataLogicalOperator,
												LogicalSearchValueCondition valueCondition) {
		super(filters);
		if (dataStoreFields == null) {
			this.dataStoreFields = null;
		} else {
			this.dataStoreFields = Collections.unmodifiableList((List<DataStoreField>) dataStoreFields);
		}
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

		LogicalSearchValueCondition newValueCondition = new CompositeLogicalSearchValueOperator(AND,
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
	public String getSolrQuery(SolrQueryBuilderContext params) {
		String query = "(";

		if (dataStoreFields == null) {
			query += " *:*";
		} else {

			for (int i = 0; i < dataStoreFields.size() - 1; i++) {
				query += " " + valueCondition.getSolrQuery(getSearchedField(params, dataStoreFields.get(i))) + " "
						 + metadataLogicalOperator;
			}

			DataStoreField metadata = dataStoreFields.get(dataStoreFields.size() - 1);
			String solrQuery = valueCondition.getSolrQuery(getSearchedField(params, metadata));
			query += " " + solrQuery;
		}

		query += " )";

		return query;
	}

	private DataStoreField getSearchedField(SolrQueryBuilderContext params, DataStoreField dataStoreField) {

		boolean useSecondaryLanguageField = false;

		if ((filters instanceof SchemaFilters) && params.isSecondaryLanguage()) {
			if (((SchemaFilters) filters).getSchemaType() != null) {
				useSecondaryLanguageField = params.isMultilingual(((SchemaFilters) filters).getSchemaType(), dataStoreField);
			}
		}

		if (useSecondaryLanguageField) {
			if (params.isPreferAnalyzedFields() && dataStoreField.isSearchable()) {
				return dataStoreField.getAnalyzedField(params.getLanguageCode())
						.getSecondaryLanguageField(params.getLanguageCode());
			} else {
				return dataStoreField.getSecondaryLanguageField(params.getLanguageCode());
			}
		} else {
			if (params.isPreferAnalyzedFields() && dataStoreField.isSearchable()) {
				return dataStoreField.getAnalyzedField(params.getLanguageCode());
			} else {
				return dataStoreField;
			}
		}
	}

	public DataStoreFieldLogicalSearchCondition replacingValueConditionWith(
			LogicalSearchValueCondition newValueCondition) {
		return new DataStoreFieldLogicalSearchCondition(filters, dataStoreFields, metadataLogicalOperator,
				newValueCondition);
	}

	@Override
	public boolean test(Record record) {

		if (valueCondition == null) {
			return true;
		}

		boolean returnedValue = this.metadataLogicalOperator == AND;

		for (DataStoreField queriedField : dataStoreFields) {
			if (this.metadataLogicalOperator == AND) {
				returnedValue &= valueCondition.testConditionOnField((Metadata) queriedField, record);
			} else {
				returnedValue |= valueCondition.testConditionOnField((Metadata) queriedField, record);
			}
		}

		return returnedValue;
	}

	@Override
	public boolean isSupportingMemoryExecution() {
		return valueCondition == null || valueCondition.isSupportingMemoryExecution();
	}
}
