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
package com.constellio.app.ui.pages.events;

import java.util.List;

import com.constellio.app.ui.tools.RecordContainerWebElement;
import com.constellio.app.ui.tools.RecordContainerWebElement.RecordContainerWebElementRow;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;

public class EventViewFacade {
	protected RecordContainerWebElement eventsContainerWebElement;

	public EventViewFacade(ConstellioWebDriver driver) {
		eventsContainerWebElement = new RecordContainerWebElement(driver.find(EventViewImpl.EVENT_TABLE_STYLE));
	}

	public String getFirstEventValueAtColumn(String columnTitle) {
		RecordContainerWebElementRow firstRow = eventsContainerWebElement.getRow(0);
		List<String> headerTitles = eventsContainerWebElement.getHeaderTitles();
		for(int columnIndex = 0; columnIndex < headerTitles.size(); columnIndex++){
			String currentColumnTitle = headerTitles.get(columnIndex);
			if(currentColumnTitle.equals(columnTitle)){
				return firstRow.getValueInColumn(columnIndex);
			}
		}
		throw new NotFoundColumnTitleRuntimeException(columnTitle);
	}

	private class NotFoundColumnTitleRuntimeException extends RuntimeException {
		public NotFoundColumnTitleRuntimeException(String columnTitle) {
			super(columnTitle);
		}
	}
}
