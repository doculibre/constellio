package com.constellio.app.client.entities;

import java.util.List;

import com.constellio.model.entities.security.global.GlobalGroupStatus;

public class GlobalGroupResource {

	private String code;

	private String name;

	private List<String> usersAutomaticallyAddedToCollections;

	private String parent;

	private GlobalGroupStatus status = GlobalGroupStatus.ACTIVE;

	public GlobalGroupResource() {
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getUsersAutomaticallyAddedToCollections() {
		return usersAutomaticallyAddedToCollections;
	}

	public void setUsersAutomaticallyAddedToCollections(List<String> usersAutomaticallyAddedToCollections) {
		this.usersAutomaticallyAddedToCollections = usersAutomaticallyAddedToCollections;
	}

	public void setParent(String parent) {
		this.parent = parent;
	}

	public String getParent() {
		return parent;
	}

	public GlobalGroupStatus getStatus() {
		return status;
	}

	public void setStatus(GlobalGroupStatus status) {
		this.status = status;
	}
}
