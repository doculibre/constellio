package com.constellio.sdk.tests;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

public class FailureDetectionTestWatcher extends TestWatcher {

	private boolean failed;

	FailureDetectionTestWatcherListener listener;

	public FailureDetectionTestWatcher(FailureDetectionTestWatcherListener listener) {
		this.listener = listener;
	}

	@Override
	protected void failed(Throwable e, Description description) {
		e.printStackTrace();
		listener.failed(e, description);
		failed = true;
	}

	@Override
	protected void finished(Description description) {
		if (!failed) {
			listener.finished(description);
		}
	}

	public static interface FailureDetectionTestWatcherListener {

		void failed(Throwable e, Description description);

		void finished(Description description);

	}

	public boolean isFailed() {
		return failed;
	}
}
