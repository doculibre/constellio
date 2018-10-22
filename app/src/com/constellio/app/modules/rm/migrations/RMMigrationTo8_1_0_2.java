package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.RMEmailTemplateConstants;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_8_1_0_2;
import com.constellio.data.dao.managers.config.ConfigManagerException;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

public class RMMigrationTo8_1_0_2 implements MigrationScript {
	@Override
	public String getVersion() {
		return "8.1.0.2";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {
		//for i18ns
		new SchemaAlterationsFor8_1_0_2(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	private static class SchemaAlterationsFor8_1_0_2 extends MetadataSchemasAlterationHelper {
		protected SchemaAlterationsFor8_1_0_2(String collection, MigrationResourcesProvider provider,
											  AppLayerFactory factory) {
			super(collection, provider, factory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder builder) {
		}
	}
}
