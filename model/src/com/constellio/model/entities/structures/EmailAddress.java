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
package com.constellio.model.entities.structures;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.ModifiableStructure;

public class EmailAddress implements ModifiableStructure {

	String name;
	String email;
	boolean dirty;

	public EmailAddress() {
	}

	public EmailAddress(String name, String email) {
		this.name = name;
		this.email = email;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		dirty = true;
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		dirty = true;
		this.email = email;
	}

	@Override
	public boolean isDirty() {
		return dirty;
	}

	@Override
	public String toString() {
		return "EmailAddress{" +
				"name='" + name + '\'' +
				", email='" + email + '\'' +
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

	public static List<EmailAddress> forUsers(List<User> users) {
		List<EmailAddress> addresses = new ArrayList<>();
		if (users != null) {
			for (User user : users) {
				addresses.add(new EmailAddress(user.getTitle(), user.getEmail()));
			}
		}
		return addresses;
	}

}
