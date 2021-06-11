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
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class RMMigrationTo9_2_15 extends MigrationHelper implements MigrationScript {

	@Override
	public String getVersion() {
		return "9.2.15";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {
		new SchemaAlterationFor9_2_15(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	class SchemaAlterationFor9_2_15 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationFor9_2_15(String collection, MigrationResourcesProvider migrationResourcesProvider,
											AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataBuilder foldersInContainer = typesBuilder.getDefaultSchema(Folder.SCHEMA_TYPE)
					.get(Folder.CONTAINER);
			typesBuilder.getDefaultSchema(ContainerRecord.SCHEMA_TYPE).createUndeletable(ContainerRecord.NUMBER_OF_FOLDERS)
					.setType(MetadataValueType.NUMBER).defineDataEntry().asReferenceCount(foldersInContainer)
					.setSearchable(true);

		}
	}
}
