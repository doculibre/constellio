package com.constellio.app.modules.rm.migrations.records;

import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataSchemasRuntimeException;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.SDKFoldersLocator;
import org.junit.Test;

import java.io.File;

import static com.constellio.model.services.records.reindexing.ReindexationMode.RECALCULATE_AND_REWRITE;
import static junit.framework.TestCase.fail;

public class RMFolderMigrationTo8_1_1__1_AcceptanceTest extends ConstellioTest {

	private static final String DECOMMISSIONING_DATE = "decommissioningDate";

	@Test
	public void givenSystemIn8_1_0_thenMigrated() throws Exception {
		givenSystemIn8_1_0();
		waitForBatchProcess();

		getConstellioFactories().getModelLayerFactory().getRecordMigrationsManager().checkScriptsToFinish();

		getModelLayerFactory().newReindexingServices().reindexCollections(RECALCULATE_AND_REWRITE);
		waitForBatchProcess();

		try {
			MetadataSchemaTypes types = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection);
			types.getDefaultSchema(Folder.SCHEMA_TYPE).getMetadata(DECOMMISSIONING_DATE);
			fail("should be deleted");
		} catch (MetadataSchemasRuntimeException.NoSuchMetadata e) {
			//OK
		}
	}

	private void givenSystemIn8_1_0() {
		givenBackgroundThreadsEnabled();
		givenTransactionLogIsEnabled();
		File statesFolder = new SDKFoldersLocator().getInitialStatesFolder();
		File state = new File(statesFolder, "given_system_in_8.1.0_with_rm_module.zip");

		getCurrentTestSession().getFactoriesTestFeatures().givenSystemInState(state);
	}

}
