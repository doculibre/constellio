package com.constellio.app.ui.entities;

import java.io.Serializable;
import java.util.List;

public class RoleAuthVO implements Serializable {

	private String id;
	private String target;
	private List<String> roles;

	public RoleAuthVO(String id, String target, List<String> roles) {
		this.id = id;
		this.target = target;
		this.roles = roles;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public List<String> getRoles() {
		return roles;
	}

	public void setRoles(List<String> roles) {
		this.roles = roles;
	}
}
