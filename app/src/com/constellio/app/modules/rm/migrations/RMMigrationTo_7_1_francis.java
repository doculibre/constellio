package com.constellio.app.modules.rm.migrations;

import static com.constellio.model.entities.schemas.MetadataTransiency.TRANSIENT_EAGER;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.schemas.MetadataTransiency;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class RMMigrationTo_7_1_francis implements MigrationScript {
	@Override
	public String getVersion() {
		return "7.1.666";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider provider, AppLayerFactory factory)
			throws Exception {
		new SchemaAlterationsFor7_1_666(collection, provider, factory).migrate();

	}

	public static class SchemaAlterationsFor7_1_666 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationsFor7_1_666(String collection, MigrationResourcesProvider provider, AppLayerFactory factory) {
			super(collection, provider, factory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaBuilder folderSchema = typesBuilder.getSchema(Folder.DEFAULT_SCHEMA);

			//Used in search conditions:
			//folderSchema.getMetadata(Folder.ACTIVE_RETENTION_TYPE).setTransiency(TRANSIENT_EAGER);
			//folderSchema.getMetadata(Folder.INACTIVE_DISPOSAL_TYPE).setTransiency(TRANSIENT_EAGER);

			//TODO : Tester si ces métadonnées peuvent être rendu transients
			folderSchema.getMetadata(Folder.APPLICABLE_COPY_RULES).setTransiency(TRANSIENT_EAGER);
			folderSchema.getMetadata(Folder.ACTIVE_RETENTION_CODE).setTransiency(TRANSIENT_EAGER);

			folderSchema.getMetadata(Folder.SEMIACTIVE_RETENTION_CODE).setTransiency(TRANSIENT_EAGER);
			folderSchema.getMetadata(Folder.SEMIACTIVE_RETENTION_TYPE).setTransiency(TRANSIENT_EAGER);

			folderSchema.getMetadata(Folder.COPY_RULES_EXPECTED_TRANSFER_DATES).setTransiency(TRANSIENT_EAGER);
			folderSchema.getMetadata(Folder.COPY_RULES_EXPECTED_DEPOSIT_DATES).setTransiency(TRANSIENT_EAGER);
			folderSchema.getMetadata(Folder.COPY_RULES_EXPECTED_DESTRUCTION_DATES).setTransiency(TRANSIENT_EAGER);
			folderSchema.getMetadata(Folder.MEDIA_TYPE).setTransiency(TRANSIENT_EAGER);
			folderSchema.getMetadata(Folder.MAIN_COPY_RULE).setTransiency(TRANSIENT_EAGER);
			folderSchema.getMetadata(Folder.DECOMMISSIONING_DATE).setTransiency(TRANSIENT_EAGER);
		}
	}

}