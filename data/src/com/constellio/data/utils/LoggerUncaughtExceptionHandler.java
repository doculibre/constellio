package com.constellio.data.utils;

import java.lang.Thread.UncaughtExceptionHandler;

import org.slf4j.LoggerFactory;

public class LoggerUncaughtExceptionHandler implements UncaughtExceptionHandler {

	public static final LoggerUncaughtExceptionHandler instance = new LoggerUncaughtExceptionHandler();

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		LoggerFactory.getLogger(t.getClass()).error("Uncaught thread exception", e);
	}

}
