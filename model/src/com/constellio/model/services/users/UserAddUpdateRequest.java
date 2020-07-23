package com.constellio.model.services.users;

import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.security.global.AgentStatus;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import org.joda.time.LocalDateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserAddUpdateRequest {

	private String username;

	private List<String> addToGroup;
	private List<String> removeFromGroup;

	private List<String> addToCollections;
	private List<String> removeFromCollections;

	private Map<String, LocalDateTime> newTokens;
	private List<String> removedtokens;

	private Map<String, Object> modifiedProperties = new HashMap<>();

	private boolean dnUnicityValidationCheck = true;

	List<String> currentCollections;
	List<String> currentGroups;

	public UserAddUpdateRequest(String username, List<String> currentCollections,
								List<String> currentGroups) {
		this.username = username;
		this.currentCollections = new ArrayList<>(currentCollections);
		this.currentGroups = new ArrayList<>(currentGroups);
	}


	public String getUsername() {
		return username;
	}

	public UserAddUpdateRequest setFirstName(String firstName) {
		this.modifiedProperties.put(UserCredential.FIRST_NAME, firstName);
		return this;
	}

	public UserAddUpdateRequest setLastName(String lastName) {
		this.modifiedProperties.put(UserCredential.LAST_NAME, lastName);
		return this;
	}

	public UserAddUpdateRequest setEmail(String email) {
		this.modifiedProperties.put(UserCredential.EMAIL, email);
		return this;
	}

	public UserAddUpdateRequest setPersonalEmails(List<String> personalEmails) {
		this.modifiedProperties.put(UserCredential.PERSONAL_EMAILS, personalEmails);
		return this;
	}

	public UserAddUpdateRequest setServiceKey(String serviceKey) {
		this.modifiedProperties.put(UserCredential.SERVICE_KEY, serviceKey);
		return this;
	}

	public UserAddUpdateRequest setSystemAdmin(Boolean systemAdmin) {
		this.modifiedProperties.put(UserCredential.SYSTEM_ADMIN, systemAdmin);
		return this;
	}

	public UserAddUpdateRequest setStatus(UserCredentialStatus status) {
		this.modifiedProperties.put(UserCredential.STATUS, status);
		return this;
	}


	public UserAddUpdateRequest setDomain(String domain) {
		this.modifiedProperties.put(UserCredential.DOMAIN, domain);
		return this;
	}

	public UserAddUpdateRequest setMsExchangeDelegateList(List<String> msExchangeDelegateList) {
		this.modifiedProperties.put(UserCredential.MS_EXCHANGE_DELEGATE_LIST, msExchangeDelegateList);
		return this;
	}

	public UserAddUpdateRequest setMsExchDelegateListBL(List<String> msExchangeDelegateList) {
		this.modifiedProperties.put(UserCredential.MS_EXCHANGE_DELEGATE_LIST, msExchangeDelegateList);
		return this;
	}

	public UserAddUpdateRequest setDn(String dn) {
		this.modifiedProperties.put(UserCredential.DN, dn);
		return this;
	}

	public UserAddUpdateRequest setPhone(String phone) {
		this.modifiedProperties.put(UserCredential.PHONE, phone);
		return this;
	}

	public UserAddUpdateRequest setFax(String fax) {
		this.modifiedProperties.put(UserCredential.FAX, fax);
		return this;
	}

	public UserAddUpdateRequest setJobTitle(String jobTitle) {
		this.modifiedProperties.put(UserCredential.JOB_TITLE, jobTitle);
		return this;
	}

	public UserAddUpdateRequest setAddress(String address) {
		this.modifiedProperties.put(UserCredential.ADDRESS, address);
		return this;
	}

	public UserAddUpdateRequest setAgentStatus(AgentStatus agentStatus) {
		this.modifiedProperties.put(UserCredential.AGENT_STATUS, agentStatus);
		return this;
	}

	public UserAddUpdateRequest setHasAgreedToPrivacyPolicy(Boolean hasAgreedToPrivacyPolicy) {
		this.modifiedProperties.put(UserCredential.HAS_AGREED_TO_PRIVACY_POLICY, hasAgreedToPrivacyPolicy);
		return this;
	}

	public UserAddUpdateRequest setDoNotReceiveEmails(Boolean doNotReceiveEmails) {
		this.modifiedProperties.put(UserCredential.DO_NOT_RECEIVE_EMAILS, doNotReceiveEmails);
		return this;
	}

	public UserAddUpdateRequest setEnableFacetsApplyButton(Boolean enableFacetsApplyButton) {
		this.modifiedProperties.put(UserCredential.ENABLE_FACETS_APPLY_BUTTON, enableFacetsApplyButton);
		return this;
	}

	public UserAddUpdateRequest setHasReadLastAlert(Boolean hasReadLastAlert) {
		this.modifiedProperties.put(UserCredential.HAS_READ_LAST_ALERT, hasReadLastAlert);
		return this;
	}

	public UserAddUpdateRequest setElectronicSignature(Content electronicSignature) {
		this.modifiedProperties.put(UserCredential.ELECTRONIC_SIGNATURE, electronicSignature);
		return this;
	}

	public UserAddUpdateRequest setElectronicInitials(Content electronicInitials) {
		this.modifiedProperties.put(UserCredential.ELECTRONIC_INITIALS, electronicInitials);
		return this;
	}

	public UserAddUpdateRequest setAzureUsername(String azureUsername) {
		this.modifiedProperties.put(UserCredential.AZURE_USERNAME, azureUsername);
		return this;
	}


	public UserAddUpdateRequest setCollections(List<String> newCollections) {
		newCollections.forEach((c) -> {
			if (!currentCollections.contains(c)) {
				addCollection(c);
			}
		});

		currentCollections.forEach((c) -> {
			if (!newCollections.contains(c)) {
				removeCollection(c);
			}
		});
		return this;
	}

	public UserAddUpdateRequest addCollection(String collection) {

		if (addToCollections == null) {
			addToCollections = new ArrayList<>();
		}
		if (!addToCollections.contains(collection)) {
			addToCollections.add(collection);
		}
		if (removeFromCollections != null) {
			removeFromCollections.remove(collection);
		}

		return this;
	}

	public UserAddUpdateRequest removeCollection(String collection) {

		if (removeFromCollections == null) {
			removeFromCollections = new ArrayList<>();
		}
		if (!removeFromCollections.contains(collection)) {
			removeFromCollections.add(collection);
		}
		if (addToCollections != null) {
			addToCollections.remove(collection);
		}

		return this;
	}

	public UserAddUpdateRequest addCollections(List<String> collections) {
		collections.forEach(this::addCollection);
		return this;
	}


	@Deprecated
	public UserAddUpdateRequest setGlobalGroups(List<String> newGroupCodes) {
		newGroupCodes.forEach((g) -> {
			if (!currentGroups.contains(g)) {
				addGlobalGroup(g);
			}
		});

		currentGroups.forEach((g) -> {
			if (!newGroupCodes.contains(g)) {
				removeGlobalGroup(g);
			}
		});
		return this;
	}

	public UserAddUpdateRequest addGlobalGroups(List<String> groupCodes) {
		groupCodes.forEach(this::addGlobalGroup);
		return this;
	}

	public UserAddUpdateRequest addGlobalGroup(String groupCode) {

		if (addToGroup == null) {
			addToGroup = new ArrayList<>();
		}
		if (!addToGroup.contains(groupCode)) {
			addToGroup.add(groupCode);
		}
		if (removeFromGroup != null) {
			removeFromGroup.remove(groupCode);
		}

		return this;
	}

	public UserAddUpdateRequest removeGlobalGroup(String groupCode) {

		if (removeFromGroup == null) {
			removeFromGroup = new ArrayList<>();
		}
		if (!removeFromGroup.contains(groupCode)) {
			removeFromGroup.add(groupCode);
		}
		if (addToGroup != null) {
			addToGroup.remove(groupCode);
		}

		return this;
	}

	public UserAddUpdateRequest setSystemAdminEnabled() {
		return setSystemAdmin(true);
	}


	public UserAddUpdateRequest addAccessToken(String token, LocalDateTime expiration) {
		if (newTokens == null) {
			newTokens = new HashMap<>();
		}
		newTokens.put(token, expiration);
		return this;
	}


	public UserAddUpdateRequest removeAccessToken(String token) {

		if (removedtokens == null) {
			removedtokens = new ArrayList<>();
		}
		if (!removedtokens.contains(token)) {
			removedtokens.add(token);
		}

		return this;
	}

	public boolean isDnUnicityValidationCheck() {
		return dnUnicityValidationCheck;
	}

	public UserAddUpdateRequest setDnUnicityValidationCheck(boolean dnUnicityValidationCheck) {
		this.dnUnicityValidationCheck = dnUnicityValidationCheck;
		return this;
	}

	public void set(Metadata metadata, Object value) {
		modifiedProperties.put(metadata.getLocalCode(), value);
	}

	public void set(String localCode, Object value) {
		modifiedProperties.put(localCode, value);
	}

	public Map<String, Object> getExtraMetadatas() {
		return modifiedProperties;
	}

	public List<String> getAddToCollections() {
		return addToCollections;
	}

	public List<String> getRemoveFromCollections() {
		return removeFromCollections;
	}

	public List<String> getAddToGroup() {
		return addToGroup;
	}

	public List<String> getRemoveFromGroup() {
		return removeFromGroup;
	}

	public Map<String, LocalDateTime> getNewTokens() {
		return newTokens;
	}

	public List<String> getRemovedtokens() {
		return removedtokens;
	}
}
