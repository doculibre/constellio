package com.constellio.model.services.users;

import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.security.global.AgentStatus;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import com.constellio.model.entities.security.global.UserSyncMode;
import lombok.Getter;
import org.joda.time.LocalDateTime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserAddUpdateRequest {

	private String username;

	private List<String> addToGroup;
	private List<String> removeFromGroup;

	private List<String> addToCollections;
	private List<String> removeFromCollections;

	private Map<String, List<String>> addToGroupInCollection;
	private Map<String, List<String>> removeFromGroupInCollection;

	private Map<String, LocalDateTime> newTokens;
	private List<String> removedtokens;
	private UserSyncMode syncMode = UserSyncMode.LOCALLY_CREATED;

	private Map<String, Object> modifiedProperties = new HashMap<>();

	private boolean dnUnicityValidationCheck = true;

	List<String> currentCollections;
	List<String> currentGroups;
	private Map<String, Map<String, Object>> modifiedCollectionsProperties = new HashMap<>();

	private boolean ldapSyncRequest;

	private boolean stopSyncingLDAP;

	private boolean resumeSyncingLDAP;

	@Getter
	private boolean markedForDeletionInAllCollections;

	public UserAddUpdateRequest(String username, List<String> currentCollections,
								List<String> currentGroups) {
		this.username = username;
		this.currentCollections = new ArrayList<>(currentCollections);
		this.currentGroups = new ArrayList<>(currentGroups);
	}


	public boolean isStopSyncingLDAP() {
		return stopSyncingLDAP;
	}

	public boolean isResumeSyncingLDAP() {
		return resumeSyncingLDAP;
	}

	public boolean isLdapSyncRequest() {
		return ldapSyncRequest;
	}

	public UserAddUpdateRequest ldapSyncRequest() {
		this.ldapSyncRequest = true;
		return this;
	}

	public UserAddUpdateRequest stopSyncingLDAP() {
		this.stopSyncingLDAP = true;
		return this;
	}

	public UserAddUpdateRequest resumeSyncingLDAP() {
		this.resumeSyncingLDAP = true;
		return this;
	}

	public String getUsername() {
		return username;
	}

	public UserAddUpdateRequest setName(String firstName, String lastName) {
		setFirstName(firstName);
		setLastName(lastName);
		return this;
	}

	public UserAddUpdateRequest setNameEmail(String firstName, String lastName, String email) {
		setFirstName(firstName);
		setLastName(lastName);
		setEmail(email);
		return this;
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

	public UserAddUpdateRequest setStatusForAllCollections(UserCredentialStatus status) {
		this.modifiedProperties.put(UserCredential.STATUS, status);
		return this;
	}

	public UserSyncMode getSyncMode() {
		return syncMode;
	}

	@Deprecated
	public UserAddUpdateRequest setSyncMode(UserSyncMode syncMode) {
		this.modifiedProperties.put(UserCredential.SYNC_MODE, syncMode);
		return this;
	}

	public UserAddUpdateRequest setStatusForCollection(UserCredentialStatus status, String collection) {
		modifyCollectionProperties(collection).put(User.STATUS, status);
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
				addToCollection(c);
			}
		});

		currentCollections.forEach((c) -> {
			if (!newCollections.contains(c)) {
				removeFromCollection(c);
			}
		});
		return this;
	}

	public UserAddUpdateRequest addToCollection(String collection) {

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

	public UserAddUpdateRequest removeFromCollection(String collection) {

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

	public UserAddUpdateRequest removeFromCollections(String... collections) {
		Arrays.stream(collections).forEach(this::removeFromCollection);
		return this;
	}

	public UserAddUpdateRequest addToCollections(List<String> collections) {
		collections.forEach(this::addToCollection);
		return this;
	}

	public UserAddUpdateRequest addToCollections(String... collections) {
		Arrays.stream(collections).forEach(this::addToCollection);
		return this;
	}


	@Deprecated
	public UserAddUpdateRequest setGlobalGroups(List<String> newGroupCodes) {
		newGroupCodes.forEach((g) -> {
			if (!currentGroups.contains(g)) {
				addToGroupInEachCollection(g);
			}
		});

		currentGroups.forEach((g) -> {
			if (!newGroupCodes.contains(g)) {
				removeFromGroupOfEachCollection(g);
			}
		});
		return this;
	}

	public UserAddUpdateRequest addToGroupsInEachCollection(List<String> groupCodes) {
		groupCodes.forEach(this::addToGroupInEachCollection);
		return this;
	}

	public UserAddUpdateRequest addToGroupsInEachCollection(String... groupCodes) {
		Arrays.stream(groupCodes).forEach(this::addToGroupInEachCollection);
		return this;
	}

	public UserAddUpdateRequest addToGroupInEachCollection(String groupCode) {

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

	public UserAddUpdateRequest removeFromGroupOfEachCollection(String groupCode) {

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


	public UserAddUpdateRequest addToGroupsInCollection(List<String> groupCodes, String collection) {
		if (addToGroupInCollection == null) {
			addToGroupInCollection = new HashMap<>();
		}
		List<String> groups = new ArrayList<>(groupCodes);
		if (addToGroupInCollection.containsKey(collection)) {
			groups.addAll(addToGroupInCollection.get(collection));
		}
		addToGroupInCollection.put(collection, groups);
		return this;
	}

	public UserAddUpdateRequest addToGroupInCollection(String groupCode, String collection) {
		addToGroupsInCollection(Collections.singletonList(groupCode), collection);
		return this;
	}

	public UserAddUpdateRequest removeFromGroupOfCollection(String groupCode, String collection) {
		throw new UnsupportedOperationException("TODO Rabab");
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

	public UserAddUpdateRequest set(Metadata metadata, Object value) {
		modifiedProperties.put(metadata.getLocalCode(), value);
		return this;
	}

	public UserAddUpdateRequest set(String localCode, Object value) {
		modifiedProperties.put(localCode, value);
		return this;
	}

	public Map<String, Object> getExtraMetadatas() {
		return modifiedProperties;
	}

	public Map<String, Map<String, Object>> getModifiedCollectionsProperties() {
		return modifiedCollectionsProperties;
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

	public Map<String, List<String>> getAddToGroupInCollection() {
		return addToGroupInCollection;
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

	public UserAddUpdateRequest removeFromAllCollections() {
		markedForDeletionInAllCollections = true;
		return this;
	}


	private Map<String, Object> modifyCollectionProperties(String collection) {
		Map<String, Object> modifiedCollectionProperties = this.modifiedCollectionsProperties.get(collection);
		if (modifiedCollectionProperties == null) {
			modifiedCollectionProperties = new HashMap<>();
			this.modifiedCollectionsProperties.put(collection, modifiedCollectionProperties);
		}
		return modifiedCollectionProperties;
	}

}
