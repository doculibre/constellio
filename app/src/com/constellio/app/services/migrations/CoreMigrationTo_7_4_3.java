package com.constellio.app.services.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.entities.schemasDisplay.SchemaTypesDisplayConfig;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaDisplayManagerTransaction;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.enums.SearchPageLength;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

import java.util.List;
import java.util.Set;

public class CoreMigrationTo_7_4_3 implements MigrationScript {

	@Override
	public String getVersion() {
		return "7.4.3";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory)
			throws Exception {
		new CoreSchemaAlterationFor7_4_3(collection, migrationResourcesProvider, appLayerFactory).migrate();
		List<MetadataSchemaType> schemaTypes = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection).getSchemaTypes();
		SchemasDisplayManager metadataSchemasDisplayManager = appLayerFactory.getMetadataSchemasDisplayManager();
		SchemaDisplayManagerTransaction transaction = new SchemaDisplayManagerTransaction();
		for(MetadataSchemaType type: schemaTypes) {
			List<MetadataSchema> allSchemas = type.getAllSchemas();
			for(MetadataSchema schema: allSchemas) {
				if(schema.hasMetadataWithCode(Schemas.PATH.getLocalCode())) {
					metadataSchemasDisplayManager.saveMetadata(metadataSchemasDisplayManager.getMetadata(collection,
							schema.get(Schemas.PATH.getLocalCode()).getCode()).withVisibleInAdvancedSearchStatus(true));
				}
			}
		}
//		metadataSchemasDisplayManager.execute();
	}

	private class CoreSchemaAlterationFor7_4_3 extends MetadataSchemasAlterationHelper {
		public CoreSchemaAlterationFor7_4_3(String collection, MigrationResourcesProvider migrationResourcesProvider,
				AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {

		}
	}
}