package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaTypesDisplayTransactionBuilder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class RMMigrationTo5_1_0_3 implements MigrationScript {

	@Override
	public String getVersion() {
		return "5.1.0.3";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
			AppLayerFactory appLayerFactory)
			throws Exception {

		new SchemaAlterationFor5_1_0_3(collection, migrationResourcesProvider, appLayerFactory).migrate();
		updateFormSearchResultAndDisplayConfigs(collection, appLayerFactory);

	}

	private void updateFormSearchResultAndDisplayConfigs(String collection, AppLayerFactory appLayerFactory) {
		SchemasDisplayManager manager = appLayerFactory.getMetadataSchemasDisplayManager();

		SchemaTypesDisplayTransactionBuilder transactionBuilder = manager.newTransactionBuilderFor(collection);

		transactionBuilder.in(AdministrativeUnit.SCHEMA_TYPE)
				.removeFromDisplay(AdministrativeUnit.FILING_SPACES);
		transactionBuilder.in(AdministrativeUnit.SCHEMA_TYPE)
				.removeFromDisplay(AdministrativeUnit.FILING_SPACES_ADMINISTRATORS);
		transactionBuilder.in(AdministrativeUnit.SCHEMA_TYPE)
				.removeFromDisplay(AdministrativeUnit.FILING_SPACES_USERS);

		transactionBuilder.in(AdministrativeUnit.SCHEMA_TYPE)
				.removeFromForm(AdministrativeUnit.FILING_SPACES);
		transactionBuilder.in(AdministrativeUnit.SCHEMA_TYPE)
				.removeFromForm(AdministrativeUnit.FILING_SPACES_ADMINISTRATORS);
		transactionBuilder.in(AdministrativeUnit.SCHEMA_TYPE)
				.removeFromForm(AdministrativeUnit.FILING_SPACES_USERS);

		transactionBuilder.in(AdministrativeUnit.SCHEMA_TYPE)
				.removeFromSearchResult(AdministrativeUnit.FILING_SPACES);
		transactionBuilder.in(AdministrativeUnit.SCHEMA_TYPE)
				.removeFromSearchResult(AdministrativeUnit.FILING_SPACES_ADMINISTRATORS);
		transactionBuilder.in(AdministrativeUnit.SCHEMA_TYPE)
				.removeFromSearchResult(AdministrativeUnit.FILING_SPACES_USERS);

		manager.execute(transactionBuilder.build());
	}

	class SchemaAlterationFor5_1_0_3 extends MetadataSchemasAlterationHelper {
		MetadataSchemaTypes types;

		protected SchemaAlterationFor5_1_0_3(String collection, MigrationResourcesProvider migrationResourcesProvider,
				AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
			types = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection);
		}

		public String getVersion() {
			return "5.1.0.3";
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {

			MetadataSchemaBuilder adminstrativeUnitSchema = typesBuilder.getSchema(AdministrativeUnit.DEFAULT_SCHEMA);
			adminstrativeUnitSchema.getMetadata(AdministrativeUnit.FILING_SPACES).setEssential(false)
					.setDefaultRequirement(false);
			adminstrativeUnitSchema.getMetadata(AdministrativeUnit.FILING_SPACES_ADMINISTRATORS).setEssential(false)
					.setDefaultRequirement(false);
			adminstrativeUnitSchema.getMetadata(AdministrativeUnit.FILING_SPACES_USERS).setEssential(false)
					.setDefaultRequirement(false);
		}
	}
}
