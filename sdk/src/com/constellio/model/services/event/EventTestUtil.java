package com.constellio.model.services.event;

import com.constellio.model.entities.records.wrappers.Event;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class EventTestUtil {
	public static void assertEvent(XMLStreamReader xmlStreamReader, List<Event> event, int numberOfEventToBeExpected)
			throws XMLStreamException {
		int eventCounter = 0;
		while (xmlStreamReader.hasNext()) {
			int elementType = xmlStreamReader.next();
			if (elementType == XMLStreamConstants.START_ELEMENT && xmlStreamReader.getLocalName().equals("Event")) {
				;
				for (int i = 0; i < xmlStreamReader.getAttributeCount(); i++) {
					String key = xmlStreamReader.getAttributeLocalName(i);
					assertThat(event.get(eventCounter).get(key).toString()).isEqualTo(xmlStreamReader.getAttributeValue(i));
				}
				eventCounter++;
			}
		}
		assertThat(eventCounter).isEqualTo(numberOfEventToBeExpected);
	}

	public static LocalDateTime getLocalDateTimeFromString(String dateTimeAsString) {
		DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(EventService.DATE_TIME_FORMAT);
		return dateTimeFormatter.parseLocalDateTime(dateTimeAsString);
	}
}
