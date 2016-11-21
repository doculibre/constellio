package com.constellio.model.conf.ldap.config;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import com.constellio.model.conf.ldap.LDAPDirectoryType;

public class LDAPServerConfiguration implements Serializable {
	private Boolean ldapAuthenticationActive;

	private LDAPDirectoryType directoryType;

	NonAzureAdServerConfig nonAzureAdServerConfig = new NonAzureAdServerConfig();
	AzureADServerConfig azurServerConfig = new AzureADServerConfig();

	public LDAPServerConfiguration(List<String> urls, List<String> domains, LDAPDirectoryType directoryType,
			Boolean ldapAuthenticationActive, Boolean followReferences) {
		this.nonAzureAdServerConfig.urls = Collections.unmodifiableList(urls);
		this.nonAzureAdServerConfig.domains = Collections.unmodifiableList(domains);
		this.directoryType = directoryType;
		this.ldapAuthenticationActive = ldapAuthenticationActive;
		this.nonAzureAdServerConfig.followReferences = followReferences;
	}

	public LDAPServerConfiguration(AzureADServerConfig serverConfig, Boolean ldapAuthenticationActive) {
		this.directoryType = LDAPDirectoryType.AZURE_AD;
		this.ldapAuthenticationActive = ldapAuthenticationActive;
		this.azurServerConfig.authorityTenantId = serverConfig.authorityTenantId;
		this.azurServerConfig.clientId = serverConfig.clientId;
	}

	public List<String> getUrls() {
		return nonAzureAdServerConfig.urls;
	}

	public List<String> getDomains() {
		return nonAzureAdServerConfig.domains;
	}

	public LDAPDirectoryType getDirectoryType() {
		return directoryType;
	}

	public Boolean getLdapAuthenticationActive() {
		return ldapAuthenticationActive;
	}

	public Boolean getFollowReferences() {
		return nonAzureAdServerConfig.followReferences;
	}

	public String getTenantName() {
		return this.azurServerConfig.getTenantName();
	}

	public String getClientId() {
		return this.azurServerConfig.getClientId();
	}

}
