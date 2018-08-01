package com.constellio.model.services.batch.controller;

import com.constellio.model.entities.batchprocess.BatchProcessAction;
import com.constellio.model.entities.batchprocess.RecordBatchProcess;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.BatchProcessReport;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class BatchProcessTasksFactoryAcceptanceTest extends ConstellioTest {

	@Mock ModelLayerFactory modelLayerFactory;
	@Mock MetadataSchemasManager schemasManager;
	@Mock MetadataSchemaTypes schemaTypes;
	@Mock BatchProcessAction action;
	@Mock List<String> zeUpdatedMetadatasCodes;

	int numberOfRecordsPerTask = anInteger();
	@Mock RecordServices recordServices;
	@Mock RecordBatchProcess aBatchProcess;

	BatchProcessTasksFactory tasksFactory;

	@Mock SearchServices searchServices;
	@Mock UserServices userServices;

	@Mock ForkJoinPool pool;

	@Mock BatchProcessTask aTask;
	@Mock BatchProcessTask anotherTask;

	@Mock TaskList taskList;

	List<Record> records = new ArrayList<>();
	@Mock Record record1;
	@Mock Record record2;

	@Mock BatchProcessReport report;

	List<String> errorsList = new ArrayList<>();

	List<BatchProcessTask> tasks = new ArrayList<>();

	@Before
	public void setUp() {

		when(record1.getId()).thenReturn("record1");
		when(record2.getId()).thenReturn("record2");

		when(modelLayerFactory.newRecordServices()).thenReturn(recordServices);
		when(modelLayerFactory.getMetadataSchemasManager()).thenReturn(schemasManager);
		when(modelLayerFactory.newSearchServices()).thenReturn(searchServices);

		tasksFactory = spy(new BatchProcessTasksFactory(recordServices, searchServices, userServices, taskList, getModelLayerFactory()));
		records.add(record1);
		records.add(record2);
		tasks.add(aTask);
		tasks.add(anotherTask);

		when(schemasManager.getSchemaTypes("zeCollection")).thenReturn(schemaTypes);
		when(aBatchProcess.getAction()).thenReturn(action);
	}

	@Test
	public void whenCreatingReindexationTasksThenTasksCreated() {
		tasks = tasksFactory.createBatchProcessTasks(aBatchProcess, records, errorsList, numberOfRecordsPerTask,
				schemasManager, report);

		assertThat(tasks).isNotEmpty();
	}

	@Test
	public void whenCreatingTasksThenRightNumberOfTasksCreated() {
		tasks = tasksFactory.createBatchProcessTasks(aBatchProcess, records, errorsList, 1, schemasManager, report);

		assertThat(tasks).hasSize(2);
	}

}
