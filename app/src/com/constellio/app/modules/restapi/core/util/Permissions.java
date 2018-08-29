package com.constellio.app.modules.restapi.core.util;

import com.google.common.collect.Sets;
import lombok.experimental.UtilityClass;

import java.util.Collection;
import java.util.Set;

@UtilityClass
public final class Permissions {
	public static final String READ = "READ";
	public static final String WRITE = "WRITE";
	public static final String DELETE = "DELETE";

	private static final Set<String> ALLOWED_PERMISSIONS = Sets.newHashSet(READ, WRITE, DELETE);

	public static boolean contains(String permission) {
		return ALLOWED_PERMISSIONS.contains(permission);
	}

	public static boolean containsAll(Collection<String> permisisons) {
		return ALLOWED_PERMISSIONS.containsAll(permisisons);
	}
}
