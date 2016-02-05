package com.constellio.model.conf.email;

import java.util.Map;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.lang3.StringUtils;

import com.constellio.model.conf.email.EmailServerConfigurationRuntimeException.InvalidBlankHostRuntimeException;
import com.constellio.model.conf.email.EmailServerConfigurationRuntimeException.InvalidBlankUsernameRuntimeException;
import com.constellio.model.conf.email.EmailServerConfigurationRuntimeException.InvalidEmailAddressRuntimeException;
import com.constellio.model.conf.email.EmailServerConfigurationRuntimeException.InvalidPropertiesRuntimeException;
import com.constellio.model.conf.email.EmailServerConfigurationRuntimeException.UnknownServerConfigurationRuntimeException;

public class EmailServerConfigurationFactory {
	private static final String SMTP_SERVER_KEY = "mail.smtp.auth";

	public EmailServerConfiguration getServerConfiguration(String username, String defaultSenderEmail, String password,
			Map<String, String> serverProperties, boolean enabled) {
		if (serverProperties.isEmpty()) {
			throw new InvalidPropertiesRuntimeException();
		}
		if (StringUtils.isNotBlank(defaultSenderEmail)) {
			if (invalidAddress(defaultSenderEmail)) {
				throw new InvalidEmailAddressRuntimeException(defaultSenderEmail);
			}
		}
		if (StringUtils.isBlank(username)) {
			throw new InvalidBlankUsernameRuntimeException();
		}
		EmailServerConfiguration serverConfiguration = getSmtpServerProperties(username, defaultSenderEmail, password,
				serverProperties, enabled);
		if (serverConfiguration == null) {
			throw new UnknownServerConfigurationRuntimeException();
		}
		return serverConfiguration;
	}

	private boolean invalidAddress(String defaultSenderEmail) {
		try {
			new InternetAddress(defaultSenderEmail, true);
			return false;
		} catch (AddressException e) {
			return true;
		}
	}

	private EmailServerConfiguration getSmtpServerProperties(String username, String defaultSenderEmail, String password,
			Map<String, String> serverProperties, boolean enabled) {
		//		String isSmtpConfig = serverProperties.get(SMTP_SERVER_KEY);
		//		if (isSmtpConfig == null || !isSmtpConfig.equalsIgnoreCase("true")) {
		//			return null;
		//		}
		String host = serverProperties.get("mail.smtp.host");
		if (StringUtils.isBlank(host)) {
			throw new InvalidBlankHostRuntimeException();
		}
		//		String port = serverProperties.get("mail.smtp.port");
		//		if (StringUtils.isBlank(port)) {
		//			throw new InvalidBlankPortRuntimeException();
		//		}
		return new BaseEmailServerConfiguration(username, password, defaultSenderEmail, serverProperties, enabled);
	}

}
