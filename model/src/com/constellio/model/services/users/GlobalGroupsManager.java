package com.constellio.model.services.users;

import java.util.List;

import com.constellio.data.dao.managers.StatefulService;
import com.constellio.model.entities.security.global.GlobalGroup;

public interface GlobalGroupsManager extends StatefulService {
	void addUpdate(GlobalGroup globalGroup);

	void logicallyRemoveGroup(GlobalGroup globalGroup);

	GlobalGroup getGlobalGroupWithCode(String code);

	GlobalGroup getActiveGlobalGroupWithCode(String code);

	List<GlobalGroup> getActiveGroups();

	List<GlobalGroup> getAllGroups();

	void activateGlobalGroupHierarchy(GlobalGroup globalGroup);

	void removeCollection(String collection);
}
