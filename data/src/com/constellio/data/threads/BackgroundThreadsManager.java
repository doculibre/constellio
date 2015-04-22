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

import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.threads.BackgroundThreadsManagerRuntimeException.BackgroundThreadsManagerRuntimeException_ManagerMustBeStartedBeforeConfiguringThreads;
import com.constellio.data.threads.BackgroundThreadsManagerRuntimeException.BackgroundThreadsManagerRuntimeException_RepeatInfosNotConfigured;

public class BackgroundThreadsManager implements StatefulService {

	int threadPoolSize;

	ScheduledExecutorService scheduledExecutorService;

	public BackgroundThreadsManager(int threadPoolSize) {
		this.threadPoolSize = threadPoolSize;
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
		scheduledExecutorService.scheduleAtFixedRate(command, delayBeforeTheFirstCommandExecution, executeEverySeconds, unit);
	}

	Runnable getRunnableCommand(BackgroundThreadConfiguration backgroundThreadConfiguration) {
		return new BackgroundThreadCommand(backgroundThreadConfiguration);
	}

	ScheduledExecutorService newScheduledExecutorService() {
		return Executors.newScheduledThreadPool(threadPoolSize);
	}
}


