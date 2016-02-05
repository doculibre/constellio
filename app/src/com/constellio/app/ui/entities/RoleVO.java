package com.constellio.app.ui.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RoleVO implements Serializable {
	private String code;
	private String title;
	private final Set<String> permissions;
	private final Set<String> originalPermissions;

	public RoleVO() {
		this(null, null, new ArrayList<String>());
	}

	public RoleVO(String code, String title, List<String> permissions) {
		this.code = code;
		this.title = title;
		this.permissions = new HashSet<>(permissions);
		this.originalPermissions = new HashSet<>(permissions);
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public List<String> getPermissions() {
		return new ArrayList<>(permissions);
	}

	public boolean hasPermission(String permission) {
		return permissions.contains(permission);
	}

	public void addPermission(String permission) {
		permissions.add(permission);
	}

	public void removePermission(String permission) {
		permissions.remove(permission);
	}

	public boolean isDirty() {
		return !permissions.equals(originalPermissions);
	}

	public boolean isPermissionDirty(String permission) {
		return permissions.contains(permission) != originalPermissions.contains(permission);
	}

	public void markClean() {
		originalPermissions.clear();
		originalPermissions.addAll(permissions);
	}
}
