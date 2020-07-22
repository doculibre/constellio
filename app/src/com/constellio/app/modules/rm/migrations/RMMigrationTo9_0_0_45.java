package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

//9.0
public class RMMigrationTo9_0_0_45 extends MigrationHelper implements MigrationScript {

	@Override
	public String getVersion() {
		return "9.0.0.45";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {
		new SchemaAlterationFor9_0_0_45(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	class SchemaAlterationFor9_0_0_45 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationFor9_0_0_45(String collection, MigrationResourcesProvider migrationResourcesProvider,
											  AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {

			MetadataBuilder categoryCode = typesBuilder.getDefaultSchema(Category.SCHEMA_TYPE).getMetadata(Category.CODE);
			MetadataSchemaBuilder folder = typesBuilder.getDefaultSchema(Folder.SCHEMA_TYPE);
			MetadataSchemaBuilder document = typesBuilder.getDefaultSchema(Document.SCHEMA_TYPE);

			//These cache insertions are necessary to avoid queries with document add/updates
			folder.get(Folder.OPENING_DATE).setEnabled(true).setEssentialInSummary(true);
			folder.get(Folder.CLOSING_DATE).setEnabled(true).setEnabled(true).setEssentialInSummary(true);
			folder.get(Folder.ACTUAL_DEPOSIT_DATE).setEnabled(true).setEssentialInSummary(true);
			folder.get(Folder.ACTUAL_DESTRUCTION_DATE).setEnabled(true).setEssentialInSummary(true);
			folder.get(Folder.ACTUAL_TRANSFER_DATE).setEnabled(true).setEssentialInSummary(true);
			folder.get(Folder.EXPECTED_DESTRUCTION_DATE).setEnabled(true).setEssentialInSummary(true);
			folder.get(Folder.EXPECTED_TRANSFER_DATE).setEnabled(true).setEssentialInSummary(true);
			folder.get(Folder.EXPECTED_DEPOSIT_DATE).setEnabled(true).setEssentialInSummary(true);
			folder.get(Schemas.CAPTION).setEnabled(true).setEssentialInSummary(true);
			folder.get(Schemas.SCHEMA_AUTOCOMPLETE_FIELD).setEnabled(true).setEssentialInSummary(true);
			folder.get(Folder.MAIN_COPY_RULE).setEnabled(true).setEssentialInSummary(true);
			folder.get(Schemas.PATH).setEnabled(true).setEssentialInSummary(true);
			folder.get(Schemas.ALL_REMOVED_AUTHS).setEnabled(true).setEssentialInSummary(true);
			folder.get(Schemas.ATTACHED_ANCESTORS).setEnabled(true).setEssentialInSummary(true);

			document.get(Document.FOLDER_CATEGORY_CODE).defineDataEntry().asCopied(folder.get(Document.FOLDER_CATEGORY), categoryCode);

		}
	}
}
