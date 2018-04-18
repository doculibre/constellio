package com.constellio.model.entities.records.wrappers;

import static com.constellio.model.entities.security.Role.DELETE;
import static com.constellio.model.entities.security.Role.READ;
import static com.constellio.model.entities.security.Role.WRITE;
import static java.util.Arrays.asList;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.data.utils.LangUtils;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.security.Role;
import com.constellio.model.entities.security.global.AuthorizationDetails;
import com.constellio.model.entities.security.global.UserCredentialStatus;
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
		if (user.getStatus() != UserCredentialStatus.ACTIVE) {
			return false;
		} else if (user.isSystemAdmin()) {
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

	private Set<String> getUserPermissionsSpecificallyOnRecord(Record record) {
		Set<String> permissions = new HashSet<>();

		Set<String> allRolesOnRecord = UserAuthorizationsUtils.getRolesSpecificallyOnRecord(user, record);

		for (String role : allRolesOnRecord) {
			permissions.addAll(roles.getRole(role).getOperationPermissions());
		}

		return permissions;
	}

	public boolean on(Record record) {
		if (user.getStatus() != UserCredentialStatus.ACTIVE) {
			return false;
		} else if (user.isSystemAdmin()) {
			return true;
		}
		Set<String> userPermissionsOnRecord = getUserPermissionsOnRecord(record);

		if (anyRoles) {
			boolean result = LangUtils.containsAny(asList(permissions), LangUtils.withoutNulls(userPermissionsOnRecord));

			if (!result) {
				//				LOGGER.info("User '" + user.getUsername() + "' has no permissions in " + StringUtils
				//						.join(userPermissionsOnRecord, ", ") + " on record '" + record.getIdTitle() + "'");
			}

			return result;
		} else {

			for (String permission : permissions) {
				if (permission != null && !userPermissionsOnRecord.contains(permission)) {
					//					LOGGER.info("User '" + user.getUsername() + "' doesn't have permission '" + permission
					//							+ "' on record '" + record.getIdTitle() + "'");
					return false;
				}

			}
			return true;
		}

	}

	public boolean specificallyOn(Record record) {
		if (user.getStatus() != UserCredentialStatus.ACTIVE) {
			return false;
		}
		Set<String> userPermissionsOnRecord = getUserPermissionsSpecificallyOnRecord(record);

		if (anyRoles) {
			return LangUtils.containsAny(asList(permissions), LangUtils.withoutNulls(userPermissionsOnRecord));
		} else {
			for (String permission : permissions) {
				if (permission != null && !userPermissionsOnRecord.contains(permission)) {
					return false;
				}
			}
			return true;
		}
	}

	@Override
	public boolean onSomething() {
		if (user.getStatus() != UserCredentialStatus.ACTIVE) {
			return false;
		}
		Set<String> allUserPermissions = new HashSet<>();
		for (String authId : user.getAllUserAuthorizations()) {
			try {
				AuthorizationDetails details = user.getAuthorizationDetail(authId);
				for (String roleOrAccess : details.getRoles()) {
					if (!roleOrAccess.equals(READ) && !roleOrAccess.equals(WRITE) && !roleOrAccess.equals(DELETE)) {
						Role role = roles.getRole(roleOrAccess);
						if (role != null) {
							allUserPermissions.addAll(role.getOperationPermissions());
						}
					}
				}
			} catch (Exception e) {
				LOGGER.error(e.toString());
			}
		}

		for (String userRoleCode : user.getAllRoles()) {
			Role role = roles.getRole(userRoleCode);
			allUserPermissions.addAll(role.getOperationPermissions());
		}
		if (anyRoles) {
			boolean result = LangUtils.containsAny(asList(permissions), LangUtils.withoutNulls(allUserPermissions));

			if (!result) {
				//LOGGER.info("User '" + user.getUsername() + "' has no permissions in " + StringUtils
				//		.join(allUserPermissions, ", ") + " on something");
			}

			return result;
		} else {

			for (String permission : permissions) {
				if (permission != null && !allUserPermissions.contains(permission)) {
					//LOGGER.info("User '" + user.getUsername() + "' doesn't have permission '" + permission + "' on something");
					return false;
				}

			}
			return true;
		}
	}
}
