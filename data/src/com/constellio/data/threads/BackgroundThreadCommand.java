package com.constellio.data.threads;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

import org.joda.time.LocalTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.utils.TimeProvider;

public class BackgroundThreadCommand implements Runnable {

	BackgroundThreadConfiguration configuration;

	Logger logger;

	String threadName;

	AtomicBoolean stopRequested;
	AtomicBoolean systemStarted;
	Semaphore tasksSemaphore;

	public BackgroundThreadCommand(BackgroundThreadConfiguration configuration, AtomicBoolean systemStarted,
			AtomicBoolean stopRequested, Semaphore tasksSemaphore, DataLayerFactory dataLayerFactory) {
		this.configuration = configuration;
		this.tasksSemaphore = tasksSemaphore;
		this.logger = LoggerFactory.getLogger(configuration.getRepeatedAction().getClass());
		this.threadName = dataLayerFactory
				.toResourceName(configuration.getId() + " (" + configuration.getRepeatedAction().getClass().getName() + ")");
		this.stopRequested = stopRequested;
		this.systemStarted = systemStarted;
	}

	@Override
	public void run() {

		while (!systemStarted.get() && !stopRequested.get()) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}

		if ((configuration.getFrom() == null || configuration.getTo() == null || isBetweenInterval())
				&& !stopRequested.get()) {
			try {
				tasksSemaphore.acquire();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			try {
				runAndHandleException();
			} finally {
				tasksSemaphore.release();
			}
		}

	}

	private boolean isBetweenInterval() {
		LocalTime localTime = TimeProvider.getLocalDateTime().toLocalTime();
		if (configuration.getFrom().isBefore(configuration.getTo())) {
			return localTime.isAfter(configuration.getFrom()) && localTime.isBefore(configuration.getTo());
		} else {
			return localTime.isAfter(configuration.getFrom()) || localTime.isBefore(configuration.getTo());
		}
	}

	public void runAndHandleException() {
		setCurrentThreadName();
		//logCommandCall();
		try {
			configuration.getRepeatedAction().run();
			//logCommandCallEnd();
		} catch (Throwable e) {
			logCommandCallEndedWithException(e);
			if (configuration.getExceptionHandling() == BackgroundThreadExceptionHandling.STOP) {
				throw e;
			}
		}

	}

	public void setCurrentThreadName() {
		Thread.currentThread().setName(threadName);
	}

	public void logCommandCall() {
		logger.info("Executing background thread " + threadName);
	}

	public void logCommandCallEnd() {
		logger.info("Execution of background thread " + threadName + "' ended normally");
	}

	public void logCommandCallEndedWithException(Throwable e) {
		logger.error("Execution of background thread " + threadName + "' has thrown an exception", e);
	}

}