package com.constellio.model.services.contents;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.model.services.contents.ContentManager.VaultScanResults;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;

public class ContentManagerScanAcceptanceTest extends ConstellioTest {

	RMTestRecords records = new RMTestRecords(zeCollection);

	@Before
	public void setUp() {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus()
						.withDocumentsHavingContent()
		);
	}

	@Test
	public void givenContentManagement() {
		VaultScanResults vaultScanResults = new VaultScanResults();

		getModelLayerFactory().getContentManager().scanVaultContentAndDeleteUnreferencedFiles(vaultScanResults);


	}
}
