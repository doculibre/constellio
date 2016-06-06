package com.constellio.app.services.migrations.scripts;

import static java.util.Arrays.asList;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaTypesDisplayTransactionBuilder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class CoreMigrationTo_6_4 implements MigrationScript {
	@Override
	public String getVersion() {
		return "6.4";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider provider, AppLayerFactory appLayerFactory)
			throws Exception {
		//FIXME is called several times for the different installed modules!!
		new CoreSchemaAlterationFor6_4(collection, provider, appLayerFactory).migrate();
		//initializeUsersLanguages(collection, appLayerFactory);a
		applySchemasDisplay(collection, appLayerFactory.getMetadataSchemasDisplayManager());
	}

	private class CoreSchemaAlterationFor6_4 extends MetadataSchemasAlterationHelper {
		public CoreSchemaAlterationFor6_4(String collection, MigrationResourcesProvider provider,
				AppLayerFactory appLayerFactory) {
			super(collection, provider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {

		}
	}

	private void applySchemasDisplay(String collection, SchemasDisplayManager manager) {
		SchemaTypesDisplayTransactionBuilder transaction = manager.newTransactionBuilderFor(collection);

		transaction.add(manager.getSchema(collection, "user_default").withDisplayMetadataCodes(asList(
				"user_default_username",
				"user_default_firstname",
				"user_default_lastname",
				"user_default_title",
				"user_default_email",
				"user_default_userroles",
				"user_default_groups",
				"user_default_jobTitle",
				"user_default_phone",
				"user_default_status",
				"user_default_createdOn",
				"user_default_modifiedOn",
				"user_default_allroles"
		)));

		manager.execute(transaction.build());
	}
}
