package com.constellio.model.entities.calculators.dependencies;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.constellio.data.utils.KeyListMap;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;

public class AllPrincipalsAuthsDependencyValue {

	private List<User> users;
	private List<Group> groups;

	public AllPrincipalsAuthsDependencyValue(List<Group> groups, List<User> users) {
		this.groups = groups;
		this.users = users;
	}

	public KeyListMap<String, String> getPrincipalIdsWithAnyAuthorization(Map<String, List<String>> authorizations) {

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
				if (group.getAllAuthorizations().contains(authorization.getKey())) {
					if (group.getAllAuthorizations().contains(authorization.getKey())) {
						for (String access : authorization.getValue()) {
							principalsAccesses.add(group.getId(), access);
						}
					}
				}
			}
		}

		return principalsAccesses;
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
