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
import com.constellio.model.entities.schemas.MetadataSchema;
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
		return "7.5.2";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory)
			throws Exception {
		this.collection = collection;
		this.migrationResourcesProvider = migrationResourcesProvider;
		this.appLayerFactory = appLayerFactory;

		new SchemaAlterationFor7_5_2(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	class SchemaAlterationFor7_5_2 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationFor7_5_2(String collection, MigrationResourcesProvider migrationResourcesProvider,
				AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaBuilder defaultFolderSchema = typesBuilder.getSchemaType(Folder.SCHEMA_TYPE).getDefaultSchema();
			defaultFolderSchema.createUndeletable(Folder.MANUAL_DISPOSAL_TYPE).defineAsEnum(DisposalType.class).setDefaultRequirement(false).setEnabled(false);

			SchemasDisplayManager schemaDisplayManager = appLayerFactory.getMetadataSchemasDisplayManager();
			SchemaTypesDisplayTransactionBuilder tx = schemaDisplayManager.newTransactionBuilderFor(collection);
			tx.in(Folder.SCHEMA_TYPE).addToForm(Folder.MANUAL_DISPOSAL_TYPE)
					.afterMetadata(Folder.CATEGORY_ENTERED);

			for (MetadataSchema schema : appLayerFactory.getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection)
					.getSchemaType(Folder.SCHEMA_TYPE).getAllSchemas()) {
				tx.add(schemaDisplayManager.getMetadata(collection, schema.getCode(), Folder.MANUAL_DISPOSAL_TYPE).withInputType(MetadataInputType.DROPDOWN));
			}
		}

	}
}
