package com.constellio.model.conf.email;

import java.util.Map;

public class BaseEmailServerConfiguration implements EmailServerConfiguration {

	boolean enabled;
	String username;
	String password;
	private Map<String, String> properties;
	private String defaultSenderEmail;

	public BaseEmailServerConfiguration(String username, String password, String defaultSenderEmail,
			Map<String, String> properties,
			boolean enabled) {
		this.enabled = enabled;
		this.username = username;
		this.defaultSenderEmail = defaultSenderEmail;
		this.password = password;
		this.properties = properties;
	}

	@Override
	public Map<String, String> getProperties() {
		return properties;
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String getDefaultSenderEmail() {
		return defaultSenderEmail;
	}

	@Override
	public EmailServerConfiguration whichIsDisabled() {
		return new BaseEmailServerConfiguration(username, password, defaultSenderEmail, properties, false);
	}

	public boolean isEnabled() {
		return enabled;
	}
}
