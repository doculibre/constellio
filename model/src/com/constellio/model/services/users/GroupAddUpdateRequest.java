package com.constellio.model.services.users;

import java.util.List;

public class GroupAddUpdateRequest {

	private String code;
	private Boolean isGlobal;
	private List<String> roles;
	private String title;
	private String parent;
	private List<String> ancestors;

	public String getCode() {
		return code;
	}

	public GroupAddUpdateRequest setCode(String code) {
		this.code = code;
		return this;
	}

	public Boolean getGlobal() {
		return isGlobal;
	}

	public GroupAddUpdateRequest setGlobal(Boolean global) {
		isGlobal = global;
		return this;
	}

	public List<String> getRoles() {
		return roles;
	}

	public GroupAddUpdateRequest setRoles(List<String> roles) {
		this.roles = roles;
		return this;
	}

	public String getTitle() {
		return title;
	}

	public GroupAddUpdateRequest setTitle(String title) {
		this.title = title;
		return this;
	}

	public String getParent() {
		return parent;
	}

	public GroupAddUpdateRequest setParent(String parent) {
		this.parent = parent;
		return this;
	}

	public List<String> getAncestors() {
		return ancestors;
	}

	public GroupAddUpdateRequest setAncestors(List<String> ancestors) {
		this.ancestors = ancestors;
		return this;
	}
}
