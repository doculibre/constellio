package com.constellio.model.services.search.query.logical.condition;

import com.constellio.model.entities.schemas.DataStoreField;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.services.search.query.logical.LogicalOperator;
import com.constellio.model.services.search.query.logical.LogicalSearchConditionRuntimeException;
import com.constellio.model.services.search.query.logical.LogicalSearchValueCondition;
import com.constellio.model.services.search.query.logical.criteria.CompositeLogicalSearchValueOperator;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.model.entities.schemas.Schemas.IDENTIFIER;
import static com.constellio.model.entities.schemas.Schemas.MARKED_FOR_PARSING;
import static com.constellio.model.entities.schemas.Schemas.MARKED_FOR_PREVIEW_CONVERSION;
import static com.constellio.model.entities.schemas.Schemas.MARKED_FOR_REINDEXING;
import static com.constellio.model.services.schemas.SchemaUtils.isSummary;
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
			this.dataStoreFields = (List<DataStoreField>) dataStoreFields;
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
	public boolean test(TestedQueryRecord record) {

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
	public boolean isSupportingMemoryExecution(IsSupportingMemoryExecutionParams params) {

		if (dataStoreFields == null) {
			return true;
		}

		if (params.isQueryingTypesInSummaryCache()) {
			for (int i = 0; i < dataStoreFields.size(); i++) {
				DataStoreField queriedField = dataStoreFields.get(i);
				Metadata metadata = (Metadata) queriedField;
				if (!isSummary(metadata)
					&& !metadata.isSameLocalCodeThanAny(MARKED_FOR_REINDEXING, MARKED_FOR_PREVIEW_CONVERSION, MARKED_FOR_PARSING, IDENTIFIER)) {

					if (params.isRequiringExecutionMethod()) {
						throw new IllegalArgumentException("Query is using a metadata which is not supported with execution in cache : " + metadata.getCode());
					} else {
						return false;
					}
				}

				//The metadata is configured to support this search condition, but is it fully available in cache?
				//Since a cache rebuild is required for this, we don't throw exception

				if (metadata.getSchema() == null) {
					metadata = params.getSchemaType().getDefaultSchema().getMetadata(metadata.getLocalCode());
				}

				if (params.getLocalCacheConfigs().excludedDuringLastCacheRebuild(metadata)) {
					return false;
				}
			}
		}

		boolean valueConditionSupportingExecution = valueCondition == null || valueCondition.isSupportingMemoryExecution();
		if (!valueConditionSupportingExecution) {
			if (params.isRequiringExecutionMethod()) {
				throw new IllegalArgumentException("Query is using a value condition which is not supported with execution in cache : " + valueCondition.getClass().getName());
			} else {
				return false;
			}
		}

		return valueConditionSupportingExecution;
	}
}
