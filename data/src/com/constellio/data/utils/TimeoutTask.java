/**
 * Constellio
 * Copyright (C) 2010 DocuLibre inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.constellio.data.utils;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public abstract class TimeoutTask<T extends Object, E extends Exception> {

	private long timeoutMilliseconds;

	public TimeoutTask(long timeoutMilliseconds) {
		this.timeoutMilliseconds = timeoutMilliseconds;
	}

	public long getTimeoutSecs() {
		return timeoutMilliseconds;
	}

	public void setTimeoutSecs(long timeoutMilliseconds) {
		this.timeoutMilliseconds = timeoutMilliseconds;
	}

	@SuppressWarnings("unchecked")
	public T execute()
			throws E, TimeoutException {
		T result;
		ExecutorService executor = Executors.newFixedThreadPool(1);
		// set the executor thread working
		final Future<T> future = executor.submit(new Callable<T>() {
			@Override
			public T call() {
				try {
					return doExecute();
				} catch (Exception e) {
					throw new TimeOutTaskRuntimeException.CannotDoExcute(e);
				}
			}
		});

		// check the outcome of the executor thread and limit the time allowed for it to complete
		try {
			result = future.get(timeoutMilliseconds == -1 ? Long.MAX_VALUE : timeoutMilliseconds, TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			result = null;
			// ExecutionException: deliverer threw exception
			// TimeoutException: didn't complete within downloadTimeoutSecs
			// InterruptedException: the executor thread was interrupted

			// interrupts the worker thread if necessary
			future.cancel(true);
			onCancel();
			throw e;

		} catch (InterruptedException e) {
			future.cancel(true);
			onCancel();
			throw new TimeOutTaskRuntimeException.Interrupted(e);

		} catch (ExecutionException t) {
			future.cancel(true);
			onCancel();

			Throwable cause1 = t.getCause();
			Throwable cause2 = cause1.getCause();

			// Cast is not checked, but it will only throw ExceptionType or RuntimeException
			throw (E) cause2;

		}
		return result;
	}

	protected void onCancel() {

	}

	protected abstract T doExecute()
			throws E;

}
