package com.constellio.app.client.entities;

import java.util.ArrayList;
import java.util.List;

public class GroupCollectionPermissionsResource {

	String collection;

	String groupCode;

	String name;

	List<String> roles = new ArrayList<>();

	public List<String> getRoles() {
		return roles;
	}

	public void setRoles(List<String> roles) {
		this.roles = roles;
	}

	public String getCollection() {
		return collection;
	}

	public void setCollection(String collection) {
		this.collection = collection;
	}

	public String getGroupCode() {
		return groupCode;
	}

	public void setGroupCode(String groupCode) {
		this.groupCode = groupCode;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
