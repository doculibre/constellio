package com.constellio.app.modules.rm.migrations;

import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.security.roles.RolesManager;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.SDKFoldersLocator;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static com.constellio.model.entities.schemas.entries.DataEntryType.CALCULATED;
import static org.assertj.core.api.Assertions.assertThat;

public class RMMigrationTo9_0_AcceptanceTest extends ConstellioTest {

	@Test
	public void givenSystemIn8_3ValidateMigrationOf8_3And9_0ThenOk() {
		givenTransactionLogIsEnabled();
		File statesFolder = new SDKFoldersLocator().getInitialStatesFolder();
		File state = new File(statesFolder, "given_system_in_8.3.zip");

		getCurrentTestSession().getFactoriesTestFeatures()
				.givenSystemInState(state).withPasswordsResetAndDisableLDAPSync()
				.withFakeEncryptionServices();

		ModelLayerFactory modelLayerFactory = getModelLayerFactory();

		RolesManager rolesManager = modelLayerFactory.getRolesManager();


		List<Role> roleList1 = rolesManager.getAllRoles(zeCollection);

		for (Role role : roleList1) {
			if (role.hasOperationPermission(RMPermissionsTo.PROCESS_DECOMMISSIONING_LIST)) {
				assertThat(role.getOperationPermissions()).contains(RMPermissionsTo.CREATE_DECOMMISSIONING_LIST);
			}
		}

		List<Role> roleList2 = rolesManager.getAllRoles(zeCollection);

		for (Role role : roleList2) {
			assertThat(role.getOperationPermissions()).contains(RMPermissionsTo.CART_BATCH_DELETE);
		}

		MetadataSchema metadataSchema = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(zeCollection).getDefaultSchema(Folder.SCHEMA_TYPE);

		// Will throw NoSuchMetadata if not there
		assertThat(metadataSchema.getMetadata(Folder.OPENING_DATE).getDataEntry().getType()).isEqualTo(CALCULATED);
		assertThat(metadataSchema.getMetadata(Folder.ACTUAL_TRANSFER_DATE).getDataEntry().getType()).isEqualTo(CALCULATED);
		assertThat(metadataSchema.getMetadata(Folder.ACTUAL_DEPOSIT_DATE).getDataEntry().getType()).isEqualTo(CALCULATED);
		assertThat(metadataSchema.getMetadata(Folder.ACTUAL_DESTRUCTION_DATE).getDataEntry().getType()).isEqualTo(CALCULATED);
	}


}