package com.constellio.model.conf.email;

import java.io.Serializable;
import java.util.Map;

public interface EmailServerConfiguration extends Serializable {
	
	boolean isEnabled();

	Map<String, String> getProperties();

	String getUsername();

	String getPassword();

	String getDefaultSenderEmail();

	EmailServerConfiguration whichIsDisabled();
}
