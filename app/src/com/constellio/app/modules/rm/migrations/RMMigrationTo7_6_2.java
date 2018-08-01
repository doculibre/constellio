package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Email;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class RMMigrationTo7_6_2 extends MigrationHelper implements MigrationScript {
	@Override
	public String getVersion() {
		return "7.6.2";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {
		new SchemaAlterationFor7_6_2(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	class SchemaAlterationFor7_6_2 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationFor7_6_2(String collection, MigrationResourcesProvider migrationResourcesProvider,
										   AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			if (typesBuilder.getSchemaType(Document.SCHEMA_TYPE).hasSchema(Email.SCHEMA)) {
				typesBuilder.getSchema(Email.SCHEMA).getMetadata(Schemas.LEGACY_ID.getLocalCode())
						.addLabel(Language.French, "Ancien Identifiant");
			}
		}
	}
}
