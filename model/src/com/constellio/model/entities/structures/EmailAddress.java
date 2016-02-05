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
