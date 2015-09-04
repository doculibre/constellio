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

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.constellio.data.conf.DataLayerConfiguration;
import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.threads.BackgroundThreadsManagerRuntimeException.BackgroundThreadsManagerRuntimeException_ManagerMustBeStartedBeforeConfiguringThreads;
import com.constellio.data.threads.BackgroundThreadsManagerRuntimeException.BackgroundThreadsManagerRuntimeException_RepeatInfosNotConfigured;

public class BackgroundThreadsManager implements StatefulService {

	AtomicBoolean systemStarted = new AtomicBoolean(false);

	DataLayerConfiguration dataLayerConfiguration;

	ScheduledExecutorService scheduledExecutorService;

	public BackgroundThreadsManager(DataLayerConfiguration dataLayerConfiguration) {
		this.dataLayerConfiguration = dataLayerConfiguration;
	}

	@Override
	public void initialize() {
		this.scheduledExecutorService = newScheduledExecutorService();
	}

	@Override
	public void close() {
		if (scheduledExecutorService != null) {
			scheduledExecutorService.shutdown();
		}
	}

	public void onSystemStarted() {
		systemStarted.set(true);
	}

	public void configure(BackgroundThreadConfiguration backgroundThreadConfiguration) {
		if (scheduledExecutorService == null) {
			throw new BackgroundThreadsManagerRuntimeException_ManagerMustBeStartedBeforeConfiguringThreads();
		}
		if (backgroundThreadConfiguration.getExecuteEvery() == null) {
			throw new BackgroundThreadsManagerRuntimeException_RepeatInfosNotConfigured();
		}
		Runnable command = getRunnableCommand(backgroundThreadConfiguration);
		long delayBeforeTheFirstCommandExecution = 0;
		long executeEverySeconds = backgroundThreadConfiguration.getExecuteEvery().getStandardSeconds();
		TimeUnit unit = TimeUnit.SECONDS;

		if (dataLayerConfiguration.isBackgroundThreadsEnabled()) {
			scheduledExecutorService.scheduleAtFixedRate(command, delayBeforeTheFirstCommandExecution, executeEverySeconds, unit);
		}
	}

	Runnable getRunnableCommand(BackgroundThreadConfiguration backgroundThreadConfiguration) {
		return new BackgroundThreadCommand(backgroundThreadConfiguration, systemStarted);
	}

	ScheduledExecutorService newScheduledExecutorService() {
		return Executors.newScheduledThreadPool(dataLayerConfiguration.getBackgroudThreadsPoolSize());
	}

}


