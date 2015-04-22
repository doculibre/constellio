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

import com.constellio.model.services.notifications.EmailBuilderRuntimeException.EmailBuilderRuntimeException_InvalidEmail;
import com.constellio.model.services.notifications.EmailBuilderRuntimeException.EmailBuilderRuntimeException_MissingAttribute;

public final class EmailBuilder {

	Map<String, String> from;

	Map<String, String> to;

	String subject;

	Object content;

	public static EmailBuilder setFrom(EmailBuilder builder, Map<String, String> from) {
		builder.from = from;
		return builder;
	}

	public static EmailBuilder setTo(EmailBuilder builder, Map<String, String> to) {
		builder.to = to;
		return builder;
	}

	public static EmailBuilder setSubject(EmailBuilder builder, String subject) {
		builder.subject = subject;
		return builder;
	}

	public static EmailBuilder setContent(EmailBuilder builder, Object content) {
		builder.content = content;
		return builder;
	}

	public Email build() {
		String emailPattern = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
		if (this.from == null || !this.from.keySet().iterator().next().matches(emailPattern)) {
			throw new EmailBuilderRuntimeException_InvalidEmail("from");
		}

		for (String key : this.to.keySet()) {
			if (key == null || !key.matches(emailPattern)) {
				throw new EmailBuilderRuntimeException_InvalidEmail(key);
			}
		}
		if (this.content == null || this.content.toString().isEmpty()) {
			throw new EmailBuilderRuntimeException_MissingAttribute("content");
		}
		if (this.subject == null || this.subject.isEmpty()) {
			throw new EmailBuilderRuntimeException_MissingAttribute("subject");
		}
		return new Email(from, to, subject, content);
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
