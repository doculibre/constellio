/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.modules.rm.migrations;

import static com.constellio.sdk.tests.TestUtils.noDuplicates;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.constellio.app.entities.schemasDisplay.SchemaDisplayConfig;
import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.constants.RMRoles;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.FilingSpace;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RMTask;
import com.constellio.app.modules.tasks.TaskModule;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.SDKFoldersLocator;

@RunWith(Parameterized.class)
public class RMMigrationsAcceptanceTest extends ConstellioTest {

	RMSchemasRecordsServices rm;

	@Test
	public void testAll()
			throws Exception {

		whenMigratingToCurrentVersionThenValidSchemas();
		whenMigratingToCurrentVersionThenSchemasDisplayedCorrectly();
		whenMigratingToCurrentVersionThenHasValueListWithDefaultItems();
		whenMigratingToCurrentVersionThenHasEssentialMetadatas();
		whenMigratingToCurrentVersionThenHasRolesWithRightPermissions();
		whenMigratingToCurrentVersionThenConfigHasValidDefaultValue();
		whenMigratingToCurrentVersionThenTaskModuleIsEnabledAndExtraRMMetadatasAreCreated();
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

	public void whenMigratingToCurrentVersionThenValidSchemas()
			throws Exception {

		MetadataSchemaTypes metadataSchemaTypes = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection);

		assertThat(allSchemaTypesWithSecurity()).containsOnly(Folder.SCHEMA_TYPE, Document.SCHEMA_TYPE, Task.SCHEMA_TYPE,
				ContainerRecord.SCHEMA_TYPE, AdministrativeUnit.SCHEMA_TYPE);

		assertThat(metadataSchemaTypes.getMetadata("event_default_createdOn").getLabel()).isEqualTo("Date de l'événement");

		MetadataSchema filingSpaceSchema = metadataSchemaTypes.getSchema(FilingSpace.DEFAULT_SCHEMA);
		MetadataSchema folderSchema = metadataSchemaTypes.getSchema(Folder.DEFAULT_SCHEMA);
		MetadataSchema decommissioningListSchema = metadataSchemaTypes.getSchema(DecommissioningList.DEFAULT_SCHEMA);

		assertThat(filingSpaceSchema.getMetadata(FilingSpace.USERS).isWriteNullValues()).isTrue();
		assertThat(folderSchema.getMetadata(Folder.DESCRIPTION).isWriteNullValues()).isFalse();
		assertThat(folderSchema.getMetadata(Folder.LINEAR_SIZE).isWriteNullValues()).isFalse();
		assertThat(folderSchema.getMetadata(Folder.COMMENTS).isWriteNullValues()).isFalse();
		assertThat(folderSchema.getMetadata(Folder.BORROW_DATE).isWriteNullValues()).isTrue();
		assertThat(folderSchema.getMetadata(Folder.MAIN_COPY_RULE).isWriteNullValues()).isFalse();
		assertThat(decommissioningListSchema.getMetadata(DecommissioningList.VALIDATIONS).isWriteNullValues()).isTrue();
	}

	private List<String> allSchemaTypesWithSecurity() {
		List<String> types = new ArrayList<>();
		for (MetadataSchemaType type : getModelLayerFactory().getMetadataSchemasManager()
				.getSchemaTypes(zeCollection).getSchemaTypes()) {
			if (type.hasSecurity()) {
				types.add(type.getCode());
			}
		}
		return types;
	}

	public void whenMigratingToCurrentVersionThenSchemasDisplayedCorrectly()
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
				Folder.DEFAULT_SCHEMA + "_" + Folder.COMMENTS);

		//		SchemaDisplayConfig categoryDisplayConfig = getAppLayerFactory().getMetadataSchemasDisplayManager()
		//				.getSchema(zeCollection, Category.DEFAULT_SCHEMA);
		//
		//		assertThat(folderDisplayConfig.getDisplayMetadataCodes()).containsExactly(
		//				Folder.DEFAULT_SCHEMA + "_" + Category.CODE,
		//				Folder.DEFAULT_SCHEMA + "_" + "title");

	}

	public void whenMigratingToCurrentVersionThenHasValueListWithDefaultItems()
			throws Exception {

		assertThat(rm.PA()).isNotNull();
		assertThat(rm.DM()).isNotNull();
		assertThat(rm.FI()).isNotNull();
	}

	public void whenMigratingToCurrentVersionThenConfigHasValidDefaultValue()
			throws Exception {

		SystemConfigurationsManager manager = getModelLayerFactory().getSystemConfigurationsManager();
		assertThat(manager.getValue(RMConfigs.LINKABLE_CATEGORY_MUST_NOT_BE_ROOT)).isEqualTo(Boolean.FALSE);
	}

	public void whenMigratingToCurrentVersionThenHasEssentialMetadatas()
			throws Exception {

		assertThat(rm.administrativeUnitFilingSpaces().isEssential()).isTrue();
		assertThat(rm.defaultFolderSchema().getMetadata(Folder.CATEGORY_ENTERED).isEssential()).isTrue();
		assertThat(rm.defaultFolderSchema().getMetadata(Folder.ADMINISTRATIVE_UNIT_ENTERED).isEssential()).isTrue();
		assertThat(rm.defaultFolderSchema().getMetadata(Folder.RETENTION_RULE_ENTERED).isEssential()).isTrue();
		assertThat(rm.defaultFolderSchema().getMetadata(Folder.OPENING_DATE).isEssential()).isTrue();
		assertThat(rm.defaultFolderSchema().getMetadata(Folder.PARENT_FOLDER).isEssential()).isTrue();
	}

	public void whenMigratingToCurrentVersionThenHasRolesWithRightPermissions() {
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
		assertThat(userRole.getOperationPermissions()).doesNotContain(CorePermissions.MANAGE_SEARCH_REPORTS);

		assertThat(managerRole.getOperationPermissions()).containsAll(userRole.getOperationPermissions());
		assertThat(managerRole.getOperationPermissions()).contains(RMPermissionsTo.EDIT_DECOMMISSIONING_LIST);
		assertThat(managerRole.getOperationPermissions()).contains(RMPermissionsTo.PROCESS_DECOMMISSIONING_LIST);
		assertThat(managerRole.getOperationPermissions()).contains(RMPermissionsTo.MANAGE_FOLDER_AUTHORIZATIONS);
		assertThat(managerRole.getOperationPermissions()).contains(RMPermissionsTo.MANAGE_DOCUMENT_AUTHORIZATIONS);
		assertThat(managerRole.getOperationPermissions()).contains(RMPermissionsTo.SHARE_FOLDER);
		assertThat(managerRole.getOperationPermissions()).contains(RMPermissionsTo.SHARE_DOCUMENT);
		assertThat(managerRole.getOperationPermissions()).contains(RMPermissionsTo.MANAGE_DOCUMENT_AUTHORIZATIONS);
		assertThat(managerRole.getOperationPermissions()).contains(RMPermissionsTo.MANAGE_CONTAINERS);

		assertThat(rgdRole.getOperationPermissions()).contains(CorePermissions.MANAGE_SEARCH_REPORTS);
		assertThat(rgdRole.getOperationPermissions()).containsAll(RMPermissionsTo.PERMISSIONS.getAll());
		assertThat(rgdRole.getOperationPermissions()).containsAll(CorePermissions.PERMISSIONS.getAll());
		assertThat(rgdRole.getOperationPermissions()).has(noDuplicates());
	}
	//--------------------------------------------------------------

	String testCase;

	public RMMigrationsAcceptanceTest(String testCase) {
		this.testCase = testCase;
	}

	@Parameterized.Parameters(name = "{0}")
	public static Collection<Object[]> testCases() {
		List<Object[]> states = new ArrayList<>();
		states.add(new Object[] { "givenNewInstallation" });

		for (String state : new SDKFoldersLocator().getInitialStatesFolder().list()) {
			if (state.endsWith(".zip")) {
				states.add(new Object[] { state.replace(".zip", "") });
			}
		}

		return states;

	}

	@Before
	public void setUp()
			throws Exception {

		givenDisabledAfterTestValidations();

		if ("givenNewInstallation".equals(testCase)) {
			givenTransactionLogIsEnabled();
			givenCollection(zeCollection).withAllTestUsers().withConstellioRMModule();

		} else {

			givenTransactionLogIsEnabled();
			File statesFolder = new SDKFoldersLocator().getInitialStatesFolder();
			File state = new File(statesFolder, testCase + ".zip");

			getCurrentTestSession().getFactoriesTestFeatures().givenSystemInState(state);
		}

		rm = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());
	}
}
