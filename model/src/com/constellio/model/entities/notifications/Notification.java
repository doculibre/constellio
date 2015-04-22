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
package com.constellio.model.entities.notifications;

import org.joda.time.LocalDateTime;

public class Notification {

	private String id;
	private final String user;
	private final String idEvent;
	private final LocalDateTime createdOn;
	private final LocalDateTime seenOn;
	private final LocalDateTime sentOn;

	public Notification(String user, String idEvent, LocalDateTime createdOn) {
		super();
		this.user = user;
		this.idEvent = idEvent;
		this.createdOn = createdOn;
		this.seenOn = null;
		this.sentOn = null;
	}

	public Notification(String id, String user, String idEvent, LocalDateTime createdOn, LocalDateTime seenOn,
			LocalDateTime sentOn) {
		super();
		this.id = id;
		this.user = user;
		this.idEvent = idEvent;
		this.createdOn = createdOn;
		this.seenOn = seenOn;
		this.sentOn = sentOn;
	}

	public String getId() {
		return id;
	}

	public String getUser() {
		return user;
	}

	public String getIdEvent() {
		return idEvent;
	}

	public LocalDateTime getCreatedOn() {
		return createdOn;
	}

	public LocalDateTime getSeenOn() {
		return seenOn;
	}

	public LocalDateTime getSentOn() {
		return sentOn;
	}
}
