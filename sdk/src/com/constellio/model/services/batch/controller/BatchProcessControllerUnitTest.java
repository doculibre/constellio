package com.constellio.model.services.batch.controller;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.data.io.services.facades.OpenedResourcesWatcher;
import com.constellio.data.utils.LoggerUncaughtExceptionHandler;
import com.constellio.model.conf.ModelLayerConfiguration;
import com.constellio.model.services.batch.controller.BatchProcessControllerRuntimeException.ControllerAlreadyStarted;
import com.constellio.model.services.batch.manager.BatchProcessesManager;
import com.constellio.model.services.factories.ModelLayerFactoryImpl;
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
	@Mock ModelLayerFactoryImpl modelLayerFactory;
	@Mock ModelLayerConfiguration configuration;
	BatchProcessController controller;
	BatchProcessController unspiedController;

	@Before
	public void setUp() {
		when(modelLayerFactory.getConfiguration()).thenReturn(configuration);
		when(modelLayerFactory.getBatchProcessesManager()).thenReturn(batchProcessesManager);
		when(modelLayerFactory.newRecordServices()).thenReturn(recordServices);
		when(modelLayerFactory.getMetadataSchemasManager()).thenReturn(schemasManager);
		when(modelLayerFactory.newSearchServices()).thenReturn(searchServices);
		when(configuration.isBatchProcessesThreadEnabled()).thenReturn(true);
		unspiedController = new BatchProcessController(modelLayerFactory, anInteger());
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

		controller.start();

		verify(controller).newBatchProcessControllerThread();
		verify(thread).setUncaughtExceptionHandler(LoggerUncaughtExceptionHandler.instance);
		verify(thread).start();
	}

	@Test(expected = ControllerAlreadyStarted.class)
	public void givenThreadStartedWhenStartingThenAlreadyStartedRuntimeException()
			throws InterruptedException {
		givenStartedController();

		controller.start();
	}

	@Test
	public void givenThreadStopedWhenStartingThenRestarts()
			throws InterruptedException {
		givenStartedController();
		controller.close();
		controller.start();
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

		controller.start();

		controller.onBatchProcessesListUpdated();

		verify(thread).notifyBatchProcessesListConfigUpdated();
	}

	private void givenStartedController() {
		controller = spy(
				new BatchProcessController(modelLayerFactory, anInteger()));
		doReturn(thread).when(controller).newBatchProcessControllerThread();
		controller.start();
		reset(controller);
	}
}
