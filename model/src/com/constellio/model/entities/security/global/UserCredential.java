package com.constellio.model.entities.security.global;

import java.util.List;
import java.util.Map;

import org.joda.time.LocalDateTime;

public interface UserCredential {
	String getUsername();

	String getFirstName();

	String getLastName();

	String getEmail();

	String getServiceKey();

	Map<String, LocalDateTime> getAccessTokens();

	String getTitle();

	List<String> getTokenKeys();

	boolean isSystemAdmin();

	List<String> getCollections();

	List<String> getGlobalGroups();

	UserCredentialStatus getStatus();

	String getDomain();

	List<String> getMsExchDelegateListBL();

	UserCredential withCollections(List<String> collections);

	UserCredential withRemovedCollection(String collection);

	UserCredential withNewGlobalGroup(String newGroup);

	UserCredential withRemovedGlobalGroup(String removedGroup);

	UserCredential withGlobalGroups(List<String> globalGroups);

	UserCredential withFirstName(String firstName);

	UserCredential withLastName(String lastName);

	UserCredential withEmail(String email);

	UserCredential withStatus(UserCredentialStatus status);

	UserCredential withAccessToken(String token, LocalDateTime dateTime);

	UserCredential withRemovedToken(String key);

	UserCredential withAccessTokens(Map<String, LocalDateTime> tokens);

	UserCredential withNewCollection(String collection);

	UserCredential withSystemAdminPermission();

	UserCredential withServiceKey(String serviceKey);

	UserCredential withMsExchDelegateListBL(List<String> msExchDelegateListBL);

	UserCredential withDN(String dn);

	String getDn();
}
