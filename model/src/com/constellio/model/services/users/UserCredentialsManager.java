package com.constellio.model.services.users;

import java.util.List;
import java.util.Map;

import org.joda.time.LocalDateTime;

import com.constellio.data.dao.managers.StatefulService;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.entities.security.global.UserCredentialStatus;

public interface UserCredentialsManager extends StatefulService {
	UserCredential create(String username, String firstName, String lastName, String email, List<String> globalGroups,
			List<String> collections, UserCredentialStatus status);

	UserCredential create(String username, String firstName, String lastName, String email, List<String> globalGroups,
			List<String> collections, UserCredentialStatus status, String domain, List<String> msExchDelegateListBL, String dn);

	UserCredential create(String username, String firstName, String lastName, String email, String serviceKey,
			boolean systemAdmin, List<String> globalGroups, List<String> collections, Map<String, LocalDateTime> tokens,
			UserCredentialStatus status);

	UserCredential create(String username, String firstName, String lastName, String email, String serviceKey,
			boolean systemAdmin, List<String> globalGroups, List<String> collections, Map<String, LocalDateTime> tokens,
			UserCredentialStatus status, String domain, List<String> msExchDelegateListBL, String dn);

	UserCredential create(String username, String firstName, String lastName, String email, List<String> personalEmails, String serviceKey,
						  boolean systemAdmin, List<String> globalGroups, List<String> collections, Map<String, LocalDateTime> tokens,
						  UserCredentialStatus status, String domain, List<String> msExchDelegateListBL, String dn);

	UserCredential create(String username, String firstName, String lastName, String email, List<String> personalEmails, String serviceKey,
						  boolean systemAdmin, List<String> globalGroups, List<String> collections, Map<String, LocalDateTime> tokens,
						  UserCredentialStatus status, String domain, List<String> msExchDelegateListBL, String dn, String jobTitle, String phone, String fax, String address);


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

	void removeTimedOutTokens();

	void rewrite();
}
