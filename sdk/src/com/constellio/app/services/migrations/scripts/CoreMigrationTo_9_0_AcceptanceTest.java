package com.constellio.app.services.migrations.scripts;

import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class CoreMigrationTo_9_0_AcceptanceTest extends ConstellioTest {
	@Test
	public void whenMigratingTo9_0_0_ThenContentNeedingReconversionAreFlagged() {
		givenTransactionLogIsEnabled();

		getCurrentTestSession().getFactoriesTestFeatures()
				.givenSystemInState(getTestResourceFile("withoutNewCacheIdSaveState.zip")).withPasswordsReset()
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
