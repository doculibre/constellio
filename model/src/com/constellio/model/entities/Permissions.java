package com.constellio.model.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.constellio.data.utils.KeyListMap;

public class Permissions {
	private final String id;
	private final KeyListMap<String, String> groupedPermissions;
	private final List<String> permissions;

	public Permissions(String id) {
		this.id = id;
		groupedPermissions = new KeyListMap<>();
		permissions = new ArrayList<>();
	}

	public String add(String group, String permission) {
		String groupCode = id + "." + group;
		String permissionCode = id + "." + permission;
		if (permissions.contains(permissionCode)) {
			throw new Error("Cannot add the same permission twice");
		}
		permissions.add(permissionCode);
		groupedPermissions.add(groupCode, permissionCode);
		return permissionCode;
	}

	public List<String> getAll() {
		return Collections.unmodifiableList(permissions);
	}

	public Map<String, List<String>> getGrouped() {
		return Collections.unmodifiableMap(groupedPermissions.getNestedMap());
	}

	public List<String> getGroup(String group) {
		return Collections.unmodifiableList(groupedPermissions.get(id + "." + group));
	}
}
