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

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;

import com.constellio.data.io.services.facades.OpenedResourcesWatcher;
import com.constellio.data.utils.LoggerUncaughtExceptionHandler;
import com.constellio.model.services.batch.controller.BatchProcessControllerRuntimeException.ControllerAlreadyStarted;
import com.constellio.model.services.batch.manager.BatchProcessesManager;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.sdk.tests.ConstellioTest;

public class BatchProcessControllerUnitTest extends ConstellioTest {

	@Mock MetadataSchemasManager schemasManager;
	@Mock BatchProcessControllerThread thread;
	@Mock BatchProcessesManager batchProcessesManager;
	@Mock RecordServices recordServices;
	@Mock SearchServices searchServices;
	BatchProcessController controller;
	BatchProcessController unspiedController;

	@Before
	public void setUp() {
		unspiedController = new BatchProcessController(batchProcessesManager, recordServices, anInteger(), schemasManager,
				searchServices);
		controller = spy(unspiedController);
		doReturn(thread).when(controller).newBatchProcessControllerThread();
	}

	@After
	public void tearDown()
			throws Exception {
		OpenedResourcesWatcher.clear();

	}

	@Test
	public void whenCreatedThenRegisterAsBatchProcessManagerListener()
			throws Exception {

		verify(batchProcessesManager).registerBatchProcessesListUpdatedEvent(unspiedController);
	}

	@Test
	public void whenStartingThenCreateAndStartThread()
			throws Exception {

		controller.initialize();

		verify(controller).newBatchProcessControllerThread();
		verify(thread).setUncaughtExceptionHandler(LoggerUncaughtExceptionHandler.instance);
		verify(thread).start();
	}

	@Test
	public void givenThreadStartedWhenStoppingThenRequestStopAfterCurrentBatchProcessAndJoinThread()
			throws InterruptedException {
		givenStartedController();

		controller.close();

		InOrder inOrder = inOrder(thread);
		inOrder.verify(thread).stopRequested();

		verify(controller, never()).newBatchProcessControllerThread();
	}

	@Test(expected = ControllerAlreadyStarted.class)
	public void givenThreadStartedWhenStartingThenAlreadyStartedRuntimeException()
			throws InterruptedException {
		givenStartedController();

		controller.initialize();
	}

	@Test
	public void givenThreadStopedWhenStartingThenRestarts()
			throws InterruptedException {
		givenStartedController();
		controller.close();
		controller.initialize();
	}

	@Test
	public void givenUnstartedManagerWhenBatchProcessesListUpdatedThenDoNothing()
			throws Exception {
		controller.onBatchProcessesListUpdated();

		verify(controller).onBatchProcessesListUpdated();
		verifyNoMoreInteractions(controller);
		verifyZeroInteractions(thread);
	}

	@Test
	public void givenStartedManagerWhenBatchProcessesListUpdatedThenNotifyThread()
			throws Exception {

		controller.initialize();

		controller.onBatchProcessesListUpdated();

		verify(thread).notifyBatchProcessesListConfigUpdated();
	}

	private void givenStartedController() {
		controller = spy(
				new BatchProcessController(batchProcessesManager, recordServices, anInteger(), schemasManager, searchServices));
		doReturn(thread).when(controller).newBatchProcessControllerThread();
		controller.initialize();
		reset(controller);
	}
}
