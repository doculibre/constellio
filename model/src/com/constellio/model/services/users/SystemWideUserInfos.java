package com.constellio.model.services.users;

import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.security.global.AgentStatus;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.joda.time.LocalDateTime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SystemWideUserInfos {

	private String userCredentialId;
	private String username;
	private String firstName;
	private String lastName;
	private String email;
	private String title;
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

	public SystemWideUserInfos setUsername(String username) {
		this.username = username;
		return this;
	}

	public String getFirstName() {
		return firstName;
	}

	public SystemWideUserInfos setFirstName(String firstName) {
		this.firstName = firstName;
		return this;
	}

	public String getLastName() {
		return lastName;
	}

	public SystemWideUserInfos setLastName(String lastName) {
		this.lastName = lastName;
		return this;
	}

	public String getEmail() {
		return email;
	}

	public SystemWideUserInfos setEmail(String email) {
		this.email = email;
		return this;
	}

	public List<String> getPersonalEmails() {
		return personalEmails;
	}

	public SystemWideUserInfos setPersonalEmails(List<String> personalEmails) {
		this.personalEmails = personalEmails;
		return this;
	}

	public String getServiceKey() {
		return serviceKey;
	}

	public SystemWideUserInfos setServiceKey(String serviceKey) {
		this.serviceKey = serviceKey;
		return this;
	}

	public Boolean getSystemAdmin() {
		return systemAdmin;
	}

	public SystemWideUserInfos setSystemAdmin(Boolean systemAdmin) {
		this.systemAdmin = systemAdmin;
		return this;
	}

	public UserCredentialStatus getStatus() {
		return status;
	}

	public SystemWideUserInfos setStatus(UserCredentialStatus status) {
		this.status = status;
		return this;
	}

	public List<String> getCollections() {
		return collections;
	}

	public SystemWideUserInfos setCollections(List<String> collections) {
		this.collections = collections;
		return this;
	}

	public SystemWideUserInfos setCollections(String... collections) {
		this.collections = new ArrayList<>(Arrays.asList(collections));
		return this;
	}


	public List<String> getGlobalGroups() {
		return globalGroups;
	}

	public SystemWideUserInfos setGlobalGroups(List<String> globalGroups) {
		this.globalGroups = globalGroups;
		return this;
	}

	public String getDomain() {
		return domain;
	}

	public SystemWideUserInfos setDomain(String domain) {
		this.domain = domain;
		return this;
	}

	public List<String> getMsExchangeDelegateList() {
		return msExchangeDelegateList;
	}

	public SystemWideUserInfos setMsExchangeDelegateList(List<String> msExchangeDelegateList) {
		this.msExchangeDelegateList = msExchangeDelegateList;
		return this;
	}

	public SystemWideUserInfos setMsExchDelegateListBL(List<String> msExchangeDelegateList) {
		this.msExchangeDelegateList = msExchangeDelegateList;
		return this;
	}

	public String getDn() {
		return dn;
	}

	public SystemWideUserInfos setDn(String dn) {
		this.dn = dn;
		return this;
	}

	public String getPhone() {
		return phone;
	}

	public SystemWideUserInfos setPhone(String phone) {
		this.phone = phone;
		return this;
	}

	public String getFax() {
		return fax;
	}

	public SystemWideUserInfos setFax(String fax) {
		this.fax = fax;
		return this;
	}

	public String getJobTitle() {
		return jobTitle;
	}

	public SystemWideUserInfos setJobTitle(String jobTitle) {
		this.jobTitle = jobTitle;
		return this;
	}

	public String getAddress() {
		return address;
	}

	public SystemWideUserInfos setAddress(String address) {
		this.address = address;
		return this;
	}

	public AgentStatus getAgentStatus() {
		return agentStatus;
	}

	public SystemWideUserInfos setAgentStatus(AgentStatus agentStatus) {
		this.agentStatus = agentStatus;
		return this;
	}

	public Boolean getHasAgreedToPrivacyPolicy() {
		return hasAgreedToPrivacyPolicy;
	}

	public SystemWideUserInfos setHasAgreedToPrivacyPolicy(Boolean hasAgreedToPrivacyPolicy) {
		this.hasAgreedToPrivacyPolicy = hasAgreedToPrivacyPolicy;
		return this;
	}

	public Boolean getDoNotReceiveEmails() {
		return doNotReceiveEmails;
	}

	public SystemWideUserInfos setDoNotReceiveEmails(Boolean doNotReceiveEmails) {
		this.doNotReceiveEmails = doNotReceiveEmails;
		return this;
	}

	public Boolean getEnableFacetsApplyButton() {
		return enableFacetsApplyButton;
	}

	public SystemWideUserInfos setEnableFacetsApplyButton(Boolean enableFacetsApplyButton) {
		this.enableFacetsApplyButton = enableFacetsApplyButton;
		return this;
	}

	public Boolean getHasReadLastAlert() {
		return hasReadLastAlert;
	}

	public SystemWideUserInfos setHasReadLastAlert(Boolean hasReadLastAlert) {
		this.hasReadLastAlert = hasReadLastAlert;
		return this;
	}

	public Content getElectronicSignature() {
		return electronicSignature;
	}

	public SystemWideUserInfos setElectronicSignature(Content electronicSignature) {
		this.electronicSignature = electronicSignature;
		return this;
	}

	public Content getElectronicInitials() {
		return electronicInitials;
	}

	public SystemWideUserInfos setElectronicInitials(Content electronicInitials) {
		this.electronicInitials = electronicInitials;
		return this;
	}

	public String getAzureUsername() {
		return azureUsername;
	}

	public SystemWideUserInfos setAzureUsername(String azureUsername) {
		this.azureUsername = azureUsername;
		return this;
	}

	public Map<String, LocalDateTime> getAccessTokens() {
		return accessTokens;
	}

	public SystemWideUserInfos setAccessTokens(Map<String, LocalDateTime> accessTokens) {
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

	public SystemWideUserInfos setSystemAdminEnabled() {
		return setSystemAdmin(true);
	}

	public boolean isSystemAdmin() {
		return Boolean.TRUE.equals(getSystemAdmin());
	}

	public SystemWideUserInfos addGlobalGroup(String group) {
		if (globalGroups == null) {
			globalGroups = new ArrayList<>();
		}
		if (!globalGroups.contains(group)) {
			globalGroups.add(group);
		}
		return this;
	}

	public SystemWideUserInfos removeGlobalGroup(String group) {
		if (globalGroups != null) {
			globalGroups.remove(group);
		}

		return this;
	}

	public SystemWideUserInfos addAccessToken(String token, LocalDateTime expiration) {
		if (accessTokens == null) {
			accessTokens = new HashMap<>();
		}
		accessTokens.put(token, expiration);
		return this;
	}

	public boolean isNotReceivingEmails() {
		return Boolean.TRUE.equals(getDoNotReceiveEmails());
	}

	public String getUserCredentialId() {
		return userCredentialId;
	}

	public SystemWideUserInfos setUserCredentialId(String userCredentialId) {
		this.userCredentialId = userCredentialId;
		return this;
	}

	public String getId() {
		return userCredentialId;
	}

	public Set<String> getTokenKeys() {
		return getAccessTokens().keySet();

	}


	public boolean isActiveUser() {
		return getStatus() == UserCredentialStatus.ACTIVE || getStatus() == null;
	}

	public String getTitle() {
		return title;
	}

	public SystemWideUserInfos setTitle(String title) {
		this.title = title;
		return this;
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}
}
