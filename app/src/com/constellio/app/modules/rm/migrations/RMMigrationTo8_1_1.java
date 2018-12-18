package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.model.calculators.folder.FolderUniqueKeyCalculator;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.wrappers.DocumentListPDF;
import com.constellio.model.entities.records.wrappers.TemporaryRecord;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class RMMigrationTo8_1_1 implements MigrationScript {
	private String collection;

	private MigrationResourcesProvider migrationResourcesProvider;

	private AppLayerFactory appLayerFactory;

	@Override
	public String getVersion() {
		return "8.1.1";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {
		this.collection = collection;
		this.migrationResourcesProvider = migrationResourcesProvider;
		this.appLayerFactory = appLayerFactory;

		new SchemaAlterationFor8_1_1(collection, migrationResourcesProvider, appLayerFactory).migrate();

	}

	class SchemaAlterationFor8_1_1 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationFor8_1_1(String collection, MigrationResourcesProvider migrationResourcesProvider,
										   AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			if (!typesBuilder.getSchemaType(TemporaryRecord.SCHEMA_TYPE).hasSchema(DocumentListPDF.SCHEMA)) {
				typesBuilder.getSchemaType(TemporaryRecord.SCHEMA_TYPE).createCustomSchema(DocumentListPDF.SCHEMA);
			}

			MetadataSchemaBuilder defaultSchema = typesBuilder.getDefaultSchema(Folder.SCHEMA_TYPE);
			if (!defaultSchema.hasMetadata(Folder.UNIQUE_KEY)) {
				defaultSchema.createUndeletable(Folder.UNIQUE_KEY).setType(MetadataValueType.STRING).setSystemReserved(true)
						.setUniqueValue(true).defineDataEntry().asCalculated(FolderUniqueKeyCalculator.class);
			}
		}
	}
}
