package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.entities.schemasDisplay.SchemaDisplayConfig;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.model.entities.schemas.MetadataValueType.CONTENT;

public class RMMigrationTo9_0_1 implements MigrationScript {

	@Override
	public String getVersion() {
		return "9.0.1";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider provider, AppLayerFactory appLayerFactory)
			throws Exception {
		new RMMigrationTo9_0_1.SchemaAlterationFor9_0_1(collection, provider, appLayerFactory).migrate();
	}

	private class SchemaAlterationFor9_0_1 extends MetadataSchemasAlterationHelper {
		public SchemaAlterationFor9_0_1(String collection, MigrationResourcesProvider migrationResourcesProvider,
										AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder builder) {
			MetadataSchemaTypeBuilder schemaType = types().getSchemaType(DecommissioningList.SCHEMA_TYPE);
			MetadataSchemaBuilder defaultSchema = schemaType.getDefaultSchema();
			defaultSchema.createUndeletable(DecommissioningList.CONTENTS).setType(CONTENT).setMultivalue(true).setSearchable(true);

			SchemasDisplayManager manager = appLayerFactory.getMetadataSchemasDisplayManager();
			SchemaDisplayConfig schemaDisplayConfig = manager.getSchema(collection, DecommissioningList.DEFAULT_SCHEMA);
			List<String> formMetadataCodes = new ArrayList<>();
			formMetadataCodes.addAll(schemaDisplayConfig.getFormMetadataCodes());
			formMetadataCodes.add(DecommissioningList.DEFAULT_SCHEMA + "_" + DecommissioningList.CONTENTS);
			schemaDisplayConfig = schemaDisplayConfig.withFormMetadataCodes(formMetadataCodes);
			manager.saveSchema(schemaDisplayConfig);

			List<String> displayMetadataCodes = new ArrayList<>();
			displayMetadataCodes.addAll(schemaDisplayConfig.getDisplayMetadataCodes());
			displayMetadataCodes.add(DecommissioningList.DEFAULT_SCHEMA + "_" + DecommissioningList.CONTENTS);
			schemaDisplayConfig = schemaDisplayConfig.withDisplayMetadataCodes(displayMetadataCodes);
			manager.saveSchema(schemaDisplayConfig);
		}
	}
}
