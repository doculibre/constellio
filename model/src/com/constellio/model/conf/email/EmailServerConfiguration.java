package com.constellio.model.conf.email;

import java.util.Map;

public interface EmailServerConfiguration {
	boolean isEnabled();

	Map<String, String> getProperties();

	String getUsername();

	String getPassword();

	String getDefaultSenderEmail();

	EmailServerConfiguration whichIsDisabled();
}
