package com.constellio.model.entities.records.wrappers;

import static java.util.Arrays.asList;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.data.utils.LangUtils;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.security.roles.Roles;

public class RolesUserPermissionsChecker extends UserPermissionsChecker {

	private static final Logger LOGGER = LoggerFactory.getLogger(RolesUserPermissionsChecker.class);

	MetadataSchemaTypes types;

	Roles roles;

	boolean anyRoles;

	String[] permissions;

	RolesUserPermissionsChecker(User user, MetadataSchemaTypes types, Roles roles) {
		super(user);
		this.types = types;
		this.roles = roles;
	}

	public boolean globally() {
		if (user.isSystemAdmin()) {
			return true;
		} else if (anyRoles) {
			return roles.hasAny(user, permissions);
		} else {
			return roles.hasAll(user, permissions);
		}
	}

	private Set<String> getUserPermissionsOnRecord(Record record) {
		Set<String> permissions = new HashSet<>();

		Set<String> allRolesOnRecord = UserAuthorizationsUtils.getRolesOnRecord(user, record);

		for (String role : allRolesOnRecord) {
			permissions.addAll(roles.getRole(role).getOperationPermissions());
		}

		for (String userRoleCode : user.getAllRoles()) {
			Role role = roles.getRole(userRoleCode);
			permissions.addAll(role.getOperationPermissions());
		}
		return permissions;
	}

	public boolean on(Record record) {
		if (user.isSystemAdmin()) {
			return true;
		}
		Set<String> userPermissionsOnRecord = getUserPermissionsOnRecord(record);

		if (anyRoles) {
			boolean result = LangUtils.containsAny(asList(permissions), LangUtils.withoutNulls(userPermissionsOnRecord));

			if (!result) {
				LOGGER.info("User '" + user.getUsername() + "' has no permissions in " + StringUtils
						.join(userPermissionsOnRecord, ", ") + " on record '" + record.getIdTitle() + "'");
			}

			return result;
		} else {

			for (String permission : permissions) {
				if (permission != null && !userPermissionsOnRecord.contains(permission)) {
					LOGGER.info("User '" + user.getUsername() + "' doesn't have permission '" + permission
							+ "' on record '" + record.getIdTitle() + "'");
					return false;
				}

			}
			return true;
		}

	}

	@Override
	public boolean onSomething() {
		Set<String> allUserPermissions = new HashSet<>();
		List<String> userTokens = user.getUserTokens();
		for (String userToken : userTokens) {
			for (String authorizationRoleCode : userToken.split("_")[1].split(",")) {
				Role role = roles.getRole(authorizationRoleCode);
				if (role != null) {
					allUserPermissions.addAll(role.getOperationPermissions());
				}
			}
		}
		for (String userRoleCode : user.getAllRoles()) {
			Role role = roles.getRole(userRoleCode);
			allUserPermissions.addAll(role.getOperationPermissions());
		}
		if (anyRoles) {
			boolean result = LangUtils.containsAny(asList(permissions), LangUtils.withoutNulls(allUserPermissions));

			if (!result) {
				LOGGER.info("User '" + user.getUsername() + "' has no permissions in " + StringUtils
						.join(allUserPermissions, ", ") + " on something");
			}

			return result;
		} else {

			for (String permission : permissions) {
				if (permission != null && !allUserPermissions.contains(permission)) {
					LOGGER.info("User '" + user.getUsername() + "' doesn't have permission '" + permission + "' on something");
					return false;
				}

			}
			return true;
		}
	}
}
