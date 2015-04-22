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
