package com.constellio.sdk.load.script;

import com.constellio.model.entities.security.global.GlobalGroup;
import com.constellio.model.services.users.UserAddUpdateRequest;

import java.util.List;

public interface UserPreparator {

	public List<GlobalGroup> createGroups();

	public List<UserAddUpdateRequest> createUsers(List<String> groups);

}
