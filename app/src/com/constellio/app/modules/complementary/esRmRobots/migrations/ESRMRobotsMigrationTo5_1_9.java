package com.constellio.app.modules.complementary.esRmRobots.migrations;

import static com.constellio.app.modules.complementary.esRmRobots.model.ClassifyConnectorDocumentInFolderActionParameters.ACTION_AFTER_CLASSIFICATION;
import static com.constellio.app.modules.complementary.esRmRobots.model.ClassifyConnectorDocumentInFolderActionParameters.IN_FOLDER;
import static com.constellio.app.modules.complementary.esRmRobots.model.ClassifyConnectorDocumentInFolderActionParameters.MAJOR_VERSIONS;
import static java.util.Arrays.asList;

import java.util.HashMap;
import java.util.Map;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.complementary.esRmRobots.model.ClassifyConnectorDocumentInFolderActionParameters;
import com.constellio.app.modules.complementary.esRmRobots.model.enums.ActionAfterClassification;
import com.constellio.app.modules.robots.model.wrappers.ActionParameters;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaTypesDisplayTransactionBuilder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.Language;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class ESRMRobotsMigrationTo5_1_9 implements MigrationScript {

	@Override
	public String getVersion() {
		return "5.1.9";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory)
			throws Exception {

		new SchemaAlterationFor5_1_9(collection, migrationResourcesProvider, appLayerFactory).migrate();

		updateClassifyDocumentInFolderParametersForm(collection, migrationResourcesProvider, appLayerFactory);

	}

	class SchemaAlterationFor5_1_9 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationFor5_1_9(String collection, MigrationResourcesProvider migrationResourcesProvider,
				AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		public String getVersion() {
			return "5.1.9";
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			setupClassifyConnectorDocumentInFolderActionParametersSchema();

			// FIXME Benoit Add form display for new options in classification action
		}

		private void setupClassifyConnectorDocumentInFolderActionParametersSchema() {
			MetadataSchemaBuilder schema = typesBuilder.getSchemaType(ActionParameters.SCHEMA_TYPE)
					.getCustomSchema(ClassifyConnectorDocumentInFolderActionParameters.SCHEMA_LOCAL_CODE);
			//
			// schema.createUndeletable(ClassifyConnectorDocumentInFolderActionParameters.VERSIONS)
			// .setType(MetadataValueType.STRING);

			schema.createUndeletable(ClassifyConnectorDocumentInFolderActionParameters.ACTION_AFTER_CLASSIFICATION)
					.setDefaultRequirement(true)
					.defineAsEnum(ActionAfterClassification.class)
					.setDefaultValue(ActionAfterClassification.DO_NOTHING);
		}
	}

	private void updateClassifyDocumentInFolderParametersForm(String collection,
			MigrationResourcesProvider migrationResourcesProvider,
			AppLayerFactory appLayerFactory) {

		Language language = migrationResourcesProvider.getLanguage();
		Map<String, Map<Language, String>> groups = new HashMap<>();

		String defaultValuesTab = migrationResourcesProvider.get("tab.defaultValues");
		Map<Language, String> labelsDefaultValues = new HashMap<>();
		labelsDefaultValues.put(language, defaultValuesTab);
		groups.put("tab.defaultValues", labelsDefaultValues);

		String optionsTab = migrationResourcesProvider.get("tab.options");
		Map<Language, String> labelsOptionsTab = new HashMap<>();
		labelsDefaultValues.put(language, optionsTab);
		groups.put("tab.options", labelsOptionsTab);

		String parametersSchema = ClassifyConnectorDocumentInFolderActionParameters.SCHEMA;

		SchemasDisplayManager schemasDisplayManager = appLayerFactory.getMetadataSchemasDisplayManager();
		SchemaTypesDisplayTransactionBuilder transaction = schemasDisplayManager.newTransactionBuilderFor(collection);

		transaction.add(schemasDisplayManager.getType(collection, ActionParameters.SCHEMA_TYPE)
				.withNewMetadataGroup(groups));

		transaction.add(schemasDisplayManager.getSchema(collection, parametersSchema).withFormMetadataCodes(asList(
				parametersSchema + "_" + IN_FOLDER,
				parametersSchema + "_" + MAJOR_VERSIONS,
				parametersSchema + "_" + ACTION_AFTER_CLASSIFICATION
		)));

		transaction.add(schemasDisplayManager.getMetadata(collection, parametersSchema, IN_FOLDER)
				.withMetadataGroup(defaultValuesTab));

		transaction.add(schemasDisplayManager.getMetadata(collection, parametersSchema, MAJOR_VERSIONS)
				.withMetadataGroup(defaultValuesTab));

		transaction.add(schemasDisplayManager.getMetadata(collection, parametersSchema, ACTION_AFTER_CLASSIFICATION)
				.withMetadataGroup(optionsTab));

		schemasDisplayManager.execute(transaction.build());
	}
}
