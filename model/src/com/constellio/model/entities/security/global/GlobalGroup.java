package com.constellio.model.entities.security.global;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class GlobalGroup {

	final String parent;

	final String code;

	final String name;

	final List<String> usersAutomaticallyAddedToCollections;

	final GlobalGroupStatus status;

	public GlobalGroup(String code, String name, List<String> usersAutomaticallyAddedToCollections, String parent,
			GlobalGroupStatus status) {
		this.code = code;
		this.name = name;
		this.usersAutomaticallyAddedToCollections = Collections.unmodifiableList(usersAutomaticallyAddedToCollections);
		this.parent = parent;
		this.status = status;
	}

	public GlobalGroup(String code, String parent, GlobalGroupStatus status) {
		this.code = code;
		this.name = code;
		this.usersAutomaticallyAddedToCollections = Collections.emptyList();
		this.parent = parent;
		this.status = status;
	}

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	public List<String> getUsersAutomaticallyAddedToCollections() {
		return usersAutomaticallyAddedToCollections;
	}

	public String getParent() {
		return parent;
	}

	public GlobalGroupStatus getStatus() {
		return status;
	}

	public GlobalGroup withName(String name) {
		return new GlobalGroup(code, name, usersAutomaticallyAddedToCollections, parent, status);
	}

	public GlobalGroup withUsersAutomaticallyAddedToCollections(List<String> usersAutomaticallyAddedToCollections) {
		return new GlobalGroup(code, name, usersAutomaticallyAddedToCollections, parent, status);
	}


	public GlobalGroup withStatus(GlobalGroupStatus status) {
		return new GlobalGroup(code, name, usersAutomaticallyAddedToCollections, parent, status);
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	@Override
	public String toString() {
		return code;
	}


}
