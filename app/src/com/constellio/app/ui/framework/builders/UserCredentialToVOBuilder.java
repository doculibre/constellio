package com.constellio.app.ui.framework.builders;

import com.constellio.app.ui.entities.UserCredentialVO;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import com.constellio.model.entities.security.global.UserSyncMode;
import com.constellio.model.services.users.SystemWideUserInfos;
import com.google.common.base.Joiner;
import org.apache.commons.collections.CollectionUtils;
import org.joda.time.LocalDateTime;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.constellio.model.entities.security.global.UserCredentialStatus.ACTIVE;
import static com.constellio.model.entities.security.global.UserCredentialStatus.DISABLED;

@SuppressWarnings("serial")
public class UserCredentialToVOBuilder implements Serializable {

	public UserCredentialVO build(UserCredential userCredential) {
		String username = userCredential.getUsername();
		String firstName = userCredential.getFirstName();
		String lastName = userCredential.getLastName();
		String email = userCredential.getEmail();
		String jobTitle = userCredential.getJobTitle();
		String phone = userCredential.getPhone();
		String fax = userCredential.getFax();
		String address = userCredential.getAddress();
		Map<String, LocalDateTime> tokens = userCredential.getAccessTokens();
		String serviceKey = userCredential.getServiceKey();
		boolean systemAdmin = userCredential.isSystemAdmin();
		List<String> globalGroups = userCredential.getGlobalGroups();
		Set<String> collections = new HashSet<>();
		collections.addAll(userCredential.getCollections());
		UserCredentialStatus status = userCredential.getStatus();
		UserSyncMode syncMode = userCredential.getSyncMode();

		String personalEmailsPresentation = null;
		if (!CollectionUtils.isEmpty(userCredential.getPersonalEmails())) {
			personalEmailsPresentation = Joiner.on("\n").join(userCredential.getPersonalEmails());
		}

		return new UserCredentialVO(username, firstName, lastName, email, jobTitle, phone, fax, address, personalEmailsPresentation, serviceKey, systemAdmin, globalGroups, collections,
				tokens, null, null, status, syncMode);

	}

	public UserCredentialVO build(SystemWideUserInfos userCredential) {
		String username = userCredential.getUsername();
		String firstName = userCredential.getFirstName();
		String lastName = userCredential.getLastName();
		String email = userCredential.getEmail();
		String jobTitle = userCredential.getJobTitle();
		String phone = userCredential.getPhone();
		String fax = userCredential.getFax();
		String address = userCredential.getAddress();
		Map<String, LocalDateTime> tokens = userCredential.getAccessTokens();
		String serviceKey = userCredential.getServiceKey();
		boolean systemAdmin = userCredential.isSystemAdmin();
		List<String> globalGroups = userCredential.getGlobalGroups();
		Set<String> collections = new HashSet<>();
		collections.addAll(userCredential.getCollections());
		UserCredentialStatus status = userCredential.hasStatusInAnyCollection(ACTIVE) ? ACTIVE : DISABLED;
		UserSyncMode syncMode = userCredential.getSyncMode();

		String personalEmailsPresentation = null;
		if (!CollectionUtils.isEmpty(userCredential.getPersonalEmails())) {
			personalEmailsPresentation = Joiner.on("\n").join(userCredential.getPersonalEmails());
		}

		return new UserCredentialVO(username, firstName, lastName, email, jobTitle, phone, fax, address, personalEmailsPresentation, serviceKey, systemAdmin, globalGroups, collections,
				tokens, null, null, status, syncMode);

	}
}
