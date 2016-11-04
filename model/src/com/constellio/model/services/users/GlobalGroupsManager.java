package com.constellio.model.services.users;

import java.util.List;

import com.constellio.data.dao.managers.StatefulService;
import com.constellio.model.entities.security.global.GlobalGroup;
import com.constellio.model.entities.security.global.GlobalGroupStatus;

public interface GlobalGroupsManager extends StatefulService {
	GlobalGroup create(String code, String name, List<String> collections, String parent, GlobalGroupStatus status, boolean locallyCreated);

	GlobalGroup create(String code, String parent, GlobalGroupStatus status, boolean locallyCreated);

	void addUpdate(GlobalGroup globalGroup);

	void logicallyRemoveGroup(GlobalGroup globalGroup);

	GlobalGroup getGlobalGroupWithCode(String code);

	GlobalGroup getActiveGlobalGroupWithCode(String code);

	List<GlobalGroup> getActiveGroups();

	List<GlobalGroup> getAllGroups();

	void activateGlobalGroupHierarchy(GlobalGroup globalGroup);

	void removeCollection(String collection);

	List<GlobalGroup> getHierarchy(String code);
}
