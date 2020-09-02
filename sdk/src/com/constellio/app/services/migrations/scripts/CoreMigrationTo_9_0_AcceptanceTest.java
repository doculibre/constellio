package com.constellio.app.services.migrations.scripts;

import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.security.roles.RolesManager;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.SDKFoldersLocator;
import org.junit.Test;

import java.io.File;
import java.util.List;

import com.constellio.model.services.collections.CollectionsListManager;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class CoreMigrationTo_9_0_AcceptanceTest extends ConstellioTest {
	@Test
	public void whenMigratingTo9_0ThenMetadtaAreCreatedAndRoleAreSet() {
		givenTransactionLogIsEnabled();
		File statesFolder = new SDKFoldersLocator().getInitialStatesFolder();
		File state = new File(statesFolder, "given_system_in_8.3.zip");

		getCurrentTestSession().getFactoriesTestFeatures()
				.givenSystemInState(state).withPasswordsResetAndDisableLDAPSync()
				.withFakeEncryptionServices();

		ModelLayerFactory modelLayerFactory = getModelLayerFactory();

		RolesManager rolesManager = modelLayerFactory.getRolesManager();
		List<Role> roleList = rolesManager.getAllRoles(zeCollection);


		getCurrentTestSession().getFactoriesTestFeatures().givenSystemInState(state);

		for (Role role : roleList) {
			assertThat(role.getOperationPermissions()).contains(CorePermissions.MODIFY_RECORDS_USING_BATCH_PROCESS);
		}
	}

	@Test
	public void whenMigratingTo9_0_0_ThenContentNeedingReconversionAreFlagged() {
		givenTransactionLogIsEnabled();

		getCurrentTestSession().getFactoriesTestFeatures()
				.givenSystemInState(getTestResourceFile("withoutNewCacheIdSaveState.zip")).withPasswordsResetAndDisableLDAPSync()
				.withFakeEncryptionServices();

		CollectionsListManager collectionsListManager = getModelLayerFactory().getCollectionsListManager();

		Map<Byte, String> mapByteCollection = new HashMap<>();

		for(String currentCollection : collectionsListManager.getCollections()) {
			Byte collectionId = collectionsListManager.getCollectionId(currentCollection);

			assertThat(mapByteCollection.get(collectionId)).isNull();

			mapByteCollection.put(collectionId, currentCollection);
		}
	}
}
