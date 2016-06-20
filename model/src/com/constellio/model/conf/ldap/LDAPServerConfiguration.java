package com.constellio.model.conf.ldap;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class LDAPServerConfiguration implements Serializable {
	private Boolean ldapAuthenticationActive;
	private Boolean followReferences;

	private List<String> urls;

	private List<String> domains;

	private LDAPDirectoryType directoryType;

	public LDAPServerConfiguration(List<String> urls, List<String> domains, LDAPDirectoryType directoryType,
			Boolean ldapAuthenticationActive, Boolean followReferences) {
		this.urls = Collections.unmodifiableList(urls);
		this.domains = Collections.unmodifiableList(domains);
		this.directoryType = directoryType;
		this.ldapAuthenticationActive = ldapAuthenticationActive;
		this.followReferences = followReferences;
	}

	public LDAPServerConfiguration(Boolean ldapAuthenticationActive, String azurClientID1, String azur2, String azur3) {
		this.directoryType = LDAPDirectoryType.AZUR_AD;
		this.ldapAuthenticationActive = ldapAuthenticationActive;
		//TODO
	}

	public List<String> getUrls() {
		return urls;
	}

	public List<String> getDomains() {
		return domains;
	}

	public LDAPDirectoryType getDirectoryType() {
		return directoryType;
	}

	public Boolean getLdapAuthenticationActive() {
		return ldapAuthenticationActive;
	}

	public Boolean getFollowReferences() {
		return followReferences;
	}
}
