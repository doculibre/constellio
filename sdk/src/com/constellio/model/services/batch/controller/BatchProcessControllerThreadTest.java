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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.model.entities.batchprocess.BatchProcess;
import com.constellio.model.entities.batchprocess.BatchProcessPart;
import com.constellio.model.services.batch.manager.BatchProcessesManager;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.sdk.tests.ConstellioTest;

public class BatchProcessControllerThreadTest extends ConstellioTest {

	@Mock MetadataSchemasManager schemasManager;
	int numberOfRecordsPerTask = anInteger();
	@Mock BatchProcessesManager batchProcessesManager;
	@Mock RecordServices recordServices;
	@Mock SearchServices searchServices;

	@Mock BatchProcessPart aBatchPart;
	@Mock BatchProcessPart anotherBatchPart;
	@Mock BatchProcess aBatchProcess;

	@Mock BatchProcessTasksFactory tasksFactory;

	@Mock ForkJoinPool pool;

	@Mock BatchProcessTask aTask;
	@Mock BatchProcessTask anotherTask;

	@Mock TaskList taskList;

	List<String> recordIds = new ArrayList<>();
	String aRecordId = "aRecordId";
	String anotherRecordId = "anotherRecordId";

	List<BatchProcessTask> tasks = new ArrayList<>();

	BatchProcessControllerThread thread;

	@Before
	public void setUp()
			throws Exception {
		thread = spy(new BatchProcessControllerThread(batchProcessesManager, recordServices, numberOfRecordsPerTask,
				schemasManager, searchServices));

		doNothing().when(thread).waitUntilNotified();

		when(aBatchPart.getBatchProcess()).thenReturn(aBatchProcess);
		doReturn(tasksFactory).when(thread).newBatchProcessTasksFactory(any(TaskList.class));
		doReturn(pool).when(thread).newForkJoinPool();

		recordIds.add(aRecordId);
		recordIds.add(anotherRecordId);
		tasks.add(aTask);
		tasks.add(anotherTask);
	}

	@Test
	public void givenStopRequestedThenIsStopRequested()
			throws Exception {
		assertThat(thread.isStopRequested()).isFalse();
		thread.stopRequested();
		assertThat(thread.isStopRequested()).isTrue();
	}

	@Test
	public void whenRunningThenProcessUntilStopRequested()
			throws Exception {
		doReturn(false).doReturn(false).doReturn(false).doReturn(true).when(thread).isStopRequested();
		doNothing().when(thread).process();

		thread.run();

		verify(thread, times(3)).process();
	}

	@Test
	public void givenBatchPartNotNullWhenProcessingThenPartProcessed()
			throws Exception {
		doReturn(null).when(thread).processBatchProcessPart(any(BatchProcessPart.class));
		when(batchProcessesManager.getCurrentBatchProcessPart()).thenReturn(aBatchPart);

		thread.process();

		verify(thread).processBatchProcessPart(aBatchPart);
	}

	@Test
	public void givenBatchPartNullWhenProcessingThenPartNotProcessed()
			throws Exception {
		doReturn(null).when(thread).processBatchProcessPart(any(BatchProcessPart.class));

		thread.process();

		verify(thread, never()).processBatchProcessPart(any(BatchProcessPart.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void whenProcessingBatchProcessPartThenTasksCreated()
			throws Exception {
		doReturn(tasksFactory).when(thread).newBatchProcessTasksFactory(any(TaskList.class));
		when(aBatchPart.getRecordIds()).thenReturn(recordIds);

		thread.processBatchProcessPart(aBatchPart);

		verify(tasksFactory).createBatchProcessTasks(eq(aBatchProcess), eq(recordIds), any(List.class),
				eq(numberOfRecordsPerTask), eq(schemasManager));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void whenProcessingBatchPartThenAllTasksExecuteShutdownAndAwaitTermination()
			throws Exception {
		when(
				tasksFactory.createBatchProcessTasks(eq(aBatchProcess), eq(recordIds), anyList(), eq(numberOfRecordsPerTask),
						eq(schemasManager))).thenReturn(tasks);
		when(aBatchPart.getRecordIds()).thenReturn(recordIds);

		thread.processBatchProcessPart(aBatchPart);

		verify(pool, times(1)).execute(aTask);
		verify(pool, times(1)).execute(anotherTask);
		verify(pool, times(1)).shutdown();
		verify(pool, times(1)).awaitTermination(anyLong(), any(TimeUnit.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void whenProcessingBatchPartThenAllTasksExecuteShutdownAndAwaitTerminationAndNextPartProcessed()
			throws Exception {
		when(
				tasksFactory.createBatchProcessTasks(eq(aBatchProcess), eq(recordIds), anyList(), eq(numberOfRecordsPerTask),
						eq(schemasManager))).thenReturn(tasks);
		when(aBatchPart.getRecordIds()).thenReturn(recordIds);
		when(batchProcessesManager.markBatchProcessPartAsFinishedAndGetAnotherPart(eq(aBatchPart), anyList())).thenReturn(
				anotherBatchPart);

		assertThat(thread.processBatchProcessPart(aBatchPart)).isEqualTo(anotherBatchPart);

		verify(pool).execute(aTask);
		verify(pool).execute(anotherTask);
		verify(pool).shutdown();
		verify(pool).awaitTermination(anyLong(), any(TimeUnit.class));
	}
}
