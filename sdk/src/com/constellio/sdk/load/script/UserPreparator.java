package com.constellio.sdk.load.script;

import java.util.List;

import com.constellio.model.entities.security.global.GlobalGroup;
import com.constellio.model.entities.security.global.UserCredential;

public interface UserPreparator {

	public List<GlobalGroup> createGroups();

	public List<UserCredential> createUsers(List<String> groups);

}
