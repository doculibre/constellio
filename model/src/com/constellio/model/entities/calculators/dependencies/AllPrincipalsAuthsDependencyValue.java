package com.constellio.model.entities.calculators.dependencies;

import com.constellio.data.utils.KeyListMap;
import com.constellio.model.entities.enums.GroupAuthorizationsInheritance;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class AllPrincipalsAuthsDependencyValue {

	private List<User> users;
	private List<Group> groups;
	private List<String> disabledGroupCodes;

	private AllPrincipalsAuthsDependencyValue(List<Group> groups, List<User> users, List<String> disabledGroupCodes) {
		this.groups = groups;
		this.users = users;
		this.disabledGroupCodes = disabledGroupCodes;
	}

	public KeyListMap<String, String> getPrincipalIdsWithAnyAuthorization(Map<String, List<String>> authorizations,
																		  GroupAuthorizationsInheritance inheritanceMode) {

		KeyListMap<String, String> principalsAccesses = new KeyListMap<>();
		for (User user : users) {
			for (Entry<String, List<String>> authorization : authorizations.entrySet()) {
				if (user.getUserAuthorizations().contains(authorization.getKey())) {
					for (String access : authorization.getValue()) {
						principalsAccesses.add(user.getId(), access);
					}
				}
			}
		}

		for (Group group : groups) {
			for (Entry<String, List<String>> authorization : authorizations.entrySet()) {
				if (inheritanceMode == GroupAuthorizationsInheritance.FROM_CHILD_TO_PARENT) {
					if (group.getList(Schemas.AUTHORIZATIONS).contains(authorization.getKey())) {

						for (String groupHierarchyId : getGroupHierarchyIds(group)) {
							for (String access : authorization.getValue()) {
								principalsAccesses.add(groupHierarchyId, access);
							}
						}
					}

				} else {
					if (group.getAllAuthorizations().contains(authorization.getKey())) {
						if (group.getAllAuthorizations().contains(authorization.getKey())) {
							for (String access : authorization.getValue()) {
								principalsAccesses.add(group.getId(), access);
							}
						}
					}
				}
			}
		}

		return principalsAccesses;
	}

	private List<String> getGroupHierarchyIds(Group group) {
		List<String> ids = new ArrayList<>();
		if (!disabledGroupCodes.contains(group.getCode())) {
			ids.add(group.getId());
			if (group.getParent() != null) {
				for (Group aGroup : groups) {
					if (aGroup.getId().equals(group.getParent())) {
						ids.addAll(getGroupHierarchyIds(aGroup));
						break;
					}
				}
			}
		}

		return ids;

	}

	//	public KeyListMap<String, String> getPrincipalIdsWithAnyAuthorization(List<AuthorizationDetails> authorizations) {
	//
	//		KeyListMap<String, String> principalsAccesses = new KeyListMap<>();
	//		for (User user : users) {
	//			for (AuthorizationDetails authorization : authorizations) {
	//				if (user.getUserAuthorizations().contains(authorization.getId())) {
	//					for (String access : authorization.getRoles()) {
	//						principalsAccesses.add(user.getId(), access);
	//					}
	//				}
	//			}
	//		}
	//
	//		for (Group group : groups) {
	//			for (AuthorizationDetails authorization : authorizations) {
	//				if (group.getAllAuthorizations().contains(authorization.getId())) {
	//					for (String access : authorization.getRoles()) {
	//						principalsAccesses.add(group.getId(), access);
	//					}
	//				}
	//			}
	//		}
	//
	//		return principalsAccesses;
	//	}
}
