package com.constellio.app.modules.complementary.esRmRobots.migrations;

import static com.constellio.app.modules.complementary.esRmRobots.model.ClassifyConnectorFolderInTaxonomyActionParameters.ACTION_AFTER_CLASSIFICATION;
import static com.constellio.app.modules.complementary.esRmRobots.model.ClassifyConnectorFolderInTaxonomyActionParameters.DEFAULT_ADMIN_UNIT;
import static com.constellio.app.modules.complementary.esRmRobots.model.ClassifyConnectorFolderInTaxonomyActionParameters.DEFAULT_CATEGORY;
import static com.constellio.app.modules.complementary.esRmRobots.model.ClassifyConnectorFolderInTaxonomyActionParameters.DEFAULT_COPY_STATUS;
import static com.constellio.app.modules.complementary.esRmRobots.model.ClassifyConnectorFolderInTaxonomyActionParameters.DEFAULT_OPEN_DATE;
import static com.constellio.app.modules.complementary.esRmRobots.model.ClassifyConnectorFolderInTaxonomyActionParameters.DEFAULT_PARENT_FOLDER;
import static com.constellio.app.modules.complementary.esRmRobots.model.ClassifyConnectorFolderInTaxonomyActionParameters.DEFAULT_RETENTION_RULE;
import static com.constellio.app.modules.complementary.esRmRobots.model.ClassifyConnectorFolderInTaxonomyActionParameters.DOCUMENT_MAPPING;
import static com.constellio.app.modules.complementary.esRmRobots.model.ClassifyConnectorFolderInTaxonomyActionParameters.FOLDER_MAPPING;
import static java.util.Arrays.asList;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.complementary.esRmRobots.model.ClassifyConnectorFolderDirectlyInThePlanActionParameters;
import com.constellio.app.modules.complementary.esRmRobots.model.ClassifyConnectorFolderInParentFolderActionParameters;
import com.constellio.app.modules.complementary.esRmRobots.model.ClassifyConnectorFolderInTaxonomyActionParameters;
import com.constellio.app.modules.complementary.esRmRobots.model.enums.ActionAfterClassification;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.modules.robots.model.wrappers.ActionParameters;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaTypesDisplayTransactionBuilder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class ESRMRobotsMigrationTo6_1 implements MigrationScript {

	@Override
	public String getVersion() {
		return "6.1";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory)
			throws Exception {

		new SchemaAlterationFor6_1(collection, migrationResourcesProvider, appLayerFactory).migrate();

		configureClassifyInTaxonomyParametersForm(collection, migrationResourcesProvider, appLayerFactory);
	}

	class SchemaAlterationFor6_1 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationFor6_1(String collection, MigrationResourcesProvider migrationResourcesProvider,
				AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		public String getVersion() {
			return "6.1";
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			setupClassifyConnectorFolderInParentFolderActionParametersSchema();
			setupClassifyConnectorFolderDirectlyInThePlanActionParametersSchema();
			modifyClassifyConnectorFolderInTaxonomyActionParametersSchema();
		}

		private void modifyClassifyConnectorFolderInTaxonomyActionParametersSchema() {
			MetadataSchemaBuilder schema = typesBuilder.getSchemaType(ActionParameters.SCHEMA_TYPE)
					.getSchema(ClassifyConnectorFolderInTaxonomyActionParameters.SCHEMA_LOCAL_CODE);

			schema.get(ClassifyConnectorFolderInTaxonomyActionParameters.PATH_PREFIX).setDefaultRequirement(true);
			schema.get(ClassifyConnectorFolderInTaxonomyActionParameters.IN_TAXONOMY).setDefaultRequirement(true);
			schema.get(ClassifyConnectorFolderInTaxonomyActionParameters.DELIMITER).setDefaultRequirement(true);
		}

		private void setupClassifyConnectorFolderInParentFolderActionParametersSchema() {

			MetadataSchemaTypeBuilder folderSchemaType = typesBuilder.getSchemaType(Folder.SCHEMA_TYPE);

			MetadataSchemaBuilder schema = typesBuilder.getSchemaType(ActionParameters.SCHEMA_TYPE)
					.createCustomSchema(ClassifyConnectorFolderInParentFolderActionParameters.SCHEMA_LOCAL_CODE);
			schema.create(ClassifyConnectorFolderInParentFolderActionParameters.ACTION_AFTER_CLASSIFICATION)
					.setDefaultRequirement(true)
					.defineAsEnum(ActionAfterClassification.class)
					.setDefaultValue(ActionAfterClassification.DO_NOTHING);

			schema.create(ClassifyConnectorFolderInParentFolderActionParameters.FOLDER_MAPPING).setDefaultRequirement(false)
					.setType(
							MetadataValueType.CONTENT);
			schema.create(ClassifyConnectorFolderInParentFolderActionParameters.DOCUMENT_MAPPING).setDefaultRequirement(false)
					.setType(
							MetadataValueType.CONTENT);

			schema.createUndeletable(ClassifyConnectorFolderInParentFolderActionParameters.DEFAULT_PARENT_FOLDER)
					.setDefaultRequirement(false)
					.defineReferencesTo(folderSchemaType);

			schema.create(ClassifyConnectorFolderInParentFolderActionParameters.DEFAULT_OPEN_DATE).setDefaultRequirement(false)
					.setType(MetadataValueType.DATE);
		}

		private void setupClassifyConnectorFolderDirectlyInThePlanActionParametersSchema() {

			MetadataSchemaTypeBuilder categorySchemaType = typesBuilder.getSchemaType(Category.SCHEMA_TYPE);
			MetadataSchemaTypeBuilder retentionRuleSchemaType = typesBuilder.getSchemaType(RetentionRule.SCHEMA_TYPE);
			MetadataSchemaTypeBuilder administrativeUnitSchemaType = typesBuilder.getSchemaType(AdministrativeUnit.SCHEMA_TYPE);

			MetadataSchemaBuilder schema = typesBuilder.getSchemaType(ActionParameters.SCHEMA_TYPE)
					.createCustomSchema(ClassifyConnectorFolderDirectlyInThePlanActionParameters.SCHEMA_LOCAL_CODE);

			schema.create(ClassifyConnectorFolderInTaxonomyActionParameters.ACTION_AFTER_CLASSIFICATION)
					.setDefaultRequirement(true).defineAsEnum(ActionAfterClassification.class)
					.setDefaultValue(ActionAfterClassification.DO_NOTHING);

			schema.create(ClassifyConnectorFolderDirectlyInThePlanActionParameters.FOLDER_MAPPING)
					.setDefaultRequirement(false).setType(MetadataValueType.CONTENT);

			schema.create(ClassifyConnectorFolderDirectlyInThePlanActionParameters.DOCUMENT_MAPPING)
					.setDefaultRequirement(false).setType(MetadataValueType.CONTENT);

			schema.create(ClassifyConnectorFolderDirectlyInThePlanActionParameters.DEFAULT_ADMIN_UNIT)
					.setDefaultRequirement(true).defineReferencesTo(administrativeUnitSchemaType);

			schema.create(ClassifyConnectorFolderDirectlyInThePlanActionParameters.DEFAULT_CATEGORY)
					.setDefaultRequirement(true).defineReferencesTo(categorySchemaType);

			schema.create(ClassifyConnectorFolderDirectlyInThePlanActionParameters.DEFAULT_RETENTION_RULE)
					.setDefaultRequirement(true).defineReferencesTo(retentionRuleSchemaType);

			schema.create(ClassifyConnectorFolderDirectlyInThePlanActionParameters.DEFAULT_OPEN_DATE)
					.setDefaultRequirement(true).setType(MetadataValueType.DATE);

			schema.create(ClassifyConnectorFolderDirectlyInThePlanActionParameters.DEFAULT_COPY_STATUS)
					.setDefaultRequirement(true).defineAsEnum(CopyType.class);
		}
	}

	private void configureClassifyInTaxonomyParametersForm(String collection,
			MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory) {

		String taxonomyTab = migrationResourcesProvider.get("tab.taxonomy");
		String optionsTab = migrationResourcesProvider.get("tab.options");
		String defaultValuesTab = migrationResourcesProvider.get("tab.defaultValues");
		String mappingsTab = migrationResourcesProvider.get("tab.mappings");
		String advancedTab = migrationResourcesProvider.get("tab.advanced");

		String inFolderSchema = ClassifyConnectorFolderInParentFolderActionParameters.SCHEMA;
		String inPlanSchema = ClassifyConnectorFolderDirectlyInThePlanActionParameters.SCHEMA;

		SchemasDisplayManager schemasDisplayManager = appLayerFactory.getMetadataSchemasDisplayManager();
		SchemaTypesDisplayTransactionBuilder transaction = schemasDisplayManager.newTransactionBuilderFor(collection);

		transaction.add(schemasDisplayManager.getType(collection, ActionParameters.SCHEMA_TYPE)
				.withNewMetadataGroup(advancedTab));

		transaction.add(schemasDisplayManager.getSchema(collection, inPlanSchema).withFormMetadataCodes(asList(
				inPlanSchema + "_" + ClassifyConnectorFolderDirectlyInThePlanActionParameters.DEFAULT_ADMIN_UNIT,
				inPlanSchema + "_" + ClassifyConnectorFolderDirectlyInThePlanActionParameters.DEFAULT_CATEGORY,
				inPlanSchema + "_" + ClassifyConnectorFolderDirectlyInThePlanActionParameters.DEFAULT_RETENTION_RULE,
				inPlanSchema + "_" + ClassifyConnectorFolderDirectlyInThePlanActionParameters.DEFAULT_COPY_STATUS,
				inPlanSchema + "_" + ClassifyConnectorFolderDirectlyInThePlanActionParameters.DEFAULT_OPEN_DATE,

				inPlanSchema + "_" + ClassifyConnectorFolderDirectlyInThePlanActionParameters.DOCUMENT_MAPPING,
				inPlanSchema + "_" + ClassifyConnectorFolderDirectlyInThePlanActionParameters.FOLDER_MAPPING,

				inPlanSchema + "_" + ClassifyConnectorFolderDirectlyInThePlanActionParameters.ACTION_AFTER_CLASSIFICATION

		)));

		transaction.add(schemasDisplayManager.getSchema(collection, inFolderSchema).withFormMetadataCodes(asList(
				inFolderSchema + "_" + ClassifyConnectorFolderInParentFolderActionParameters.DEFAULT_PARENT_FOLDER,
				inFolderSchema + "_" + ClassifyConnectorFolderInParentFolderActionParameters.DEFAULT_OPEN_DATE,

				inFolderSchema + "_" + ClassifyConnectorFolderInParentFolderActionParameters.DOCUMENT_MAPPING,
				inFolderSchema + "_" + ClassifyConnectorFolderInParentFolderActionParameters.FOLDER_MAPPING,

				inFolderSchema + "_" + ClassifyConnectorFolderInParentFolderActionParameters.ACTION_AFTER_CLASSIFICATION

		)));

		transaction.add(schemasDisplayManager.getMetadata(collection, inPlanSchema, DEFAULT_ADMIN_UNIT)
				.withMetadataGroup(defaultValuesTab));
		transaction.add(schemasDisplayManager.getMetadata(collection, inPlanSchema, DEFAULT_CATEGORY)
				.withMetadataGroup(defaultValuesTab));
		transaction.add(schemasDisplayManager.getMetadata(collection, inPlanSchema, DEFAULT_RETENTION_RULE)
				.withMetadataGroup(defaultValuesTab));
		transaction.add(schemasDisplayManager.getMetadata(collection, inPlanSchema, DEFAULT_COPY_STATUS)
				.withMetadataGroup(defaultValuesTab));
		transaction.add(schemasDisplayManager.getMetadata(collection, inPlanSchema, DEFAULT_OPEN_DATE)
				.withMetadataGroup(defaultValuesTab));
		transaction.add(schemasDisplayManager.getMetadata(collection, inPlanSchema, DOCUMENT_MAPPING)
				.withMetadataGroup(advancedTab));
		transaction.add(schemasDisplayManager.getMetadata(collection, inPlanSchema, FOLDER_MAPPING)
				.withMetadataGroup(advancedTab));
		transaction.add(schemasDisplayManager.getMetadata(collection, inPlanSchema, ACTION_AFTER_CLASSIFICATION)
				.withMetadataGroup(optionsTab));

		transaction.add(schemasDisplayManager.getMetadata(collection, inFolderSchema, DEFAULT_PARENT_FOLDER)
				.withMetadataGroup(defaultValuesTab));
		transaction.add(schemasDisplayManager.getMetadata(collection, inFolderSchema, DEFAULT_OPEN_DATE)
				.withMetadataGroup(defaultValuesTab));
		transaction.add(schemasDisplayManager.getMetadata(collection, inFolderSchema, DOCUMENT_MAPPING)
				.withMetadataGroup(advancedTab));
		transaction.add(schemasDisplayManager.getMetadata(collection, inFolderSchema, FOLDER_MAPPING)
				.withMetadataGroup(advancedTab));
		transaction.add(schemasDisplayManager.getMetadata(collection, inFolderSchema, ACTION_AFTER_CLASSIFICATION)
				.withMetadataGroup(optionsTab));

		schemasDisplayManager.execute(transaction.build());
	}
}
