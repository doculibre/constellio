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
