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
package com.constellio.app.modules.rm.wrappers.structures;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.joda.time.LocalDateTime;

import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.ModifiableStructure;

public class Comment implements ModifiableStructure {

	String message;
	String userId;
	String username;
	LocalDateTime dateTime;
	boolean dirty;

	public Comment(String message, User user, LocalDateTime dateTime) {
		this.message = message;
		this.userId = user.getId();
		this.username = user.getUsername();
		this.dateTime = dateTime;
	}

	public Comment() {
	}

	public String getUserId() {
		return userId;
	}

	public String getUsername() {
		return username;
	}

	public void setUser(User user) {
		dirty = true;
		if (user != null) {
			userId = user.getId();
			username = user.getUsername();
		}
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		dirty = true;
		this.message = message;
	}

	public LocalDateTime getDateTime() {
		return dateTime;
	}

	public void setDateTime(LocalDateTime dateTime) {
		dirty = true;
		this.dateTime = dateTime;
	}

	@Override
	public boolean isDirty() {
		return dirty;
	}

	@Override
	public String toString() {
		return "Comment{" +
				"message='" + message + '\'' +
				", borrowUserId='" + userId + '\'' +
				", borrowerUsername='" + username + '\'' +
				", dateTime=" + dateTime +
				", dirty=" + dirty +
				'}';
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this, "dirty");
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj, "dirty");
	}
}
