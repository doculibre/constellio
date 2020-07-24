package com.constellio.data.threads;

import com.constellio.data.conf.DataLayerConfiguration;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.threads.BackgroundThreadsManagerRuntimeException.BackgroundThreadsManagerRuntimeException_ManagerMustBeStartedBeforeConfiguringThreads;
import com.constellio.data.threads.BackgroundThreadsManagerRuntimeException.BackgroundThreadsManagerRuntimeException_RepeatInfosNotConfigured;
import com.constellio.sdk.tests.ConstellioTest;
import org.joda.time.Duration;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.concurrent.ScheduledExecutorService;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class BackgroundThreadsManagerUnitTest extends ConstellioTest {

	@Mock DataLayerFactory dataLayerFactory;
	@Mock DataLayerConfiguration dataLayerConfiguration;
	@Mock Runnable repeatedAction, command;
	@Mock ScheduledExecutorService scheduledExecutorService;
	BackgroundThreadsManager backgroundThreadsManager;

	@Before
	public void setUp()
			throws Exception {

		when(dataLayerConfiguration.isBackgroundThreadsEnabled()).thenReturn(true);
		when(dataLayerConfiguration.getBackgroudThreadsPoolSize()).thenReturn(4);

		backgroundThreadsManager = spy(new BackgroundThreadsManager(dataLayerConfiguration, dataLayerFactory));
		doReturn(scheduledExecutorService).when(backgroundThreadsManager).newScheduledExecutorService();
	}

	@Test(expected = BackgroundThreadsManagerRuntimeException_ManagerMustBeStartedBeforeConfiguringThreads.class)
	public void givenNotStartedBackgroundThreadConfigurationThenCannotConfigureThreads() {
		BackgroundThreadConfiguration configuration = BackgroundThreadConfiguration.repeatingAction("zeAction", repeatedAction)
				.executedEvery(Duration.standardMinutes(42));
		doReturn(command).when(backgroundThreadsManager).getRunnableCommand(configuration);

		backgroundThreadsManager.configure(configuration);

	}


	@Test(expected = BackgroundThreadsManagerRuntimeException_RepeatInfosNotConfigured.class)
	public void whenConfiguringThreadWithoutRepeatInfosThenException() {
		backgroundThreadsManager.initialize();
		BackgroundThreadConfiguration configuration = BackgroundThreadConfiguration.repeatingAction("zeAction", repeatedAction);
		backgroundThreadsManager.configure(configuration);

	}

}
