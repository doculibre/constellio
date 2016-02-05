package com.constellio.model.entities;

import java.util.Map;

public class SmtpServerConfig {

	final String user;
	final String email;
	final String password;
	final Map<String, String> properties;

	public SmtpServerConfig(String email, String user, String password, Map<String, String> properties) {
		super();
		this.user = user;
		this.email = email;
		this.password = password;
		this.properties = properties;
	}

	public String getUser() {
		return user;
	}

	public String getEmail() {
		return email;
	}

	public String getPassword() {
		return password;
	}

	public Map<String, String> getProperties() {
		return properties;
	}

}
