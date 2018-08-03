package com.constellio.app.modules.rm.services.events;

import com.constellio.model.entities.records.wrappers.Event;
import org.joda.time.LocalDateTime;

import java.util.*;

/**
 * Created by Nouha on 2015-01-28.
 */
public class EventUtils {

	public static List<Event> keepOnlyMostRecentEventPerUser(List<Event> eventList) {
		Map map = new HashMap<String, Event>();
		for (Event event : eventList) {
			String currentUserName = event.getUsername();
			Event userAssociatedEvent = (Event) map.get(currentUserName);
			if (userAssociatedEvent == null) {
				map.put(currentUserName, event);
			} else {
				LocalDateTime currentEventDate = event.getCreatedOn();
				LocalDateTime savedEventDate = userAssociatedEvent.getCreatedOn();
				if (savedEventDate.compareTo(currentEventDate) < 0) {
					map.put(currentUserName, event);
				}
			}
		}
		Collection<Event> mostRecentEvents = map.values();
		return new ArrayList<>(mostRecentEvents);
	}
}
