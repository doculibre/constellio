package com.constellio.app.ui.framework.builders;

import com.constellio.app.ui.entities.SystemWideUserInfosVO;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import com.constellio.model.services.users.SystemWideUserInfos;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SystemWideUserInfosToVOBuilder {
	public SystemWideUserInfosVO build(SystemWideUserInfos systemWideUserInfos) {
		String userName = systemWideUserInfos.getUsername();
		String firstName = systemWideUserInfos.getFirstName();
		String lastName = systemWideUserInfos.getLastName();
		String email = systemWideUserInfos.getEmail();
		String title = systemWideUserInfos.getTitle();
		Set<String> collections = new HashSet<>();
		collections.addAll(systemWideUserInfos.getCollections());
		Map<String, UserCredentialStatus> statuses = systemWideUserInfos.getStatuses();
		String userInfosUserCredentialId = systemWideUserInfos.getUserCredentialId();
		Map<String, List<String>> groupsCodes = systemWideUserInfos.getGroupCodes();
		Map<String, List<String>> groupIds = systemWideUserInfos.getGroupIds();


		return new SystemWideUserInfosVO(userInfosUserCredentialId, userName, firstName, lastName, email, title, new ArrayList<>(collections), groupsCodes, groupIds, statuses);
	}
}
