package com.constellio.app.modules.es.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.es.model.connectors.ConnectorInstance;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbInstance;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaTypesDisplayTransactionBuilder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

import java.util.Map;

import static com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbInstance.SKIP_CONTENT_AND_ACL;
import static com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbInstance.SKIP_SHARE_ACCESS_CONTROL;
import static java.util.Arrays.asList;

public class ESMigrationTo8_1_1 extends MigrationHelper implements MigrationScript {

	MigrationResourcesProvider migrationResourcesProvider;

	@Override
	public String getVersion() {
		return "8.1.1";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {
		this.migrationResourcesProvider = migrationResourcesProvider;

		new SchemaAlterationFor8_1_1(collection, migrationResourcesProvider, appLayerFactory).migrate();
		updateFormAndDisplay(collection, appLayerFactory);
	}

	private void updateFormAndDisplay(String collection, AppLayerFactory appLayerFactory) {
		SchemasDisplayManager manager = appLayerFactory.getMetadataSchemasDisplayManager();

		String advancedTab = "connectors.advanced";

		SchemaTypesDisplayTransactionBuilder transaction = manager.newTransactionBuilderFor(collection);
		transaction.add(manager.getSchema(collection, ConnectorSmbInstance.SCHEMA_CODE)
				.withNewFormMetadata(ConnectorSmbInstance.SCHEMA_CODE + "_" + SKIP_CONTENT_AND_ACL));
		transaction.add(manager.getMetadata(collection, ConnectorSmbInstance.SCHEMA_CODE, SKIP_CONTENT_AND_ACL)
				.withMetadataGroup(advancedTab));

		manager.execute(transaction.build());
	}

	static class SchemaAlterationFor8_1_1 extends MetadataSchemasAlterationHelper {
		MetadataSchemaTypes types;

		protected SchemaAlterationFor8_1_1(String collection, MigrationResourcesProvider migrationResourcesProvider,
										 AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
			types = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection);
		}

		public String getVersion() {
			return "6.1";
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder types) {
			MetadataSchemaBuilder smbConnectorSchemaType = types.getSchema(ConnectorSmbInstance.SCHEMA_CODE);
			smbConnectorSchemaType.create(SKIP_CONTENT_AND_ACL)
					.setType(MetadataValueType.BOOLEAN).setDefaultValue(false);
		}
	}
}
