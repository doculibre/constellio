/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.model.services.batch.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.model.entities.batchprocess.BatchProcess;
import com.constellio.model.entities.batchprocess.BatchProcessAction;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.sdk.tests.ConstellioTest;

public class BatchProcessTasksFactoryTest extends ConstellioTest {

	@Mock MetadataSchemasManager schemasManager;
	@Mock MetadataSchemaTypes schemaTypes;
	@Mock BatchProcessAction action;
	@Mock List<String> zeUpdatedMetadatasCodes;

	int numberOfRecordsPerTask = anInteger();
	@Mock RecordServices recordServices;
	@Mock BatchProcess aBatchProcess;

	BatchProcessTasksFactory tasksFactory;

	@Mock SearchServices searchServices;

	@Mock ForkJoinPool pool;

	@Mock BatchProcessTask aTask;
	@Mock BatchProcessTask anotherTask;

	@Mock TaskList taskList;

	List<String> recordIds = new ArrayList<>();
	String record1 = "record1";
	String record2 = "record2";

	List<String> errorsList = new ArrayList<>();

	List<BatchProcessTask> tasks = new ArrayList<>();

	@Before
	public void setUp() {

		tasksFactory = spy(new BatchProcessTasksFactory(recordServices, searchServices, taskList));
		recordIds.add(record1);
		recordIds.add(record2);
		tasks.add(aTask);
		tasks.add(anotherTask);

		when(schemasManager.getSchemaTypes("zeCollection")).thenReturn(schemaTypes);
		when(aBatchProcess.getAction()).thenReturn(action);
	}

	@Test
	public void whenCreatingReindexationTasksThenTasksCreated() {
		tasks = tasksFactory.createBatchProcessTasks(aBatchProcess, recordIds, errorsList, numberOfRecordsPerTask,
				schemasManager);

		assertThat(tasks).isNotEmpty();
	}

	@Test
	public void whenCreatingTasksThenRightNumberOfTasksCreated() {
		tasks = tasksFactory.createBatchProcessTasks(aBatchProcess, recordIds, errorsList, 1, schemasManager);

		assertThat(tasks).hasSize(2);
	}

}
