package com.constellio.model.conf.ldap.config;

public class AzureADUserSynchConfig {
	String applicationKey;

	public String getApplicationKey() {
		return applicationKey;
	}

	public AzureADUserSynchConfig setApplicationKey(String applicationKey) {
		this.applicationKey = applicationKey;
		return this;
	}
}
