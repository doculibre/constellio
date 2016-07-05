package com.constellio.model.conf.ldap.config;

public class ADAzurServerConfig {
	String clientId;
	String authorityUrl = "https://login.microsoftonline.com/";
	String authorityTenantId;

	public String getClientId() {
		return clientId;
	}

	public ADAzurServerConfig setClientId(String clientId) {
		this.clientId = clientId;
		return this;
	}

	public String getAuthorityUrl() {
		return authorityUrl;
	}

	public ADAzurServerConfig setAuthorityUrl(String authorityUrl) {
		this.authorityUrl = authorityUrl;
		return this;
	}

	public String getAuthorityTenantId() {
		return authorityTenantId;
	}

	public ADAzurServerConfig setAuthorityTenantId(String authorityTenantId) {
		this.authorityTenantId = authorityTenantId;
		return this;
	}
}
