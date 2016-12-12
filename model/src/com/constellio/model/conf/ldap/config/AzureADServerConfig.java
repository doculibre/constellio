package com.constellio.model.conf.ldap.config;

public class AzureADServerConfig {

	String clientId;
	String authorityTenantId;

	public String getClientId() {
		return clientId;
	}

	public AzureADServerConfig setClientId(String clientId) {
		this.clientId = clientId;
		return this;
	}

	public String getTenantName() {
		return authorityTenantId;
	}

	public AzureADServerConfig setAuthorityTenantId(String authorityTenantId) {
		this.authorityTenantId = authorityTenantId;
		return this;
	}

}
