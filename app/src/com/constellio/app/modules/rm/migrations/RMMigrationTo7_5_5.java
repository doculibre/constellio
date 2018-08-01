package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.model.calculators.folder.FolderMainCopyRuleCodeCalculator;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

/**
 * Created by constellios on 2017-07-13.
 */
public class RMMigrationTo7_5_5 extends MigrationHelper implements MigrationScript {
	private String collection;

	private MigrationResourcesProvider migrationResourcesProvider;

	private AppLayerFactory appLayerFactory;

	@Override
	public String getVersion() {
		return "7.5.5";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {
		this.collection = collection;
		this.migrationResourcesProvider = migrationResourcesProvider;
		this.appLayerFactory = appLayerFactory;

		new RMMigrationTo7_5_5.SchemaAlterationFor7_5_5(collection, migrationResourcesProvider, appLayerFactory).migrate();
		SchemasDisplayManager displayManager = appLayerFactory.getMetadataSchemasDisplayManager();
		displayManager.saveMetadata(displayManager.getMetadata(collection, Folder.DEFAULT_SCHEMA + "_" + Folder.MAIN_COPY_RULE_CODE)
				.withVisibleInAdvancedSearchStatus(true));
		appLayerFactory.getSystemGlobalConfigsManager().setReindexingRequired(true);
	}

	class SchemaAlterationFor7_5_5 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationFor7_5_5(String collection, MigrationResourcesProvider migrationResourcesProvider,
										   AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			typesBuilder.getDefaultSchema(Folder.SCHEMA_TYPE).createUndeletable(Folder.MAIN_COPY_RULE_CODE)
					.setType(MetadataValueType.STRING).setSystemReserved(true).setSearchable(true).defineDataEntry()
					.asCalculated(FolderMainCopyRuleCodeCalculator.class);
		}

	}
}
