package com.constellio.app.modules.complementary.esRmRobots.migrations;

import static com.constellio.app.modules.complementary.esRmRobots.model.ClassifyConnectorFolderInTaxonomyActionParameters.ACTION_AFTER_CLASSIFICATION;
import static com.constellio.app.modules.complementary.esRmRobots.model.ClassifyConnectorFolderInTaxonomyActionParameters.DEFAULT_ADMIN_UNIT;
import static com.constellio.app.modules.complementary.esRmRobots.model.ClassifyConnectorFolderInTaxonomyActionParameters.DEFAULT_CATEGORY;
import static com.constellio.app.modules.complementary.esRmRobots.model.ClassifyConnectorFolderInTaxonomyActionParameters.DEFAULT_COPY_STATUS;
import static com.constellio.app.modules.complementary.esRmRobots.model.ClassifyConnectorFolderInTaxonomyActionParameters.DEFAULT_OPEN_DATE;
import static com.constellio.app.modules.complementary.esRmRobots.model.ClassifyConnectorFolderInTaxonomyActionParameters.DEFAULT_RETENTION_RULE;
import static com.constellio.app.modules.complementary.esRmRobots.model.ClassifyConnectorFolderInTaxonomyActionParameters.DELIMITER;
import static com.constellio.app.modules.complementary.esRmRobots.model.ClassifyConnectorFolderInTaxonomyActionParameters.DOCUMENT_MAPPING;
import static com.constellio.app.modules.complementary.esRmRobots.model.ClassifyConnectorFolderInTaxonomyActionParameters.FOLDER_MAPPING;
import static com.constellio.app.modules.complementary.esRmRobots.model.ClassifyConnectorFolderInTaxonomyActionParameters.IN_TAXONOMY;
import static com.constellio.app.modules.complementary.esRmRobots.model.ClassifyConnectorFolderInTaxonomyActionParameters.PATH_PREFIX;
import static java.util.Arrays.asList;

import java.util.HashMap;
import java.util.Map;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.complementary.esRmRobots.model.ClassifyConnectorFolderInTaxonomyActionParameters;
import com.constellio.app.modules.complementary.esRmRobots.model.enums.ActionAfterClassification;
import com.constellio.app.modules.rm.constants.RMTaxonomies;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.modules.robots.model.wrappers.ActionParameters;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaTypesDisplayTransactionBuilder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class ESRMRobotsMigrationTo5_1_5 implements MigrationScript {

	@Override
	public String getVersion() {
		return "5.1.5";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory)
			throws Exception {

		new SchemaAlterationFor5_1_5(collection, migrationResourcesProvider, appLayerFactory).migrate();

		configureClassifyInTaxonomyParametersForm(collection, migrationResourcesProvider, appLayerFactory);
	}

	class SchemaAlterationFor5_1_5 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationFor5_1_5(String collection, MigrationResourcesProvider migrationResourcesProvider,
				AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		public String getVersion() {
			return "5.1.5";
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			setupClassifyConnectorTaxonomyActionParametersSchema();
		}

		private void setupClassifyConnectorTaxonomyActionParametersSchema() {

			MetadataSchemaTypeBuilder categorySchemaType = typesBuilder.getSchemaType(Category.SCHEMA_TYPE);
			MetadataSchemaTypeBuilder retentionRuleSchemaType = typesBuilder.getSchemaType(RetentionRule.SCHEMA_TYPE);
			MetadataSchemaTypeBuilder administrativeUnitSchemaType = typesBuilder.getSchemaType(AdministrativeUnit.SCHEMA_TYPE);

			MetadataSchemaBuilder schema = typesBuilder.getSchemaType(ActionParameters.SCHEMA_TYPE)
					.createCustomSchema(ClassifyConnectorFolderInTaxonomyActionParameters.SCHEMA_LOCAL_CODE);
			schema.create(ClassifyConnectorFolderInTaxonomyActionParameters.IN_TAXONOMY).setDefaultRequirement(true)
					.setType(MetadataValueType.STRING).setDefaultValue(RMTaxonomies.ADMINISTRATIVE_UNITS);
			schema.create(ClassifyConnectorFolderInTaxonomyActionParameters.ACTION_AFTER_CLASSIFICATION)
					.setDefaultRequirement(true)
					.defineAsEnum(ActionAfterClassification.class)
					.setDefaultValue(ActionAfterClassification.DO_NOTHING);
			schema.create(ClassifyConnectorFolderInTaxonomyActionParameters.DELIMITER).setDefaultRequirement(false).setType(
					MetadataValueType.STRING);
			schema.create(ClassifyConnectorFolderInTaxonomyActionParameters.FOLDER_MAPPING).setDefaultRequirement(false).setType(
					MetadataValueType.CONTENT);
			schema.create(ClassifyConnectorFolderInTaxonomyActionParameters.DOCUMENT_MAPPING).setDefaultRequirement(false)
					.setType(
							MetadataValueType.CONTENT);
			schema.create(DEFAULT_ADMIN_UNIT).setDefaultRequirement(false)
					.defineReferencesTo(administrativeUnitSchemaType);
			schema.create(ClassifyConnectorFolderInTaxonomyActionParameters.DEFAULT_CATEGORY).setDefaultRequirement(false)
					.defineReferencesTo(categorySchemaType);
			schema.create(ClassifyConnectorFolderInTaxonomyActionParameters.DEFAULT_RETENTION_RULE).setDefaultRequirement(false)
					.defineReferencesTo(retentionRuleSchemaType);
			schema.create(ClassifyConnectorFolderInTaxonomyActionParameters.DEFAULT_OPEN_DATE).setDefaultRequirement(false)
					.setType(
							MetadataValueType.DATE);
			schema.create(ClassifyConnectorFolderInTaxonomyActionParameters.DEFAULT_COPY_STATUS).setDefaultRequirement(false)
					.defineAsEnum(CopyType.class);
			schema.create(ClassifyConnectorFolderInTaxonomyActionParameters.PATH_PREFIX).setDefaultRequirement(true).setType(
					MetadataValueType.STRING).setDefaultValue("smb://");
		}

	}

	private void configureClassifyInTaxonomyParametersForm(String collection,
			MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory) {

		Language language = migrationResourcesProvider.getLanguage();
		Map<String, Map<Language, String>> groups = new HashMap<>();

		String taxonomyTab = migrationResourcesProvider.get("tab.taxonomy");
		Map<Language, String> labelsTaxo = new HashMap<>();
		labelsTaxo.put(language, taxonomyTab);
		groups.put("tab.taxonomy", labelsTaxo);

		String optionsTab = migrationResourcesProvider.get("tab.options");
		Map<Language, String> labelsOptions = new HashMap<>();
		labelsOptions.put(language, optionsTab);
		groups.put("tab.options", labelsOptions);

		String defaultValuesTab = migrationResourcesProvider.get("tab.defaultValues");
		Map<Language, String> labelsDefaultValues = new HashMap<>();
		labelsDefaultValues.put(language, defaultValuesTab);
		groups.put("tab.defaultValues", labelsDefaultValues);

		String mappingsTab = migrationResourcesProvider.get("tab.mappings");
		Map<Language, String> labelsMappings = new HashMap<>();
		labelsMappings.put(language, mappingsTab);
		groups.put("tab.mappings", labelsMappings);

		String parametersSchema = ClassifyConnectorFolderInTaxonomyActionParameters.SCHEMA;

		SchemasDisplayManager schemasDisplayManager = appLayerFactory.getMetadataSchemasDisplayManager();
		SchemaTypesDisplayTransactionBuilder transaction = schemasDisplayManager.newTransactionBuilderFor(collection);

		transaction.add(schemasDisplayManager.getType(collection, ActionParameters.SCHEMA_TYPE)
				.withNewMetadataGroup(groups));

		transaction.add(schemasDisplayManager.getSchema(collection, parametersSchema).withFormMetadataCodes(asList(
				parametersSchema + "_" + IN_TAXONOMY,
				parametersSchema + "_" + PATH_PREFIX,
				parametersSchema + "_" + DELIMITER,

				parametersSchema + "_" + DEFAULT_ADMIN_UNIT,
				parametersSchema + "_" + DEFAULT_CATEGORY,
				parametersSchema + "_" + DEFAULT_RETENTION_RULE,
				parametersSchema + "_" + DEFAULT_COPY_STATUS,
				parametersSchema + "_" + DEFAULT_OPEN_DATE,

				parametersSchema + "_" + DOCUMENT_MAPPING,
				parametersSchema + "_" + FOLDER_MAPPING,

				parametersSchema + "_" + ACTION_AFTER_CLASSIFICATION

		)));

		transaction.add(schemasDisplayManager.getMetadata(collection, parametersSchema, IN_TAXONOMY)
				.withMetadataGroup(taxonomyTab));
		transaction.add(schemasDisplayManager.getMetadata(collection, parametersSchema, DELIMITER)
				.withMetadataGroup(taxonomyTab));
		transaction.add(schemasDisplayManager.getMetadata(collection, parametersSchema, PATH_PREFIX)
				.withMetadataGroup(taxonomyTab));

		transaction.add(schemasDisplayManager.getMetadata(collection, parametersSchema, DEFAULT_ADMIN_UNIT)
				.withMetadataGroup(defaultValuesTab));
		transaction.add(schemasDisplayManager.getMetadata(collection, parametersSchema, DEFAULT_CATEGORY)
				.withMetadataGroup(defaultValuesTab));
		transaction.add(schemasDisplayManager.getMetadata(collection, parametersSchema, DEFAULT_COPY_STATUS)
				.withMetadataGroup(defaultValuesTab));
		transaction.add(schemasDisplayManager.getMetadata(collection, parametersSchema, DEFAULT_OPEN_DATE)
				.withMetadataGroup(defaultValuesTab));
		transaction.add(schemasDisplayManager.getMetadata(collection, parametersSchema, DEFAULT_RETENTION_RULE)
				.withMetadataGroup(defaultValuesTab));

		transaction.add(schemasDisplayManager.getMetadata(collection, parametersSchema, DOCUMENT_MAPPING)
				.withMetadataGroup(mappingsTab));
		transaction.add(schemasDisplayManager.getMetadata(collection, parametersSchema, FOLDER_MAPPING)
				.withMetadataGroup(mappingsTab));

		transaction.add(schemasDisplayManager.getMetadata(collection, parametersSchema, ACTION_AFTER_CLASSIFICATION)
				.withMetadataGroup(optionsTab));

		schemasDisplayManager.execute(transaction.build());
	}
}
