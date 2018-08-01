package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.modules.rm.model.enums.DisposalType;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaTypesDisplayTransactionBuilder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

/**
 * Created by constellios on 2017-07-13.
 */
public class RMMigrationTo7_5_3 extends MigrationHelper implements MigrationScript {
	private String collection;

	private MigrationResourcesProvider migrationResourcesProvider;

	private AppLayerFactory appLayerFactory;

	@Override
	public String getVersion() {
		return "7.5.3";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {
		this.collection = collection;
		this.migrationResourcesProvider = migrationResourcesProvider;
		this.appLayerFactory = appLayerFactory;

		new SchemaAlterationFor7_5_3(collection, migrationResourcesProvider, appLayerFactory).migrate();

		setupDisplayConfig(appLayerFactory);
	}

	private void setupDisplayConfig(AppLayerFactory appLayerFactory) {
		SchemasDisplayManager schemaDisplayManager = appLayerFactory.getMetadataSchemasDisplayManager();
		SchemaTypesDisplayTransactionBuilder tx = schemaDisplayManager.newTransactionBuilderFor(collection);
		tx.add(schemaDisplayManager.getMetadata(collection, Folder.DEFAULT_SCHEMA, Folder.MANUAL_DISPOSAL_TYPE)
				.withInputType(MetadataInputType.DROPDOWN));

		schemaDisplayManager.execute(tx.build());
	}

	class SchemaAlterationFor7_5_3 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationFor7_5_3(String collection, MigrationResourcesProvider migrationResourcesProvider,
										   AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaBuilder defaultFolderSchema = typesBuilder.getSchemaType(Folder.SCHEMA_TYPE).getDefaultSchema();
			if (!defaultFolderSchema.hasMetadata(Folder.MANUAL_DISPOSAL_TYPE)) {
				defaultFolderSchema.createUndeletable(Folder.MANUAL_DISPOSAL_TYPE).defineAsEnum(DisposalType.class)
						.setDefaultRequirement(false).setEnabled(false);
			}
		}

	}
}
