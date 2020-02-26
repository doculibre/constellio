package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.entities.schemasDisplay.SchemaDisplayConfig;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.constants.RMRoles;
import com.constellio.app.modules.rm.model.CopyRetentionRuleFactory;
import com.constellio.app.modules.rm.model.calculators.CategoryIsLinkableCalculator;
import com.constellio.app.modules.rm.model.calculators.ContainerTitleCalculator;
import com.constellio.app.modules.rm.model.calculators.FolderActiveRetentionTypeCalculator;
import com.constellio.app.modules.rm.model.calculators.FolderApplicableCopyRuleCalculator;
import com.constellio.app.modules.rm.model.calculators.FolderClosingDateCalculator;
import com.constellio.app.modules.rm.model.calculators.FolderCopyRulesExpectedDepositDatesCalculator;
import com.constellio.app.modules.rm.model.calculators.FolderCopyRulesExpectedDestructionDatesCalculator;
import com.constellio.app.modules.rm.model.calculators.FolderCopyRulesExpectedTransferDatesCalculator;
import com.constellio.app.modules.rm.model.calculators.FolderCopyStatusCalculator;
import com.constellio.app.modules.rm.model.calculators.FolderExpectedDepositDateCalculator;
import com.constellio.app.modules.rm.model.calculators.FolderExpectedDestructionDateCalculator;
import com.constellio.app.modules.rm.model.calculators.FolderExpectedTransferDateCalculator;
import com.constellio.app.modules.rm.model.calculators.FolderInactiveDisposalTypeCalculator;
import com.constellio.app.modules.rm.model.calculators.FolderMainCopyRuleCalculator;
import com.constellio.app.modules.rm.model.calculators.FolderSemiActiveRetentionTypeCalculator;
import com.constellio.app.modules.rm.model.calculators.FolderStatusCalculator;
import com.constellio.app.modules.rm.model.calculators.decommissioningList.DecomListContainersCalculator;
import com.constellio.app.modules.rm.model.calculators.decommissioningList.DecomListFoldersCalculator;
import com.constellio.app.modules.rm.model.calculators.decommissioningList.DecomListHasAnalogicalMediumTypesCalculator;
import com.constellio.app.modules.rm.model.calculators.decommissioningList.DecomListHasElectronicMediumTypesCalculator;
import com.constellio.app.modules.rm.model.calculators.decommissioningList.DecomListIsUniform;
import com.constellio.app.modules.rm.model.calculators.decommissioningList.DecomListStatusCalculator;
import com.constellio.app.modules.rm.model.calculators.decommissioningList.DecomListUniformCategoryCalculator;
import com.constellio.app.modules.rm.model.calculators.decommissioningList.DecomListUniformCopyRuleCalculator;
import com.constellio.app.modules.rm.model.calculators.decommissioningList.DecomListUniformCopyTypeCalculator;
import com.constellio.app.modules.rm.model.calculators.decommissioningList.DecomListUniformRuleCalculator;
import com.constellio.app.modules.rm.model.calculators.folder.FolderApplicableCategoryCalculator;
import com.constellio.app.modules.rm.model.calculators.folder.FolderAppliedAdministrativeUnitCalculator;
import com.constellio.app.modules.rm.model.calculators.folder.FolderAppliedFilingSpaceCalculator;
import com.constellio.app.modules.rm.model.calculators.folder.FolderAppliedRetentionRuleCalculator;
import com.constellio.app.modules.rm.model.calculators.folder.FolderAppliedUniformSubdivisionCalculator;
import com.constellio.app.modules.rm.model.calculators.folder.FolderMediaTypesCalculator;
import com.constellio.app.modules.rm.model.calculators.rule.RuleDocumentTypesCalculator;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.model.enums.DecomListStatus;
import com.constellio.app.modules.rm.model.enums.DecommissioningListType;
import com.constellio.app.modules.rm.model.enums.DecommissioningType;
import com.constellio.app.modules.rm.model.enums.DisposalType;
import com.constellio.app.modules.rm.model.enums.FolderMediaType;
import com.constellio.app.modules.rm.model.enums.FolderStatus;
import com.constellio.app.modules.rm.model.enums.OriginStatus;
import com.constellio.app.modules.rm.model.enums.RetentionType;
import com.constellio.app.modules.rm.model.validators.RetentionRuleValidator;
import com.constellio.app.modules.rm.services.ValueListItemSchemaTypeBuilder;
import com.constellio.app.modules.rm.services.ValueListItemSchemaTypeBuilder.ValueListItemSchemaTypeBuilderOptions;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.FilingSpace;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.modules.rm.wrappers.StorageSpace;
import com.constellio.app.modules.rm.wrappers.UniformSubdivision;
import com.constellio.app.modules.rm.wrappers.structures.CommentFactory;
import com.constellio.app.modules.rm.wrappers.structures.DecomListContainerDetailFactory;
import com.constellio.app.modules.rm.wrappers.structures.DecomListFolderDetailFactory;
import com.constellio.app.modules.rm.wrappers.structures.RetentionRuleDocumentTypeFactory;
import com.constellio.app.modules.rm.wrappers.type.ContainerRecordType;
import com.constellio.app.modules.rm.wrappers.type.DocumentType;
import com.constellio.app.modules.rm.wrappers.type.FolderType;
import com.constellio.app.modules.rm.wrappers.type.MediumType;
import com.constellio.app.modules.rm.wrappers.type.StorageSpaceType;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.migrations.MigrationUtil;
import com.constellio.app.services.schemasDisplay.SchemaDisplayManagerTransaction;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.security.roles.RolesManager;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.constellio.app.modules.rm.constants.RMTaxonomies.ADMINISTRATIVE_UNITS;
import static com.constellio.app.modules.rm.constants.RMTaxonomies.CLASSIFICATION_PLAN;
import static com.constellio.app.modules.rm.constants.RMTaxonomies.STORAGES;
import static com.constellio.model.entities.schemas.MetadataValueType.BOOLEAN;
import static com.constellio.model.entities.schemas.MetadataValueType.CONTENT;
import static com.constellio.model.entities.schemas.MetadataValueType.DATE;
import static com.constellio.model.entities.schemas.MetadataValueType.NUMBER;
import static com.constellio.model.entities.schemas.MetadataValueType.REFERENCE;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static com.constellio.model.entities.schemas.MetadataValueType.STRUCTURE;
import static com.constellio.model.entities.schemas.MetadataValueType.TEXT;
import static com.constellio.model.entities.schemas.Schemas.TITLE_CODE;
import static java.util.Arrays.asList;

public class RMMigrationTo5_0_1 extends MigrationHelper implements MigrationScript {
	@Override
	public String getVersion() {
		return "5.0.1";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory) {
		new SchemaAlterationFor5_0_1(collection, migrationResourcesProvider, appLayerFactory).migrate();
		setupTaxonomies(collection, appLayerFactory.getModelLayerFactory(), migrationResourcesProvider);
		setupDisplayConfig(collection, appLayerFactory);
		setupRoles(collection, appLayerFactory.getModelLayerFactory(), migrationResourcesProvider);
	}

	private static void setupTaxonomies(String collection, ModelLayerFactory modelLayerFactory,
										MigrationResourcesProvider migrationResourcesProvider) {

		setupClassificationPlanTaxonomies(collection, modelLayerFactory, migrationResourcesProvider);
		setupStorageSpaceTaxonomy(collection, modelLayerFactory, migrationResourcesProvider);
		setupAdminUnitTaxonomy(collection, modelLayerFactory, migrationResourcesProvider);
	}

	public static void setupStorageSpaceTaxonomy(String collection, ModelLayerFactory modelLayerFactory,
												 MigrationResourcesProvider migrationResourcesProvider) {

		MetadataSchemasManager metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
		TaxonomiesManager taxonomiesManager = modelLayerFactory.getTaxonomiesManager();

		Map<Language, String> mapMultiLanguageString = getMultiLanguageString(collection, modelLayerFactory,
				migrationResourcesProvider, "init.rm.containers");

		Taxonomy storagesTaxonomy = Taxonomy.createHiddenInHomePage(STORAGES, mapMultiLanguageString, collection,
				StorageSpace.SCHEMA_TYPE);
		taxonomiesManager.addTaxonomy(storagesTaxonomy, metadataSchemasManager);

	}

	public static void setupAdminUnitTaxonomy(String collection, ModelLayerFactory modelLayerFactory,
											  MigrationResourcesProvider migrationResourcesProvider) {

		MetadataSchemasManager metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
		TaxonomiesManager taxonomiesManager = modelLayerFactory.getTaxonomiesManager();

		Map<Language, String> mapMultiLanguageString = getMultiLanguageString(collection, modelLayerFactory,
				migrationResourcesProvider, "init.rm.admUnits");

		Taxonomy unitTaxonomy = Taxonomy.createPublic(
				ADMINISTRATIVE_UNITS, mapMultiLanguageString, collection,
				AdministrativeUnit.SCHEMA_TYPE);
		taxonomiesManager.addTaxonomy(unitTaxonomy, metadataSchemasManager);

		taxonomiesManager.setPrincipalTaxonomy(unitTaxonomy, metadataSchemasManager);
	}

	public static void setupClassificationPlanTaxonomies(String collection, ModelLayerFactory modelLayerFactory,
														 MigrationResourcesProvider migrationResourcesProvider) {

		MetadataSchemasManager metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
		TaxonomiesManager taxonomiesManager = modelLayerFactory.getTaxonomiesManager();

		Map<Language, String> mapLangageTitre = getMultiLanguageString(collection, modelLayerFactory, migrationResourcesProvider,
				"init.rm.plan");

		taxonomiesManager
				.addTaxonomy(Taxonomy.createPublic(CLASSIFICATION_PLAN, mapLangageTitre, collection,
						Category.SCHEMA_TYPE), metadataSchemasManager);

	}

	@NotNull
	private static Map<Language, String> getMultiLanguageString(String collection, ModelLayerFactory modelLayerFactory,
																MigrationResourcesProvider migrationResourcesProvider,
																String key) {
		List<String> languageList = modelLayerFactory.getCollectionsListManager().getCollectionLanguages(collection);

		Map<Language, String> mapLangageTitre = new HashMap<>();

		for (String language : languageList) {
			Locale locale = new Locale(language);
			mapLangageTitre.put(Language.withLocale(locale), migrationResourcesProvider.getString(key, locale));
		}
		return mapLangageTitre;
	}

	private void setupDisplayConfig(String collection, AppLayerFactory appLayerFactory) {
		SchemasDisplayManager manager = appLayerFactory.getMetadataSchemasDisplayManager();

		manager.enableAllMetadatasInAdvancedSearch(collection, Folder.SCHEMA_TYPE);
		manager.enableAllMetadatasInAdvancedSearch(collection, Document.SCHEMA_TYPE);
		manager.enableAllMetadatasInAdvancedSearch(collection, ContainerRecord.SCHEMA_TYPE);

		SchemaDisplayManagerTransaction transaction = new SchemaDisplayManagerTransaction();

		// Enable search for Folders
		transaction.add(manager.getType(collection, Folder.SCHEMA_TYPE)
				.withSimpleSearchStatus(true).withAdvancedSearchStatus(true));

		// Enable search for Documents
		transaction.add(manager.getType(collection, Document.SCHEMA_TYPE)
				.withSimpleSearchStatus(true).withAdvancedSearchStatus(true));

		// Enable search for ContainerRecords
		transaction.add(manager.getType(collection, ContainerRecord.SCHEMA_TYPE)
				.withSimpleSearchStatus(true).withAdvancedSearchStatus(true));

		transaction.setModifiedCollectionTypes(manager.getTypes(collection).withFacetMetadataCodes(asList(
				Folder.DEFAULT_SCHEMA + "_schema",
				Folder.DEFAULT_SCHEMA + "_" + Folder.ARCHIVISTIC_STATUS,
				Folder.DEFAULT_SCHEMA + "_" + Folder.CATEGORY,
				Folder.DEFAULT_SCHEMA + "_" + Folder.ADMINISTRATIVE_UNIT,
				Folder.DEFAULT_SCHEMA + "_" + Folder.FILING_SPACE,
				Folder.DEFAULT_SCHEMA + "_" + Folder.MEDIUM_TYPES,
				Folder.DEFAULT_SCHEMA + "_" + Folder.COPY_STATUS)));

		// FOLDER TYPE
		SchemaDisplayConfig schemaFormFolderTypeConfig = order(collection, appLayerFactory, "form",
				manager.getSchema(collection, FolderType.DEFAULT_SCHEMA),
				Schemas.TITLE.getLocalCode(),
				FolderType.CODE,
				FolderType.DESCRIPTION,
				FolderType.LINKED_SCHEMA);
		SchemaDisplayConfig schemaDisplayFolderTypeConfig = order(collection, appLayerFactory, "display",
				manager.getSchema(collection, FolderType.DEFAULT_SCHEMA),
				Schemas.TITLE.getLocalCode(),
				FolderType.CODE,
				FolderType.DESCRIPTION,
				FolderType.LINKED_SCHEMA);
		transaction.add(
				schemaDisplayFolderTypeConfig.withFormMetadataCodes(schemaFormFolderTypeConfig.getFormMetadataCodes()));

		// FOLDER
		SchemaDisplayConfig schemaFormFolderConfig = order(collection, appLayerFactory, "form",
				manager.getSchema(collection, Folder.DEFAULT_SCHEMA),
				Folder.TYPE,
				Schemas.TITLE.getLocalCode(),
				Folder.PARENT_FOLDER,
				Folder.CATEGORY_ENTERED,
				Folder.UNIFORM_SUBDIVISION_ENTERED,
				Folder.RETENTION_RULE_ENTERED,
				Folder.COPY_STATUS_ENTERED,
				Folder.OPENING_DATE,
				Folder.ENTERED_CLOSING_DATE,
				Folder.FILING_SPACE_ENTERED,
				Folder.ADMINISTRATIVE_UNIT_ENTERED,
				Folder.MEDIUM_TYPES,
				Folder.KEYWORDS,
				Folder.DESCRIPTION,
				Folder.CONTAINER,
				Folder.ACTUAL_TRANSFER_DATE,
				Folder.ACTUAL_DEPOSIT_DATE,
				Folder.ACTUAL_DESTRUCTION_DATE);

		SchemaDisplayConfig schemaDisplayFolderConfig = order(collection, appLayerFactory, "display",
				manager.getSchema(collection, Folder.DEFAULT_SCHEMA),
				Folder.PARENT_FOLDER,
				Schemas.TITLE.getLocalCode(),
				Schemas.CREATED_BY.getLocalCode(),
				Folder.DESCRIPTION,
				Folder.FILING_SPACE,
				Folder.ADMINISTRATIVE_UNIT,
				Folder.MEDIUM_TYPES,
				Folder.COPY_STATUS,
				Folder.ARCHIVISTIC_STATUS,
				Folder.CONTAINER,
				Folder.CATEGORY,
				Folder.UNIFORM_SUBDIVISION,
				Folder.RETENTION_RULE,
				Folder.MAIN_COPY_RULE,
				Folder.RETENTION_RULE_ADMINISTRATIVE_UNITS,
				Folder.KEYWORDS,
				Schemas.CREATED_ON.getLocalCode(),
				Folder.OPENING_DATE,
				Folder.CLOSING_DATE,
				Folder.ACTUAL_TRANSFER_DATE,
				Folder.EXPECTED_TRANSFER_DATE,
				Folder.ACTUAL_DEPOSIT_DATE,
				Folder.EXPECTED_DEPOSIT_DATE,
				Folder.ACTUAL_DESTRUCTION_DATE,
				Folder.EXPECTED_DESTRUCTION_DATE,
				Folder.COMMENTS);
		transaction.add(
				schemaDisplayFolderConfig.withFormMetadataCodes(schemaFormFolderConfig.getFormMetadataCodes()));
		transaction.add(manager.getMetadata(collection, Folder.DEFAULT_SCHEMA + "_" + Folder.MEDIUM_TYPES)
				.withInputType(MetadataInputType.CHECKBOXES));

		// DOCUMENT TYPE
		SchemaDisplayConfig schemaFormDocumentTypeConfig = order(collection, appLayerFactory, "form",
				manager.getSchema(collection, DocumentType.DEFAULT_SCHEMA),
				Schemas.TITLE.getLocalCode(),
				DocumentType.CODE,
				DocumentType.DESCRIPTION,
				DocumentType.LINKED_SCHEMA);
		SchemaDisplayConfig schemaDisplayDocumentTypeConfig = order(collection, appLayerFactory, "display",
				manager.getSchema(collection, DocumentType.DEFAULT_SCHEMA),
				Schemas.TITLE.getLocalCode(),
				DocumentType.CODE,
				DocumentType.DESCRIPTION,
				DocumentType.LINKED_SCHEMA);
		transaction.add(
				schemaDisplayDocumentTypeConfig.withFormMetadataCodes(schemaFormDocumentTypeConfig.getFormMetadataCodes()));

		// DOCUMENT
		SchemaDisplayConfig schemaFormDocumentConfig = order(collection, appLayerFactory, "form",
				manager.getSchema(collection, Document.DEFAULT_SCHEMA),
				Document.TYPE,
				Schemas.TITLE.getLocalCode(),
				Document.FOLDER,
				Document.KEYWORDS,
				Document.CONTENT);
		SchemaDisplayConfig schemaDisplayDocumentConfig = order(collection, appLayerFactory, "display",
				manager.getSchema(collection, Document.DEFAULT_SCHEMA),
				Schemas.TITLE.getLocalCode(),
				Document.CONTENT,
				Document.FOLDER,
				Document.TYPE,
				Schemas.CREATED_ON.getLocalCode(),
				Document.KEYWORDS,
				Document.DESCRIPTION,
				Document.COMMENTS);
		transaction.add(
				schemaDisplayDocumentConfig.withFormMetadataCodes(schemaFormDocumentConfig.getFormMetadataCodes()));

		// CONTAINER RECORD TYPE
		SchemaDisplayConfig schemaFormContainerTypeConfig = order(collection, appLayerFactory, "form",
				manager.getSchema(collection, ContainerRecordType.DEFAULT_SCHEMA),
				Schemas.TITLE.getLocalCode(),
				ContainerRecordType.CODE,
				ContainerRecordType.DESCRIPTION,
				ContainerRecordType.LINKED_SCHEMA);
		SchemaDisplayConfig schemaDisplayContainerTypeConfig = order(collection, appLayerFactory, "display",
				manager.getSchema(collection, ContainerRecordType.DEFAULT_SCHEMA),
				Schemas.TITLE.getLocalCode(),
				ContainerRecordType.CODE,
				ContainerRecordType.DESCRIPTION,
				ContainerRecordType.LINKED_SCHEMA);
		transaction.add(
				schemaDisplayContainerTypeConfig.withFormMetadataCodes(schemaFormContainerTypeConfig.getFormMetadataCodes()));

		// CONTAINER RECORD
		SchemaDisplayConfig schemaFormContainerConfig = order(collection, appLayerFactory, "form",
				manager.getSchema(collection, ContainerRecord.DEFAULT_SCHEMA),
				ContainerRecord.TYPE,
				ContainerRecord.TEMPORARY_IDENTIFIER,
				ContainerRecord.IDENTIFIER,
				ContainerRecord.FULL,
				ContainerRecord.DESCRIPTION,
				ContainerRecord.POSITION);
		SchemaDisplayConfig schemaDisplayContainerConfig = order(collection, appLayerFactory, "display",
				manager.getSchema(collection, ContainerRecord.DEFAULT_SCHEMA),
				ContainerRecord.TYPE,
				ContainerRecord.TEMPORARY_IDENTIFIER,
				ContainerRecord.IDENTIFIER,
				ContainerRecord.FULL,
				ContainerRecord.DESCRIPTION,
				ContainerRecord.ADMINISTRATIVE_UNIT,
				ContainerRecord.STORAGE_SPACE);
		transaction.add(
				schemaDisplayContainerConfig.withFormMetadataCodes(schemaFormContainerConfig.getFormMetadataCodes()));
		transaction.add(
				manager.getMetadata(collection, ContainerRecord.DEFAULT_SCHEMA + "_" + ContainerRecord.TYPE)
						.withInputType(MetadataInputType.DROPDOWN));

		// MEDIUM TYPE
		SchemaDisplayConfig schemaFormMediumTypeConfig = order(collection, appLayerFactory, "form",
				manager.getSchema(collection, MediumType.DEFAULT_SCHEMA),
				Schemas.TITLE.getLocalCode(),
				MediumType.CODE,
				MediumType.DESCRIPTION,
				MediumType.ANALOGICAL);
		SchemaDisplayConfig schemaDisplayMediumTypeConfig = order(collection, appLayerFactory, "display",
				manager.getSchema(collection, MediumType.DEFAULT_SCHEMA),
				Schemas.TITLE.getLocalCode(),
				MediumType.CODE,
				MediumType.DESCRIPTION,
				MediumType.ANALOGICAL);
		transaction.add(
				schemaDisplayMediumTypeConfig.withFormMetadataCodes(schemaFormMediumTypeConfig.getFormMetadataCodes()));

		// ADMINISTRATIVE UNIT
		SchemaDisplayConfig schemaFormAdministrativeUnitConfig = order(collection, appLayerFactory, "form",
				manager.getSchema(collection, AdministrativeUnit.DEFAULT_SCHEMA),
				AdministrativeUnit.CODE,
				AdministrativeUnit.FILING_SPACES,
				Schemas.TITLE.getLocalCode(),
				AdministrativeUnit.PARENT);
		SchemaDisplayConfig schemaDisplayAdministrativeUnitConfig = order(collection, appLayerFactory,
				"display",
				manager.getSchema(collection, AdministrativeUnit.DEFAULT_SCHEMA),
				AdministrativeUnit.CODE,
				Schemas.TITLE.getLocalCode(),
				AdministrativeUnit.PARENT,
				AdministrativeUnit.FILING_SPACES,
				Schemas.CREATED_ON.getLocalCode(),
				Schemas.MODIFIED_BY.getLocalCode(),
				Schemas.CREATED_BY.getLocalCode());
		transaction.add(schemaDisplayAdministrativeUnitConfig
				.withFormMetadataCodes(schemaFormAdministrativeUnitConfig.getFormMetadataCodes()));

		// CATEGORY
		SchemaDisplayConfig schemaFormCategoryConfig = order(collection, appLayerFactory, "form",
				manager.getSchema(collection, Category.DEFAULT_SCHEMA),
				Category.CODE,
				Schemas.TITLE.getLocalCode(),
				Category.DESCRIPTION,
				Category.KEYWORDS,
				Category.PARENT,
				Category.RETENTION_RULES);
		SchemaDisplayConfig schemaDisplayCategoryConfig = order(collection, appLayerFactory, "display",
				manager.getSchema(collection, Category.DEFAULT_SCHEMA),
				Category.CODE,
				Schemas.TITLE.getLocalCode(),
				Category.DESCRIPTION,
				Schemas.CREATED_ON.getLocalCode(),
				Schemas.MODIFIED_BY.getLocalCode(),
				Schemas.CREATED_BY.getLocalCode(),
				Category.KEYWORDS,
				Category.PARENT,
				Category.RETENTION_RULES,
				Category.COMMENTS);
		transaction.add(schemaDisplayCategoryConfig
				.withFormMetadataCodes(schemaFormCategoryConfig.getFormMetadataCodes()));

		// DECOMMISSIONING LIST
		SchemaDisplayConfig schemaFormDecommissioningListConfig = order(collection, appLayerFactory, "form",
				manager.getSchema(collection, DecommissioningList.DEFAULT_SCHEMA),
				Schemas.TITLE.getLocalCode(),
				DecommissioningList.DESCRIPTION);
		SchemaDisplayConfig schemaDisplayDecommissioningListConfig = order(collection, appLayerFactory,
				"display",
				manager.getSchema(collection, DecommissioningList.DEFAULT_SCHEMA),
				Schemas.TITLE.getLocalCode(),
				DecommissioningList.TYPE,
				DecommissioningList.DESCRIPTION,
				DecommissioningList.ADMINISTRATIVE_UNIT,
				DecommissioningList.FILING_SPACE,
				Schemas.CREATED_ON.getLocalCode(),
				Schemas.CREATED_BY.getLocalCode(),
				Schemas.MODIFIED_ON.getLocalCode(),
				Schemas.MODIFIED_BY.getLocalCode(),
				DecommissioningList.UNIFORM_CATEGORY,
				DecommissioningList.UNIFORM_RULE,
				DecommissioningList.STATUS,
				DecommissioningList.APPROVAL_DATE,
				DecommissioningList.APPROVAL_USER);
		transaction.add(schemaDisplayDecommissioningListConfig
				.withFormMetadataCodes(schemaFormDecommissioningListConfig.getFormMetadataCodes()));

		// FILING SPACE
		SchemaDisplayConfig schemaFormFilingSpaceConfig = order(collection, appLayerFactory, "form",
				manager.getSchema(collection, FilingSpace.DEFAULT_SCHEMA),
				FilingSpace.CODE,
				Schemas.TITLE.getLocalCode(),
				FilingSpace.ADMINISTRATORS,
				FilingSpace.USERS);
		SchemaDisplayConfig schemaDisplayFilingSpaceConfig = order(collection, appLayerFactory, "display",
				manager.getSchema(collection, FilingSpace.DEFAULT_SCHEMA),
				FilingSpace.CODE,
				Schemas.TITLE.getLocalCode(),
				FilingSpace.DESCRIPTION,
				FilingSpace.USERS,
				FilingSpace.ADMINISTRATORS);
		transaction.add(
				schemaDisplayFilingSpaceConfig.withFormMetadataCodes(schemaFormFilingSpaceConfig.getFormMetadataCodes()));

		// RETENTION RULE
		SchemaDisplayConfig schemaFormRetentionRuleConfig = order(collection, appLayerFactory, "form",
				manager.getSchema(collection, RetentionRule.DEFAULT_SCHEMA),
				RetentionRule.CODE,
				RetentionRule.APPROVED,
				RetentionRule.APPROVAL_DATE,
				Schemas.TITLE.getLocalCode(),
				RetentionRule.CORPUS,
				RetentionRule.CORPUS_RULE_NUMBER,
				RetentionRule.ADMINISTRATIVE_UNITS,
				RetentionRule.RESPONSIBLE_ADMINISTRATIVE_UNITS,
				RetentionRule.DESCRIPTION,
				RetentionRule.JURIDIC_REFERENCE,
				RetentionRule.GENERAL_COMMENT,
				RetentionRule.KEYWORDS,
				RetentionRule.HISTORY,
				RetentionRule.ESSENTIAL_DOCUMENTS,
				RetentionRule.CONFIDENTIAL_DOCUMENTS,
				RetentionRule.COPY_RETENTION_RULES,
				RetentionRule.DOCUMENT_TYPES_DETAILS,
				RetentionRule.COPY_RULES_COMMENT);
		SchemaDisplayConfig schemaDisplayRetentionRuleConfig = order(collection, appLayerFactory, "display",
				manager.getSchema(collection, RetentionRule.DEFAULT_SCHEMA),
				RetentionRule.CODE,
				RetentionRule.APPROVED,
				RetentionRule.APPROVAL_DATE,
				Schemas.TITLE.getLocalCode(),
				RetentionRule.CORPUS,
				RetentionRule.CORPUS_RULE_NUMBER,
				RetentionRule.ADMINISTRATIVE_UNITS,
				RetentionRule.RESPONSIBLE_ADMINISTRATIVE_UNITS,
				RetentionRule.DESCRIPTION,
				RetentionRule.JURIDIC_REFERENCE,
				RetentionRule.GENERAL_COMMENT,
				RetentionRule.KEYWORDS,
				RetentionRule.HISTORY,
				RetentionRule.ESSENTIAL_DOCUMENTS,
				RetentionRule.CONFIDENTIAL_DOCUMENTS,
				RetentionRule.COPY_RETENTION_RULES,
				RetentionRule.DOCUMENT_TYPES_DETAILS,
				RetentionRule.COPY_RULES_COMMENT);
		transaction.add(
				schemaDisplayRetentionRuleConfig.withFormMetadataCodes(schemaFormRetentionRuleConfig.getFormMetadataCodes()));

		// STORAGE SPACE TYPE
		SchemaDisplayConfig schemaFormStorageSpaceTypeConfig = order(collection, appLayerFactory, "form",
				manager.getSchema(collection, StorageSpaceType.DEFAULT_SCHEMA),
				Schemas.TITLE.getLocalCode(),
				StorageSpaceType.CODE,
				StorageSpaceType.DESCRIPTION,
				StorageSpaceType.LINKED_SCHEMA);
		SchemaDisplayConfig schemaDisplayStorageSpaceTypeConfig = order(collection, appLayerFactory, "display",
				manager.getSchema(collection, StorageSpaceType.DEFAULT_SCHEMA),
				Schemas.TITLE.getLocalCode(),
				StorageSpaceType.CODE,
				StorageSpaceType.DESCRIPTION,
				StorageSpaceType.LINKED_SCHEMA);
		transaction.add(schemaDisplayStorageSpaceTypeConfig
				.withFormMetadataCodes(schemaFormStorageSpaceTypeConfig.getFormMetadataCodes()));

		// STORAGE SPACE
		SchemaDisplayConfig schemaFormStorageSpaceConfig = order(collection, appLayerFactory, "form",
				manager.getSchema(collection, StorageSpace.DEFAULT_SCHEMA),
				StorageSpace.TYPE,
				StorageSpace.CODE,
				Schemas.TITLE.getLocalCode(),
				StorageSpace.DESCRIPTION,
				StorageSpace.CAPACITY,
				StorageSpace.DECOMMISSIONING_TYPE,
				StorageSpace.PARENT_STORAGE_SPACE);
		SchemaDisplayConfig schemaDisplayStorageSpaceConfig = order(collection, appLayerFactory, "display",
				manager.getSchema(collection, StorageSpace.DEFAULT_SCHEMA),
				StorageSpace.TYPE,
				StorageSpace.CODE,
				Schemas.TITLE.getLocalCode(),
				Schemas.CREATED_BY.getLocalCode(),
				Schemas.CREATED_ON.getLocalCode(),
				Schemas.MODIFIED_ON.getLocalCode(),
				StorageSpace.CAPACITY,
				StorageSpace.DECOMMISSIONING_TYPE,
				StorageSpace.PARENT_STORAGE_SPACE,
				StorageSpace.DESCRIPTION);
		transaction.add(
				schemaDisplayStorageSpaceConfig.withFormMetadataCodes(schemaFormStorageSpaceConfig.getFormMetadataCodes()));

		// UNIFORM SUBDIVISION
		SchemaDisplayConfig schemaFormUniformSubDivisionConfig = order(collection, appLayerFactory, "form",
				manager.getSchema(collection, UniformSubdivision.DEFAULT_SCHEMA),
				UniformSubdivision.CODE,
				Schemas.TITLE.getLocalCode(),
				UniformSubdivision.RETENTION_RULE,
				UniformSubdivision.DESCRIPTION);
		SchemaDisplayConfig schemaDisplayUniformSubDivisionConfig = order(collection, appLayerFactory,
				"display",
				manager.getSchema(collection, UniformSubdivision.DEFAULT_SCHEMA),
				UniformSubdivision.CODE,
				Schemas.TITLE.getLocalCode(),
				UniformSubdivision.RETENTION_RULE,
				UniformSubdivision.DESCRIPTION);
		transaction.add(schemaDisplayUniformSubDivisionConfig
				.withFormMetadataCodes(schemaFormUniformSubDivisionConfig.getFormMetadataCodes()));

		manager.execute(transaction);
	}

	private void setupRoles(String collection, ModelLayerFactory modelLayerFactory,
							MigrationResourcesProvider migrationResourcesProvider) {
		RolesManager rolesManager = modelLayerFactory.getRolesManager();

		List<String> userPermissions = new ArrayList<>();
		userPermissions.add(RMPermissionsTo.CREATE_DOCUMENTS);
		userPermissions.add(RMPermissionsTo.CREATE_FOLDERS);
		userPermissions.add(RMPermissionsTo.CREATE_SUB_FOLDERS);

		List<String> managerPermissions = new ArrayList<>();
		managerPermissions.addAll(userPermissions);
		managerPermissions.add(RMPermissionsTo.EDIT_DECOMMISSIONING_LIST);
		managerPermissions.add(RMPermissionsTo.PROCESS_DECOMMISSIONING_LIST);
		managerPermissions.add(RMPermissionsTo.MANAGE_FOLDER_AUTHORIZATIONS);
		managerPermissions.add(RMPermissionsTo.MANAGE_DOCUMENT_AUTHORIZATIONS);
		managerPermissions.add(RMPermissionsTo.MANAGE_CONTAINERS);
		managerPermissions.add(RMPermissionsTo.APPROVE_DECOMMISSIONING_LIST);
		managerPermissions.add(RMPermissionsTo.CREATE_DECOMMISSIONING_LIST);

		List<String> rgdPermissions = new ArrayList<>();
		rgdPermissions.addAll(RMPermissionsTo.PERMISSIONS.getAll());
		rgdPermissions.addAll(CorePermissions.PERMISSIONS.getAll());
		rgdPermissions.add(RMPermissionsTo.CREATE_DECOMMISSIONING_LIST);
		rgdPermissions.add(RMPermissionsTo.PROCESS_DECOMMISSIONING_LIST);

		rolesManager.addRole(new Role(collection, RMRoles.USER,
				migrationResourcesProvider.getValuesOfAllLanguagesWithSeparator("init.roles.U", " / "), userPermissions));
		rolesManager.addRole(new Role(collection, RMRoles.MANAGER,
				migrationResourcesProvider.getValuesOfAllLanguagesWithSeparator("init.roles.M", " / "), managerPermissions));
		rolesManager.addRole(
				new Role(collection, RMRoles.RGD, migrationResourcesProvider.getValuesOfAllLanguagesWithSeparator("init.roles.RGD", " / "), rgdPermissions));
	}
}

class SchemaAlterationFor5_0_1 extends MetadataSchemasAlterationHelper {

	protected SchemaAlterationFor5_0_1(String collection, MigrationResourcesProvider migrationResourcesProvider,
									   AppLayerFactory appLayerFactory) {
		super(collection, migrationResourcesProvider, appLayerFactory);
	}

	public String getVersion() {
		return "5.0.1";
	}

	@Override
	protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
		MetadataSchemaTypeBuilder collectionSchemaType = typesBuilder.getSchemaType(Collection.SCHEMA_TYPE);
		MetadataSchemaTypeBuilder userSchemaType = type(User.SCHEMA_TYPE);

		MetadataSchemaTypeBuilder storageSpaceTypeSchemaType = setupStorageSpaceTypeSchema();
		MetadataSchemaTypeBuilder containerTypeSchemaType = setupContainerTypeSchema();
		MetadataSchemaTypeBuilder folderTypeSchemaType = setupFolderTypeSchema();
		MetadataSchemaTypeBuilder documentTypeSchemaType = setupDocumentTypeSchema();
		MetadataSchemaTypeBuilder mediumTypeSchemaType = setupMediumTypeSchema();

		MetadataSchemaTypeBuilder filingSpaceSchemaType = setupFilingSpaceSchemaType(userSchemaType);
		MetadataSchemaTypeBuilder administrativeUnitSchemaType = setupAdministrativeUnitSchemaType(filingSpaceSchemaType);

		MetadataSchemaTypeBuilder storageSpaceSchemaType = setupStorageSpaceSchemaType(storageSpaceTypeSchemaType);
		MetadataSchemaTypeBuilder containerSchemaType = setupContainerSchemaType(storageSpaceSchemaType,
				containerTypeSchemaType, administrativeUnitSchemaType, userSchemaType, filingSpaceSchemaType);

		MetadataSchemaTypeBuilder retentionRuleSchemaType = setupRetentionRules(administrativeUnitSchemaType,
				documentTypeSchemaType);

		MetadataSchemaTypeBuilder uniformSubdivisionSchemaType = setupUniformSubdivisionSchemaType(retentionRuleSchemaType);

		MetadataSchemaTypeBuilder categorySchemaType = setupCategoriesSchemaType(retentionRuleSchemaType);

		MetadataSchemaTypeBuilder folderSchemaType = setupFolder(categorySchemaType, retentionRuleSchemaType,
				administrativeUnitSchemaType, filingSpaceSchemaType, folderTypeSchemaType, mediumTypeSchemaType,
				containerSchemaType, uniformSubdivisionSchemaType);

		MetadataSchemaTypeBuilder decommissioningListType = setupDecommissioningList(administrativeUnitSchemaType, userSchemaType,
				filingSpaceSchemaType, folderSchemaType, containerSchemaType, categorySchemaType, retentionRuleSchemaType,
				mediumTypeSchemaType);

		MetadataSchemaTypeBuilder documentSchemaType = setupDocument(folderSchemaType, documentTypeSchemaType);

		addRMFieldsToCollectionSchema(collectionSchemaType);

	}

	private void addRMFieldsToCollectionSchema(MetadataSchemaTypeBuilder collectionSchema) {

	}

	private MetadataSchemaTypeBuilder setupMediumTypeSchema() {

		Map<Language, String> mapLanguage = MigrationUtil.getLabelsByLanguage(collection, modelLayerFactory,
				migrationResourcesProvider, "init.ddvMediumType");

		MetadataSchemaTypeBuilder schemaType = new ValueListItemSchemaTypeBuilder(types())
				.createValueListItemSchema(MediumType.SCHEMA_TYPE, mapLanguage,
						ValueListItemSchemaTypeBuilderOptions.codeMetadataDisabled())
				.setSecurity(false);
		MetadataSchemaBuilder defaultSchema = schemaType.getDefaultSchema();
		defaultSchema.create(MediumType.ANALOGICAL).setType(BOOLEAN).setDefaultRequirement(true);

		return schemaType;
	}

	private MetadataSchemaTypeBuilder setupDocumentTypeSchema() {

		Map<Language, String> mapLanguage = MigrationUtil.getLabelsByLanguage(collection, modelLayerFactory,
				migrationResourcesProvider, "init.ddvDocumentType");

		MetadataSchemaTypeBuilder schemaType = new ValueListItemSchemaTypeBuilder(types())
				.createValueListItemSchema(DocumentType.SCHEMA_TYPE, mapLanguage,
						ValueListItemSchemaTypeBuilderOptions.codeMetadataDisabled())
				.setSecurity(false);
		MetadataSchemaBuilder defaultSchema = schemaType.getDefaultSchema();
		defaultSchema.create(DocumentType.LINKED_SCHEMA).setType(STRING);

		return schemaType;
	}

	private MetadataSchemaTypeBuilder setupFolderTypeSchema() {

		Map<Language, String> mapLanguage = MigrationUtil.getLabelsByLanguage(collection, modelLayerFactory,
				migrationResourcesProvider, "init.ddvFolderType");

		MetadataSchemaTypeBuilder schemaType = new ValueListItemSchemaTypeBuilder(types())
				.createValueListItemSchema(FolderType.SCHEMA_TYPE, mapLanguage,
						ValueListItemSchemaTypeBuilderOptions.codeMetadataDisabled())
				.setSecurity(false);
		MetadataSchemaBuilder defaultSchema = schemaType.getDefaultSchema();
		defaultSchema.create(FolderType.LINKED_SCHEMA).setType(STRING);

		return schemaType;
	}

	private MetadataSchemaTypeBuilder setupContainerTypeSchema() {

		Map<Language, String> mapLanguage = MigrationUtil.getLabelsByLanguage(collection, modelLayerFactory,
				migrationResourcesProvider, "init.ddvContainerRecordType");

		MetadataSchemaTypeBuilder schemaType = new ValueListItemSchemaTypeBuilder(types())
				.createValueListItemSchema(ContainerRecordType.SCHEMA_TYPE, mapLanguage,
						ValueListItemSchemaTypeBuilderOptions.codeMetadataDisabled())
				.setSecurity(false);
		MetadataSchemaBuilder defaultSchema = schemaType.getDefaultSchema();
		defaultSchema.create(ContainerRecordType.LINKED_SCHEMA).setType(STRING);

		return schemaType;
	}

	private MetadataSchemaTypeBuilder setupStorageSpaceTypeSchema() {
		Map<Language, String> mapLanguage = MigrationUtil.getLabelsByLanguage(collection, modelLayerFactory,
				migrationResourcesProvider, "init.ddvStorageSpaceType");

		MetadataSchemaTypeBuilder schemaType = new ValueListItemSchemaTypeBuilder(types())
				.createValueListItemSchema(StorageSpaceType.SCHEMA_TYPE, mapLanguage,
						ValueListItemSchemaTypeBuilderOptions.codeMetadataDisabled())
				.setSecurity(false);
		MetadataSchemaBuilder defaultSchema = schemaType.getDefaultSchema();
		defaultSchema.create(StorageSpaceType.LINKED_SCHEMA).setType(STRING);

		return schemaType;
	}

	private MetadataSchemaTypeBuilder setupRetentionRules(MetadataSchemaTypeBuilder administrativeUnitSchemaType,
														  MetadataSchemaTypeBuilder documentType) {
		MetadataSchemaTypeBuilder schemaType = types().createNewSchemaType(RetentionRule.SCHEMA_TYPE).setSecurity(false);
		MetadataSchemaBuilder defaultSchema = schemaType.getDefaultSchema();

		defaultSchema.getMetadata(TITLE_CODE).setSchemaAutocomplete(true);
		defaultSchema.defineValidators().add(RetentionRuleValidator.class);

		defaultSchema.createUndeletable(RetentionRule.CODE).setDefaultRequirement(true).setType(STRING)
				.setSearchable(true).setSchemaAutocomplete(true);

		defaultSchema.createUndeletable(RetentionRule.APPROVED).setType(BOOLEAN);
		defaultSchema.createUndeletable(RetentionRule.APPROVAL_DATE).setType(DATE);

		defaultSchema.createUndeletable(RetentionRule.CORPUS).setType(STRING);

		defaultSchema.createUndeletable(RetentionRule.CORPUS_RULE_NUMBER).setType(TEXT);

		defaultSchema.createUndeletable(RetentionRule.RESPONSIBLE_ADMINISTRATIVE_UNITS).setType(BOOLEAN);

		defaultSchema.createUndeletable(RetentionRule.ADMINISTRATIVE_UNITS).setMultivalue(true)
				.defineReferencesTo(administrativeUnitSchemaType);

		defaultSchema.createUndeletable(RetentionRule.DESCRIPTION).setType(TEXT).setMultiLingual(true);

		defaultSchema.createUndeletable(RetentionRule.JURIDIC_REFERENCE).setType(TEXT).setMultiLingual(true);

		defaultSchema.createUndeletable(RetentionRule.GENERAL_COMMENT).setType(TEXT).setMultiLingual(true);

		defaultSchema.createUndeletable(RetentionRule.KEYWORDS).setType(STRING).setMultivalue(true).setMultiLingual(true);

		defaultSchema.createUndeletable(RetentionRule.HISTORY).setType(TEXT);

		defaultSchema.createUndeletable(RetentionRule.ESSENTIAL_DOCUMENTS).setType(BOOLEAN);
		defaultSchema.createUndeletable(RetentionRule.CONFIDENTIAL_DOCUMENTS).setType(BOOLEAN);

		defaultSchema.createUndeletable(RetentionRule.COPY_RETENTION_RULES).setDefaultRequirement(true).setMultivalue(true)
				.setType(STRUCTURE).defineStructureFactory(CopyRetentionRuleFactory.class);

		defaultSchema.createUndeletable(RetentionRule.DOCUMENT_TYPES_DETAILS).setMultivalue(true)
				.setType(STRUCTURE).defineStructureFactory(RetentionRuleDocumentTypeFactory.class);

		defaultSchema.createUndeletable(RetentionRule.DOCUMENT_TYPES).setType(REFERENCE).setMultivalue(true)
				.defineReferencesTo(documentType)
				.defineDataEntry().asCalculated(RuleDocumentTypesCalculator.class);

		defaultSchema.createUndeletable(RetentionRule.COPY_RULES_COMMENT).setType(TEXT).setMultivalue(true).setMultiLingual(true);
		defaultSchema.get(RetentionRule.TITLE).setMultiLingual(true);

		return schemaType;
	}

	private MetadataSchemaTypeBuilder setupUniformSubdivisionSchemaType(
			MetadataSchemaTypeBuilder retentionRuleSchemaType) {
		MetadataSchemaTypeBuilder schemaType = types().createNewSchemaType(UniformSubdivision.SCHEMA_TYPE).setSecurity(false);
		MetadataSchemaBuilder defaultSchema = schemaType.getDefaultSchema();
		defaultSchema.getMetadata(TITLE_CODE).setSchemaAutocomplete(true);
		defaultSchema.createUndeletable(UniformSubdivision.CODE).setType(STRING).setDefaultRequirement(true).setSearchable(true);
		defaultSchema.createUndeletable(UniformSubdivision.DESCRIPTION).setType(STRING).setSearchable(true).setMultiLingual(true);
		defaultSchema.createUndeletable(UniformSubdivision.COMMENTS).setMultivalue(true)
				.setType(MetadataValueType.STRUCTURE).defineStructureFactory(CommentFactory.class);
		defaultSchema.createUndeletable(UniformSubdivision.RETENTION_RULE).setMultivalue(true)
				.defineReferencesTo(retentionRuleSchemaType);
		defaultSchema.get(UniformSubdivision.TITLE).setMultiLingual(true);

		return schemaType;
	}

	private MetadataSchemaTypeBuilder setupAdministrativeUnitSchemaType(
			MetadataSchemaTypeBuilder filingSpaceSchemaType) {
		MetadataSchemaTypeBuilder schemaType = types().createNewSchemaType(AdministrativeUnit.SCHEMA_TYPE).setSecurity(false);
		MetadataSchemaBuilder defaultSchema = schemaType.getDefaultSchema();

		defaultSchema.createUndeletable(AdministrativeUnit.CODE).setType(STRING).setDefaultRequirement(true).setSearchable(
				true).setSchemaAutocomplete(true);
		defaultSchema.getMetadata(TITLE_CODE).setSchemaAutocomplete(true).setMultiLingual(true);
		defaultSchema.createUndeletable(AdministrativeUnit.DESCRIPTION).setType(STRING).setSearchable(true).setMultiLingual(true);
		defaultSchema.createUndeletable(AdministrativeUnit.PARENT).defineChildOfRelationshipToType(schemaType);
		defaultSchema.createUndeletable(AdministrativeUnit.FILING_SPACES).setMultivalue(true)
				.defineReferencesTo(filingSpaceSchemaType);

		defaultSchema.createUndeletable(AdministrativeUnit.ADRESS).setType(STRING);

		copy(filingSpaceSchemaType, schemaType, AdministrativeUnit.FILING_SPACES, FilingSpace.USERS,
				AdministrativeUnit.FILING_SPACES_USERS);

		copy(filingSpaceSchemaType, schemaType, AdministrativeUnit.FILING_SPACES, FilingSpace.ADMINISTRATORS,
				AdministrativeUnit.FILING_SPACES_ADMINISTRATORS);

		return schemaType;
	}

	private MetadataSchemaTypeBuilder setupFilingSpaceSchemaType(MetadataSchemaTypeBuilder userSchemaType) {
		MetadataSchemaTypeBuilder schemaType = types().createNewSchemaType(FilingSpace.SCHEMA_TYPE).setSecurity(false);
		MetadataSchemaBuilder defaultSchema = schemaType.getDefaultSchema();

		defaultSchema.getMetadata(TITLE_CODE).setSchemaAutocomplete(true);
		defaultSchema.createUndeletable(FilingSpace.CODE).setType(STRING).setDefaultRequirement(true).setSearchable(
				true).setSchemaAutocomplete(true);
		defaultSchema.createUndeletable(FilingSpace.DESCRIPTION).setType(STRING).setSearchable(true);
		defaultSchema.createUndeletable(FilingSpace.USERS).setMultivalue(true).defineReferencesTo(userSchemaType);
		defaultSchema.createUndeletable(FilingSpace.ADMINISTRATORS).setMultivalue(true).defineReferencesTo(userSchemaType);

		return schemaType;
	}

	private MetadataSchemaTypeBuilder setupContainerSchemaType(MetadataSchemaTypeBuilder storageSpaceSchemaType,
															   MetadataSchemaTypeBuilder containerTypeSchemaType,
															   MetadataSchemaTypeBuilder administrativeUnitSchemaType,
															   MetadataSchemaTypeBuilder userSchemaType,
															   MetadataSchemaTypeBuilder filingSpaceSchemaType) {
		MetadataSchemaTypeBuilder schemaType = types().createNewSchemaType(ContainerRecord.SCHEMA_TYPE).setSecurity(true);
		MetadataSchemaBuilder defaultSchema = schemaType.getDefaultSchema();

		defaultSchema.getMetadata(TITLE_CODE).setSchemaAutocomplete(true);
		defaultSchema.getMetadata(Schemas.TITLE.getLocalCode()).defineDataEntry().asCalculated(ContainerTitleCalculator.class)
				.setDefaultRequirement(true);
		defaultSchema.createUndeletable(ContainerRecord.ADMINISTRATIVE_UNIT).defineTaxonomyRelationshipToType(
				administrativeUnitSchemaType);
		defaultSchema.createUndeletable(ContainerRecord.BORROW_DATE).setType(DATE);
		defaultSchema.createUndeletable(ContainerRecord.BORROWER).defineReferencesTo(userSchemaType);
		defaultSchema.createUndeletable(ContainerRecord.COMPLETION_DATE).setType(DATE);
		defaultSchema.createUndeletable(ContainerRecord.DECOMMISSIONING_TYPE).defineAsEnum(DecommissioningType.class);
		defaultSchema.createUndeletable(ContainerRecord.DESCRIPTION).setType(STRING).setSearchable(true);
		defaultSchema.createUndeletable(ContainerRecord.FILING_SPACE).defineReferencesTo(filingSpaceSchemaType);
		defaultSchema.createUndeletable(ContainerRecord.FULL).setType(BOOLEAN);
		defaultSchema.createUndeletable(ContainerRecord.IDENTIFIER).setType(STRING).setSearchable(true);
		defaultSchema.createUndeletable(ContainerRecord.PLANIFIED_RETURN_DATE).setType(DATE);
		defaultSchema.createUndeletable(ContainerRecord.REAL_DEPOSIT_DATE).setType(DATE);
		defaultSchema.createUndeletable(ContainerRecord.REAL_RETURN_DATE).setType(DATE);
		defaultSchema.createUndeletable(ContainerRecord.REAL_TRANSFER_DATE).setType(DATE);
		defaultSchema.createUndeletable(ContainerRecord.STORAGE_SPACE).defineTaxonomyRelationshipToType(storageSpaceSchemaType);
		defaultSchema.createUndeletable(ContainerRecord.TEMPORARY_IDENTIFIER).setType(STRING)
				.setDefaultRequirement(true).setSearchable(true);
		defaultSchema.createUndeletable(ContainerRecord.TYPE).defineReferencesTo(containerTypeSchemaType)
				.setDefaultRequirement(true);
		defaultSchema.createUndeletable(ContainerRecord.POSITION).setType(STRING).setSearchable(true);
		defaultSchema.createUndeletable(ContainerRecord.COMMENTS).setMultivalue(true)
				.setType(MetadataValueType.STRUCTURE).defineStructureFactory(CommentFactory.class);

		return schemaType;
	}

	private MetadataSchemaTypeBuilder setupStorageSpaceSchemaType(MetadataSchemaTypeBuilder storageSpaceTypeSchema) {
		MetadataSchemaTypeBuilder schemaType = types().createNewSchemaType(StorageSpace.SCHEMA_TYPE).setSecurity(false);
		MetadataSchemaBuilder defaultSchema = schemaType.getDefaultSchema();

		defaultSchema.getMetadata(TITLE_CODE).setSchemaAutocomplete(true);
		defaultSchema.createUndeletable(StorageSpace.CAPACITY).setType(NUMBER);
		defaultSchema.createUndeletable(StorageSpace.CODE).setType(STRING).setDefaultRequirement(true).setSearchable(
				true).setSchemaAutocomplete(true);
		defaultSchema.createUndeletable(StorageSpace.DECOMMISSIONING_TYPE).defineAsEnum(DecommissioningType.class);
		defaultSchema.createUndeletable(StorageSpace.DESCRIPTION).setType(STRING).setSearchable(true);
		defaultSchema.createUndeletable(StorageSpace.PARENT_STORAGE_SPACE).defineChildOfRelationshipToType(schemaType);
		defaultSchema.createUndeletable(StorageSpace.TYPE).defineReferencesTo(storageSpaceTypeSchema);
		defaultSchema.createUndeletable(StorageSpace.COMMENTS).setMultivalue(true)
				.setType(MetadataValueType.STRUCTURE).defineStructureFactory(CommentFactory.class);

		return schemaType;
	}

	private MetadataSchemaTypeBuilder setupCategoriesSchemaType(MetadataSchemaTypeBuilder retentionRuleSchemaType) {
		MetadataSchemaTypeBuilder schemaType = types().createNewSchemaType(Category.SCHEMA_TYPE).setSecurity(false);
		MetadataSchemaBuilder defaultSchema = schemaType.getDefaultSchema();

		defaultSchema.getMetadata(TITLE_CODE).setSchemaAutocomplete(true);
		defaultSchema.createUndeletable(Category.CODE).setType(STRING).setDefaultRequirement(true).setSearchable(
				true).setSchemaAutocomplete(true);
		defaultSchema.createUndeletable(Category.DESCRIPTION).setType(STRING).setSearchable(true).setMultiLingual(true);
		defaultSchema.createUndeletable(Category.PARENT).defineChildOfRelationshipToType(schemaType);
		defaultSchema.createUndeletable(Category.KEYWORDS).setType(STRING).setMultivalue(true).setSearchable(true)
				.setSchemaAutocomplete(true).setMultiLingual(true);
		defaultSchema.createUndeletable(Category.COMMENTS).setMultivalue(true)
				.setType(MetadataValueType.STRUCTURE).defineStructureFactory(CommentFactory.class);
		defaultSchema.createUndeletable(Category.RETENTION_RULES).setDefaultRequirement(false).setMultivalue(true)
				.defineReferencesTo(retentionRuleSchemaType);
		defaultSchema.createUndeletable(Schemas.LINKABLE.getLocalCode()).setType(BOOLEAN).defineDataEntry().asCalculated(
				CategoryIsLinkableCalculator.class);
		defaultSchema.get(Category.TITLE).setMultiLingual(true);

		return schemaType;
	}

	private MetadataSchemaTypeBuilder setupFolder(MetadataSchemaTypeBuilder categorySchemaType,
												  MetadataSchemaTypeBuilder retentionRuleSchemaType,
												  MetadataSchemaTypeBuilder administrativeUnitSchemaType,
												  MetadataSchemaTypeBuilder filingSpaceSchemaType,
												  MetadataSchemaTypeBuilder folderTypeSchemaType,
												  MetadataSchemaTypeBuilder mediumTypeSchemaType,
												  MetadataSchemaTypeBuilder containerSchemaType,
												  MetadataSchemaTypeBuilder uniformSubdivisionSchemaType) {
		MetadataSchemaTypeBuilder schemaType = types().createNewSchemaType(Folder.SCHEMA_TYPE).setSecurity(true);
		MetadataSchemaBuilder defaultSchema = schemaType.getDefaultSchema();

		defaultSchema.get(Schemas.TITLE_CODE).setDefaultRequirement(true);

		defaultSchema.createUndeletable(Folder.PARENT_FOLDER)
				.defineChildOfRelationshipToType(schemaType);

		defaultSchema.createUndeletable(Folder.ADMINISTRATIVE_UNIT_ENTERED)
				.defineTaxonomyRelationshipToType(administrativeUnitSchemaType);

		defaultSchema.createUndeletable(Folder.ADMINISTRATIVE_UNIT)
				.defineReferencesTo(administrativeUnitSchemaType)
				.defineDataEntry().asCalculated(FolderAppliedAdministrativeUnitCalculator.class)
				.setDefaultRequirement(true);

		defaultSchema.createUndeletable(Folder.FILING_SPACE_ENTERED)
				.defineReferencesTo(filingSpaceSchemaType);

		defaultSchema.createUndeletable(Folder.FILING_SPACE)
				.defineReferencesTo(filingSpaceSchemaType)
				.defineDataEntry().asCalculated(FolderAppliedFilingSpaceCalculator.class)
				.setDefaultRequirement(true);

		defaultSchema.createUndeletable(Folder.CATEGORY_ENTERED)
				.defineTaxonomyRelationshipToType(categorySchemaType);

		defaultSchema.createUndeletable(Folder.CATEGORY)
				.defineReferencesTo(categorySchemaType)
				.defineDataEntry().asCalculated(FolderApplicableCategoryCalculator.class)
				.setDefaultRequirement(true);

		defaultSchema.createUndeletable(Folder.MAIN_COPY_RULE)
				.setType(MetadataValueType.STRUCTURE).defineStructureFactory(CopyRetentionRuleFactory.class)
				.defineDataEntry().asCalculated(FolderMainCopyRuleCalculator.class);

		defaultSchema.createUndeletable(Folder.APPLICABLE_COPY_RULES).setMultivalue(true)
				.setType(MetadataValueType.STRUCTURE).defineStructureFactory(CopyRetentionRuleFactory.class)
				.defineDataEntry().asCalculated(FolderApplicableCopyRuleCalculator.class);

		defaultSchema.createUndeletable(Folder.UNIFORM_SUBDIVISION_ENTERED)
				.defineReferencesTo(uniformSubdivisionSchemaType);

		defaultSchema.createUndeletable(Folder.UNIFORM_SUBDIVISION)
				.defineReferencesTo(uniformSubdivisionSchemaType)
				.defineDataEntry().asCalculated(FolderAppliedUniformSubdivisionCalculator.class);

		defaultSchema.createUndeletable(Folder.DESCRIPTION).setType(TEXT).setSearchable(true);

		defaultSchema.createUndeletable(Folder.KEYWORDS).setType(STRING).setMultivalue(true).setSearchable(true);

		defaultSchema.createUndeletable(Folder.TYPE).defineReferencesTo(folderTypeSchemaType);

		defaultSchema.createUndeletable(Folder.MEDIUM_TYPES).defineReferencesTo(mediumTypeSchemaType).setMultivalue(true);

		defaultSchema.createUndeletable(Folder.OPENING_DATE).setType(DATE).setDefaultRequirement(true);

		defaultSchema.createUndeletable(Folder.CLOSING_DATE).setType(DATE)
				.defineDataEntry().asCalculated(FolderClosingDateCalculator.class);

		defaultSchema.createUndeletable(Folder.ENTERED_CLOSING_DATE).setType(DATE);

		defaultSchema.createUndeletable(Folder.ACTUAL_TRANSFER_DATE).setType(DATE);

		defaultSchema.createUndeletable(Folder.ACTUAL_DEPOSIT_DATE).setType(DATE);

		defaultSchema.createUndeletable(Folder.ACTUAL_DESTRUCTION_DATE).setType(DATE);

		defaultSchema.createUndeletable(Folder.COPY_RULES_EXPECTED_TRANSFER_DATES).setType(DATE).setMultivalue(true)
				.defineDataEntry().asCalculated(FolderCopyRulesExpectedTransferDatesCalculator.class);

		defaultSchema.createUndeletable(Folder.COPY_RULES_EXPECTED_DEPOSIT_DATES).setType(DATE).setMultivalue(true)
				.defineDataEntry().asCalculated(FolderCopyRulesExpectedDepositDatesCalculator.class);

		defaultSchema.createUndeletable(Folder.COPY_RULES_EXPECTED_DESTRUCTION_DATES).setType(DATE).setMultivalue(true)
				.defineDataEntry().asCalculated(FolderCopyRulesExpectedDestructionDatesCalculator.class);

		defaultSchema.createUndeletable(Folder.EXPECTED_TRANSFER_DATE).setType(DATE)
				.defineDataEntry().asCalculated(FolderExpectedTransferDateCalculator.class);

		defaultSchema.createUndeletable(Folder.EXPECTED_DEPOSIT_DATE).setType(DATE)
				.defineDataEntry().asCalculated(FolderExpectedDepositDateCalculator.class);

		defaultSchema.createUndeletable(Folder.EXPECTED_DESTRUCTION_DATE).setType(DATE)
				.defineDataEntry().asCalculated(FolderExpectedDestructionDateCalculator.class);

		defaultSchema.createUndeletable(Folder.RETENTION_RULE_ENTERED)
				.defineReferencesTo(retentionRuleSchemaType);

		defaultSchema.createUndeletable(Folder.RETENTION_RULE)
				.setDefaultRequirement(true)
				.defineReferencesTo(retentionRuleSchemaType)
				.defineDataEntry().asCalculated(FolderAppliedRetentionRuleCalculator.class);

		defaultSchema.createUndeletable(Folder.ARCHIVISTIC_STATUS).defineAsEnum(FolderStatus.class)
				.defineDataEntry().asCalculated(FolderStatusCalculator.class);

		defaultSchema.createUndeletable(Folder.COPY_STATUS).defineAsEnum(CopyType.class)
				.defineDataEntry().asCalculated(FolderCopyStatusCalculator.class).setDefaultRequirement(true);

		defaultSchema.createUndeletable(Folder.COPY_STATUS_ENTERED).defineAsEnum(CopyType.class);

		defaultSchema.createUndeletable(Folder.ACTIVE_RETENTION_TYPE).defineAsEnum(RetentionType.class)
				.defineDataEntry().asCalculated(FolderActiveRetentionTypeCalculator.class);

		defaultSchema.createUndeletable(Folder.SEMIACTIVE_RETENTION_TYPE).defineAsEnum(RetentionType.class)
				.defineDataEntry().asCalculated(FolderSemiActiveRetentionTypeCalculator.class);

		defaultSchema.createUndeletable(Folder.INACTIVE_DISPOSAL_TYPE).defineAsEnum(DisposalType.class)
				.defineDataEntry().asCalculated(FolderInactiveDisposalTypeCalculator.class);

		defaultSchema.createUndeletable(Folder.MEDIA_TYPE).defineAsEnum(FolderMediaType.class)
				.defineDataEntry().asCalculated(FolderMediaTypesCalculator.class);

		defaultSchema.createUndeletable(Folder.CONTAINER).defineReferencesTo(containerSchemaType);

		defaultSchema.createUndeletable(Folder.COMMENTS).setMultivalue(true)
				.setType(MetadataValueType.STRUCTURE).defineStructureFactory(CommentFactory.class);

		copy(categorySchemaType, schemaType, Folder.CATEGORY, Category.CODE, Folder.CATEGORY_CODE);
		copy(retentionRuleSchemaType, schemaType, Folder.RETENTION_RULE, RetentionRule.ADMINISTRATIVE_UNITS,
				Folder.RETENTION_RULE_ADMINISTRATIVE_UNITS);
		copy(filingSpaceSchemaType, schemaType, Folder.FILING_SPACE, FilingSpace.CODE, Folder.FILING_SPACE_CODE);

		return schemaType;
	}

	private MetadataSchemaTypeBuilder setupDecommissioningList(MetadataSchemaTypeBuilder administrativeUnitSchemaType,
															   MetadataSchemaTypeBuilder userSchemaType,
															   MetadataSchemaTypeBuilder filingSpaceSchemaType,
															   MetadataSchemaTypeBuilder folderSchemaType,
															   MetadataSchemaTypeBuilder containerSchemaType,
															   MetadataSchemaTypeBuilder categorySchemaType,
															   MetadataSchemaTypeBuilder retentionRuleSchemaType,
															   MetadataSchemaTypeBuilder mediumTypeSchemaType) {
		MetadataSchemaTypeBuilder schemaType = types().createNewSchemaType(DecommissioningList.SCHEMA_TYPE).setSecurity(false);
		MetadataSchemaBuilder defaultSchema = schemaType.getDefaultSchema();

		defaultSchema.getMetadata(Schemas.TITLE_CODE).setDefaultRequirement(true);

		defaultSchema.createUndeletable(DecommissioningList.DESCRIPTION).setType(TEXT).setSearchable(true);

		defaultSchema.createUndeletable(DecommissioningList.FILING_SPACE).setType(REFERENCE).defineReferencesTo(
				filingSpaceSchemaType);

		defaultSchema.createUndeletable(DecommissioningList.ADMINISTRATIVE_UNIT).defineReferencesTo(administrativeUnitSchemaType);

		defaultSchema.createSystemReserved(DecommissioningList.TYPE).defineAsEnum(DecommissioningListType.class)
				.setSearchable(true);

		defaultSchema.createUndeletable(DecommissioningList.VALIDATION_DATE).setType(DATE);

		defaultSchema.createUndeletable(DecommissioningList.VALIDATION_USER).setType(REFERENCE)
				.defineReferencesTo(userSchemaType);

		defaultSchema.createUndeletable(DecommissioningList.APPROVAL_REQUEST_DATE).setType(DATE);

		defaultSchema.createUndeletable(DecommissioningList.APPROVAL_REQUEST).setType(REFERENCE)
				.defineReferencesTo(userSchemaType);

		defaultSchema.createUndeletable(DecommissioningList.APPROVAL_DATE).setType(DATE);

		defaultSchema.createUndeletable(DecommissioningList.APPROVAL_USER).setType(REFERENCE)
				.defineReferencesTo(userSchemaType);

		defaultSchema.createUndeletable(DecommissioningList.PROCESSING_DATE).setType(DATE);

		defaultSchema.createUndeletable(DecommissioningList.PROCESSING_USER).setType(REFERENCE)
				.defineReferencesTo(userSchemaType);

		defaultSchema.createUndeletable(DecommissioningList.FOLDER_DETAILS).setMultivalue(true)
				.setType(STRUCTURE).defineStructureFactory(DecomListFolderDetailFactory.class);

		defaultSchema.createUndeletable("folders").setMultivalue(true)
				.defineReferencesTo(folderSchemaType)
				.defineDataEntry().asCalculated(DecomListFoldersCalculator.class);

		defaultSchema.createUndeletable(DecommissioningList.CONTAINER_DETAILS).setMultivalue(true)
				.setType(STRUCTURE).defineStructureFactory(DecomListContainerDetailFactory.class);

		defaultSchema.createUndeletable(DecommissioningList.CONTAINERS).setType(REFERENCE)
				.defineReferencesTo(containerSchemaType).setMultivalue(true)
				.defineDataEntry().asCalculated(DecomListContainersCalculator.class);

		copy(folderSchemaType, schemaType, "folders", Folder.MEDIA_TYPE,
				DecommissioningList.FOLDERS_MEDIA_TYPES);

		defaultSchema.createUndeletable(DecommissioningList.ANALOGICAL_MEDIUM).setType(BOOLEAN)
				.defineDataEntry().asCalculated(DecomListHasAnalogicalMediumTypesCalculator.class);

		defaultSchema.createUndeletable(DecommissioningList.ELECTRONIC_MEDIUM).setType(BOOLEAN)
				.defineDataEntry().asCalculated(DecomListHasElectronicMediumTypesCalculator.class);

		defaultSchema.createUndeletable(DecommissioningList.UNIFORM).setType(BOOLEAN)
				.defineDataEntry().asCalculated(DecomListIsUniform.class);

		defaultSchema.createUndeletable(DecommissioningList.STATUS).defineAsEnum(DecomListStatus.class)
				.defineDataEntry().asCalculated(DecomListStatusCalculator.class);

		defaultSchema.createUndeletable(DecommissioningList.UNIFORM_CATEGORY)
				.setType(REFERENCE).defineReferencesTo(categorySchemaType)
				.defineDataEntry().asCalculated(DecomListUniformCategoryCalculator.class);

		defaultSchema.createUndeletable(DecommissioningList.UNIFORM_COPY_RULE)
				.setType(STRUCTURE).defineStructureFactory(CopyRetentionRuleFactory.class)
				.defineDataEntry().asCalculated(DecomListUniformCopyRuleCalculator.class);

		defaultSchema.createUndeletable(DecommissioningList.UNIFORM_COPY_TYPE).defineAsEnum(CopyType.class)
				.defineDataEntry().asCalculated(DecomListUniformCopyTypeCalculator.class);

		defaultSchema.createUndeletable(DecommissioningList.UNIFORM_RULE)
				.setType(REFERENCE).defineReferencesTo(retentionRuleSchemaType)
				.defineDataEntry().asCalculated(DecomListUniformRuleCalculator.class);

		defaultSchema.createSystemReserved(DecommissioningList.ORIGIN_ARCHIVISTIC_STATUS).defineAsEnum(OriginStatus.class);

		defaultSchema.createUndeletable(DecommissioningList.COMMENTS).setMultivalue(true)
				.setType(MetadataValueType.ENUM).defineStructureFactory(CommentFactory.class);

		return schemaType;
	}

	private MetadataSchemaTypeBuilder setupDocument(MetadataSchemaTypeBuilder folderSchemaType,
													MetadataSchemaTypeBuilder documentTypeSchema) {
		MetadataSchemaTypeBuilder schemaType = types().createNewSchemaType(Document.SCHEMA_TYPE).setSecurity(true);
		MetadataSchemaBuilder defaultSchema = schemaType.getDefaultSchema();

		defaultSchema.getMetadata(Schemas.TITLE_CODE).setDefaultRequirement(true);
		defaultSchema.createUndeletable(Document.FOLDER)
				.defineChildOfRelationshipToType(folderSchemaType).setDefaultRequirement(true);
		defaultSchema.createUndeletable(Document.CONTENT).setType(CONTENT).setSearchable(true);
		defaultSchema.createUndeletable(Document.KEYWORDS).setType(STRING).setMultivalue(true).setSearchable(true);
		defaultSchema.createUndeletable(Document.DESCRIPTION).setType(TEXT).setSearchable(true);
		defaultSchema.createUndeletable(Document.TYPE).defineReferencesTo(documentTypeSchema);
		defaultSchema.createUndeletable(Document.COMMENTS).setMultivalue(true).setType(MetadataValueType.STRUCTURE)
				.defineStructureFactory(CommentFactory.class);

		String ref = Document.FOLDER;
		copy(folderSchemaType, schemaType, ref, Folder.CATEGORY, Document.FOLDER_CATEGORY);
		copy(folderSchemaType, schemaType, ref, Folder.ADMINISTRATIVE_UNIT, Document.FOLDER_ADMINISTRATIVE_UNIT);
		copy(folderSchemaType, schemaType, ref, Folder.FILING_SPACE, Document.FOLDER_FILING_SPACE);
		copy(folderSchemaType, schemaType, ref, Folder.RETENTION_RULE, Document.FOLDER_RETENTION_RULE);
		copy(folderSchemaType, schemaType, ref, Folder.ARCHIVISTIC_STATUS, Document.FOLDER_ARCHIVISTIC_STATUS);
		copy(folderSchemaType, schemaType, ref, Folder.ACTUAL_DEPOSIT_DATE, Document.FOLDER_ACTUAL_DEPOSIT_DATE);
		copy(folderSchemaType, schemaType, ref, Folder.ACTUAL_DESTRUCTION_DATE, Document.FOLDER_ACTUAL_DESTRUCTION_DATE);
		copy(folderSchemaType, schemaType, ref, Folder.ACTUAL_TRANSFER_DATE, Document.FOLDER_ACTUAL_TRANSFER_DATE);
		copy(folderSchemaType, schemaType, ref, Folder.EXPECTED_DEPOSIT_DATE, Document.FOLDER_EXPECTED_DEPOSIT_DATE);
		copy(folderSchemaType, schemaType, ref, Folder.EXPECTED_DESTRUCTION_DATE, Document.FOLDER_EXPECTED_DESTRUCTION_DATE);
		copy(folderSchemaType, schemaType, ref, Folder.EXPECTED_TRANSFER_DATE, Document.FOLDER_EXPECTED_TRANSFER_DATE);
		copy(folderSchemaType, schemaType, ref, Folder.OPENING_DATE, Document.FOLDER_OPENING_DATE);
		copy(folderSchemaType, schemaType, ref, Folder.CLOSING_DATE, Document.FOLDER_CLOSING_DATE);

		return schemaType;
	}

	private MetadataBuilder copy(MetadataSchemaTypeBuilder sourceSchemaType,
								 MetadataSchemaTypeBuilder destinationSchemaType,

								 String referenceLocalCode, String sourceMetadataLocalCode,
								 String destinationMetadataLocalCode) {

		MetadataSchemaBuilder sourceDefaultSchema = sourceSchemaType.getDefaultSchema();
		MetadataSchemaBuilder destinationDefaultSchema = destinationSchemaType.getDefaultSchema();

		MetadataBuilder refMetadata = destinationDefaultSchema.getMetadata(referenceLocalCode);
		MetadataBuilder sourceMetadata = sourceDefaultSchema.getMetadata(sourceMetadataLocalCode);
		MetadataBuilder destinationMetadata = destinationDefaultSchema.createUndeletable(destinationMetadataLocalCode);

		destinationMetadata.setMultivalue(sourceMetadata.isMultivalue() || refMetadata.isMultivalue());
		destinationMetadata.setType(sourceMetadata.getType());
		destinationMetadata.defineDataEntry().asCopied(refMetadata, sourceMetadata);

		if (sourceMetadata.getType() == MetadataValueType.REFERENCE) {
			String referenceSchemaType = sourceMetadata.getAllowedReferencesBuilder().getSchemaType();
			destinationMetadata.defineReferences().setCompleteSchemaTypeCode(referenceSchemaType);
		} else if (sourceMetadata.getType() == MetadataValueType.ENUM) {
			destinationMetadata.defineAsEnum(sourceMetadata.getEnumClass());
		}

		return destinationMetadata;
	}
}
