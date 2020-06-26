package com.constellio.data.threads;

import org.joda.time.Duration;
import org.joda.time.LocalTime;

public class BackgroundThreadConfiguration {

	private String id;

	private Runnable repeatedAction;

	private Duration executeEvery;

	private LocalTime from;

	private LocalTime to;

	private BackgroundThreadExceptionHandling exceptionHandling = BackgroundThreadExceptionHandling.STOP;

	private boolean runOnAllInstances;

	private int permitsRequired = 1;

	private BackgroundThreadConfiguration(String id, Runnable repeatedAction) {
		this.id = id;
		this.repeatedAction = repeatedAction;
	}

	public static BackgroundThreadConfiguration repeatingAction(String id, Runnable repeatedAction) {
		return new BackgroundThreadConfiguration(id, repeatedAction);
	}

	public BackgroundThreadConfiguration executedEvery(Duration duration) {
		this.executeEvery = duration;
		return this;
	}

	public BackgroundThreadConfiguration between(LocalTime from, LocalTime to) {
		this.from = from;
		this.to = to;
		return this;
	}

	public BackgroundThreadConfiguration handlingExceptionWith(BackgroundThreadExceptionHandling exceptionHandling) {
		this.exceptionHandling = exceptionHandling;
		return this;
	}

	public BackgroundThreadConfiguration runningOnAllInstances() {
		this.runOnAllInstances = true;
		return this;
	}

	public BackgroundThreadConfiguration usingPermits(int permitsRequired) {
		this.permitsRequired = permitsRequired;
		return this;
	}

	public boolean isRunOnAllInstances() {
		return runOnAllInstances;
	}

	public Runnable getRepeatedAction() {
		return repeatedAction;
	}

	public Duration getExecuteEvery() {
		return executeEvery;
	}

	public LocalTime getFrom() {
		return from;
	}

	public LocalTime getTo() {
		return to;
	}

	public String getId() {
		return id;
	}

	public BackgroundThreadExceptionHandling getExceptionHandling() {
		return exceptionHandling;
	}

	public int getPermitsRequired() {
		return permitsRequired;
	}
}
