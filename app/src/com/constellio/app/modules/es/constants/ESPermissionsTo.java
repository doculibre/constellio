package com.constellio.app.modules.es.constants;

import com.constellio.app.modules.es.ConstellioESModule;
import com.constellio.model.entities.Permissions;

public class ESPermissionsTo {
	public static Permissions PERMISSIONS = new Permissions(ConstellioESModule.ID);

	private static String permission(String group, String permission) {
		return PERMISSIONS.add(group, permission);
	}

}
