package com.constellio.app.ui.framework.data;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;

public class AllSchemaRecordVODataProvider extends RecordVODataProvider {

	public AllSchemaRecordVODataProvider(MetadataSchemaVO schema, RecordToVOBuilder voBuilder,
										 ModelLayerFactory modelLayerFactory) {
		super(schema, voBuilder, modelLayerFactory);
	}

	public AllSchemaRecordVODataProvider(String schemaCode, String collection) {
		this(getSchemaVO(schemaCode, collection), new RecordToVOBuilder(), ConstellioFactories.getInstance()
				.getModelLayerFactory());
	}

	private static MetadataSchema getSchema(String schemaCode, String collection, ModelLayerFactory modelLayerFactory) {
		MetadataSchemasManager metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
		MetadataSchemaTypes schemaTypes = metadataSchemasManager.getSchemaTypes(collection);
		return schemaTypes.getSchema(schemaCode);
	}

	private static MetadataSchemaType getSchemaType(String schemaTypeCode, String collection,
													ModelLayerFactory modelLayerFactory) {
		MetadataSchemasManager metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
		MetadataSchemaTypes schemaTypes = metadataSchemasManager.getSchemaTypes(collection);
		return schemaTypes.getSchemaType(schemaTypeCode);
	}

	private static MetadataSchemaVO getSchemaVO(String schemaCode, String collection) {
		ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
		ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
		MetadataSchema schema = getSchema(schemaCode, collection, modelLayerFactory);
		return new MetadataSchemaToVOBuilder().build(schema, VIEW_MODE.TABLE);
	}

	@Override
	public LogicalSearchQuery getQuery() {
		MetadataSchemaVO schemaVO = getSchema();
		String schemaCode = schemaVO.getCode();
		String collection = schemaVO.getCollection();
		LogicalSearchQuery query = new LogicalSearchQuery();
		if (schemaCode.endsWith("_default")) {
			String schemaTypeCode = SchemaUtils.getSchemaTypeCode(schemaCode);
			MetadataSchemaType schemaType = getSchemaType(schemaTypeCode, collection, modelLayerFactory);
			query.setCondition(LogicalSearchQueryOperators.from(schemaType).returnAll());
		} else {
			MetadataSchema schema = getSchema(schemaCode, collection, modelLayerFactory);
			query.setCondition(LogicalSearchQueryOperators.from(schema).returnAll());
		}
		query.filteredByStatus(StatusFilter.ACTIVES);
		query.sortAsc(Schemas.TITLE);
		return query;
	}

	@Override
	protected boolean isSearchCache() {
		return true;
	}

}
