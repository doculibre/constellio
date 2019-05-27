package com.constellio.model.services.batch.manager;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.model.entities.batchprocess.BatchProcess;
import com.constellio.model.entities.batchprocess.RecordBatchProcess;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.batch.actions.ChangeValueOfMetadataBatchProcessAction;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class BatchProcessesManagerAcceptanceTest extends ConstellioTest {

	private RMTestRecords records = new RMTestRecords(zeCollection);
	private Users users = new Users();
	private BatchProcessesManager batchProcessesManager;
	private ConfigManager configManager;

	private ChangeValueOfMetadataBatchProcessAction action;
	private List<String> recordIds;
	private Map<String, Object> metadataChangedValues;

	@Before
	public void setUp() {
		prepareSystem(withZeCollection().withConstellioRMModule().withRMTest(records).withAllTest(users)
				.withFoldersAndContainersOfEveryStatus());
		givenBackgroundThreadsEnabled();

		batchProcessesManager = getModelLayerFactory().getBatchProcessesManager();
		configManager = getModelLayerFactory().getDataLayerFactory().getConfigManager();

		recordIds = asList(records.folder_A01, records.folder_A02);

		metadataChangedValues = new HashMap<>();
		metadataChangedValues.put(Folder.TITLE, "newTitle");
		action = new ChangeValueOfMetadataBatchProcessAction(metadataChangedValues);
	}

	@Test
	public void whenProcessIsFinishedAndHistoryLimitIsExceededThenOldestProcessIsDeletedFromXml()
			throws InterruptedException {
		givenConfig(ConstellioEIMConfigs.BATCH_PROCESSES_MAXIMUM_HISTORY_SIZE, 3);

		List<String> batchProcessIds = new ArrayList<>();

		for (int i = 0; i < 3; i++) {
			RecordBatchProcess process = batchProcessesManager
					.addPendingBatchProcess(recordIds, action, User.ADMIN, null, zeCollection);
			batchProcessesManager.waitUntilFinished(process);
			batchProcessIds.add(process.getId());
		}

		List<BatchProcess> finishedBatchProcesses = batchProcessesManager.getFinishedBatchProcesses();
		assertThat(finishedBatchProcesses).extracting("id")
				.containsExactly(batchProcessIds.get(0), batchProcessIds.get(1), batchProcessIds.get(2));

		RecordBatchProcess process = batchProcessesManager
				.addPendingBatchProcess(recordIds, action, User.ADMIN, null, zeCollection);
		batchProcessesManager.waitUntilFinished(process);
		waitForBatchProcess();

		finishedBatchProcesses = batchProcessesManager.getFinishedBatchProcesses();
		assertThat(finishedBatchProcesses).extracting("id")
				.containsExactly(batchProcessIds.get(1), batchProcessIds.get(2), process.getId());
	}

	@Test
	public void whenFinishedProcessesInXmlAreWayOverLimitAndFinishedProcessesInXmlAreReducedToLimit() throws Exception {
		givenConfig(ConstellioEIMConfigs.BATCH_PROCESSES_MAXIMUM_HISTORY_SIZE, 5);

		List<String> batchProcessIds = new ArrayList<>();

		for (int i = 0; i < 5; i++) {
			RecordBatchProcess process = batchProcessesManager
					.addPendingBatchProcess(recordIds, action, User.ADMIN, null, zeCollection);
			batchProcessesManager.waitUntilFinished(process);
			batchProcessIds.add(process.getId());
		}

		List<BatchProcess> finishedBatchProcesses = batchProcessesManager.getFinishedBatchProcesses();
		assertThat(finishedBatchProcesses).extracting("id").containsExactly(
				batchProcessIds.get(0), batchProcessIds.get(1), batchProcessIds.get(2),
				batchProcessIds.get(3), batchProcessIds.get(4));

		givenConfig(ConstellioEIMConfigs.BATCH_PROCESSES_MAXIMUM_HISTORY_SIZE, 2);
		waitForBatchProcess();

		RecordBatchProcess process = batchProcessesManager
				.addPendingBatchProcess(recordIds, action, User.ADMIN, null, zeCollection);
		batchProcessesManager.waitUntilFinished(process);

		finishedBatchProcesses = batchProcessesManager.getFinishedBatchProcesses();
		assertThat(finishedBatchProcesses).extracting("id")
				.containsExactly(batchProcessIds.get(4), process.getId());
	}

}
