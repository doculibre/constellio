package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.ComboMigrationScript;
import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.entities.schemasDisplay.SchemaDisplayConfig;
import com.constellio.app.modules.rm.RMEmailTemplateConstants;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.constants.RMRoles;
import com.constellio.app.modules.rm.constants.RMTaxonomies;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Email;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.Printable;
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
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentManager.UploadOptions;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.emails.EmailTemplatesManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.builders.CommonMetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.security.roles.RolesManager;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.model.services.users.UserServices;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.constellio.model.entities.Language.French;
import static java.util.Arrays.asList;

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
		scripts.add(new RMMigrationTo6_5());
		scripts.add(new RMMigrationTo6_5_1());
		scripts.add(new RMMigrationTo6_5_7());
		scripts.add(new RMMigrationTo6_5_20());
		scripts.add(new RMMigrationTo6_5_21());
		scripts.add(new RMMigrationTo6_5_33());
		scripts.add(new RMMigrationTo6_5_34());
		scripts.add(new RMMigrationTo6_5_36());
		scripts.add(new RMMigrationTo6_5_37());
		scripts.add(new RMMigrationTo6_5_50());
		scripts.add(new RMMigrationTo6_5_54());
		scripts.add(new RMMigrationTo6_6());
		scripts.add(new RMMigrationTo6_7());
		scripts.add(new RMMigrationTo7_0_5());
		scripts.add(new RMMigrationTo7_0_10_5());
		scripts.add(new RMMigrationTo7_1());
		scripts.add(new RMMigrationTo7_1_1());
		scripts.add(new RMMigrationTo7_1_2());
		scripts.add(new RMMigrationTo7_2());
		scripts.add(new RMMigrationTo7_2_0_1());
		scripts.add(new RMMigrationTo7_2_0_2());
		scripts.add(new RMMigrationTo7_2_0_3());
		scripts.add(new RMMigrationTo7_2_0_4());
		scripts.add(new RMMigrationTo7_3());
		scripts.add(new RMMigrationTo7_3_1());
		scripts.add(new RMMigrationTo7_4());
		scripts.add(new RMMigrationTo7_4_2());
		scripts.add(new RMMigrationTo7_4_48());
		scripts.add(new RMMigrationTo7_4_48_1());
		scripts.add(new RMMigrationTo7_4_49());
		scripts.add(new RMMigrationTo7_5());
		scripts.add(new RMMigrationTo7_5_2());
		scripts.add(new RMMigrationTo7_5_3());
		scripts.add(new RMMigrationTo7_5_5());
		scripts.add(new RMMigrationTo7_6());
		scripts.add(new RMMigrationTo7_6_2());
		scripts.add(new RMMigrationTo7_6_3());
		scripts.add(new RMMigrationTo7_6_6());
		scripts.add(new RMMigrationTo7_6_6_1());
		scripts.add(new RMMigrationTo7_6_6_2());
		scripts.add(new RMMigrationTo7_6_8());
		scripts.add(new RMMigrationTo7_6_9());
		scripts.add(new RMMigrationTo7_6_10());
		scripts.add(new RMMigrationTo7_6_11());
		scripts.add(new RMMigrationTo7_7());
		scripts.add(new RMMigrationTo7_7_0_42());
		scripts.add(new RMMigrationTo7_7_1());
		scripts.add(new RMMigrationTo7_7_2());
		scripts.add(new RMMigrationTo7_7_3());
		scripts.add(new RMMigrationTo7_7_4());
		scripts.add(new RMMigrationTo7_7_4_33());
		scripts.add(new RMMigrationTo7_7_5_4());
		scripts.add(new RMMigrationTo7_7_5_5());
		scripts.add(new RMMigrationTo8_0_1());
		scripts.add(new RMMigrationTo8_0_2());
		scripts.add(new RMMigrationTo8_0_3());
		scripts.add(new RMMigrationTo8_1());
		scripts.add(new RMMigrationTo8_1_0_1());
		scripts.add(new RMMigrationTo8_1_1());
		scripts.add(new RMMigrationTo8_1_1_1());
		scripts.add(new RMMigrationTo8_1_1_2());
		scripts.add(new RMMigrationTo8_1_1_6());
		scripts.add(new RMMigrationTo8_1_2());
		scripts.add(new RMMigrationTo8_1_4());
		scripts.add(new RMMigrationTo8_2());
		scripts.add(new RMMigrationTo8_2_42());
		scripts.add(new RMMigrationTo8_2_1_4());
		scripts.add(new RMMigrationTo8_2_1_5());
		scripts.add(new RMMigrationTo8_2_2_4());
		scripts.add(new RMMigrationTo8_2_2_5());
		scripts.add(new RMMigrationTo8_2_3());
		scripts.add(new RMMigrationTo8_3());
		scripts.add(new RMMigrationTo8_3_1());
		scripts.add(new RMMigrationTo8_3_1_1());
		scripts.add(new RMMigrationTo8_3_2());
		scripts.add(new RMMigrationTo8_3_2_1());
		scripts.add(new RMMigrationTo8_3_2_2());
		scripts.add(new RMMigrationTo9_0());
		scripts.add(new RMMigrationTo9_0_0_1());
		scripts.add(new RMMigrationTo8_2_1_5());
		scripts.add(new RMMigrationTo9_0_0_3());
		scripts.add(new RMMigrationTo9_0_0_4());
		scripts.add(new RMMigrationTo9_0_0_33());
		scripts.add(new RMMigrationTo9_0_0_42());

		return scripts;
	}

	@Override
	public String getVersion() {
		return getVersions().get(getVersions().size() - 1).getVersion();
	}

	GeneratedRMMigrationCombo generatedComboMigration;

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {
		ModelLayerFactory modelLayerFactory = appLayerFactory.getModelLayerFactory();
		generatedComboMigration = new GeneratedRMMigrationCombo(collection, appLayerFactory,
				migrationResourcesProvider);

		new SchemaAlteration(collection, migrationResourcesProvider, appLayerFactory).migrate();
		generatedComboMigration.applyGeneratedRoles();
		generatedComboMigration.applySchemasDisplay(appLayerFactory.getMetadataSchemasDisplayManager());
		applySchemasDisplay2(collection, appLayerFactory.getMetadataSchemasDisplayManager());

		RolesManager rolesManager = appLayerFactory.getModelLayerFactory().getRolesManager();
		rolesManager.updateRole(rolesManager.getRole(collection, "ADM").withTitle("Administrateur / Administrator"));

		RecordServices recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
		MetadataSchemaTypes types = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection);

		RMMigrationTo5_0_1.setupClassificationPlanTaxonomies(collection, modelLayerFactory, migrationResourcesProvider);
		RMMigrationTo5_0_1.setupStorageSpaceTaxonomy(collection, modelLayerFactory, migrationResourcesProvider);
		RMMigrationTo5_0_1.setupAdminUnitTaxonomy(collection, modelLayerFactory, migrationResourcesProvider);
		addEmailTemplates(appLayerFactory, migrationResourcesProvider, collection);

		recordServices.execute(createRecordTransaction(collection, migrationResourcesProvider, appLayerFactory, types));

		SystemConfigurationsManager configManager = modelLayerFactory.getSystemConfigurationsManager();
		String defaultTaxonomy = modelLayerFactory.getSystemConfigs().getDefaultTaxonomy();
		if (defaultTaxonomy == null) {
			configManager.setValue(ConstellioEIMConfigs.DEFAULT_TAXONOMY, RMTaxonomies.ADMINISTRATIVE_UNITS);
		}

		modelLayerFactory.getMetadataSchemasManager().modify(collection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getDefaultSchema(Folder.SCHEMA_TYPE).get(Folder.MAIN_COPY_RULE).addLabel(French, "Exemplaire");
			}
		});

		//TODO These lines are totally useless, they are only there to ensure that the taxonomies are written in the same order in the file, for combo test validation purpose
		TaxonomiesManager taxonomiesManager = modelLayerFactory.getTaxonomiesManager();
		taxonomiesManager.editTaxonomy(taxonomiesManager.getEnabledTaxonomyWithCode(collection, "plan"));
		taxonomiesManager.editTaxonomy(taxonomiesManager.getEnabledTaxonomyWithCode(collection, "admUnits"));

		RMMigrationTo7_2.reloadEmailTemplates(appLayerFactory, migrationResourcesProvider, collection);

		rolesManager.updateRole(rolesManager.getRole(collection, RMRoles.MANAGER)
				.withNewPermissions(asList(RMPermissionsTo.CREATE_DECOMMISSIONING_LIST)));

		rolesManager.updateRole(rolesManager.getRole(collection, RMRoles.RGD)
				.withNewPermissions(asList(RMPermissionsTo.CREATE_DECOMMISSIONING_LIST, RMPermissionsTo.PROCESS_DECOMMISSIONING_LIST)));

	}


	private void applySchemasDisplay2(String collection, SchemasDisplayManager manager) {
		SchemaTypesDisplayTransactionBuilder transaction = manager.newTransactionBuilderFor(collection);
		//transaction.add(manager.getSchema(collection, "cart_default").withRemovedFormMetadatas("cart_default_title"));

		SchemaDisplayConfig userTask = manager.getSchema(collection, "userTask_default");
		userTask = userTask.withNewDisplayMetadataBefore("userTask_default_administrativeUnit", "userTask_default_comments");
		userTask = userTask.withNewDisplayMetadataBefore("userTask_default_linkedContainers", "userTask_default_comments");
		userTask = userTask.withNewDisplayMetadataBefore("userTask_default_linkedDocuments", "userTask_default_comments");
		userTask = userTask.withNewDisplayMetadataBefore("userTask_default_linkedFolders", "userTask_default_comments");

		userTask = userTask.withNewFormMetadata("userTask_default_linkedDocuments");
		userTask = userTask.withNewFormMetadata("userTask_default_linkedFolders");
		userTask = userTask.withNewFormMetadata("userTask_default_linkedContainers");
		userTask = userTask.withNewFormMetadata("userTask_default_reason");

		userTask = userTask.withTableMetadataCodes(asList("userTask_default_title", "userTask_default_status",
				"userTask_default_dueDate", "userTask_default_assignee", "userTask_default_assignee",
				"userTask_default_starredByUsers"));
		transaction.add(userTask);

		SchemaDisplayConfig userDocument = manager.getSchema(collection, "userDocument_default");
		transaction.add(userDocument.withRemovedDisplayMetadatas("userDocument_default_folder")
				.withRemovedFormMetadatas("userDocument_default_folder"));

		SchemaDisplayConfig container = manager.getSchema(collection, ContainerRecord.DEFAULT_SCHEMA);
		//container = container.withNewFormMetadata(ContainerRecord.DEFAULT_SCHEMA + "_" + ContainerRecord.FILL_RATIO_ENTRED);
		transaction.add(container);

		manager.execute(transaction.build());
	}

	private Transaction createRecordTransaction(String collection,
												MigrationResourcesProvider migrationResourcesProvider,
												AppLayerFactory appLayerFactory, MetadataSchemaTypes types) {
		Transaction transaction = new Transaction();

		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(types.getCollection(), appLayerFactory.getModelLayerFactory());

		transaction.add(rm.newMediumType().setCode(migrationResourcesProvider.getDefaultLanguageString("MediumType.paperCode"))
				.setTitles(migrationResourcesProvider.getLanguagesString("MediumType.paperTitle"))
				.setAnalogical(true).setActivatedOnContent(null));

		transaction.add(rm.newMediumType().setCode(migrationResourcesProvider.getDefaultLanguageString("MediumType.filmCode"))
				.setTitles(migrationResourcesProvider.getLanguagesString("MediumType.filmTitle"))
				.setAnalogical(true).setActivatedOnContent(null));

		transaction.add(rm.newMediumType().setCode(migrationResourcesProvider.getDefaultLanguageString("MediumType.driveCode"))
				.setTitles(migrationResourcesProvider.getLanguagesString("MediumType.driveTitle"))
				.setAnalogical(false).setActivatedOnContent(null));

		transaction.add(rm.newDocumentType().setCode(DocumentType.EMAIL_DOCUMENT_TYPE)
				.setTitles(migrationResourcesProvider.getLanguagesString("DocumentType.emailDocumentType"))
				.setLinkedSchema(Email.SCHEMA));

		transaction.add(rm.newVariableRetentionPeriod().setCode("888")
				.setTitles(migrationResourcesProvider.getLanguagesString("init.variablePeriod888")));

		transaction.add(rm.newVariableRetentionPeriod().setCode("999")
				.setTitles(migrationResourcesProvider.getLanguagesString("init.variablePeriod999")));

		transaction.add(rm.newFacetField().setOrder(2).setFieldDataStoreCode("administrativeUnitId_s")
				.setTitles(migrationResourcesProvider.getLanguagesString("init.facet.administrativeUnit")));
		transaction.add(rm.newFacetField().setOrder(2).setFieldDataStoreCode("categoryId_s")
				.setTitles(migrationResourcesProvider.getLanguagesString("init.facet.category")));
		transaction.add(rm.newFacetField().setOrder(2).setFieldDataStoreCode("archivisticStatus_s")
				.setTitles(migrationResourcesProvider.getLanguagesString("init.facet.archivisticStatus")));
		transaction.add(rm.newFacetField().setOrder(3).setFieldDataStoreCode("copyStatus_s")
				.setTitles(migrationResourcesProvider.getLanguagesString("init.facet.copyStatus")));

		transaction.add(rm.newFacetField().setTitles(migrationResourcesProvider.getLanguagesString("facets.folderType"))
				.setFieldDataStoreCode(rm.folder.folderType().getDataStoreCode()).setActive(false));
		transaction.add(rm.newFacetField().setTitles(migrationResourcesProvider.getLanguagesString("facets.documentType"))
				.setFieldDataStoreCode(rm.documentDocumentType().getDataStoreCode()).setActive(false));

		try {
			transaction = createDefaultLabel(collection, appLayerFactory, migrationResourcesProvider, transaction);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		try {
			RMMigrationTo7_2.createNewTaskTypes(appLayerFactory, collection, transaction, migrationResourcesProvider);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}

		return transaction;
	}

	public Transaction createDefaultLabel(String collection, AppLayerFactory factory,
										  MigrationResourcesProvider provider,
										  Transaction trans)
			throws FileNotFoundException {
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
																								   provider.getDefaultLanguageString("Migration.typeAveryDroite") :
																								   provider.getDefaultLanguageString("Migration.typeAveryGauche"));
			} else {
				titre += provider.getDefaultLanguageString("Migration.typeSchemaConteneur");
			}
			String etiquetteName = provider.getDefaultLanguageString("Migration.etiquetteName");
			String extension = provider.getDefaultLanguageString("Migration.fileExtension");
			titre += " (" + etiquetteName + " " + format + ")";
			record.set(typeBuilder.getMetadata(PrintableLabel.COLONNE), 2);
			record.set(typeBuilder.getMetadata(Printable.ISDELETABLE), true);
			UploadOptions options = new UploadOptions(etiquetteName + " " + format + " " + type)
					.setHandleDeletionOfUnreferencedHashes(false);
			ContentVersionDataSummary upload = contentManager.upload(new FileInputStream(fi), options)
					.getContentVersionDataSummary();
			record.set(typeBuilder.getMetadata(Report.TITLE), titre);
			record.set(typeBuilder.getMetadata(Printable.JASPERFILE),
					contentManager.createSystemContent(etiquetteName + "-" + format + "-" + type + extension, upload));
			trans.add(record);
		}
		return trans;
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
								   MigrationResourcesProvider migrationResourcesProvider,
								   AppLayerFactory appLayerFactory) {
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

			for (String metadata : asList("administrativeUnit_default_description", "administrativeUnit_default_title",
					"category_default_description", "category_default_keywords", "category_default_title",
					"ddvContainerRecordType_default_description", "ddvContainerRecordType_default_title",
					"ddvDocumentType_default_description", "ddvDocumentType_default_title",
					"ddvFolderType_default_description", "ddvFolderType_default_title",
					"ddvMediumType_default_description", "ddvMediumType_default_title",
					"ddvStorageSpaceType_default_description", "ddvStorageSpaceType_default_title",
					"ddvVariablePeriod_default_description", "ddvVariablePeriod_default_title",
					"ddvYearType_default_description", "ddvYearType_default_title",
					"retentionRule_default_title", "retentionRule_default_juridicReference",
					"retentionRule_default_generalComment",
					"retentionRule_default_keywords", "retentionRule_default_copyRulesComment",
					"retentionRule_default_description",
					"uniformSubdivision_default_title")) {

				typesBuilder.getMetadata(metadata).setMultiLingual(true);
			}
			new CommonMetadataBuilder().addCommonMetadataToAllExistingSchemas(typesBuilder);
		}

	}

	private void addEmailTemplates(AppLayerFactory appLayerFactory,
								   MigrationResourcesProvider migrationResourcesProvider,
								   String collection) {
		addEmailTemplates(appLayerFactory, migrationResourcesProvider, collection, "remindReturnBorrowedFolderTemplate.html",
				RMEmailTemplateConstants.REMIND_BORROW_TEMPLATE_ID);
		addEmailTemplates(appLayerFactory, migrationResourcesProvider, collection, "approvalRequestForDecomListTemplate.html",
				RMEmailTemplateConstants.APPROVAL_REQUEST_TEMPLATE_ID);
		addEmailTemplates(appLayerFactory, migrationResourcesProvider, collection, "validationRequestForDecomListTemplate.html",
				RMEmailTemplateConstants.VALIDATION_REQUEST_TEMPLATE_ID);
		addEmailTemplates(appLayerFactory, migrationResourcesProvider, collection, "alertAvailableTemplate.html",
				RMEmailTemplateConstants.ALERT_AVAILABLE_ID);
		addEmailTemplates(appLayerFactory, migrationResourcesProvider, collection,
				"alertWhenDecommissioningListCreatedTemplate.html",
				RMEmailTemplateConstants.DECOMMISSIONING_LIST_CREATION_TEMPLATE_ID);

		if (appLayerFactory.getModelLayerFactory().getCollectionsListManager().getCollectionLanguages(collection).get(0)
				.equals("fr")) {
			addEmailTemplates(appLayerFactory, migrationResourcesProvider,
					collection, "approvalRequestDeniedForDecomListTemplate.html",
					RMEmailTemplateConstants.APPROVAL_REQUEST_DENIED_TEMPLATE_ID);
		} else {
			addEmailTemplates(appLayerFactory, migrationResourcesProvider,
					collection, "approvalRequestDeniedForDecomListTemplate_en.html",
					RMEmailTemplateConstants.APPROVAL_REQUEST_DENIED_TEMPLATE_ID);

		}
	}

	private void addEmailTemplates(AppLayerFactory appLayerFactory,
								   MigrationResourcesProvider migrationResourcesProvider,
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
