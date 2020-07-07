package com.constellio.model.services.users;

import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.security.global.AgentStatus;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import org.joda.time.LocalDateTime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserAddUpdateRequest {

	private String username;
	private String firstName;
	private String lastName;
	private String email;
	private List<String> personalEmails = new ArrayList<>();
	private String serviceKey;
	private Boolean systemAdmin;
	private UserCredentialStatus status = UserCredentialStatus.ACTIVE;
	private List<String> collections = new ArrayList<>();
	private List<String> globalGroups = new ArrayList<>();
	private String domain;
	private List<String> msExchangeDelegateList = new ArrayList<>();
	private String dn;
	private String phone;
	private String fax;
	private String jobTitle;
	private String address;
	private AgentStatus agentStatus;
	private Boolean hasAgreedToPrivacyPolicy;
	private Boolean doNotReceiveEmails;
	private Boolean enableFacetsApplyButton;
	private Boolean hasReadLastAlert;
	private Content electronicSignature;
	private Content electronicInitials;
	private String azureUsername;
	private Map<String, LocalDateTime> accessTokens = new HashMap<>();


	public String getUsername() {
		return username;
	}

	public UserAddUpdateRequest setUsername(String username) {
		this.username = username;
		return this;
	}

	public String getFirstName() {
		return firstName;
	}

	public UserAddUpdateRequest setFirstName(String firstName) {
		this.firstName = firstName;
		return this;
	}

	public String getLastName() {
		return lastName;
	}

	public UserAddUpdateRequest setLastName(String lastName) {
		this.lastName = lastName;
		return this;
	}

	public String getEmail() {
		return email;
	}

	public UserAddUpdateRequest setEmail(String email) {
		this.email = email;
		return this;
	}

	public List<String> getPersonalEmails() {
		return personalEmails;
	}

	public UserAddUpdateRequest setPersonalEmails(List<String> personalEmails) {
		this.personalEmails = personalEmails;
		return this;
	}

	public String getServiceKey() {
		return serviceKey;
	}

	public UserAddUpdateRequest setServiceKey(String serviceKey) {
		this.serviceKey = serviceKey;
		return this;
	}

	public Boolean getSystemAdmin() {
		return systemAdmin;
	}

	public UserAddUpdateRequest setSystemAdmin(Boolean systemAdmin) {
		this.systemAdmin = systemAdmin;
		return this;
	}

	public UserCredentialStatus getStatus() {
		return status;
	}

	public UserAddUpdateRequest setStatus(UserCredentialStatus status) {
		this.status = status;
		return this;
	}

	public List<String> getCollections() {
		return collections;
	}

	public UserAddUpdateRequest setCollections(List<String> collections) {
		this.collections = collections;
		return this;
	}

	public UserAddUpdateRequest setCollections(String... collections) {
		this.collections = new ArrayList<>(Arrays.asList(collections));
		return this;
	}


	public List<String> getGlobalGroups() {
		return globalGroups;
	}

	public UserAddUpdateRequest setGlobalGroups(List<String> globalGroups) {
		this.globalGroups = globalGroups;
		return this;
	}

	public String getDomain() {
		return domain;
	}

	public UserAddUpdateRequest setDomain(String domain) {
		this.domain = domain;
		return this;
	}

	public List<String> getMsExchangeDelegateList() {
		return msExchangeDelegateList;
	}

	public UserAddUpdateRequest setMsExchangeDelegateList(List<String> msExchangeDelegateList) {
		this.msExchangeDelegateList = msExchangeDelegateList;
		return this;
	}

	public UserAddUpdateRequest setMsExchDelegateListBL(List<String> msExchangeDelegateList) {
		this.msExchangeDelegateList = msExchangeDelegateList;
		return this;
	}

	public String getDn() {
		return dn;
	}

	public UserAddUpdateRequest setDn(String dn) {
		this.dn = dn;
		return this;
	}

	public String getPhone() {
		return phone;
	}

	public UserAddUpdateRequest setPhone(String phone) {
		this.phone = phone;
		return this;
	}

	public String getFax() {
		return fax;
	}

	public UserAddUpdateRequest setFax(String fax) {
		this.fax = fax;
		return this;
	}

	public String getJobTitle() {
		return jobTitle;
	}

	public UserAddUpdateRequest setJobTitle(String jobTitle) {
		this.jobTitle = jobTitle;
		return this;
	}

	public String getAddress() {
		return address;
	}

	public UserAddUpdateRequest setAddress(String address) {
		this.address = address;
		return this;
	}

	public AgentStatus getAgentStatus() {
		return agentStatus;
	}

	public UserAddUpdateRequest setAgentStatus(AgentStatus agentStatus) {
		this.agentStatus = agentStatus;
		return this;
	}

	public Boolean getHasAgreedToPrivacyPolicy() {
		return hasAgreedToPrivacyPolicy;
	}

	public UserAddUpdateRequest setHasAgreedToPrivacyPolicy(Boolean hasAgreedToPrivacyPolicy) {
		this.hasAgreedToPrivacyPolicy = hasAgreedToPrivacyPolicy;
		return this;
	}

	public Boolean getDoNotReceiveEmails() {
		return doNotReceiveEmails;
	}

	public UserAddUpdateRequest setDoNotReceiveEmails(Boolean doNotReceiveEmails) {
		this.doNotReceiveEmails = doNotReceiveEmails;
		return this;
	}

	public Boolean getEnableFacetsApplyButton() {
		return enableFacetsApplyButton;
	}

	public UserAddUpdateRequest setEnableFacetsApplyButton(Boolean enableFacetsApplyButton) {
		this.enableFacetsApplyButton = enableFacetsApplyButton;
		return this;
	}

	public Boolean getHasReadLastAlert() {
		return hasReadLastAlert;
	}

	public UserAddUpdateRequest setHasReadLastAlert(Boolean hasReadLastAlert) {
		this.hasReadLastAlert = hasReadLastAlert;
		return this;
	}

	public Content getElectronicSignature() {
		return electronicSignature;
	}

	public UserAddUpdateRequest setElectronicSignature(Content electronicSignature) {
		this.electronicSignature = electronicSignature;
		return this;
	}

	public Content getElectronicInitials() {
		return electronicInitials;
	}

	public UserAddUpdateRequest setElectronicInitials(Content electronicInitials) {
		this.electronicInitials = electronicInitials;
		return this;
	}

	public String getAzureUsername() {
		return azureUsername;
	}

	public UserAddUpdateRequest setAzureUsername(String azureUsername) {
		this.azureUsername = azureUsername;
		return this;
	}

	public Map<String, LocalDateTime> getAccessTokens() {
		return accessTokens;
	}

	public UserAddUpdateRequest setAccessTokens(Map<String, LocalDateTime> accessTokens) {
		this.accessTokens = accessTokens;
		return this;
	}

	public void addCollection(String collection) {
		if (collections == null) {
			collections = new ArrayList<>();
		}
		if (!collections.contains(collection)) {
			collections.add(collection);
		}

	}

	public UserAddUpdateRequest setSystemAdminEnabled() {
		return setSystemAdmin(true);
	}

	public boolean isSystemAdmin() {
		return Boolean.TRUE.equals(getSystemAdmin());
	}

	public UserAddUpdateRequest addGlobalGroup(String group) {
		if (globalGroups == null) {
			globalGroups = new ArrayList<>();
		}
		if (!globalGroups.contains(group)) {
			globalGroups.add(group);
		}
		return this;
	}

	public UserAddUpdateRequest removeGlobalGroup(String group) {
		if (globalGroups != null) {
			globalGroups.remove(group);
		}

		return this;
	}

	public UserAddUpdateRequest addAccessToken(String token, LocalDateTime expiration) {
		if (accessTokens == null) {
			accessTokens = new HashMap<>();
		}
		accessTokens.put(token, expiration);
		return this;
	}
}
