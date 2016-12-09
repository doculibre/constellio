package com.constellio.app.ui.framework.builders;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Joiner;
import org.apache.commons.collections.CollectionUtils;
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
		Map<String, LocalDateTime> tokens = userCredential.getAccessTokens();
		String serviceKey = userCredential.getServiceKey();
		boolean systemAdmin = userCredential.isSystemAdmin();
		List<String> globalGroups = userCredential.getGlobalGroups();
		Set<String> collections = new HashSet<>();
		collections.addAll(userCredential.getCollections());
		UserCredentialStatus status = userCredential.getStatus();

		String personalEmailsPresentation = null;
		if (!CollectionUtils.isEmpty(userCredential.getPersonalEmails())) {
			personalEmailsPresentation = Joiner.on("\n").join(userCredential.getPersonalEmails());
		}

		return new UserCredentialVO(username, firstName, lastName, email, personalEmailsPresentation, serviceKey, systemAdmin, globalGroups, collections,
				tokens, null, null, status);

	}
}
