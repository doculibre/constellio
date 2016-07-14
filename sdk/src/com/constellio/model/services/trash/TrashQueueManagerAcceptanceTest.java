package com.constellio.model.services.trash;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.sdk.tests.ConstellioTest;

public class TrashQueueManagerAcceptanceTest extends ConstellioTest {
	String recordDeletedLogicallyBefore30DaysInZeCollection, recordDeletedLogicallyBefore29DaysInZeCollection, recordDeletedLogicallyNowInZeCollection,
			recordInZeCollection;
	String recordDeletedLogicallyBefore30DaysInBusinessCollection, recordDeletedLogicallyNowInBusinessCollection,
			recordInBusinessCollection;

	private RecordServices recordServices;
	private SearchServices searchServices;
	private TasksSchemasRecordsServices businessTaskSchemas, zeCollectionTaskSchemas;
	private TrashQueueManager trashManager;
	private LocalDateTime now, beforeNow30Days, beforeNow29Days;

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(
				withZeCollection().withTasksModule(),
				withCollection(businessCollection).withTasksModule()
		);

		recordServices = getModelLayerFactory().newRecordServices();
		searchServices = getModelLayerFactory().newSearchServices();
		businessTaskSchemas = new TasksSchemasRecordsServices(businessCollection, getAppLayerFactory());
		zeCollectionTaskSchemas = new TasksSchemasRecordsServices(zeCollection, getAppLayerFactory());
		trashManager = getModelLayerFactory().getTrashQueueManager();

		now = TimeProvider.getLocalDateTime();
		beforeNow29Days = now.minusDays(29);
		beforeNow30Days = now.minusDays(30);

		initTests();
	}

	private void initTests()
			throws RecordServicesException {
		givenTimeIs(beforeNow30Days);

		Task task = zeCollectionTaskSchemas.newTask()
				.setTitle("recordDeletedLogicallyBefore30DaysInZeCollection");
		recordServices.add(task);
		recordServices.logicallyDelete(task.getWrappedRecord(), null);
		recordDeletedLogicallyBefore30DaysInZeCollection = task.getId();

		task = businessTaskSchemas.newTask()
				.setTitle("recordDeletedLogicallyBefore30DaysInBusinessCollection");
		recordServices.add(task);
		recordServices.logicallyDelete(task.getWrappedRecord(), null);
		recordDeletedLogicallyBefore30DaysInBusinessCollection = task.getId();

		givenTimeIs(beforeNow29Days);
		task = zeCollectionTaskSchemas.newTask()
				.setTitle("recordDeletedLogicallyBefore29DaysInZeCollection");
		recordServices.add(task);
		recordServices.logicallyDelete(task.getWrappedRecord(), null);
		recordDeletedLogicallyBefore29DaysInZeCollection = task.getId();

		givenTimeIs(now);
		task = zeCollectionTaskSchemas.newTask()
				.setTitle("recordDeletedLogicallyNowInZeCollection");
		recordServices.add(task);
		recordServices.logicallyDelete(task.getWrappedRecord(), null);
		recordDeletedLogicallyNowInZeCollection = task.getId();

		task = zeCollectionTaskSchemas.newTask()
				.setTitle("recordInZeCollection");
		recordServices.add(task);
		recordInZeCollection = task.getId();

		task = businessTaskSchemas.newTask()
				.setTitle("recordDeletedLogicallyNowInBusinessCollection");
		recordServices.add(task);
		recordServices.logicallyDelete(task.getWrappedRecord(), null);
		recordDeletedLogicallyNowInBusinessCollection = task.getId();

		task = businessTaskSchemas.newTask()
				.setTitle("recordInBusinessCollection");
		recordServices.add(task);
		recordInBusinessCollection = task.getId();
	}

	@Test
	public void givenTrashPurgeAfter31DaysWhenDeleteTrashRecordsThenOk() {
		givenTimeIs(now);
		givenConfig(ConstellioEIMConfigs.TRASH_PURGE_DELAI, 31);
		trashManager.deleteTrashRecords();

		List<String> remainingRecords = getRemainingRecords();
		assertThat(remainingRecords)
				.containsOnly(recordDeletedLogicallyBefore30DaysInZeCollection, recordDeletedLogicallyBefore29DaysInZeCollection,
						recordDeletedLogicallyNowInZeCollection, recordInZeCollection,
						recordDeletedLogicallyBefore30DaysInBusinessCollection, recordDeletedLogicallyNowInBusinessCollection,
						recordInBusinessCollection);
	}

	@Test
	public void givenTrashPurgeAfter30DaysWhenDeleteTrashRecordsThenOk() {
		givenTimeIs(now);
		givenConfig(ConstellioEIMConfigs.TRASH_PURGE_DELAI, 30);
		trashManager.deleteTrashRecords();

		List<String> remainingRecords = getRemainingRecords();
		assertThat(remainingRecords)
				.containsOnly(recordDeletedLogicallyBefore29DaysInZeCollection,
						recordDeletedLogicallyNowInZeCollection, recordInZeCollection,
						recordDeletedLogicallyNowInBusinessCollection,
						recordInBusinessCollection);
	}

	@Test
	public void givenTrashPurgeAfter29DaysWhenDeleteTrashRecordsThenOk() {
		givenTimeIs(now);
		givenConfig(ConstellioEIMConfigs.TRASH_PURGE_DELAI, 29);
		trashManager.deleteTrashRecords();

		List<String> remainingRecords = getRemainingRecords();
		assertThat(remainingRecords).containsOnly(recordDeletedLogicallyNowInZeCollection, recordInZeCollection,
				recordDeletedLogicallyNowInBusinessCollection, recordInBusinessCollection);
	}

	@Test
	public void givenTrashPurgeAfter0DaysWhenDeleteTrashRecordsThenOk() {
		givenTimeIs(now);
		givenConfig(ConstellioEIMConfigs.TRASH_PURGE_DELAI, 0);
		trashManager.deleteTrashRecords();

		List<String> remainingRecords = getRemainingRecords();
		assertThat(remainingRecords).containsOnly(recordInZeCollection, recordInBusinessCollection);
	}

	private List<String> getRemainingRecords() {
		List<String> returnList = new ArrayList<>();
		List<Record> tasks = searchServices
				.search(new LogicalSearchQuery(from(businessTaskSchemas.taskSchemaType()).returnAll()));
		for (Record task : tasks) {
			returnList.add(task.getId());
		}
		tasks = searchServices
				.search(new LogicalSearchQuery(from(zeCollectionTaskSchemas.taskSchemaType()).returnAll()));
		for (Record task : tasks) {
			returnList.add(task.getId());
		}
		return returnList;
	}
}
