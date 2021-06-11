package com.constellio.model.services.security.roles;

import com.constellio.model.entities.security.Role;

import java.util.List;

public interface RolesManagerListener {

	void onRolesModified(String collection, List<Role> newRoles);
}
