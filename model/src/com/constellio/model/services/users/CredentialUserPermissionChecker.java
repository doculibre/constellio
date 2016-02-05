package com.constellio.model.services.users;

import com.constellio.model.entities.records.wrappers.User;

import java.util.List;

public class CredentialUserPermissionChecker {

	List<User> users;

	public CredentialUserPermissionChecker(List<User> users) {
		this.users = users;
	}

	public boolean globalPermissionInAnyCollection(String permission) {
		for (User user : users) {
			if (user.has(permission).globally()) {
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
			if (user.hasAny(permissions).globally()) {
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
			if (user.hasAll(permissions).globally()) {
				return true;
			}
		}
		return false;
	}
}
