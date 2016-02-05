package com.constellio.app.modules.es.connectors.smb.security;

public class Credentials {
	private String domain;
	private String username;
	private String password;

	public Credentials() {
	}

	public Credentials(String domain, String username, String password) {
		this.domain = domain;
		this.username = username;
		this.password = password;
	}

	public String getDomain() {
		return domain;
	}

	public Credentials setDomain(String domain) {
		this.domain = domain;
		return this;
	}

	public String getUsername() {
		return username;
	}

	public Credentials setUsername(String username) {
		this.username = username;
		return this;
	}

	public String getPassword() {
		return password;
	}

	public Credentials setPassword(String password) {
		this.password = password;
		return this;
	}
}