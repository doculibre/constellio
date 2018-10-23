package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
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
	public void migrate(String collection, MigrationResourcesProvider provider, AppLayerFactory appLayerFactory)
			throws Exception {

		new SchemaAlterationFor8_1_0_2(collection, provider, appLayerFactory).migrate();

	}

	private class SchemaAlterationFor8_1_0_2 extends MetadataSchemasAlterationHelper {
		public SchemaAlterationFor8_1_0_2(String collection, MigrationResourcesProvider migrationResourcesProvider,
										  AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder builder) {
			builder.getDefaultSchema(Folder.SCHEMA_TYPE)
					.createUndeletable(Folder.FAVORITES_LIST).setType(MetadataValueType.STRING).setMultivalue(true);
			builder.getDefaultSchema(Document.SCHEMA_TYPE)
					.createUndeletable(Document.FAVORITES_LIST).setType(MetadataValueType.STRING).setMultivalue(true);
			builder.getDefaultSchema(ContainerRecord.SCHEMA_TYPE)
					.createUndeletable(ContainerRecord.FAVORITES_LIST).setType(MetadataValueType.STRING).setMultivalue(true);
		}
	}

}
