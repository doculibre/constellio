package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.schemasDisplay.SchemaDisplayConfig;
import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.constants.RMRoles;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.*;
import com.constellio.app.modules.rm.wrappers.type.DocumentType;
import com.constellio.app.modules.tasks.TaskModule;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.records.wrappers.UserDocument;
import com.constellio.model.entities.records.wrappers.UserFolder;
import com.constellio.model.entities.schemas.*;
import com.constellio.model.entities.security.Role;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.SDKFoldersLocator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.constellio.model.entities.schemas.MetadataValueType.ENUM;
import static com.constellio.model.entities.security.global.UserCredential.AGENT_STATUS;
import static com.constellio.sdk.tests.TestUtils.noDuplicates;
import static org.assertj.core.api.Assertions.assertThat;

public class RMMigrationsAcceptanceTest extends ConstellioTest {

	RMSchemasRecordsServices rm;

	protected void validateZeCollectionState()
			throws Exception {
		if (getModelLayerFactory().getCollectionsListManager().getCollections().contains(zeCollection)) {
			MetadataSchemaTypes metadataSchemaTypes = getModelLayerFactory().getMetadataSchemasManager()
					.getSchemaTypes(zeCollection);
			if (metadataSchemaTypes.hasType(RetentionRule.SCHEMA_TYPE)) {
				whenMigratingToCurrentVersionThenValidSchemas();
				whenMigratingToCurrentVersionThenValidSystemCollectionSchemas();
				whenMigratingToCurrentVersionThenSchemasDisplayedCorrectly();
				whenMigratingToCurrentVersionThenHasValueListWithDefaultItems();
				whenMigratingToCurrentVersionThenHasEssentialMetadatas();
				whenMigratingToCurrentVersionThenHasRolesWithRightPermissions();
				whenMigratingToCurrentVersionThenConfigHasValidDefaultValue();
				whenMigratingToCurrentVersionThenTaskModuleIsEnabledAndExtraRMMetadatasAreCreated();
				whenMigratingToCurrentVersionThenMarkedForReindexingIfVersionIsBefore5_1_3();
				whenMigratingToCurrentVersionThenOnlyEnabledNonSystemReservedManuallyValuedMetadataAreDuplicable(
						metadataSchemaTypes);
				whenMigratingToCurrentVersionThenEmailDocumentTypeIsNotLogicallyDeleted();
				whenMigratingToCurrentVersionThenAllSchemaTypeHasNewCommonMetadatas(metadataSchemaTypes);
				whenMigratingToCurrentVersionThenValidateUserFolderWasAdded();
				whenMigrationToCurrentVersionThenTaxonomyValidateTitle();

				getModelLayerFactory().getBatchProcessesManager().waitUntilAllFinished();
				getModelLayerFactory().getRecordMigrationsManager().checkScriptsToFinish();

				validateSystemAfterRecordsMigrations();
			}
		}
	}

	private void whenMigrationToCurrentVersionThenTaxonomyValidateTitle() {
		Taxonomy taxonomy = getModelLayerFactory().getTaxonomiesManager().getTaxonomyFor(zeCollection, AdministrativeUnit.SCHEMA_TYPE);

		String frenchLanguage = taxonomy.getTitle(Language.French);
		assertThat(frenchLanguage).isEqualTo("Unités administratives");
	}

	private void validateSystemAfterRecordsMigrations() {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		assertThat(rm.containerRecord.schema().hasMetadataWithCode("administrativeUnit")).isFalse();
	}

	private void whenMigratingToCurrentVersionThenAllSchemaTypeHasNewCommonMetadatas(
			final MetadataSchemaTypes metadataSchemaTypes) {

		for (MetadataSchemaType type : metadataSchemaTypes.getSchemaTypes()) {
			assertThat(type.getDefaultSchema().get(Schemas.ALL_REFERENCES.getLocalCode())).isNotNull();
			assertThat(type.getDefaultSchema().get(Schemas.MARKED_FOR_REINDEXING.getLocalCode())).isNotNull();
		}

	}

	private void whenMigratingToCurrentVersionThenEmailDocumentTypeIsNotLogicallyDeleted() {
		DocumentType documentType = rm.emailDocumentType();
		assertThat(documentType.isLogicallyDeletedStatus()).isFalse();
	}

	private void whenMigratingToCurrentVersionThenOnlyEnabledNonSystemReservedManuallyValuedMetadataAreDuplicable(
			final MetadataSchemaTypes metadataSchemaTypes) {
		final MetadataSchema folderSchema = metadataSchemaTypes.getSchema(Folder.DEFAULT_SCHEMA);
		assertThat(folderSchema.getMetadata(Folder.TITLE).isDuplicable()).isTrue();
		assertThat(folderSchema.getMetadata(Folder.TYPE).isDuplicable()).isTrue();
		assertThat(folderSchema.getMetadata(Schemas.IDENTIFIER.getCode()).isDuplicable()).isFalse();
		assertThat(folderSchema.getMetadata(Schemas.PATH.getCode()).isDuplicable()).isFalse();

		final MetadataSchema documentSchema = metadataSchemaTypes.getSchema(Document.DEFAULT_SCHEMA);
		assertThat(documentSchema.getMetadata(Folder.TITLE).isDuplicable()).isTrue();
		assertThat(documentSchema.getMetadata(Folder.TYPE).isDuplicable()).isTrue();
		assertThat(documentSchema.getMetadata(Schemas.IDENTIFIER.getCode()).isDuplicable()).isFalse();
		assertThat(documentSchema.getMetadata(Schemas.PATH.getCode()).isDuplicable()).isFalse();
	}

	private void whenMigratingToCurrentVersionThenMarkedForReindexingIfVersionIsBefore5_1_3() {
		boolean requireReindexing = version != null && (
				version.startsWith("5.0") || version.equals("5.1") || version.startsWith("5.1.1") || version.startsWith("5.1.2"));

		if (requireReindexing) {
			assertThat(getAppLayerFactory().getSystemGlobalConfigsManager().isReindexingRequired()).isEqualTo(true);
		}
	}

	private void whenMigratingToCurrentVersionThenTaskModuleIsEnabledAndExtraRMMetadatasAreCreated() {
		assertThat(getAppLayerFactory().getModulesManager().isModuleEnabled(zeCollection, new TaskModule())).isTrue();

		MetadataSchemaTypes metadataSchemaTypes = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection);
		MetadataSchema taskSchema = metadataSchemaTypes.getSchemaType(Task.SCHEMA_TYPE).getDefaultSchema();

		Metadata adminUnitMetadata = taskSchema.getMetadata(RMTask.ADMINISTRATIVE_UNIT);
		Metadata linkedFoldersMetadata = taskSchema.getMetadata(RMTask.LINKED_FOLDERS);
		Metadata linkedDocumentsMetadata = taskSchema.getMetadata(RMTask.LINKED_DOCUMENTS);

		assertThat(adminUnitMetadata.getType()).isEqualTo(MetadataValueType.REFERENCE);
		assertThat(adminUnitMetadata.isMultivalue()).isFalse();
		assertThat(adminUnitMetadata.getAllowedReferences().getAllowedSchemaType()).isEqualTo(AdministrativeUnit.SCHEMA_TYPE);

		assertThat(linkedFoldersMetadata.getType()).isEqualTo(MetadataValueType.REFERENCE);
		assertThat(linkedFoldersMetadata.isMultivalue()).isTrue();
		assertThat(linkedFoldersMetadata.getAllowedReferences().getAllowedSchemaType()).isEqualTo(Folder.SCHEMA_TYPE);

		assertThat(linkedDocumentsMetadata.getType()).isEqualTo(MetadataValueType.REFERENCE);
		assertThat(linkedDocumentsMetadata.isMultivalue()).isTrue();
		assertThat(linkedDocumentsMetadata.getAllowedReferences().getAllowedSchemaType()).isEqualTo(Document.SCHEMA_TYPE);
	}

	private void whenMigratingToCurrentVersionThenValidSchemas()
			throws Exception {

		MetadataSchemaTypes metadataSchemaTypes = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection);

		if (testCase.contains("rm") && !testCase.contains("es")) {
			assertThat(allSchemaTypesWithSecurity()).containsOnly(Folder.SCHEMA_TYPE, Document.SCHEMA_TYPE, Task.SCHEMA_TYPE,
					AdministrativeUnit.SCHEMA_TYPE);
		} else {
			assertThat(allSchemaTypesWithSecurity()).doesNotContain(ContainerRecord.SCHEMA_TYPE, Category.SCHEMA_TYPE);
		}

		assertThat(metadataSchemaTypes.getMetadata("event_default_createdOn").getLabel(Language.French))
				.isEqualTo("Date de l'événement");

		MetadataSchema filingSpaceSchema = metadataSchemaTypes.getSchema(FilingSpace.DEFAULT_SCHEMA);
		MetadataSchema folderSchema = metadataSchemaTypes.getSchema(Folder.DEFAULT_SCHEMA);
		MetadataSchema decommissioningListSchema = metadataSchemaTypes.getSchema(DecommissioningList.DEFAULT_SCHEMA);
		MetadataSchema retentionRuleSchema = metadataSchemaTypes.getSchema(RetentionRule.DEFAULT_SCHEMA);

		assertThat(retentionRuleSchema.getMetadata(RetentionRule.TITLE).isUniqueValue()).isFalse();

		assertThat(getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(Collection.SYSTEM_COLLECTION)
				.getSchema(UserCredential.DEFAULT_SCHEMA).getMetadata(AGENT_STATUS).getType()).isEqualTo(ENUM);

	}

	private void whenMigratingToCurrentVersionThenValidSystemCollectionSchemas()
			throws Exception {

		MetadataSchemaTypes metadataSchemaTypes = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(
				Collection.SYSTEM_COLLECTION);

		assertThat(metadataSchemaTypes.getSchema(UserCredential.DEFAULT_SCHEMA)
				.getMetadata(UserCredential.PERSONAL_EMAILS).getLabel(Language.French)).isEqualTo("Courriels personnels");

	}

	private List<String> allSchemaTypesWithSecurity() {
		List<String> types = new ArrayList<>();
		for (String collection : getModelLayerFactory().getCollectionsListManager().getCollections()) {
			for (MetadataSchemaType type : getModelLayerFactory().getMetadataSchemasManager()
					.getSchemaTypes(collection).getSchemaTypes()) {
				if (type.hasSecurity()) {
					types.add(type.getCode());
				}
			}
		}
		return types;
	}

	private void whenMigratingToCurrentVersionThenSchemasDisplayedCorrectly()
			throws Exception {

		SchemaDisplayConfig folderDisplayConfig = getAppLayerFactory().getMetadataSchemasDisplayManager()
				.getSchema(zeCollection, Folder.DEFAULT_SCHEMA);

		assertThat(folderDisplayConfig.getFormMetadataCodes()).endsWith(
				Folder.DEFAULT_SCHEMA + "_" + Folder.BORROW_PREVIEW_RETURN_DATE,
				Folder.DEFAULT_SCHEMA + "_" + Folder.LINEAR_SIZE);

		assertThat(folderDisplayConfig.getDisplayMetadataCodes()).endsWith(
				Folder.DEFAULT_SCHEMA + "_" + Folder.BORROWED,
				Folder.DEFAULT_SCHEMA + "_" + Folder.BORROW_DATE,
				Folder.DEFAULT_SCHEMA + "_" + Folder.BORROW_USER_ENTERED,
				Folder.DEFAULT_SCHEMA + "_" + Folder.BORROW_PREVIEW_RETURN_DATE,
				Folder.DEFAULT_SCHEMA + "_" + Folder.BORROWING_TYPE,
				Folder.DEFAULT_SCHEMA + "_" + Folder.LINEAR_SIZE,
				Folder.DEFAULT_SCHEMA + "_" + Folder.FORM_CREATED_BY,
				Folder.DEFAULT_SCHEMA + "_" + Folder.FORM_CREATED_ON,
				Folder.DEFAULT_SCHEMA + "_" + Folder.FORM_MODIFIED_BY,
				Folder.DEFAULT_SCHEMA + "_" + Folder.FORM_MODIFIED_ON,
				Folder.DEFAULT_SCHEMA + "_" + Folder.REACTIVATION_DATES,
				Folder.DEFAULT_SCHEMA + "_" + Folder.REACTIVATION_USERS,
				Folder.DEFAULT_SCHEMA + "_" + Folder.PREVIOUS_TRANSFER_DATES,
				Folder.DEFAULT_SCHEMA + "_" + Folder.PREVIOUS_DEPOSIT_DATES,
				Folder.DEFAULT_SCHEMA + "_" + Folder.COMMENTS);

		//		SchemaDisplayConfig categoryDisplayConfig = getAppLayerFactory().getMetadataSchemasDisplayManager()
		//				.getSchema(zeCollection, Category.DEFAULT_SCHEMA);
		//
		//		assertThat(folderDisplayConfig.getDisplayMetadataCodes()).containsExactly(
		//				Folder.DEFAULT_SCHEMA + "_" + Category.CODE,
		//				Folder.DEFAULT_SCHEMA + "_" + "title");

		SchemaDisplayConfig retentionRuleDisplayConfig = getAppLayerFactory().getMetadataSchemasDisplayManager()
				.getSchema(zeCollection, RetentionRule.DEFAULT_SCHEMA);

		assertThat(retentionRuleDisplayConfig.getDisplayMetadataCodes())
				.contains("retentionRule_default_scope",
						"retentionRule_default_principalDefaultDocumentCopyRetentionRule",
						"retentionRule_default_secondaryDefaultDocumentCopyRetentionRule",
						"retentionRule_default_documentCopyRetentionRules");

	}

	private void whenMigratingToCurrentVersionThenHasValueListWithDefaultItems()
			throws Exception {

		assertThat(rm.PA()).isNotNull();
		assertThat(rm.DM()).isNotNull();
		assertThat(rm.FI()).isNotNull();
	}

	private void whenMigratingToCurrentVersionThenConfigHasValidDefaultValue()
			throws Exception {

		SystemConfigurationsManager manager = getModelLayerFactory().getSystemConfigurationsManager();
		assertThat(manager.<Boolean>getValue(RMConfigs.LINKABLE_CATEGORY_MUST_NOT_BE_ROOT)).isEqualTo(Boolean.FALSE);
	}

	private void whenMigratingToCurrentVersionThenHasEssentialMetadatas()
			throws Exception {

		assertThat(rm.administrativeUnit.filingSpaces().isEssential()).isFalse();
		assertThat(rm.defaultFolderSchema().getMetadata(Folder.CATEGORY_ENTERED).isEssential()).isTrue();
		assertThat(rm.defaultFolderSchema().getMetadata(Folder.ADMINISTRATIVE_UNIT_ENTERED).isEssential()).isTrue();
		assertThat(rm.defaultFolderSchema().getMetadata(Folder.RETENTION_RULE_ENTERED).isEssential()).isTrue();
		assertThat(rm.defaultFolderSchema().getMetadata(Folder.OPENING_DATE).isEssential()).isTrue();
		assertThat(rm.defaultFolderSchema().getMetadata(Folder.PARENT_FOLDER).isEssential()).isTrue();
	}

	private void whenMigratingToCurrentVersionThenHasRolesWithRightPermissions() {
		Role userRole = getModelLayerFactory().getRolesManager().getRole(zeCollection, RMRoles.USER);
		Role managerRole = getModelLayerFactory().getRolesManager().getRole(zeCollection, RMRoles.MANAGER);
		Role rgdRole = getModelLayerFactory().getRolesManager().getRole(zeCollection, RMRoles.RGD);

		assertThat(userRole.getOperationPermissions()).contains(RMPermissionsTo.SHARE_FOLDER);
		assertThat(userRole.getOperationPermissions()).contains(RMPermissionsTo.SHARE_DOCUMENT);
		assertThat(userRole.getOperationPermissions()).contains(RMPermissionsTo.CREATE_DOCUMENTS);
		assertThat(userRole.getOperationPermissions()).contains(RMPermissionsTo.CREATE_DOCUMENTS);
		assertThat(userRole.getOperationPermissions()).contains(RMPermissionsTo.CREATE_FOLDERS);
		assertThat(userRole.getOperationPermissions()).contains(RMPermissionsTo.CREATE_SUB_FOLDERS);
		assertThat(userRole.getOperationPermissions()).contains(RMPermissionsTo.DELETE_SEMIACTIVE_DOCUMENT);
		assertThat(userRole.getOperationPermissions()).contains(RMPermissionsTo.DELETE_SEMIACTIVE_FOLDERS);
		assertThat(userRole.getOperationPermissions()).contains(RMPermissionsTo.SHARE_A_SEMIACTIVE_DOCUMENT);
		assertThat(userRole.getOperationPermissions()).contains(RMPermissionsTo.SHARE_A_SEMIACTIVE_FOLDER);
		assertThat(userRole.getOperationPermissions()).contains(RMPermissionsTo.UPLOAD_SEMIACTIVE_DOCUMENT);
		assertThat(userRole.getOperationPermissions()).contains(RMPermissionsTo.UPLOAD_SEMIACTIVE_DOCUMENT);
		assertThat(userRole.getOperationPermissions()).doesNotContain(RMPermissionsTo.MANAGE_FOLDER_AUTHORIZATIONS);
		assertThat(userRole.getOperationPermissions()).doesNotContain(RMPermissionsTo.MANAGE_DOCUMENT_AUTHORIZATIONS);

		assertThat(managerRole.getOperationPermissions()).containsAll(userRole.getOperationPermissions());
		assertThat(managerRole.getOperationPermissions()).doesNotContain(RMPermissionsTo.EDIT_DECOMMISSIONING_LIST);
		assertThat(managerRole.getOperationPermissions()).doesNotContain(RMPermissionsTo.PROCESS_DECOMMISSIONING_LIST);
		assertThat(managerRole.getOperationPermissions()).contains(RMPermissionsTo.MANAGE_FOLDER_AUTHORIZATIONS);
		assertThat(managerRole.getOperationPermissions()).contains(RMPermissionsTo.MANAGE_DOCUMENT_AUTHORIZATIONS);
		assertThat(managerRole.getOperationPermissions()).contains(RMPermissionsTo.SHARE_FOLDER);
		assertThat(managerRole.getOperationPermissions()).contains(RMPermissionsTo.SHARE_DOCUMENT);
		assertThat(managerRole.getOperationPermissions()).contains(RMPermissionsTo.MANAGE_DOCUMENT_AUTHORIZATIONS);
		assertThat(managerRole.getOperationPermissions()).contains(RMPermissionsTo.MANAGE_CONTAINERS);

		//assertThat(rgdRole.getOperationPermissions()).containsAll(RMPermissionsTo.PERMISSIONS.getAll());
		//assertThat(rgdRole.getOperationPermissions()).containsAll(CorePermissions.PERMISSIONS.getAll());
		assertThat(rgdRole.getOperationPermissions()).has(noDuplicates());
	}
	//--------------------------------------------------------------

	String testCase, version;

	public RMMigrationsAcceptanceTest(String testCase) {
		this.testCase = testCase;
		if (!testCase.equals("givenNewInstallation")) {
			version = testCase.substring(testCase.indexOf("_in_") + 4, testCase.indexOf("_with_"));
		}
	}

	protected void setUp(boolean old)
			throws Exception {

		givenDisabledAfterTestValidations();

		if ("givenNewInstallation".equals(testCase)) {
			givenTransactionLogIsEnabled();
			//givenCollection(zeCollection).withAllTestUsers().withConstellioRMModule();
			prepareSystem(withZeCollection().withAllTestUsers().withConstellioRMModule());
		} else {

			givenTransactionLogIsEnabled();
			File state = new File(getStatesFolder(old), testCase + ".zip");

			getCurrentTestSession().getFactoriesTestFeatures().givenSystemInState(state).withFakeEncryptionServices();
		}

		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());

		givenConfig(RMConfigs.ENFORCE_CATEGORY_AND_RULE_RELATIONSHIP_IN_FOLDER, false);
	}

	protected static File getStatesFolder(boolean old) {
		if (old) {
			return new File(new SDKFoldersLocator().getInitialStatesFolder(), "olds");
		} else {
			return new SDKFoldersLocator().getInitialStatesFolder();
		}
	}

	private void whenMigratingToCurrentVersionThenValidateUserFolderWasAdded() {
		MetadataSchemaTypes types = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection);
		MetadataSchema defaultSchema = types.getSchemaType(UserFolder.SCHEMA_TYPE).getDefaultSchema();
		MetadataSchema userDocumentSchema = types.getSchemaType(UserDocument.SCHEMA_TYPE).getDefaultSchema();
		assertThat(defaultSchema).isNotNull();
		assertThat(defaultSchema.getMetadatas()).contains(
				defaultSchema.getMetadata(UserFolder.USER),
				defaultSchema.getMetadata(UserFolder.FORM_CREATED_ON),
				defaultSchema.getMetadata(UserFolder.FORM_MODIFIED_ON),
				defaultSchema.getMetadata(UserFolder.PARENT_USER_FOLDER),
				defaultSchema.getMetadata(RMUserFolder.ADMINISTRATIVE_UNIT),
				defaultSchema.getMetadata(RMUserFolder.CATEGORY),
				defaultSchema.getMetadata(RMUserFolder.RETENTION_RULE));
		assertThat(userDocumentSchema.getMetadatas()).contains(userDocumentSchema.getMetadata(UserDocument.USER_FOLDER));
		assertThat(userDocumentSchema.getMetadatas()).contains(userDocumentSchema.getMetadata(UserDocument.FORM_CREATED_ON));
		assertThat(userDocumentSchema.getMetadatas()).contains(userDocumentSchema.getMetadata(UserDocument.FORM_MODIFIED_ON));
	}
}
