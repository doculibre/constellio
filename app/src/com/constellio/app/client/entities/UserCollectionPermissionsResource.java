package com.constellio.app.client.entities;

import java.util.ArrayList;
import java.util.List;

public class UserCollectionPermissionsResource {

	String collection;

	String username;

	List<String> roles = new ArrayList<>();

	private boolean readAccess;

	private boolean writeAccess;

	private boolean deleteAccess;

	public String getCollection() {
		return collection;
	}

	public void setCollection(String collection) {
		this.collection = collection;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public List<String> getRoles() {
		return roles;
	}

	public void setRoles(List<String> roles) {
		this.roles = roles;
	}

	public boolean isReadAccess() {
		return readAccess;
	}

	public void setReadAccess(boolean readAccess) {
		this.readAccess = readAccess;
	}

	public boolean isWriteAccess() {
		return writeAccess;
	}

	public void setWriteAccess(boolean writeAccess) {
		this.writeAccess = writeAccess;
	}

	public boolean isDeleteAccess() {
		return deleteAccess;
	}

	public void setDeleteAccess(boolean deleteAccess) {
		this.deleteAccess = deleteAccess;
	}

}
