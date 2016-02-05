package com.constellio.sdk.tests.selenium.conditions;

import java.util.Date;

public abstract class ActionWithTimeout<T> {

	protected abstract T execute()
			throws Exception;

	/**
	 * This method will throw any exception that might have happened right away instead of waiting until the timeout is over.
	 * 
	 * @param totalWaitInMS
	 * @return
	 * @throws Exception
	 */
	public final T executeUntilNotNull(long totalWaitInMS)
			throws Exception {
		long start = new Date().getTime();
		while (new Date().getTime() - start < totalWaitInMS) {
			T result = execute();
			if (result != null) {
				return result;
			} else {
				Thread.sleep(100);
			}
		}
		return null;
	}

	/**
	 * This method will wait until after the timeout to throw any exception that could have occurred.
	 * 
	 * @param totalWaitInMS
	 * @return
	 * @throws Exception
	 */
	public final T tryExecute(long totalWaitInMS)
			throws Exception {
		Exception lastException = null;
		long start = new Date().getTime();
		while (new Date().getTime() - start < totalWaitInMS) {
			try {
				return execute();
			} catch (Exception e) {
				lastException = e;
			}
			Thread.sleep(100);
		}

		if (lastException != null) {
			throw lastException;
		} else {
			throw new RuntimeException("Failed to execute action");
		}
	}

}
