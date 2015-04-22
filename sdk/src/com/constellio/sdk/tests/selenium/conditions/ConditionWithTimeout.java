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
