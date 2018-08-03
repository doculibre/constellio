package com.constellio.sdk.load.script;

import com.constellio.model.entities.security.global.GlobalGroup;
import com.constellio.model.entities.security.global.UserCredential;

import java.util.List;

public interface UserPreparator {

	public List<GlobalGroup> createGroups();

	public List<UserCredential> createUsers(List<String> groups);

}
