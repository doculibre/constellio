package com.constellio.app.modules.rm.migrations;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;

import com.constellio.app.entities.modules.ComboMigrationScript;
import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.entities.schemasDisplay.SchemaDisplayConfig;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Email;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaTypesDisplayTransactionBuilder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.UserDocument;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class RMMigrationCombo implements ComboMigrationScript {
	@Override
	public List<MigrationScript> getVersions() {
		List<MigrationScript> scripts = new ArrayList<>();
		scripts.add(new RMMigrationTo5_0_1());
		scripts.add(new RMMigrationTo5_0_2());
		scripts.add(new RMMigrationTo5_0_3());
		scripts.add(new RMMigrationTo5_0_4());
		scripts.add(new RMMigrationTo5_0_4_1());
		scripts.add(new RMMigrationTo5_0_5());
		scripts.add(new RMMigrationTo5_0_6());
		scripts.add(new RMMigrationTo5_0_7());
		scripts.add(new RMMigrationTo5_1_0_3());
		scripts.add(new RMMigrationTo5_1_0_4());
		scripts.add(new RMMigrationTo5_1_0_6());
		scripts.add(new RMMigrationTo5_1_2());
		scripts.add(new RMMigrationTo5_1_2_2());
		scripts.add(new RMMigrationTo5_1_3());
		scripts.add(new RMMigrationTo5_1_3());
		scripts.add(new RMMigrationTo5_1_4_1());
		scripts.add(new RMMigrationTo5_1_5());
		scripts.add(new RMMigrationTo5_1_7());
		scripts.add(new RMMigrationTo5_1_9());
		scripts.add(new RMMigrationTo6_1());
		scripts.add(new RMMigrationTo6_1_4());
		scripts.add(new RMMigrationTo6_2());
		scripts.add(new RMMigrationTo6_2_0_7());
		scripts.add(new RMMigrationTo6_3());
		scripts.add(new RMMigrationTo6_4());
		return scripts;
	}

	@Override
	public String getVersion() {
		return getVersions().get(getVersions().size() - 1).getVersion();
	}

	GeneratedRMMigrationCombo generatedComboMigration;

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory)
			throws Exception {
		generatedComboMigration = new GeneratedRMMigrationCombo(collection, appLayerFactory,
				migrationResourcesProvider);

		new SchemaAlteration(collection, migrationResourcesProvider, appLayerFactory).migrate();
		generatedComboMigration.applyGeneratedRoles();
		generatedComboMigration.applySchemasDisplay(appLayerFactory.getMetadataSchemasDisplayManager());
		applySchemasDisplay2(collection, appLayerFactory.getMetadataSchemasDisplayManager());

		RecordServices recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
		MetadataSchemaTypes types = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection);

		recordServices.execute(createRecordTransaction(collection, migrationResourcesProvider, appLayerFactory, types));
	}

	private void applySchemasDisplay2(String collection, SchemasDisplayManager manager) {
		SchemaTypesDisplayTransactionBuilder transaction = manager.newTransactionBuilderFor(collection);
		//transaction.add(manager.getSchema(collection, "cart_default").withRemovedFormMetadatas("cart_default_title"));

		SchemaDisplayConfig userTask = manager.getSchema(collection, "userTask_default");
		userTask = userTask.withNewDisplayMetadataBefore("userTask_default_administrativeUnit", "userTask_default_assignedOn");
		userTask = userTask.withNewDisplayMetadataBefore("userTask_default_linkedDocuments", "userTask_default_modifiedOn");
		userTask = userTask.withNewDisplayMetadataBefore("userTask_default_linkedFolders", "userTask_default_modifiedOn");
		userTask = userTask.withNewFormMetadataBefore("userTask_default_linkedDocuments", "userTask_default_parentTask");
		userTask = userTask.withNewFormMetadataBefore("userTask_default_linkedFolders", "userTask_default_parentTask");
		userTask = userTask.withTableMetadataCodes(
				asList("userTask_default_assignee", "userTask_default_dueDate", "userTask_default_status",
						"userTask_default_title"));
		transaction.add(userTask);
		manager.execute(transaction.build());
	}

	private Transaction createRecordTransaction(String collection, MigrationResourcesProvider migrationResourcesProvider,
			AppLayerFactory appLayerFactory, MetadataSchemaTypes types) {
		Transaction transaction = new Transaction();

		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(types.getCollection(), appLayerFactory.getModelLayerFactory());

		return transaction;
	}

	class SchemaAlteration extends MetadataSchemasAlterationHelper {

		protected SchemaAlteration(String collection,
				MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {

			//This module is fixing problems in other module
			for (MetadataSchemaTypeBuilder typeBuilder : typesBuilder.getTypes()) {
				for (MetadataBuilder metadata : typeBuilder.getAllMetadatas()) {
					if (metadata.getLocalCode().equals("comments")) {
						metadata.setTypeWithoutValidation(MetadataValueType.STRUCTURE);
					}
				}
			}

			for (MetadataSchemaTypeBuilder typeBuilder : typesBuilder.getTypes()) {
				MetadataSchemaBuilder schemaBuilder = typeBuilder.getDefaultSchema();
				if (schemaBuilder.hasMetadata("description")) {
					schemaBuilder.get("description").setEnabled(true).setEssentialInSummary(true);
				}
			}

			for (MetadataSchemaTypeBuilder typeBuilder : typesBuilder.getTypes()) {
				for (MetadataBuilder metadataBuilder : typeBuilder.getDefaultSchema().getMetadatas()) {
					if ("code".equals(metadataBuilder.getLocalCode())) {
						metadataBuilder.setUniqueValue(true);
						metadataBuilder.setDefaultRequirement(true);
					}
				}
			}

			generatedComboMigration.applyGeneratedSchemaAlteration(typesBuilder);

			typesBuilder.getDefaultSchema(UserDocument.SCHEMA_TYPE).getMetadata(Schemas.TITLE_CODE).getPopulateConfigsBuilder()
					.addProperty("subject").addProperty("title");

			typesBuilder.getSchema(Email.SCHEMA).getMetadata(Schemas.TITLE_CODE).getPopulateConfigsBuilder()
					.addProperty("subject");
		}

	}
}
