package com.constellio.model.services.users;

import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.security.global.AgentStatus;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import com.constellio.model.entities.security.global.UserSyncMode;
import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.joda.time.LocalDateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Builder
public class SystemWideUserInfos {

	@Getter
	private String userCredentialId;

	@Getter
	private String username;

	@Getter
	private String firstName;

	@Getter
	private String lastName;

	@Getter
	private String email;

	@Getter
	private String title;

	@Getter
	private List<String> personalEmails = new ArrayList<>();

	@Getter
	private String serviceKey;

	@Getter
	private Boolean systemAdmin;

	@Getter
	private Map<String, UserCredentialStatus> statuses = new HashMap<>();

	@Getter
	private UserSyncMode syncMode = UserSyncMode.LOCALLY_CREATED;

	@Getter
	private List<String> collections = new ArrayList<>();

	@Getter
	private Map<String, List<String>> groupCodes = new HashMap<>();

	@Getter
	private Map<String, List<String>> groupIds = new HashMap<>();

	@Getter
	private String domain;

	@Getter
	private List<String> msExchangeDelegateList = new ArrayList<>();

	@Getter
	private String dn;

	@Getter
	private String phone;

	@Getter
	private String fax;

	@Getter
	private String jobTitle;

	@Getter
	private String address;

	@Getter
	private AgentStatus agentStatus;

	@Getter
	private Boolean hasAgreedToPrivacyPolicy;

	@Getter
	private Boolean doNotReceiveEmails;

	@Getter
	private Boolean enableFacetsApplyButton;

	@Getter
	private Boolean hasReadLastAlert;

	@Getter
	private Content electronicSignature;

	@Getter
	private Content electronicInitials;

	@Getter
	private String azureUsername;

	@Getter
	private Map<String, LocalDateTime> accessTokens = new HashMap<>();

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	public List<String> getGroupIds(String collection) {
		List<String> groupIdsList = groupIds.get(collection);
		return groupIdsList == null ? Collections.emptyList() : groupIdsList;
	}

	public List<String> getGroupCodes(String collection) {
		List<String> groupCodesList = groupCodes.get(collection);
		return groupCodesList == null ? Collections.emptyList() : groupCodesList;
	}

	public boolean isSystemAdmin() {
		return Boolean.TRUE.equals(getSystemAdmin());
	}

	public String getId() {
		return getUserCredentialId();

	}

	public UserCredentialStatus getStatus(String collection) {
		return statuses.get(collection);
	}

	public boolean isActiveInAnyCollection() {
		return hasStatusInAnyCollection(UserCredentialStatus.ACTIVE);
	}

	public boolean hasStatusInAnyCollection(UserCredentialStatus status) {
		for (UserCredentialStatus aStatus : statuses.values()) {
			if (aStatus == status) {
				return true;
			}
		}

		return false;
	}

	public boolean hasStatusInAllCollection(UserCredentialStatus status) {
		for (UserCredentialStatus aStatus : statuses.values()) {
			if (aStatus != status) {
				return false;
			}
		}

		return !statuses.isEmpty();
	}

	public boolean isNotReceivingEmails() {
		return Boolean.TRUE.equals(getDoNotReceiveEmails());

	}

	public Set<String> getTokenKeys() {
		return getAccessTokens().keySet();
	}

	public boolean isInGroupInAnyCollection(String groupCode) {
		for (List<String> groupCodesOfACollection : groupCodes.values()) {
			if (groupCodesOfACollection.contains(groupCode)) {
				return true;
			}
		}
		return false;
	}
}
