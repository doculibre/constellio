package com.constellio.app.ui.framework.builders;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.LocalDateTime;

import com.constellio.app.ui.entities.UserCredentialVO;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.entities.security.global.UserCredentialStatus;

@SuppressWarnings("serial")
public class UserCredentialToVOBuilder implements Serializable {

	public UserCredentialVO build(UserCredential userCredential) {
		String username = userCredential.getUsername();
		String firstName = userCredential.getFirstName();
		String lastName = userCredential.getLastName();
		String email = userCredential.getEmail();
		String personalEmails = userCredential.getPersonalEmails();
		Map<String, LocalDateTime> tokens = userCredential.getAccessTokens();
		String serviceKey = userCredential.getServiceKey();
		boolean systemAdmin = userCredential.isSystemAdmin();
		List<String> globalGroups = userCredential.getGlobalGroups();
		Set<String> collections = new HashSet<>();
		collections.addAll(userCredential.getCollections());
		UserCredentialStatus status = userCredential.getStatus();

		return new UserCredentialVO(username, firstName, lastName, email, personalEmails, serviceKey, systemAdmin, globalGroups, collections,
				tokens, null, null, status);

	}
}
