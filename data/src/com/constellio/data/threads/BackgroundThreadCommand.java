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

import java.util.concurrent.atomic.AtomicBoolean;

import org.joda.time.LocalTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.data.utils.TimeProvider;

public class BackgroundThreadCommand implements Runnable {

	BackgroundThreadConfiguration configuration;

	Logger logger;

	String threadName;

	AtomicBoolean systemStarted = new AtomicBoolean();

	public BackgroundThreadCommand(BackgroundThreadConfiguration configuration, AtomicBoolean systemStarted) {
		this.configuration = configuration;
		this.logger = LoggerFactory.getLogger(configuration.getRepeatedAction().getClass());
		this.threadName = configuration.getId() + " (" + configuration.getRepeatedAction().getClass().getName() + ")";
		this.systemStarted = systemStarted;
	}

	@Override
	public void run() {

		while (!systemStarted.get()) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}

		if (configuration.getFrom() == null || configuration.getTo() == null || isBetweenInterval()) {
			runAndHandleException();
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
		logCommandCall();
		try {
			configuration.getRepeatedAction().run();
			logCommandCallEnd();
		} catch (RuntimeException e) {
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

	public void logCommandCallEndedWithException(RuntimeException e) {
		logger.error("Execution of background thread " + threadName + "' has thrown an exception", e);
	}

}