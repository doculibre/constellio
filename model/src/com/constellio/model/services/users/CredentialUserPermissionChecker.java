package com.constellio.model.services.users;

import java.util.List;

import com.constellio.model.entities.records.wrappers.User;

public class CredentialUserPermissionChecker {

	List<User> users;

	public CredentialUserPermissionChecker(List<User> users) {
		this.users = users;
	}

	public boolean globalPermissionInAnyCollection(String permission) {
		for (User user : users) {
			if (user != null && user.has(permission).globally()) {
				return true;
			}
		}
		return false;
	}

	public boolean anyGlobalPermissionInAnyCollection(List<String> permissions) {
		return anyGlobalPermissionInAnyCollection(permissions.toArray(new String[1]));
	}

	public boolean anyGlobalPermissionInAnyCollection(String... permissions) {
		for (User user : users) {
			if (user != null && user.hasAny(permissions).globally()) {
				return true;
			}
		}
		return false;
	}

	public boolean allGlobalPermissionsInAnyCollection(List<String> permissions) {
		return allGlobalPermissionsInAnyCollection(permissions.toArray(new String[1]));
	}

	public boolean allGlobalPermissionsInAnyCollection(String... permissions) {
		for (User user : users) {
			if (user != null && user.hasAll(permissions).globally()) {
				return true;
			}
		}
		return false;
	}
}
