package com.constellio.app.modules.es.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.es.model.connectors.http.ConnectorHttpDocument;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.RecordCacheType;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class ESMigrationTo9_0 extends MigrationHelper implements MigrationScript {

	MigrationResourcesProvider migrationResourcesProvider;

	@Override
	public String getVersion() {
		return "9.0";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {
		new SchemaAlterationFor9_0(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	static class SchemaAlterationFor9_0 extends MetadataSchemasAlterationHelper {
		MetadataSchemaTypes types;

		protected SchemaAlterationFor9_0(String collection, MigrationResourcesProvider migrationResourcesProvider,
										 AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
			types = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection);
		}

		public String getVersion() {
			return "9.0";
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder types) {
			types.getSchemaType(ConnectorHttpDocument.SCHEMA_TYPE).setRecordCacheType(RecordCacheType.NOT_CACHED);
		}
	}
}
