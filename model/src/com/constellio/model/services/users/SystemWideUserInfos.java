package com.constellio.model.services.users;

import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.security.global.AgentStatus;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import lombok.Builder;
import lombok.Getter;
import com.constellio.model.entities.security.global.UserSyncMode;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.joda.time.LocalDateTime;

import java.util.ArrayList;
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

	//TODO : Replace with Map<String, UserCredentialStatus> using collection codes as keys
	private UserCredentialStatus status = UserCredentialStatus.ACTIVE;

	@Getter
	private UserSyncMode syncMode = UserSyncMode.LOCALLY_CREATED;

	@Getter
	private List<String> collections = new ArrayList<>();

	@Getter
	private List<String> globalGroups = new ArrayList<>();

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

	public boolean isSystemAdmin() {
		return Boolean.TRUE.equals(getSystemAdmin());
	}

	public String getId() {
		return getUserCredentialId();

	}

	//TODO Philippe : store by collection!
	public UserCredentialStatus getStatus(String collection) {
		return status;
	}

	@Deprecated
	//TODO Philippe : remove!
	public UserCredentialStatus getStatus() {
		return status;
	}

	public boolean isActiveInAnyCollection() {
		return UserCredentialStatus.ACTIVE.equals(status);
	}

	public boolean hasStatusInAnyCollection(UserCredentialStatus status) {
		return status == this.status;
	}

	public boolean hasStatusInAllCollection(UserCredentialStatus status) {
		return status == this.status;
	}

	public boolean isNotReceivingEmails() {
		return Boolean.TRUE.equals(getDoNotReceiveEmails());

	}

	public Set<String> getTokenKeys() {
		return getAccessTokens().keySet();
	}
}
