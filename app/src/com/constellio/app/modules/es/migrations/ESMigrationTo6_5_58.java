package com.constellio.app.modules.es.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.es.connectors.smb.model.SmbFolderPathPartsCalculator;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbFolder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.services.schemas.builders.CommonMetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class ESMigrationTo6_5_58 implements MigrationScript {

	@Override
	public String getVersion() {
		return "6.5.58";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory)
			throws Exception {
		new SchemaAlterationFor6_5_58(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	private class SchemaAlterationFor6_5_58 extends MetadataSchemasAlterationHelper {
		public SchemaAlterationFor6_5_58(String collection, MigrationResourcesProvider migrationResourcesProvider,
											AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			applyGeneratedSchemaAlteration(typesBuilder);
		}

		private void applyGeneratedSchemaAlteration(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaBuilder connectorSmbFolderSchema = typesBuilder.getSchema(ConnectorSmbFolder.DEFAULT_SCHEMA);
			MetadataBuilder connectorSmbFolder_pathParts = connectorSmbFolderSchema.get(CommonMetadataBuilder.PATH_PARTS);
			connectorSmbFolder_pathParts.defineDataEntry().asCalculated(SmbFolderPathPartsCalculator.class);
		}
	}
}
