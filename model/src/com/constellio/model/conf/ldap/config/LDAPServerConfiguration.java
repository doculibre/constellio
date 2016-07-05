package com.constellio.model.conf.ldap.config;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.constellio.model.conf.ldap.LDAPDirectoryType;
import com.constellio.model.entities.records.wrappers.Collection;

public class LDAPServerConfiguration implements Serializable {
	private Boolean ldapAuthenticationActive;

	private LDAPDirectoryType directoryType;

	NonAzurAdServerConfig nonAzurAdServerConfig = new NonAzurAdServerConfig();
	ADAzurServerConfig azurServerConfig = new ADAzurServerConfig();

	public LDAPServerConfiguration(List<String> urls, List<String> domains, LDAPDirectoryType directoryType,
			Boolean ldapAuthenticationActive, Boolean followReferences) {
		this.nonAzurAdServerConfig.urls = Collections.unmodifiableList(urls);
		this.nonAzurAdServerConfig.domains = Collections.unmodifiableList(domains);
		this.directoryType = directoryType;
		this.ldapAuthenticationActive = ldapAuthenticationActive;
		this.nonAzurAdServerConfig.followReferences = followReferences;
	}

	public LDAPServerConfiguration(ADAzurServerConfig serverConfig, Boolean ldapAuthenticationActive) {
		this.directoryType = LDAPDirectoryType.AZUR_AD;
		this.ldapAuthenticationActive = ldapAuthenticationActive;
		this.azurServerConfig.authorityTenantId = serverConfig.authorityTenantId;
		this.azurServerConfig.authorityUrl = serverConfig.authorityUrl;
		this.azurServerConfig.clientId = serverConfig.clientId;
	}

	public List<String> getUrls() {
		return nonAzurAdServerConfig.urls;
	}

	public List<String> getDomains() {
		return nonAzurAdServerConfig.domains;
	}

	public LDAPDirectoryType getDirectoryType() {
		return directoryType;
	}

	public Boolean getLdapAuthenticationActive() {
		return ldapAuthenticationActive;
	}

	public Boolean getFollowReferences() {
		return nonAzurAdServerConfig.followReferences;
	}

	public String getAuthorityUrl() {
		return this.azurServerConfig.authorityUrl;
	}

	public String getAuthorityTenantId() {
		return this.azurServerConfig.authorityTenantId;
	}

	public String getClientId() {
		return this.azurServerConfig.clientId;
	}
}
