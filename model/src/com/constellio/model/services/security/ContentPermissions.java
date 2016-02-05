package com.constellio.model.services.security;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.constellio.data.utils.ImpossibleRuntimeException;

public final class ContentPermissions {

	public static final ContentPermission READ = new ContentPermission("read");
	public static final ContentPermission WRITE = new ContentPermission("write", READ);
	public static final ContentPermission DELETE = new ContentPermission("delete", READ);

	public static final List<ContentPermission> ALL_CONTENT_PERMISSIONs;

	static {
		List<ContentPermission> allContentPermissions = new ArrayList<>();
		allContentPermissions.add(READ);
		allContentPermissions.add(WRITE);
		allContentPermissions.add(DELETE);

		ALL_CONTENT_PERMISSIONs = Collections.unmodifiableList(allContentPermissions);

	}

	public static ContentPermission get(String permissionCode) {
		for (ContentPermission contentPermission : ALL_CONTENT_PERMISSIONs) {
			if (contentPermission.getCode().equals(permissionCode)) {
				return contentPermission;
			}
		}
		throw new ImpossibleRuntimeException("No such permission : " + permissionCode);
	}

	public static boolean hasPermission(List<ContentPermission> contentPermissions, ContentPermission wantedContentPermission) {
		String wantedPermissionCode = wantedContentPermission.getCode();
		for (ContentPermission contentPermission : contentPermissions) {
			if (contentPermission.getCode().equals(wantedPermissionCode) || contentPermission.getDependencies()
					.contains(wantedPermissionCode)) {
				return true;
			}
		}
		return false;
	}

}
