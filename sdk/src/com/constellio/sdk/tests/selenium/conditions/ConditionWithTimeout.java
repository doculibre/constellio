package com.constellio.sdk.tests.selenium.conditions;

import java.util.Date;

public abstract class ConditionWithTimeout {

	protected abstract boolean evaluate();

	public final void waitForTrue(long totalWaitInMS) {
		RuntimeException lastException = null;
		long start = new Date().getTime();
		while (new Date().getTime() - start < totalWaitInMS) {
			try {
				if (evaluate()) {
					return;
				} else {
					lastException = null;
				}
			} catch (RuntimeException e) {
				lastException = e;
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}

		if (lastException != null) {
			throw new ConditionTimeoutRuntimeException(lastException);
		} else {
			throw new RuntimeException("Failed to execute action");
		}
	}

}
