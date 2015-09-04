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
package com.constellio.data.threads;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.joda.time.Duration;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.data.conf.DataLayerConfiguration;
import com.constellio.data.threads.BackgroundThreadsManagerRuntimeException.BackgroundThreadsManagerRuntimeException_ManagerMustBeStartedBeforeConfiguringThreads;
import com.constellio.data.threads.BackgroundThreadsManagerRuntimeException.BackgroundThreadsManagerRuntimeException_RepeatInfosNotConfigured;
import com.constellio.sdk.tests.ConstellioTest;

public class BackgroundThreadsManagerUnitTest extends ConstellioTest {

	@Mock DataLayerConfiguration dataLayerConfiguration;
	@Mock Runnable repeatedAction, command;
	@Mock ScheduledExecutorService scheduledExecutorService;
	BackgroundThreadsManager backgroundThreadsManager;

	@Before
	public void setUp()
			throws Exception {

		when(dataLayerConfiguration.isBackgroundThreadsEnabled()).thenReturn(true);
		when(dataLayerConfiguration.getBackgroudThreadsPoolSize()).thenReturn(4);

		backgroundThreadsManager = spy(new BackgroundThreadsManager(dataLayerConfiguration));
		doReturn(scheduledExecutorService).when(backgroundThreadsManager).newScheduledExecutorService();
	}

	@Test(expected = BackgroundThreadsManagerRuntimeException_ManagerMustBeStartedBeforeConfiguringThreads.class)
	public void givenNotStartedBackgroundThreadConfigurationThenCannotConfigureThreads() {
		BackgroundThreadConfiguration configuration = BackgroundThreadConfiguration.repeatingAction("zeAction", repeatedAction)
				.executedEvery(Duration.standardMinutes(42));
		doReturn(command).when(backgroundThreadsManager).getRunnableCommand(configuration);

		backgroundThreadsManager.configure(configuration);

	}

	@Test
	public void whenConfiguringThreadThenConfiguredWithCorrectInfos() {
		backgroundThreadsManager.initialize();
		BackgroundThreadConfiguration configuration = BackgroundThreadConfiguration.repeatingAction("zeAction", repeatedAction)
				.executedEvery(Duration.standardMinutes(42));
		doReturn(command).when(backgroundThreadsManager).getRunnableCommand(configuration);

		backgroundThreadsManager.configure(configuration);

		verify(scheduledExecutorService).scheduleAtFixedRate(command, 0, 42 * 60, TimeUnit.SECONDS);

	}

	@Test(expected = BackgroundThreadsManagerRuntimeException_RepeatInfosNotConfigured.class)
	public void whenConfiguringThreadWithoutRepeatInfosThenException() {
		backgroundThreadsManager.initialize();
		BackgroundThreadConfiguration configuration = BackgroundThreadConfiguration.repeatingAction("zeAction", repeatedAction);
		backgroundThreadsManager.configure(configuration);

	}

}
