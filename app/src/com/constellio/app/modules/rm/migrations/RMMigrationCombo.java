package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.ComboMigrationScript;
import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.entities.schemasDisplay.SchemaDisplayConfig;
import com.constellio.app.modules.reports.wrapper.Printable;
import com.constellio.app.modules.rm.RMEmailTemplateConstants;
import com.constellio.app.modules.rm.constants.RMTaxonomies;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Email;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.PrintableLabel;
import com.constellio.app.modules.rm.wrappers.type.DocumentType;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaTypesDisplayTransactionBuilder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.data.dao.managers.config.ConfigManagerException.OptimisticLockingConfiguration;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Report;
import com.constellio.model.entities.records.wrappers.UserDocument;
import com.constellio.model.entities.schemas.*;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.emails.EmailTemplatesManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.users.UserServices;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.constellio.app.ui.i18n.i18n.$;
import static java.util.Arrays.asList;

public class RMMigrationCombo implements ComboMigrationScript {
	@Override
	public List<MigrationScript> getVersions() {
		return asList(
				new RMMigrationTo5_0_1(),
				new RMMigrationTo5_0_2(),
				new RMMigrationTo5_0_3(),
				new RMMigrationTo5_0_4(),
				new RMMigrationTo5_0_4_1(),
				new RMMigrationTo5_0_5(),
				new RMMigrationTo5_0_6(),
				new RMMigrationTo5_0_7(),
				new RMMigrationTo5_1_0_3(),
				new RMMigrationTo5_1_0_4(),
				new RMMigrationTo5_1_0_6(),
				new RMMigrationTo5_1_2(),
				new RMMigrationTo5_1_2_2(),
				new RMMigrationTo5_1_3(),
				new RMMigrationTo5_1_3(),
				new RMMigrationTo5_1_4_1(),
				new RMMigrationTo5_1_5(),
				new RMMigrationTo5_1_7(),
				new RMMigrationTo5_1_9(),
				new RMMigrationTo6_1(),
				new RMMigrationTo6_1_4(),
				new RMMigrationTo6_2(),
				new RMMigrationTo6_2_0_7(),
				new RMMigrationTo6_3(),
				new RMMigrationTo6_4(),
				new RMMigrationTo6_5(),
				new RMMigrationTo6_5_1(),
				new RMMigrationTo6_5_7(),
				new RMMigrationTo6_5_20(),
				new RMMigrationTo6_5_21(),
				new RMMigrationTo6_5_33(),
				new RMMigrationTo6_5_34(),
				new RMMigrationTo6_5_36(),
				new RMMigrationTo6_5_37(),
				new RMMigrationTo6_5_50(),
				new RMMigrationTo6_5_54(),
				new RMMigrationTo6_6(),
				new RMMigrationTo6_7(),
				new RMMigrationTo7_0_5(),
				new RMMigrationTo7_1(),
				new RMMigrationTo_7_1_francis()
		);
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
		createDefaultLabel(collection, appLayerFactory, migrationResourcesProvider);

		SystemConfigurationsManager configManager = modelLayerFactory.getSystemConfigurationsManager();
		String defaultTaxonomy = modelLayerFactory.getSystemConfigs().getDefaultTaxonomy();
		if (defaultTaxonomy == null) {
			configManager.setValue(ConstellioEIMConfigs.DEFAULT_TAXONOMY, RMTaxonomies.ADMINISTRATIVE_UNITS);
		}
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

		SchemaDisplayConfig userDocument = manager.getSchema(collection, "userDocument_default");
		transaction.add(userDocument.withRemovedDisplayMetadatas("userDocument_default_folder")
				.withRemovedFormMetadatas("userDocument_default_folder"));
		//userDocument.withNew

		SchemaDisplayConfig container = manager.getSchema(collection, ContainerRecord.DEFAULT_SCHEMA);
		container = container.withNewFormMetadata(ContainerRecord.DEFAULT_SCHEMA + "_" + ContainerRecord.FILL_RATIO_ENTRED);
		transaction.add(container);

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
				.setFieldDataStoreCode(rm.folder.folderType().getDataStoreCode()).setActive(false));
		transaction.add(rm.newFacetField().setTitle(migrationResourcesProvider.getDefaultLanguageString("facets.documentType"))
				.setFieldDataStoreCode(rm.documentDocumentType().getDataStoreCode()).setActive(false));



		return transaction;
	}

	public void createDefaultLabel(String collection, AppLayerFactory factory, MigrationResourcesProvider provider)
			throws Exception {
		Map<String, Integer> map = new HashMap<>();
		map.put("5159", 7);
		map.put("5161", 10);
		map.put("5162", 7);
		map.put("5163", 5);
		ModelLayerFactory model = factory.getModelLayerFactory();
		RecordServices rs = model.newRecordServices();
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, factory);
		MetadataSchemaType metaBuilder = factory.getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection)
				.getSchemaType(Printable.SCHEMA_TYPE);
		MetadataSchema typeBuilder = metaBuilder.getSchema(PrintableLabel.SCHEMA_LABEL);
		ContentManager contentManager = model.getContentManager();
		UserServices userServices = model.newUserServices();
		Transaction trans = new Transaction();
		File f = provider.getFile("defaultJasperFiles");
		List<File> files = getFolders(f, provider);
		for (File fi : files) {
			Record record = rs.newRecordWithSchema(metaBuilder.getSchema(PrintableLabel.SCHEMA_LABEL));
			String type = fi.getName().matches("(.)+_(Container.jasper)") ? ContainerRecord.SCHEMA_TYPE : Folder.SCHEMA_TYPE;
			String titre = "Code de plan justifié ";
			Matcher m = Pattern.compile("(.)+_(\\d{4})_(.)+").matcher(fi.getName());
			m.find();
			String format = m.group(2);
			record.set(typeBuilder.getMetadata(PrintableLabel.TYPE_LABEL), type);
			record.set(typeBuilder.getMetadata(PrintableLabel.LIGNE), map.get(format));

			if (type.equals(Folder.SCHEMA_TYPE)) {
				titre += provider.getDefaultLanguageString("Migration.typeSchemaDossier") + " " + (fi.getName().contains("_D_") ?
						provider.getDefaultLanguageString("Migration.typeAveryDroite") : provider.getDefaultLanguageString("Migration.typeAveryGauche"));
			} else {
				titre += provider.getDefaultLanguageString("Migration.typeSchemaConteneur");
			}
			String etiquetteName = provider.getDefaultLanguageString("Migration.etiquetteName");
			String extension = provider.getDefaultLanguageString("Migration.fileExtension");
			titre += " (" + etiquetteName + " " + format + ")";
			record.set(typeBuilder.getMetadata(PrintableLabel.COLONNE), 2);
			record.set(typeBuilder.getMetadata(Printable.ISDELETABLE), false);
			ContentVersionDataSummary upload = contentManager.upload(new FileInputStream(fi), etiquetteName + " " + format + " " + type);
			record.set(typeBuilder.getMetadata(Report.TITLE), titre);
			record.set(typeBuilder.getMetadata(Printable.JASPERFILE), contentManager.createFileSystem(etiquetteName + "-" + format + "-" + type + extension, upload));
			trans.add(record);
		}
		rs.execute(trans);
	}

	public List<File> getFolders(File file, MigrationResourcesProvider provider) {
		List<File> temp = new ArrayList<>();
		ArrayList<File> files = new ArrayList<>(asList(file.listFiles()));
		String extension = provider.getDefaultLanguageString("Migration.fileExtension");
		for (File f : files) {
			if (f.isDirectory()) {
				temp.addAll(getFolders(f, provider));
			} else if (f.getName().endsWith(extension)) {
				temp.add(f);
			}
		}
		return temp;
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
		addEmailTemplates(appLayerFactory, migrationResourcesProvider, collection, "alertWhenDecommissioningListCreatedTemplate.html",
				RMEmailTemplateConstants.DECOMMISSIONING_LIST_CREATION_TEMPLATE_ID);
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
