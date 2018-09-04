package com.constellio.app.client.entities;

import com.constellio.model.entities.security.global.GlobalGroupStatus;

import java.util.List;

public class GlobalGroupResource {

	private String code;

	private String name;

	private List<String> usersAutomaticallyAddedToCollections;

	private String parent;

	private GlobalGroupStatus status = GlobalGroupStatus.ACTIVE;

	private boolean locallyCreated = true;

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

	public boolean isLocallyCreated() {
		return locallyCreated;
	}

	public void setLocallyCreated(boolean locallyCreated) {
		this.locallyCreated = locallyCreated;
	}
}
