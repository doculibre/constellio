package com.constellio.model.conf.ldap.config;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import com.constellio.model.conf.ldap.LDAPDirectoryType;

public class LDAPServerConfiguration implements Serializable {
	private Boolean ldapAuthenticationActive;

	private LDAPDirectoryType directoryType;

	NonAzurAdServerConfig nonAzurAdServerConfig;
	ADAzurServerConfig azurServerConfig;

	public LDAPServerConfiguration(List<String> urls, List<String> domains, LDAPDirectoryType directoryType,
			Boolean ldapAuthenticationActive, Boolean followReferences) {
		this.nonAzurAdServerConfig.urls = Collections.unmodifiableList(urls);
		this.nonAzurAdServerConfig.domains = Collections.unmodifiableList(domains);
		this.directoryType = directoryType;
		this.ldapAuthenticationActive = ldapAuthenticationActive;
		this.nonAzurAdServerConfig.followReferences = followReferences;
	}

	public LDAPServerConfiguration(Boolean ldapAuthenticationActive, String authorityTenantId, String authorityUrl,
			String clientId) {
		this.directoryType = LDAPDirectoryType.AZUR_AD;
		this.ldapAuthenticationActive = ldapAuthenticationActive;
		this.azurServerConfig.authorityTenantId = authorityTenantId;
		this.azurServerConfig.authorityUrl = authorityUrl;
		this.azurServerConfig.clientId = clientId;
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

	public String getAuthorityUrl(){
		return this.azurServerConfig.authorityUrl;
	}

	public String getAuthorityTenantId(){
		return this.azurServerConfig.authorityTenantId;
	}

	public String getclientId(){
		return this.azurServerConfig.clientId;
	}
}
