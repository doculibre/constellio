package com.constellio.data.threads;

import com.constellio.data.conf.DataLayerConfiguration;
import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.threads.BackgroundThreadsManagerRuntimeException.BackgroundThreadsManagerRuntimeException_ManagerMustBeStartedBeforeConfiguringThreads;
import com.constellio.data.threads.BackgroundThreadsManagerRuntimeException.BackgroundThreadsManagerRuntimeException_RepeatInfosNotConfigured;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class BackgroundThreadsManager implements StatefulService {

	AtomicBoolean systemStarted = new AtomicBoolean(false);
	AtomicBoolean stopRequested = new AtomicBoolean(false);

	DataLayerConfiguration dataLayerConfiguration;

	ScheduledExecutorService scheduledExecutorService;

	Semaphore tasksSemaphore;

	DataLayerFactory dataLayerFactory;

	public BackgroundThreadsManager(DataLayerConfiguration dataLayerConfiguration, DataLayerFactory dataLayerFactory) {
		this.dataLayerConfiguration = dataLayerConfiguration;
		this.tasksSemaphore = new Semaphore(1000);
		this.dataLayerFactory = dataLayerFactory;
	}

	@Override
	public void initialize() {
		this.scheduledExecutorService = newScheduledExecutorService();
	}

	@Override
	public void close() {
		stopRequested.set(true);
		if (scheduledExecutorService != null) {
			scheduledExecutorService.shutdownNow();
			try {
				scheduledExecutorService.awaitTermination(1, TimeUnit.HOURS);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
		try {
			tasksSemaphore.acquire(1000);
			tasksSemaphore.release(1000);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
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

		// FIXME added a longer delay as a temp fix for multitenancy test
		long delayBeforeTheFirstCommandExecution = 30;
		long executeEverySeconds = backgroundThreadConfiguration.getExecuteEvery().getStandardSeconds();
		TimeUnit unit = TimeUnit.SECONDS;
		Runnable command = getRunnableCommand(backgroundThreadConfiguration);
		if (dataLayerConfiguration.isBackgroundThreadsEnabled()) {
			scheduledExecutorService
					.scheduleWithFixedDelay(command, delayBeforeTheFirstCommandExecution, executeEverySeconds, unit);
		}
	}

	Runnable getRunnableCommand(BackgroundThreadConfiguration backgroundThreadConfiguration) {
		return new BackgroundThreadCommand(backgroundThreadConfiguration, systemStarted, stopRequested, tasksSemaphore,
				dataLayerFactory);
	}

	ScheduledExecutorService newScheduledExecutorService() {
		return Executors.newScheduledThreadPool(42 * dataLayerConfiguration.getBackgroudThreadsPoolSize());
	}

}


