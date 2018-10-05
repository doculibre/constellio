package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Email;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class RMMigrationTo8_1_1_1 extends MigrationHelper implements MigrationScript {
	@Override
	public String getVersion() {
		return "8.1.1.1";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {

		new SchemaAlterationsFor8_1_1_1(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}



	private static class SchemaAlterationsFor8_1_1_1 extends MetadataSchemasAlterationHelper {
		protected SchemaAlterationsFor8_1_1_1(String collection, MigrationResourcesProvider provider,
										  AppLayerFactory factory) {
			super(collection, provider, factory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder builder) {

			builder.getDefaultSchema(Document.SCHEMA_TYPE).get(Document.COMPANY).setEssential(false);
			builder.getDefaultSchema(Document.SCHEMA_TYPE).get(Document.SUBJECT).setEssential(false);
			builder.getSchema(Email.SCHEMA).get(Email.EMAIL_COMPANY).setEssential(false);
			builder.getSchema(Email.SCHEMA).get(Email.EMAIL_CC_TO).setEssential(false);
			builder.getSchema(Email.SCHEMA).get(Email.EMAIL_BCC_TO).setEssential(false);
			builder.getSchema(Email.SCHEMA).get(Email.EMAIL_IN_NAME_OF).setEssential(false);
			builder.getSchema(Email.SCHEMA).get(Email.EMAIL_FROM).setEssential(false);
			builder.getSchema(Email.SCHEMA).get(Email.EMAIL_TO).setEssential(false);
			builder.getSchema(Email.SCHEMA).get(Email.EMAIL_SENT_ON).setEssential(false);
			builder.getSchema(Email.SCHEMA).get(Email.EMAIL_RECEIVED_ON).setEssential(false);
			builder.getSchema(Email.SCHEMA).get(Email.SUBJECT_TO_BROADCAST_RULE).setEssential(false);
		}

	}
}
