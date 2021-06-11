package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.wrappers.LegalReference;
import com.constellio.app.modules.rm.wrappers.LegalRequirement;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class RMMigrationFrom9_4_AddLegalRequirementAndLegalReferenceToSearch implements MigrationScript {

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory) throws Exception {
		new SchemaAlteration(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	private class SchemaAlteration extends MetadataSchemasAlterationHelper {
		private SchemasDisplayManager manager;

		SchemaAlteration(String collection, MigrationResourcesProvider migrationResourcesProvider,
						 AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			manager = appLayerFactory.getMetadataSchemasDisplayManager();
			manager.saveType(manager.getType(collection, LegalRequirement.SCHEMA_TYPE)
					.withSimpleAndAdvancedSearchStatus(true)
					.withAdvancedSearchStatus(true));
			manager.saveType(manager.getType(collection, LegalReference.SCHEMA_TYPE)
					.withSimpleAndAdvancedSearchStatus(true)
					.withAdvancedSearchStatus(true));
		}
	}
}
