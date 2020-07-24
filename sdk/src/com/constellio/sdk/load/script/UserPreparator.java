package com.constellio.sdk.load.script;

import com.constellio.model.entities.security.global.GroupAddUpdateRequest;

import java.util.List;

public interface UserPreparator {

	public List<GroupAddUpdateRequest> createGroups();

	public List<com.constellio.model.services.users.UserAddUpdateRequest> createUsers(List<String> groups);

}
