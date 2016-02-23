package com.constellio.model.services.users;

import java.util.List;

import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.dao.managers.config.events.ConfigUpdatedEventListener;
import com.constellio.model.entities.security.global.UserCredential;

public interface UserCredentialsManager extends StatefulService, ConfigUpdatedEventListener {
	void addUpdate(UserCredential userCredential);

	UserCredential getUserCredential(String username);

	List<UserCredential> getUserCredentials();

	List<UserCredential> getActiveUserCredentials();

	List<UserCredential> getSuspendedUserCredentials();

	List<UserCredential> getPendingApprovalUserCredentials();

	List<UserCredential> getDeletedUserCredentials();

	List<UserCredential> getUserCredentialsInGlobalGroup(String group);

	void removeCollection(String collection);

	void removeToken(String token);

	void removeUserCredentialFromCollection(UserCredential userCredential, String collection);

	void removeGroup(String codeGroup);

	String getUsernameByServiceKey(String serviceKey);

	String getServiceKeyByToken(String token);

	void removedTimedOutTokens();

	void rewrite();
}
