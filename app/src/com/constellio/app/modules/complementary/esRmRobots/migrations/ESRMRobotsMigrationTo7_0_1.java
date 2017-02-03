package com.constellio.app.modules.complementary.esRmRobots.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.complementary.esRmRobots.model.ClassifyConnectorFolderDirectlyInThePlanActionParameters;
import com.constellio.app.modules.complementary.esRmRobots.model.ClassifyConnectorFolderInTaxonomyActionParameters;
import com.constellio.app.modules.rm.wrappers.UniformSubdivision;
import com.constellio.app.modules.robots.model.wrappers.ActionParameters;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaTypesDisplayTransactionBuilder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class ESRMRobotsMigrationTo7_0_1 implements MigrationScript {

	@Override
	public String getVersion() {
		return "7.0.1";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory)
			throws Exception {

		new SchemaAlterationFor7_0_1(collection, migrationResourcesProvider, appLayerFactory).migrate();

		configureClassifyInTaxonomyParametersForm(collection, migrationResourcesProvider, appLayerFactory);
	}

	class SchemaAlterationFor7_0_1 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationFor7_0_1(String collection, MigrationResourcesProvider migrationResourcesProvider,
				AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		public String getVersion() {
			return "7.0.1";
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			setupClassifyConnectorFolderDirectlyInThePlanActionParametersSchema();
		}

		private void setupClassifyConnectorFolderDirectlyInThePlanActionParametersSchema() {

			MetadataSchemaTypeBuilder subdivisionSchemaType = typesBuilder.getSchemaType(UniformSubdivision.SCHEMA_TYPE);

			MetadataSchemaBuilder schema = typesBuilder.getSchemaType(ActionParameters.SCHEMA_TYPE)
					.getCustomSchema(ClassifyConnectorFolderDirectlyInThePlanActionParameters.SCHEMA_LOCAL_CODE);

			schema.create(ClassifyConnectorFolderDirectlyInThePlanActionParameters.DEFAULT_UNIFORM_SUBDIVISION)
					.setDefaultRequirement(false).defineReferencesTo(subdivisionSchemaType);

			schema = typesBuilder.getSchemaType(ActionParameters.SCHEMA_TYPE)
					.getCustomSchema(ClassifyConnectorFolderInTaxonomyActionParameters.SCHEMA_LOCAL_CODE);

			schema.create(ClassifyConnectorFolderInTaxonomyActionParameters.DEFAULT_UNIFORM_SUBDIVISION)
					.setDefaultRequirement(false).defineReferencesTo(subdivisionSchemaType);
		}
	}

	private void configureClassifyInTaxonomyParametersForm(String collection,
			MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory) {

		String defaultValuesTab = "tab.defaultValues";

		String inPlanSchema = ClassifyConnectorFolderDirectlyInThePlanActionParameters.SCHEMA;

		SchemasDisplayManager schemasDisplayManager = appLayerFactory.getMetadataSchemasDisplayManager();
		SchemaTypesDisplayTransactionBuilder transaction = schemasDisplayManager.newTransactionBuilderFor(collection);

		transaction.add(schemasDisplayManager.getSchema(collection, inPlanSchema).withNewFormMetadataBefore(
				inPlanSchema + "_" + ClassifyConnectorFolderDirectlyInThePlanActionParameters.DEFAULT_UNIFORM_SUBDIVISION,
				inPlanSchema + "_" + ClassifyConnectorFolderDirectlyInThePlanActionParameters.DEFAULT_RETENTION_RULE));

		transaction.add(schemasDisplayManager.getMetadata(collection, inPlanSchema,
				ClassifyConnectorFolderDirectlyInThePlanActionParameters.DEFAULT_UNIFORM_SUBDIVISION)
				.withMetadataGroup(defaultValuesTab));

		inPlanSchema = ClassifyConnectorFolderInTaxonomyActionParameters.SCHEMA;

		transaction.add(schemasDisplayManager.getSchema(collection, inPlanSchema).withNewFormMetadataBefore(
				inPlanSchema + "_" + ClassifyConnectorFolderInTaxonomyActionParameters.DEFAULT_UNIFORM_SUBDIVISION,
				inPlanSchema + "_" + ClassifyConnectorFolderInTaxonomyActionParameters.DEFAULT_RETENTION_RULE));

		transaction.add(schemasDisplayManager.getMetadata(collection, inPlanSchema,
				ClassifyConnectorFolderInTaxonomyActionParameters.DEFAULT_UNIFORM_SUBDIVISION)
				.withMetadataGroup(defaultValuesTab));

		schemasDisplayManager.execute(transaction.build());
	}
}
