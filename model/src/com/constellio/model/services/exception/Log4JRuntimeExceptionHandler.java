package com.constellio.model.services.exception;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Log4JRuntimeExceptionHandler implements Thread.UncaughtExceptionHandler {

	private static Logger log = LogManager.getLogger(Log4JRuntimeExceptionHandler.class);

	public void uncaughtException(Thread t, Throwable ex) {
		log.fatal("Uncaught exception in thread: " + t.getName(), ex);
	}

}