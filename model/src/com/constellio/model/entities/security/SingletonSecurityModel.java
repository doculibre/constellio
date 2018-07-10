package com.constellio.model.entities.security;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.constellio.data.utils.KeyListMap;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.global.AuthorizationDetails;

public class SingletonSecurityModel implements SecurityModel {

	List<SecurityModelAuthorization> authorizations;

	Map<String, SecurityModelAuthorization> authorizationsById;

	KeyListMap<String, SecurityModelAuthorization> authorizationsByTargets;

	List<AuthorizationDetails> authorizationDetails;
	List<User> users;
	List<Group> groups;

	public static SingletonSecurityModel EMPTY = new SingletonSecurityModel(Collections.<AuthorizationDetails>emptyList(),
			Collections.<User>emptyList(), Collections.<Group>emptyList());

	public SingletonSecurityModel(List<AuthorizationDetails> authorizationDetails, List<User> users, List<Group> groups) {
		this.authorizationDetails = authorizationDetails;
		this.users = users;
		this.groups = groups;

		authorizations = new ArrayList<>();

		for (AuthorizationDetails authorizationDetail : authorizationDetails) {
			SecurityModelAuthorization securityModelAuthorization = new SecurityModelAuthorization(authorizationDetail);
			authorizations.add(securityModelAuthorization);
			authorizationsById.put(authorizationDetail.getId(), securityModelAuthorization);
			authorizationsByTargets.add(authorizationDetail.getTarget(), securityModelAuthorization);
		}

		for (Group group : groups) {
			for (String authId : group.<String>getList(Schemas.AUTHORIZATIONS)) {
				SecurityModelAuthorization securityModelAuthorization = authorizationsById.get(authId);
				securityModelAuthorization.addGroup(group);
			}
		}

		for (User user : users) {
			for (String authId : user.<String>getList(Schemas.AUTHORIZATIONS)) {
				SecurityModelAuthorization securityModelAuthorization = authorizationsById.get(authId);
				securityModelAuthorization.addUser(user);
			}
		}
	}

	@Override
	public List<SecurityModelAuthorization> getAuthorizationsOnTarget(String recordId) {
		return authorizationsByTargets.get(recordId);
	}

}
