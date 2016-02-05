package com.constellio.app.modules.robots.constants;

import com.constellio.app.modules.robots.ConstellioRobotsModule;
import com.constellio.model.entities.Permissions;

public class RobotsPermissionsTo {
	public static Permissions PERMISSIONS = new Permissions(ConstellioRobotsModule.ID);

	private static String permission(String group, String permission) {
		return PERMISSIONS.add(group, permission);
	}

	// --------------------------------------------

	private static final String ROBOTS = "robots";

	public static final String MANAGE_ROBOTS = permission(ROBOTS, "manageRobots");
}
