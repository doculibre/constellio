package com.constellio.sdk.tests.concurrent;

public class ConcurrentExecutionResults {

	private int invokations;

	private long duration;

	public ConcurrentExecutionResults(int invokations, long duration) {
		super();
		this.invokations = invokations;
		this.duration = duration;
	}

	public int getInvokations() {
		return invokations;
	}

	public long getDuration() {
		return duration;
	}

}
