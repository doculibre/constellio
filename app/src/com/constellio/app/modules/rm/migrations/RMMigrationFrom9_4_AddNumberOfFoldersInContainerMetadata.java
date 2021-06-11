package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class RMMigrationFrom9_4_AddNumberOfFoldersInContainerMetadata extends MigrationHelper implements MigrationScript {

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {
		new SchemaAlteration(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	class SchemaAlteration extends MetadataSchemasAlterationHelper {

		private final String NUMBER_OF_FOLDERS = "numberOfFolders";

		protected SchemaAlteration(String collection, MigrationResourcesProvider migrationResourcesProvider,
								   AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataBuilder foldersInContainer = typesBuilder.getDefaultSchema(Folder.SCHEMA_TYPE)
					.get(Folder.CONTAINER);
			MetadataSchemaBuilder containerBuilder = typesBuilder.getDefaultSchema(ContainerRecord.SCHEMA_TYPE);
			if (!containerBuilder.hasMetadata(ContainerRecord.FOLDERS_COUNT)) {
				containerBuilder.createUndeletable(ContainerRecord.FOLDERS_COUNT)
						.setType(MetadataValueType.NUMBER).defineDataEntry().asReferenceCount(foldersInContainer)
						.setSearchable(true);
			}
			if (containerBuilder.hasMetadata(NUMBER_OF_FOLDERS)) {
				containerBuilder.deleteMetadataWithoutValidation(NUMBER_OF_FOLDERS);
			}
		}
	}
}
