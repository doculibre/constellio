package com.constellio.model.conf.ldap.config;

public class AzureADServerConfig {
	public static final String GRAPH_API_URL = "https://graph.windows.net/";

	public static final String GRAPH_API_VERSION = "1.6";

	String clientId;
	String authorityUrl = "https://login.microsoftonline.com/";
	String authorityTenantId;
	String resource = GRAPH_API_URL;

	public String getClientId() {
		return clientId;
	}

	public AzureADServerConfig setClientId(String clientId) {
		this.clientId = clientId;
		return this;
	}

	public String getAuthorityUrl() {
		return authorityUrl;
	}

	public AzureADServerConfig setAuthorityUrl(String authorityUrl) {
		this.authorityUrl = authorityUrl;
		return this;
	}

	public String getTenantName() {
		return authorityTenantId;
	}

	public AzureADServerConfig setAuthorityTenantId(String authorityTenantId) {
		this.authorityTenantId = authorityTenantId;
		return this;
	}

	public String getResource() {
		return resource;
	}

	public AzureADServerConfig setResource(String resource) {
		this.resource = resource;
		return this;
	}
}
