package com.constellio.app.modules.complementary.esRmRobots.migrations;

import static com.constellio.app.modules.complementary.esRmRobots.model.ClassifyConnectorDocumentInFolderActionParameters.ACTION_AFTER_CLASSIFICATION;
import static com.constellio.app.modules.complementary.esRmRobots.model.ClassifyConnectorDocumentInFolderActionParameters.DOCUMENT_TYPE;
import static com.constellio.app.modules.complementary.esRmRobots.model.ClassifyConnectorDocumentInFolderActionParameters.IN_FOLDER;
import static com.constellio.app.modules.complementary.esRmRobots.model.ClassifyConnectorDocumentInFolderActionParameters.MAJOR_VERSIONS;
import static java.util.Arrays.asList;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.complementary.esRmRobots.model.ClassifyConnectorDocumentInFolderActionParameters;
import com.constellio.app.modules.rm.wrappers.type.DocumentType;
import com.constellio.app.modules.robots.model.wrappers.ActionParameters;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaTypesDisplayTransactionBuilder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class ESRMRobotsMigrationTo6_0 implements MigrationScript {

	@Override
	public String getVersion() {
		return "6.0";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory)
			throws Exception {

		new SchemaAlterationFor6_0(collection, migrationResourcesProvider, appLayerFactory).migrate();

		updateClassifyDocumentInFolderParametersForm(collection, migrationResourcesProvider, appLayerFactory);

	}

	class SchemaAlterationFor6_0 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationFor6_0(String collection, MigrationResourcesProvider migrationResourcesProvider,
				AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		public String getVersion() {
			return "6.0";
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			setupClassifyConnectorDocumentInFolderActionParametersSchema();

		}

		private void setupClassifyConnectorDocumentInFolderActionParametersSchema() {
			MetadataSchemaBuilder schema = typesBuilder.getSchemaType(ActionParameters.SCHEMA_TYPE)
					.getCustomSchema(ClassifyConnectorDocumentInFolderActionParameters.SCHEMA_LOCAL_CODE);

			MetadataSchemaTypeBuilder documentTypeSchemaType = typesBuilder.getSchemaType(DocumentType.SCHEMA_TYPE);

			schema.createUndeletable(ClassifyConnectorDocumentInFolderActionParameters.DOCUMENT_TYPE)
					.defineReferencesTo(documentTypeSchemaType);
		}
	}

	private void updateClassifyDocumentInFolderParametersForm(String collection,
			MigrationResourcesProvider migrationResourcesProvider,
			AppLayerFactory appLayerFactory) {

		String defaultValuesTab = migrationResourcesProvider.get("tab.defaultValues");

		String parametersSchema = ClassifyConnectorDocumentInFolderActionParameters.SCHEMA;

		SchemasDisplayManager schemasDisplayManager = appLayerFactory.getMetadataSchemasDisplayManager();
		SchemaTypesDisplayTransactionBuilder transaction = schemasDisplayManager.newTransactionBuilderFor(collection);

		transaction.add(schemasDisplayManager.getSchema(collection, parametersSchema).withFormMetadataCodes(asList(
				parametersSchema + "_" + IN_FOLDER,
				parametersSchema + "_" + DOCUMENT_TYPE,
				parametersSchema + "_" + MAJOR_VERSIONS,
				parametersSchema + "_" + ACTION_AFTER_CLASSIFICATION
		)));

		transaction.add(schemasDisplayManager.getMetadata(collection, parametersSchema, IN_FOLDER)
				.withMetadataGroup(defaultValuesTab));

		transaction.add(schemasDisplayManager.getMetadata(collection, parametersSchema, MAJOR_VERSIONS)
				.withMetadataGroup(defaultValuesTab));

		transaction.add(schemasDisplayManager.getMetadata(collection, parametersSchema, DOCUMENT_TYPE)
				.withMetadataGroup(defaultValuesTab));

		schemasDisplayManager.execute(transaction.build());
	}
}
