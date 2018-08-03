package com.constellio.app.modules.complementary.esRmRobots.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.complementary.esRmRobots.model.ClassifyConnectorFolderDirectlyInThePlanActionParameters;
import com.constellio.app.modules.complementary.esRmRobots.model.ClassifyConnectorFolderInParentFolderActionParameters;
import com.constellio.app.modules.complementary.esRmRobots.model.ClassifyConnectorFolderInTaxonomyActionParameters;
import com.constellio.app.modules.rm.wrappers.type.DocumentType;
import com.constellio.app.modules.rm.wrappers.type.FolderType;
import com.constellio.app.modules.robots.model.wrappers.ActionParameters;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaTypesDisplayTransactionBuilder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

import static com.constellio.app.modules.complementary.esRmRobots.model.ClassifyConnectorFolderInTaxonomyActionParameters.DOCUMENT_TYPE;
import static com.constellio.app.modules.complementary.esRmRobots.model.ClassifyConnectorFolderInTaxonomyActionParameters.FOLDER_TYPE;

public class ESRMRobotsMigrationTo7_5 implements MigrationScript {

	@Override
	public String getVersion() {
		return "7.5";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {

		new SchemaAlterationFor7_5(collection, migrationResourcesProvider, appLayerFactory).migrate();
		configNewMetadatasDisplayConfiguration(collection, migrationResourcesProvider, appLayerFactory);

	}

	private void configNewMetadatasDisplayConfiguration(String collection,
														MigrationResourcesProvider migrationResourcesProvider,
														AppLayerFactory appLayerFactory) {
		String defaultValuesTab = "tab.defaultValues";
		SchemasDisplayManager schemasDisplayManager = appLayerFactory.getMetadataSchemasDisplayManager();
		SchemaTypesDisplayTransactionBuilder transaction = schemasDisplayManager.newTransactionBuilderFor(collection);

		String parametersSchema = ClassifyConnectorFolderDirectlyInThePlanActionParameters.SCHEMA;
		transaction.add(schemasDisplayManager.getSchema(collection, parametersSchema)
				.withNewFormAndDisplayMetadatas(parametersSchema + "_" + FOLDER_TYPE, parametersSchema + "_" + DOCUMENT_TYPE));
		transaction.add(schemasDisplayManager.getMetadata(collection, parametersSchema, FOLDER_TYPE)
				.withMetadataGroup(defaultValuesTab));
		transaction.add(schemasDisplayManager.getMetadata(collection, parametersSchema, DOCUMENT_TYPE)
				.withMetadataGroup(defaultValuesTab));

		parametersSchema = ClassifyConnectorFolderInParentFolderActionParameters.SCHEMA;
		transaction.add(schemasDisplayManager.getSchema(collection, parametersSchema)
				.withNewFormAndDisplayMetadatas(parametersSchema + "_" + FOLDER_TYPE, parametersSchema + "_" + DOCUMENT_TYPE));
		transaction.add(schemasDisplayManager.getMetadata(collection, parametersSchema, FOLDER_TYPE)
				.withMetadataGroup(defaultValuesTab));
		transaction.add(schemasDisplayManager.getMetadata(collection, parametersSchema, DOCUMENT_TYPE)
				.withMetadataGroup(defaultValuesTab));

		parametersSchema = ClassifyConnectorFolderInTaxonomyActionParameters.SCHEMA;
		transaction.add(schemasDisplayManager.getSchema(collection, parametersSchema)
				.withNewFormAndDisplayMetadatas(parametersSchema + "_" + FOLDER_TYPE, parametersSchema + "_" + DOCUMENT_TYPE));
		transaction.add(schemasDisplayManager.getMetadata(collection, parametersSchema, FOLDER_TYPE)
				.withMetadataGroup(defaultValuesTab));
		transaction.add(schemasDisplayManager.getMetadata(collection, parametersSchema, DOCUMENT_TYPE)
				.withMetadataGroup(defaultValuesTab));

		schemasDisplayManager.execute(transaction.build());
	}

	class SchemaAlterationFor7_5 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationFor7_5(String collection, MigrationResourcesProvider migrationResourcesProvider,
										 AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		public String getVersion() {
			return "7.5";
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaTypeBuilder actionParameters = typesBuilder.getSchemaType(ActionParameters.SCHEMA_TYPE);

			actionParameters.getSchema(ClassifyConnectorFolderDirectlyInThePlanActionParameters.SCHEMA_LOCAL_CODE)
					.createUndeletable(ClassifyConnectorFolderDirectlyInThePlanActionParameters.FOLDER_TYPE).setType(MetadataValueType.REFERENCE)
					.defineReferencesTo(typesBuilder.getSchemaType(FolderType.SCHEMA_TYPE));
			actionParameters.getSchema(ClassifyConnectorFolderDirectlyInThePlanActionParameters.SCHEMA_LOCAL_CODE)
					.createUndeletable(ClassifyConnectorFolderDirectlyInThePlanActionParameters.DOCUMENT_TYPE).setType(MetadataValueType.REFERENCE)
					.defineReferencesTo(typesBuilder.getSchemaType(DocumentType.SCHEMA_TYPE));

			actionParameters.getSchema(ClassifyConnectorFolderInParentFolderActionParameters.SCHEMA_LOCAL_CODE)
					.createUndeletable(ClassifyConnectorFolderInParentFolderActionParameters.FOLDER_TYPE).setType(MetadataValueType.REFERENCE)
					.defineReferencesTo(typesBuilder.getSchemaType(FolderType.SCHEMA_TYPE));
			actionParameters.getSchema(ClassifyConnectorFolderInParentFolderActionParameters.SCHEMA_LOCAL_CODE)
					.createUndeletable(ClassifyConnectorFolderInParentFolderActionParameters.DOCUMENT_TYPE).setType(MetadataValueType.REFERENCE)
					.defineReferencesTo(typesBuilder.getSchemaType(DocumentType.SCHEMA_TYPE));

			actionParameters.getSchema(ClassifyConnectorFolderInTaxonomyActionParameters.SCHEMA_LOCAL_CODE)
					.createUndeletable(ClassifyConnectorFolderInTaxonomyActionParameters.FOLDER_TYPE).setType(MetadataValueType.REFERENCE)
					.defineReferencesTo(typesBuilder.getSchemaType(FolderType.SCHEMA_TYPE));
			actionParameters.getSchema(ClassifyConnectorFolderInTaxonomyActionParameters.SCHEMA_LOCAL_CODE)
					.createUndeletable(ClassifyConnectorFolderInTaxonomyActionParameters.DOCUMENT_TYPE).setType(MetadataValueType.REFERENCE)
					.defineReferencesTo(typesBuilder.getSchemaType(DocumentType.SCHEMA_TYPE));

		}

	}

}
