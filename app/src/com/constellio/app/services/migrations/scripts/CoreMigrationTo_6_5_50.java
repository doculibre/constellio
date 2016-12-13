package com.constellio.app.services.migrations.scripts;

import static com.constellio.app.services.migrations.CoreRoles.ADMINISTRATOR;
import static com.constellio.model.entities.CorePermissions.USE_EXTERNAL_APIS_FOR_COLLECTION;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static java.util.Arrays.asList;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.schemasDisplay.MetadataDisplayConfig;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.security.global.SolrUserCredential;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.services.security.roles.RolesManager;

import java.util.HashMap;
import java.util.Map;

public class CoreMigrationTo_6_5_50 implements MigrationScript {
	private final static Logger LOGGER = LoggerFactory.getLogger(CoreMigrationTo_6_5_50.class);

	@Override
	public String getVersion() {
		return "6.5.50";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider provider, AppLayerFactory appLayerFactory)
			throws Exception {
		new CoreSchemaAlterationFor6_5_50(collection, provider, appLayerFactory).migrate();
		modifyEvents(collection, appLayerFactory.getModelLayerFactory().getMetadataSchemasManager(), appLayerFactory);
	}

	private void modifyEvents(final String collection, MetadataSchemasManager manager, AppLayerFactory appLayerFactory) {
		SchemasDisplayManager schemasDisplayManager = appLayerFactory.getMetadataSchemasDisplayManager();
		schemasDisplayManager
				.saveMetadata(new MetadataDisplayConfig(collection, Event.DEFAULT_SCHEMA + "_" + Event.RECORD_VERSION, true,
						MetadataInputType.FIELD, true, "default", null));
	}

	private class CoreSchemaAlterationFor6_5_50 extends MetadataSchemasAlterationHelper {
		public CoreSchemaAlterationFor6_5_50(String collection, MigrationResourcesProvider migrationResourcesProvider,
				AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder builder) {
			builder.getDefaultSchema(Event.SCHEMA_TYPE).create(Event.RECORD_VERSION).setType(STRING).setMultivalue(false);
		}

	}

}
