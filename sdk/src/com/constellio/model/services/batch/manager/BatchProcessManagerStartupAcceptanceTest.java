package com.constellio.model.services.batch.manager;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.SearchServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.SlowTest;
import org.junit.Test;

import java.io.File;

import static com.constellio.model.entities.schemas.Schemas.PATH;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;
import static org.assertj.core.api.Assertions.assertThat;

@SlowTest
public class BatchProcessManagerStartupAcceptanceTest extends ConstellioTest {

	@Test
	public void givenSystemWithCurrentBatchProcessesIsStartingThenFinished()
			throws Exception {

		Toggle.MIGRATING_LEGACY_SAVESTATE.enable();
		Toggle.VALIDATE_BYTE_ARRAY_DTOS_AFTER_CREATION.disable();
		givenBackgroundThreadsEnabled();
		givenSystemWithCurrentBatchProcessesIsStarting();

		ModelLayerFactory modelLayerFactory = getModelLayerFactory();
		AppLayerFactory appLayerFactory = getAppLayerFactory();
		BatchProcessesManager batchProcessesManager = modelLayerFactory.getBatchProcessesManager();
		waitForBatchProcessAcceptingErrors();
		assertThat(batchProcessesManager.getStandbyBatchProcesses()).isEmpty();
		assertThat(batchProcessesManager.getCurrentBatchProcess()).isNull();

		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, appLayerFactory);

		SearchServices searchServices = modelLayerFactory.newSearchServices();
		assertThat(searchServices.getResultsCount(fromAllSchemasIn(zeCollection).where(rm.folder.keywords())
				.isEqualTo("newKeywords"))).isEqualTo(105);

	}

	@Test
	public void givenStartedBatchProcessWhenStartingACollectionThenPutTo()
			throws Exception {

		Toggle.MIGRATING_LEGACY_SAVESTATE.enable();
		Toggle.VALIDATE_BYTE_ARRAY_DTOS_AFTER_CREATION.disable();
		givenWaitForBatchProcessAfterTestIsDisabled();
		given_some_processed_batch_process_and_a_current_jammed_process();

		ModelLayerFactory modelLayerFactory = getModelLayerFactory();
		AppLayerFactory appLayerFactory = getAppLayerFactory();
		BatchProcessesManager batchProcessesManager = modelLayerFactory.getBatchProcessesManager();
		waitForBatchProcessAcceptingErrors();
		assertThat(batchProcessesManager.getStandbyBatchProcesses()).isEmpty();
		assertThat(batchProcessesManager.getCurrentBatchProcess()).isNull();

		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, appLayerFactory);
		SearchServices searchServices = modelLayerFactory.newSearchServices();

		String xRubric = rm.getCategoryWithCode("X").getId();
		String zRubric = rm.getCategoryWithCode("Z").getId();

		assertThat(searchServices.hasResults(fromAllSchemasIn(zeCollection).where(PATH).isStartingWithText("/plan/" + xRubric)))
				.isFalse();

		assertThat(searchServices.hasResults(fromAllSchemasIn(zeCollection).where(PATH).isStartingWithText("/plan/" + zRubric)))
				.isTrue();

	}

	@Test
	public void givenStartedBatchProcessWhenStartingACollectionThenDeletePreviousCompletedOnesBeforeStarting()
			throws Exception {

		Toggle.MIGRATING_LEGACY_SAVESTATE.enable();
		Toggle.VALIDATE_BYTE_ARRAY_DTOS_AFTER_CREATION.disable();
		givenWaitForBatchProcessAfterTestIsDisabled();
		given_some_processed_batch_process_and_a_current_jammed_process();

		ModelLayerFactory modelLayerFactory = getModelLayerFactory();
		ConfigManager configManager = getDataLayerFactory().getConfigManager();
		BatchProcessesManager batchProcessesManager = modelLayerFactory.getBatchProcessesManager();
		waitForBatchProcessAcceptingErrors();
		batchProcessesManager.deleteFinishedWithoutErrors();
		assertThat(batchProcessesManager.getFinishedBatchProcesses()).extracting("id").containsOnly(
				"previousWithErrors1",
				"previousWithErrors2"
		);

		assertThat(configManager.exist("/batchProcesses/2df47bc2-5b16-4869-bb94-228c494c71af.xml")).isFalse();
		assertThat(configManager.exist("/batchProcesses/897d1806-3dc2-4480-9ef7-c3dbe77a5caf.xml")).isFalse();
		assertThat(configManager.exist("/batchProcesses/ed53a3b4-e3c1-4d53-a353-3d98d100042f.xml")).isFalse();
		assertThat(configManager.exist("/batchProcesses/previousWithErrors1.xml")).isTrue();
		assertThat(configManager.exist("/batchProcesses/previousWithErrors2.xml")).isTrue();
		assertThat(configManager.exist("/batchProcesses/previousWithoutErrors1.xml")).isFalse();
		assertThat(configManager.exist("/batchProcesses/previousWithoutErrors2.xml")).isFalse();

		assertThat(batchProcessesManager.getCurrentBatchProcess()).isNull();

	}

	private void given_some_processed_batch_process_and_a_current_jammed_process() {
		givenTransactionLogIsEnabled();
		File state = getTestResourceFile("given_some_processed_batch_process_and_a_current_jammed_process.zip");
		getCurrentTestSession().getFactoriesTestFeatures().givenSystemInState(state);
	}

	private void givenSystemWithCurrentBatchProcessesIsStarting() {
		givenTransactionLogIsEnabled();
		File state = getTestResourceFile("given_system_with_current_batch_processes.zip");
		getCurrentTestSession().getFactoriesTestFeatures().givenSystemInState(state);
	}
}
