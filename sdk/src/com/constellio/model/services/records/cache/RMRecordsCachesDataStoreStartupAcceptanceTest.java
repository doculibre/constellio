package com.constellio.model.services.records.cache;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.data.dao.dto.records.RecordId;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RMRecordsCachesDataStoreStartupAcceptanceTest extends ConstellioTest {

	Users users = new Users();
	RMTestRecords records = new RMTestRecords(zeCollection);

	@Test
	public void whenStartingApplicationThenCacheHooksPopulated() {

		prepareSystem(withZeCollection().withAllTest(users).withConstellioRMModule().withRMTest(records)
				.withFoldersAndContainersOfEveryStatus());

		User admin = users.adminIn(zeCollection);
		RecordId spaceId = records.getStorageSpaceS02_02().getWrappedRecordId();
		assertThat(getModelLayerFactory().getTaxonomyRecordsHookRetriever(zeCollection)
				.hasUserAccessToSomethingInSecondaryConcept(admin, spaceId, true, false)).isTrue();
		restartLayers();
		assertThat(getModelLayerFactory().getTaxonomyRecordsHookRetriever(zeCollection)
				.hasUserAccessToSomethingInSecondaryConcept(admin, spaceId, true, false)).isTrue();

	}

}
