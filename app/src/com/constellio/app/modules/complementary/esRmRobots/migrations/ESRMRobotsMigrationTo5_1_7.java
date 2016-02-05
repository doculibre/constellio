package com.constellio.app.modules.complementary.esRmRobots.migrations;

import static com.constellio.app.modules.complementary.esRmRobots.model.ClassifyConnectorTaxonomyActionParameters.DEFAULT_ADMIN_UNIT;
import static com.constellio.app.modules.complementary.esRmRobots.model.ClassifyConnectorTaxonomyActionParameters.DEFAULT_PARENT_FOLDER;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.entities.schemasDisplay.SchemaDisplayConfig;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.modules.complementary.esRmRobots.model.ClassifyConnectorTaxonomyActionParameters;
import com.constellio.app.modules.complementary.esRmRobots.validators.ClassifyConnectorTaxonomyActionParametersValidator;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.robots.model.wrappers.ActionParameters;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaTypesDisplayTransactionBuilder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class ESRMRobotsMigrationTo5_1_7 implements MigrationScript {

	@Override
	public String getVersion() {
		return "5.1.7";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory)
			throws Exception {

		new SchemaAlterationFor5_1_7(collection, migrationResourcesProvider, appLayerFactory).migrate();

		updateClassifyInTaxonomyParametersForm(collection, migrationResourcesProvider, appLayerFactory);
	}

	class SchemaAlterationFor5_1_7 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationFor5_1_7(String collection, MigrationResourcesProvider migrationResourcesProvider,
				AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		public String getVersion() {
			return "5.1.7";
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			setupClassifyConnectorTaxonomyActionParametersSchema();
		}

		private void setupClassifyConnectorTaxonomyActionParametersSchema() {

			MetadataSchemaTypeBuilder folderType = typesBuilder.getSchemaType(Folder.SCHEMA_TYPE);

			MetadataSchemaBuilder schema = typesBuilder.getSchemaType(ActionParameters.SCHEMA_TYPE)
					.getCustomSchema(ClassifyConnectorTaxonomyActionParameters.SCHEMA_LOCAL_CODE);
			schema.createUndeletable(ClassifyConnectorTaxonomyActionParameters.DEFAULT_PARENT_FOLDER).setDefaultRequirement(false)
					.defineReferencesTo(folderType);
			schema.get(ClassifyConnectorTaxonomyActionParameters.PATH_PREFIX).setDefaultRequirement(false);
			schema.defineValidators().add(ClassifyConnectorTaxonomyActionParametersValidator.class);
		}

	}

	private void updateClassifyInTaxonomyParametersForm(String collection,
			MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory) {

		String defaultValuesTab = migrationResourcesProvider.get("tab.defaultValues");

		String parametersSchema = ClassifyConnectorTaxonomyActionParameters.SCHEMA;

		SchemasDisplayManager schemasDisplayManager = appLayerFactory.getMetadataSchemasDisplayManager();
		SchemaTypesDisplayTransactionBuilder transaction = schemasDisplayManager.newTransactionBuilderFor(collection);

		SchemaDisplayConfig displayConfig = schemasDisplayManager.getSchema(collection, parametersSchema);
		displayConfig = displayConfig
				.withNewDisplayMetadataBefore(parametersSchema + "_" + DEFAULT_PARENT_FOLDER,
						parametersSchema + "_" + DEFAULT_ADMIN_UNIT)
				.withNewFormMetadataBefore(parametersSchema + "_" + DEFAULT_PARENT_FOLDER,
						parametersSchema + "_" + DEFAULT_ADMIN_UNIT);

		transaction.add(displayConfig);

		transaction.add(schemasDisplayManager.getMetadata(collection, parametersSchema, DEFAULT_PARENT_FOLDER)
				.withMetadataGroup(defaultValuesTab).withInputType(MetadataInputType.LOOKUP));

		schemasDisplayManager.execute(transaction.build());
	}

}
