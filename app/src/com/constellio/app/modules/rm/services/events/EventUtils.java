package com.constellio.app.modules.rm.services.events;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.LocalDateTime;

import com.constellio.model.entities.records.wrappers.Event;

/**
 * Created by Nouha on 2015-01-28.
 */
public class EventUtils {

	public static List<Event> keepOnlyMostRecentEventPerUser(List<Event> eventList) {
		Map map = new HashMap<String, Event>();
		for (Event event : eventList){
			String currentUserName = event.getUsername();
			Event userAssociatedEvent = (Event)map.get(currentUserName);
			if (userAssociatedEvent == null){
				map.put(currentUserName, event);
			}else{
				LocalDateTime currentEventDate = event.getCreatedOn();
				LocalDateTime savedEventDate = userAssociatedEvent.getCreatedOn();
				if (savedEventDate.compareTo(currentEventDate) < 0 ){
					map.put(currentUserName, event);
				}
			}
		}
		Collection<Event> mostRecentEvents = map.values();
		return new ArrayList<>(mostRecentEvents);
	}
}
