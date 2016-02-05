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

	public static boolean running = false;

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
			try {
				scheduledExecutorService.awaitTermination(1, TimeUnit.HOURS);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
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
		return Executors.newScheduledThreadPool(4 * dataLayerConfiguration.getBackgroudThreadsPoolSize());
	}

}


