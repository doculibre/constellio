package com.constellio.app.modules.rm.migrations;

import static com.constellio.app.ui.i18n.i18n.$;
import static java.util.Arrays.asList;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.constellio.app.entities.modules.ComboMigrationScript;
import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.entities.schemasDisplay.SchemaDisplayConfig;
import com.constellio.app.modules.rm.RMEmailTemplateConstants;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Email;
import com.constellio.app.modules.rm.wrappers.type.DocumentType;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaTypesDisplayTransactionBuilder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.data.dao.managers.config.ConfigManagerException.OptimisticLockingConfiguration;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.UserDocument;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.emails.EmailTemplatesManager;
import com.constellio.model.services.factories.ModelLayerFactory;
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
		ModelLayerFactory modelLayerFactory = appLayerFactory.getModelLayerFactory();
		generatedComboMigration = new GeneratedRMMigrationCombo(collection, appLayerFactory,
				migrationResourcesProvider);

		new SchemaAlteration(collection, migrationResourcesProvider, appLayerFactory).migrate();
		generatedComboMigration.applyGeneratedRoles();
		generatedComboMigration.applySchemasDisplay(appLayerFactory.getMetadataSchemasDisplayManager());
		applySchemasDisplay2(collection, appLayerFactory.getMetadataSchemasDisplayManager());

		RecordServices recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
		MetadataSchemaTypes types = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection);

		RMMigrationTo5_0_1.setupClassificationPlanTaxonomies(collection, modelLayerFactory, migrationResourcesProvider);
		RMMigrationTo5_0_1.setupStorageSpaceTaxonomy(collection, modelLayerFactory, migrationResourcesProvider);
		RMMigrationTo5_0_1.setupAdminUnitTaxonomy(collection, modelLayerFactory, migrationResourcesProvider);
		addEmailTemplates(appLayerFactory, migrationResourcesProvider, collection);

		recordServices.execute(createRecordTransaction(collection, migrationResourcesProvider, appLayerFactory, types));
	}

	private void applySchemasDisplay2(String collection, SchemasDisplayManager manager) {
		SchemaTypesDisplayTransactionBuilder transaction = manager.newTransactionBuilderFor(collection);
		//transaction.add(manager.getSchema(collection, "cart_default").withRemovedFormMetadatas("cart_default_title"));

		SchemaDisplayConfig userTask = manager.getSchema(collection, "userTask_default");
		userTask = userTask.withNewDisplayMetadataBefore("userTask_default_administrativeUnit", "userTask_default_comments");
		userTask = userTask.withNewDisplayMetadataBefore("userTask_default_linkedDocuments", "userTask_default_comments");
		userTask = userTask.withNewDisplayMetadataBefore("userTask_default_linkedFolders", "userTask_default_comments");
		userTask = userTask.withNewFormMetadata("userTask_default_linkedDocuments");
		userTask = userTask.withNewFormMetadata("userTask_default_linkedFolders");
		userTask = userTask.withTableMetadataCodes(
				asList("userTask_default_title", "userTask_default_status", "userTask_default_dueDate",
						"userTask_default_assignee"));
		transaction.add(userTask);
		manager.execute(transaction.build());
	}

	private Transaction createRecordTransaction(String collection, MigrationResourcesProvider migrationResourcesProvider,
			AppLayerFactory appLayerFactory, MetadataSchemaTypes types) {
		Transaction transaction = new Transaction();

		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(types.getCollection(), appLayerFactory.getModelLayerFactory());

		transaction.add(rm.newMediumType().setCode(migrationResourcesProvider.getDefaultLanguageString("MediumType.paperCode"))
				.setTitle(migrationResourcesProvider.getDefaultLanguageString("MediumType.paperTitle"))
				.setAnalogical(true));

		transaction.add(rm.newMediumType().setCode(migrationResourcesProvider.getDefaultLanguageString("MediumType.filmCode"))
				.setTitle(migrationResourcesProvider.getDefaultLanguageString("MediumType.filmTitle"))
				.setAnalogical(true));

		transaction.add(rm.newMediumType().setCode(migrationResourcesProvider.getDefaultLanguageString("MediumType.driveCode"))
				.setTitle(migrationResourcesProvider.getDefaultLanguageString("MediumType.driveTitle"))
				.setAnalogical(false));

		transaction.add(rm.newDocumentType().setCode(DocumentType.EMAIL_DOCUMENT_TYPE)
				.setTitle($("DocumentType.emailDocumentType")).setLinkedSchema(Email.SCHEMA));

		transaction.add(rm.newVariableRetentionPeriod().setCode("888")
				.setTitle(migrationResourcesProvider.getDefaultLanguageString("init.variablePeriod888")));

		transaction.add(rm.newVariableRetentionPeriod().setCode("999")
				.setTitle(migrationResourcesProvider.getDefaultLanguageString("init.variablePeriod999")));

		transaction.add(rm.newFacetField().setOrder(2).setFieldDataStoreCode("administrativeUnitId_s")
				.setTitle(migrationResourcesProvider.get("init.facet.administrativeUnit")));
		transaction.add(rm.newFacetField().setOrder(2).setFieldDataStoreCode("categoryId_s")
				.setTitle(migrationResourcesProvider.get("init.facet.category")));
		transaction.add(rm.newFacetField().setOrder(2).setFieldDataStoreCode("archivisticStatus_s")
				.setTitle(migrationResourcesProvider.get("init.facet.archivisticStatus")));
		transaction.add(rm.newFacetField().setOrder(3).setFieldDataStoreCode("copyStatus_s")
				.setTitle(migrationResourcesProvider.get("init.facet.copyStatus")));

		transaction.add(rm.newFacetField().setTitle(migrationResourcesProvider.getDefaultLanguageString("facets.folderType"))
				.setFieldDataStoreCode(rm.folderFolderType().getDataStoreCode()).setActive(false));
		transaction.add(rm.newFacetField().setTitle(migrationResourcesProvider.getDefaultLanguageString("facets.documentType"))
				.setFieldDataStoreCode(rm.documentDocumentType().getDataStoreCode()).setActive(false));

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

	private void addEmailTemplates(AppLayerFactory appLayerFactory, MigrationResourcesProvider migrationResourcesProvider,
			String collection) {
		addEmailTemplates(appLayerFactory, migrationResourcesProvider, collection, "remindReturnBorrowedFolderTemplate.html",
				RMEmailTemplateConstants.REMIND_BORROW_TEMPLATE_ID);
		addEmailTemplates(appLayerFactory, migrationResourcesProvider, collection, "approvalRequestForDecomListTemplate.html",
				RMEmailTemplateConstants.APPROVAL_REQUEST_TEMPLATE_ID);
		addEmailTemplates(appLayerFactory, migrationResourcesProvider, collection, "validationRequestForDecomListTemplate.html",
				RMEmailTemplateConstants.VALIDATION_REQUEST_TEMPLATE_ID);
		addEmailTemplates(appLayerFactory, migrationResourcesProvider, collection, "alertAvailableTemplate.html",
				RMEmailTemplateConstants.ALERT_AVAILABLE_ID);
	}

	private void addEmailTemplates(AppLayerFactory appLayerFactory, MigrationResourcesProvider migrationResourcesProvider,
			String collection,
			String templateFileName, String templateId) {
		InputStream templateInputStream = migrationResourcesProvider.getStream(templateFileName);
		EmailTemplatesManager emailTemplateManager = appLayerFactory.getModelLayerFactory()
				.getEmailTemplatesManager();
		try {
			emailTemplateManager.addCollectionTemplateIfInexistent(templateId, collection, templateInputStream);
		} catch (IOException | OptimisticLockingConfiguration e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(templateInputStream);
		}
	}
}
