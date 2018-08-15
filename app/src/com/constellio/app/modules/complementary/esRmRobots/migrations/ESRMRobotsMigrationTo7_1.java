package com.constellio.app.modules.complementary.esRmRobots.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.complementary.esRmRobots.model.ClassifyConnectorFolderDirectlyInThePlanActionParameters;
import com.constellio.app.modules.complementary.esRmRobots.model.ClassifyConnectorFolderInParentFolderActionParameters;
import com.constellio.app.modules.complementary.esRmRobots.model.ClassifyConnectorFolderInTaxonomyActionParameters;
import com.constellio.app.modules.robots.model.wrappers.ActionParameters;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaTypesDisplayTransactionBuilder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

import static com.constellio.app.modules.complementary.esRmRobots.model.ClassifyConnectorFolderInTaxonomyActionParameters.DEFAULT_PARENT_FOLDER;
import static com.constellio.app.modules.complementary.esRmRobots.model.ClassifyConnectorFolderInTaxonomyActionParameters.DOCUMENT_MAPPING;
import static com.constellio.app.modules.complementary.esRmRobots.model.ClassifyConnectorFolderInTaxonomyActionParameters.FOLDER_MAPPING;

public class ESRMRobotsMigrationTo7_1 implements MigrationScript {

	@Override
	public String getVersion() {
		return "7.1";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {

		new SchemaAlterationFor7_1(collection, migrationResourcesProvider, appLayerFactory).migrate();

		configureClassifyInTaxonomyParametersForm(collection, migrationResourcesProvider, appLayerFactory);
	}

	class SchemaAlterationFor7_1 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationFor7_1(String collection, MigrationResourcesProvider migrationResourcesProvider,
										 AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		public String getVersion() {
			return "7.1";
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			modifyClassifyConnectorFolderDirectlyInThePlanActionParametersSchema();
			modifyClassifyConnectorFolderInTaxonomyActionParametersSchema();
		}

		private void modifyClassifyConnectorFolderInTaxonomyActionParametersSchema() {
			MetadataSchemaBuilder schema = typesBuilder.getSchemaType(ActionParameters.SCHEMA_TYPE)
					.getSchema(ClassifyConnectorFolderInTaxonomyActionParameters.SCHEMA_LOCAL_CODE);

			schema.get(ClassifyConnectorFolderInTaxonomyActionParameters.DELIMITER).setDefaultRequirement(false);
		}

		private void modifyClassifyConnectorFolderDirectlyInThePlanActionParametersSchema() {
			MetadataSchemaBuilder schema = typesBuilder.getSchemaType(ActionParameters.SCHEMA_TYPE)
					.getSchema(ClassifyConnectorFolderDirectlyInThePlanActionParameters.SCHEMA_LOCAL_CODE);

			schema.get(ClassifyConnectorFolderDirectlyInThePlanActionParameters.DEFAULT_OPEN_DATE)
					.setDefaultRequirement(false);
		}
	}

	private void configureClassifyInTaxonomyParametersForm(String collection,
														   MigrationResourcesProvider migrationResourcesProvider,
														   AppLayerFactory appLayerFactory) {

		String mappingsTab = "tab.to.hide";

		SchemasDisplayManager schemasDisplayManager = appLayerFactory.getMetadataSchemasDisplayManager();
		SchemaTypesDisplayTransactionBuilder transaction = schemasDisplayManager.newTransactionBuilderFor(collection);

		String parametersSchema = ClassifyConnectorFolderDirectlyInThePlanActionParameters.SCHEMA;
		transaction.add(schemasDisplayManager.getMetadata(collection, parametersSchema, DOCUMENT_MAPPING)
				.withMetadataGroup(mappingsTab));
		transaction.add(schemasDisplayManager.getMetadata(collection, parametersSchema, FOLDER_MAPPING)
				.withMetadataGroup(mappingsTab));

		parametersSchema = ClassifyConnectorFolderInParentFolderActionParameters.SCHEMA;
		transaction.add(schemasDisplayManager.getMetadata(collection, parametersSchema, DOCUMENT_MAPPING)
				.withMetadataGroup(mappingsTab));
		transaction.add(schemasDisplayManager.getMetadata(collection, parametersSchema, FOLDER_MAPPING)
				.withMetadataGroup(mappingsTab));

		parametersSchema = ClassifyConnectorFolderInTaxonomyActionParameters.SCHEMA;
		transaction.add(schemasDisplayManager.getMetadata(collection, parametersSchema, DOCUMENT_MAPPING)
				.withMetadataGroup(mappingsTab));
		transaction.add(schemasDisplayManager.getMetadata(collection, parametersSchema, FOLDER_MAPPING)
				.withMetadataGroup(mappingsTab));

		//Also hide this option
		transaction.add(schemasDisplayManager.getMetadata(collection, parametersSchema, DEFAULT_PARENT_FOLDER)
				.withMetadataGroup(mappingsTab));

		schemasDisplayManager.execute(transaction.build());
	}
}
