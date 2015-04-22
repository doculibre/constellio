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
