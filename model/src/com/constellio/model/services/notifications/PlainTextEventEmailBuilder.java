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
package com.constellio.model.services.notifications;

import java.util.List;

import com.constellio.model.entities.notifications.EmailBuilder;
import com.constellio.model.entities.records.wrappers.Event;

public class PlainTextEventEmailBuilder implements EventEmailBuilder {

	@Override
	public void buildContent(EmailBuilder emailBuilder, List<Event> events) {
		StringBuffer content = new StringBuffer();
		for (Event event : events) {
			content.append(event.getId() + " " + event.getRecordId() + " "
					+ event.getType() + " on " + event.getCreatedOn() + " by " + event.getUsername()
					+ " in " + event.getCollection());
			content.append("\n\n");
		}
		EmailBuilder.setContent(emailBuilder, content.toString());
	}

	@Override
	public void buildSubject(EmailBuilder emailBuilder, List<Event> events) {
		StringBuffer subject = new StringBuffer();
		subject.append("Notification(s) d'event(s): ");
		for (Event event : events) {
			subject.append(event.getType() + " " + event.getRecordId());
			subject.append(" ");
		}
		EmailBuilder.setSubject(emailBuilder, subject.toString());
	}

}
