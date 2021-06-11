package com.constellio.app.api.graphql;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import graphql.schema.DataFetcher;
import org.apache.commons.collections.CollectionUtils;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class GraphqlDataFetchers {

	protected final RecordServices recordServices;
	protected final SearchServices searchServices;
	protected final MetadataSchemasManager schemasManager;
	protected final MetadataSchemaTypes schemaTypes;

	public GraphqlDataFetchers(AppLayerFactory appLayerFactory, String collection) {
		recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
		searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
		schemasManager = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager();
		schemaTypes = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection);
	}

	public DataFetcher getRecordByIdDataFetcher() {
		return dataFetchingEnvironment -> {
			String id = dataFetchingEnvironment.getArgument("id");
			return recordServices.get(id);
		};
	}

	public DataFetcher getRecordByCodeDataFetcher(String schemaTypeCode) {
		return dataFetchingEnvironment -> {
			String code = dataFetchingEnvironment.getArgument("code");
			MetadataSchemaType schemaType = schemaTypes.getSchemaType(schemaTypeCode);
			return searchServices.searchSingleResult(from(schemaType).where(Schemas.CODE).isEqualTo(code));
		};
	}

	public DataFetcher getMetadataDataFetcher() {
		return dataFetchingEnvironment -> {
			Record record = dataFetchingEnvironment.getSource();
			String localCode = dataFetchingEnvironment.getField().getName();
			Metadata metadata = schemaTypes.getSchema(record.getSchemaCode()).getMetadatas().getMetadataWithLocalCode(localCode);
			return record.get(metadata);
		};
	}

	public DataFetcher getStructureDataFetcher() {
		return dataFetchingEnvironment -> {
			Object structure = dataFetchingEnvironment.getSource();
			String fieldName = dataFetchingEnvironment.getField().getName();
			Field field = structure.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			return field.get(structure);
		};
	}

	public DataFetcher getRecordsByReferenceDataFetcher(String field) {
		return dataFetchingEnvironment -> {
			if (dataFetchingEnvironment.getSource() instanceof Record) {
				Record record = dataFetchingEnvironment.getSource();
				Metadata metadata = schemasManager.getSchemaOf(record).getMetadata(field);
				if (!metadata.isMultivalue()) {
					String referenceId = record.get(metadata);
					return referenceId != null ? recordServices.get(referenceId) : null;
				} else {
					List<String> referenceIds = record.get(metadata);
					return CollectionUtils.isNotEmpty(referenceIds) ? recordServices.get(referenceIds) : Collections.emptyList();
				}
			} else {
				Object structure = dataFetchingEnvironment.getSource();
				Field structField = structure.getClass().getDeclaredField(field);
				structField.setAccessible(true);
				List<String> referenceIds = (List<String>) structField.get(structure);
				return CollectionUtils.isNotEmpty(referenceIds) ? recordServices.get(referenceIds) : Collections.emptyList();
			}
		};
	}

	public DataFetcher searchRecordsByExpressionDataFetcher(String schemaTypeCode) {
		return dataFetchingEnvironment -> {
			String expression = dataFetchingEnvironment.getArgument("expression");
			MetadataSchemaType schemaType = schemaTypes.getSchemaType(schemaTypeCode);
			LogicalSearchCondition condition = from(Collections.singletonList(schemaType)).returnAll();
			LogicalSearchQuery query = new LogicalSearchQuery(condition).setFreeTextQuery(expression);
			return searchServices.search(query);
		};
	}
}
