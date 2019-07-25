package com.constellio.sdk.tests;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.fail;

public class FailureDetectionTestWatcher extends TestWatcher {

	private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(FailureDetectionTestWatcher.class);

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
			if (ConstellioTest.getInstance() != null) {
				String failMessage = ConstellioTest.getInstance().getFailMessage();
				if (failMessage != null) {
					fail(failMessage);
				}
			}
		} else {
			if (ConstellioTest.getInstance() != null) {
				String failMessage = ConstellioTest.getInstance().getFailMessage();
				if (failMessage != null) {
					LOGGER.warn("A problem was detected that may have caused test failure. " + failMessage);
				}
			}
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
