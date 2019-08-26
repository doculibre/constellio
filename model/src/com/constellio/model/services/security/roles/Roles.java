package com.constellio.model.services.security.roles;

import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.Role;
import com.constellio.model.entities.security.SecurityModel;
import com.constellio.model.services.records.SchemasRecordsServices;

import java.util.ArrayList;
import java.util.List;

public class Roles {

	List<Role> roles;

	SchemasRecordsServices schemasRecordsServices;

	public Roles(List<Role> roles, SchemasRecordsServices schemasRecordsServices) {
		this.roles = roles;
		this.schemasRecordsServices = schemasRecordsServices;
	}

	public SchemasRecordsServices getSchemasRecordsServices() {
		return schemasRecordsServices;
	}

	public boolean has(User user, String permission) {
		for (String userRole : user.getAllRoles()) {
			Role role = getRole(userRole);
			if (role.hasOperationPermission(permission)) {
				return true;
			}
		}
		return false;
	}

	public boolean hasAll(User user, String... permissions) {

		for (String permission : permissions) {
			boolean hasPermission = has(user, permission);
			if (!hasPermission) {
				return false;
			}
		}
		return true;
	}

	public boolean hasAny(User user, String... permissions) {

		for (String permission : permissions) {
			boolean hasPermission = has(user, permission);
			if (hasPermission) {
				return true;
			}
		}
		return false;
	}

	public Role getRole(String code) {
		for (Role role : roles) {
			if (role.getCode().equals(code)) {
				return role;
			}
		}
		return null;
	}

	public List<Role> getRolesGivingPermission(String permission) {
		List<Role> returnedRoles = new ArrayList<>();
		for (Role role : roles) {
			if (role.getOperationPermissions().contains(permission)) {
				returnedRoles.add(role);
			}
		}
		return returnedRoles;
	}

	public SecurityModel getSecurityModel() {
		return schemasRecordsServices.getModelLayerFactory().newRecordServices()
				.getSecurityModel(schemasRecordsServices.getCollection());

	}
}
