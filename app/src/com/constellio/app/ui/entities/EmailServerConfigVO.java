package com.constellio.app.ui.entities;

import java.io.Serializable;
import java.util.Map;

import com.constellio.model.conf.email.EmailServerConfiguration;

@SuppressWarnings("serial")
public class EmailServerConfigVO implements Serializable, EmailServerConfiguration {
	boolean enabled = true;
	String username;
	String password;
	private String defaultEmailServer;

	public Map<String, String> getProperties() {
		return properties;
	}

	public EmailServerConfigVO setProperties(Map<String, String> properties) {
		this.properties = properties;
		return this;
	}

	public String getUsername() {
		return username;
	}

	public EmailServerConfigVO setUsername(String username) {
		this.username = username;
		return this;
	}

	public String getPassword() {
		return password;
	}

	@Override
	public String getDefaultSenderEmail() {
		return defaultEmailServer;
	}

	@Override
	public EmailServerConfiguration whichIsDisabled() {
		return null;
	}

	public EmailServerConfigVO setDefaultEmailServer(String defaultEmailServer) {
		this.defaultEmailServer = defaultEmailServer;
		return this;
	}

	public EmailServerConfigVO setPassword(String password) {
		this.password = password;
		return this;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	public EmailServerConfigVO setEnabled(boolean enabled) {
		this.enabled = enabled;
		return this;
	}

	Map<String, String> properties;
}
