package com.constellio.model.conf.ldap.config;

public class AzureADUserSynchConfig {
	String applicationKey;
	private String clientId;

	public String getApplicationKey() {
		return applicationKey;
	}

	public AzureADUserSynchConfig setApplicationKey(String applicationKey) {
		this.applicationKey = applicationKey;
		return this;
	}

	public String getClientId() {
		return clientId;
	}

	public AzureADUserSynchConfig setClientId(String clientId) {
		this.clientId = clientId;
		return this;
	}
}
