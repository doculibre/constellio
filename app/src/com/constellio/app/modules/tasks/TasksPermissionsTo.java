package com.constellio.app.modules.tasks;

import com.constellio.model.entities.Permissions;

public class TasksPermissionsTo {
	public static Permissions PERMISSIONS = new Permissions(TaskModule.ID);

	private static String permission(String group, String permission) {
		return PERMISSIONS.add(group, permission);
	}

	// --------------------------------------------
	// Register groups and permissions below
}
