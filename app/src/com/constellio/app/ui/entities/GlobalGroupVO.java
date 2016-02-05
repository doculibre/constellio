package com.constellio.app.ui.entities;

import java.io.Serializable;
import java.util.Set;

import com.constellio.model.entities.security.global.GlobalGroupStatus;

@SuppressWarnings("serial")
public class GlobalGroupVO implements Serializable {

	String parent;

	String code;

	String name;

	Set<String> collections;

	GlobalGroupStatus status;

	public GlobalGroupVO() {
		this.status = GlobalGroupStatus.ACTIVE;
	}

	public GlobalGroupVO(String parent) {
		this.parent = parent;
		this.status = GlobalGroupStatus.ACTIVE;
	}

	public GlobalGroupVO(String code, String name, Set<String> collections, String parent, GlobalGroupStatus status) {
		this.code = code;
		this.name = name;
		this.collections = collections;
		this.parent = parent;
		this.status = status;
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

	public Set<String> getCollections() {
		return collections;
	}

	public void setCollections(Set<String> collections) {
		this.collections = collections;
	}

	public String getParent() {
		return parent;
	}

	public void setParent(String parent) {
		this.parent = parent;
	}

	public GlobalGroupStatus getStatus() {
		return status;
	}

	public void setStatus(GlobalGroupStatus status) {
		this.status = status;
	}
}


