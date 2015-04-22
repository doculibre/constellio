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

import java.util.Map;
import java.util.UUID;

public class Email {

	private final String id;

	private final Map<String, String> from;

	private final Map<String, String> to;

	private final String subject;

	private final Object content;

	protected Email(Map<String, String> from, Map<String, String> to, String subject, Object content) {
		super();
		this.id = UUID.randomUUID().toString();
		this.from = from;
		this.to = to;
		this.subject = id + " " + subject;
		this.content = content;
	}

	public String getId() {
		return id;
	}

	public Map<String, String> getFrom() {
		return from;
	}

	public Map<String, String> getTo() {
		return to;
	}

	public String getSubject() {
		return subject;
	}

	public Object getContent() {
		return content;
	}
}
