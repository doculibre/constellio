package com.constellio.app.services.migrations.scripts;

import java.util.List;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Facet;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

public class CoreMigrationTo_5_1_4 implements MigrationScript {
	@Override
	public String getVersion() {
		return "5.1.4";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider provider, AppLayerFactory appLayerFactory)
			throws Exception {
		new CoreSchemaAlterationFor5_1_4(collection, provider, appLayerFactory).migrate();
		setNewFacetMetadatasDefaultValues(collection, appLayerFactory.getModelLayerFactory());
		appLayerFactory.getSystemGlobalConfigsManager().setReindexingRequired(true);
	}

	private void setNewFacetMetadatasDefaultValues(String collection, ModelLayerFactory modelLayerFactory) {
		RecordServices recordServices = modelLayerFactory.newRecordServices();
		SearchServices searchServices = modelLayerFactory.newSearchServices();
		MetadataSchemasManager manager = modelLayerFactory.getMetadataSchemasManager();
		MetadataSchemaType type = manager.getSchemaTypes(collection).getSchemaType(Facet.SCHEMA_TYPE);

		LogicalSearchCondition condition = LogicalSearchQueryOperators.from(type).returnAll();
		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(condition);
		List<Record> records = searchServices.search(query);
		Transaction transaction = new Transaction();

		for (Record record : records) {
			Metadata activeMetadata = type.getSchema(record.getSchemaCode()).getMetadata(Facet.ACTIVE);
			Metadata openByDefaultMetadata = type.getSchema(record.getSchemaCode()).getMetadata(Facet.OPEN_BY_DEFAULT);
			record.set(activeMetadata, true);
			record.set(openByDefaultMetadata, true);
			transaction.update(record);
		}
		try {
			recordServices.execute(transaction);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}

	}

	private class CoreSchemaAlterationFor5_1_4 extends MetadataSchemasAlterationHelper {
		public CoreSchemaAlterationFor5_1_4(String collection, MigrationResourcesProvider migrationResourcesProvider,
				AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			createNewFacetMetadatas(typesBuilder);
		}

		private void createNewFacetMetadatas(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaBuilder facetSchema = typesBuilder.getSchemaType(Facet.SCHEMA_TYPE).getDefaultSchema();

			facetSchema.createUndeletable(Facet.ACTIVE).setType(MetadataValueType.BOOLEAN).setDefaultValue(Boolean.TRUE);
			facetSchema.createUndeletable(Facet.OPEN_BY_DEFAULT).setType(MetadataValueType.BOOLEAN).setDefaultValue(Boolean.TRUE);

		}

	}

}