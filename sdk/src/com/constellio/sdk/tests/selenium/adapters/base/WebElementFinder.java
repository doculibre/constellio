/**
 * Constellio
 * Copyright (C) 2010 DocuLibre inc.
 *
 * This program is free software: you can redistribute it and/or modifyTo
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
package com.constellio.sdk.tests.selenium.adapters.base;

import java.util.Date;

import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class WebElementFinder<T extends WebElement> {
	private static final Logger LOGGER = LoggerFactory.getLogger(WebElementFinder.class);

	private T cache;

	private long version = 0;

	protected abstract T get();

	public abstract String getOperationDescription();

	public T getUsingCache() {
		if (cache == null) {
			cache = getWarningWhenLongToExecute();
		} else {
			try {
				cache.getLocation();
				//				cache.getText();
			} catch (Exception e) {
				System.out.println("FOUND invalid element in cache!");
				cache = getWarningWhenLongToExecute();
				version++;
			}
		}

		return cache;
	}

	public long getVersion() {
		getUsingCache();
		return version;
	}

	public T getWarningWhenLongToExecute() {
		long before = new Date().getTime();
		T value = get();
		long after = new Date().getTime();
		long executedIn = after - before;

		if (executedIn > 50) {
			LOGGER.warn(getOperationDescription() + " took " + executedIn + "ms.");
		}

		return value;
	}
}
