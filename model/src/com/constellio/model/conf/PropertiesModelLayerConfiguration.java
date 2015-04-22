/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.model.conf;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang.StringUtils;
import org.joda.time.Duration;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import com.constellio.data.conf.DataLayerConfiguration;
import com.constellio.data.conf.PropertiesConfiguration;
import com.constellio.model.conf.PropertiesModelLayerConfigurationRuntimeException.PropertiesModelLayerConfigurationRuntimeException_InvalidEmail;
import com.constellio.model.conf.PropertiesModelLayerConfigurationRuntimeException.PropertiesModelLayerConfigurationRuntimeException_InvalidHost;
import com.constellio.model.conf.PropertiesModelLayerConfigurationRuntimeException.PropertiesModelLayerConfigurationRuntimeException_InvalidLdapType;
import com.constellio.model.conf.PropertiesModelLayerConfigurationRuntimeException.PropertiesModelLayerConfigurationRuntimeException_InvalidPassword;
import com.constellio.model.conf.PropertiesModelLayerConfigurationRuntimeException.PropertiesModelLayerConfigurationRuntimeException_InvalidPort;
import com.constellio.model.conf.PropertiesModelLayerConfigurationRuntimeException.PropertiesModelLayerConfigurationRuntimeException_InvalidRegex;
import com.constellio.model.conf.PropertiesModelLayerConfigurationRuntimeException.PropertiesModelLayerConfigurationRuntimeException_InvalidUser;
import com.constellio.model.conf.PropertiesModelLayerConfigurationRuntimeException.PropertiesModelLayerConfigurationRuntimeException_NotABooleanValue;
import com.constellio.model.conf.ldap.LDAPDirectoryType;
import com.constellio.model.conf.ldap.LDAPServerConfiguration;
import com.constellio.model.conf.ldap.LDAPUserSyncConfiguration;
import com.constellio.model.conf.ldap.Filter;
import com.constellio.model.conf.ldap.RegexFilter;
import com.constellio.model.services.notifications.EventEmailBuilder;
import com.constellio.model.services.notifications.HtmlEventEmailBuilder;
import com.constellio.model.services.notifications.SmtpServerConfig;

public class PropertiesModelLayerConfiguration extends PropertiesConfiguration implements ModelLayerConfiguration {

	private final DataLayerConfiguration dataLayerConfiguration;
	private final FoldersLocator foldersLocator;

	public PropertiesModelLayerConfiguration(Map<String, String> configs, DataLayerConfiguration dataLayerConfiguration,
											 FoldersLocator foldersLocator) {
		super(configs);
		this.dataLayerConfiguration = dataLayerConfiguration;
		this.foldersLocator = foldersLocator;
	}

	@Override
	public void validate() {

	}

	@Override
	public boolean isDocumentsParsedInForkProcess() {
		return getBoolean("parsing.useForkProcess", false);
	}

	@Override
	public File getTempFolder() {
		return dataLayerConfiguration.getTempFolder();
	}

	@Override
	public String getComputerName() {
		return "mainserver";
	}

	@Override
	public int getBatchProcessesPartSize() {
		//return getRequiredInt("batchProcess.partSize");
		return 500;
	}

	@Override
	public int getNumberOfRecordsPerTask() {
		return 100;
	}

	@Override
	public int getForkParsersPoolSize() {
		return 20;
	}

	@Override
	public File getImportationFolder() {
		return getFile("importationFolder", foldersLocator.getDefaultImportationFolder());
	}

	@Override
	public EventEmailBuilder getEventEmailBuilder() {
		return new HtmlEventEmailBuilder(foldersLocator);
	}

	@Override
	public SmtpServerConfig getSmtpServerConfig() {
		String user = getRequiredString("smtpServer.mail.smtp.account.user");
		String email = getRequiredString("smtpServer.mail.smtp.account.email");
		String password = getRequiredString("smtpServer.mail.smtp.account.password");
		Properties properties = new Properties();
		String smtpAuth = getRequiredString("smtpServer.mail.smtp.auth");
		String starttlsEnable = getRequiredString("smtpServer.mail.smtp.starttls.enable");
		String smtpHost = getRequiredString("smtpServer.mail.smtp.host");
		String smtpPort = getRequiredString("smtpServer.mail.smtp.port");
		properties.put("mail.smtp.auth", smtpAuth);
		properties.put("mail.smtp.starttls.enable", starttlsEnable);
		properties.put("mail.smtp.host", smtpHost);
		properties.put("mail.smtp.port", smtpPort);

		validateConfigs(email, user, password, properties);

		return new SmtpServerConfig(email, user, password, properties);
	}

	@Override
	public Duration getDelayBeforeDeletingUnreferencedContents() {
		return Duration.standardMinutes(10);
	}

	@Override
	public Duration getUnreferencedContentsThreadDelayBetweenChecks() {
		return Duration.standardMinutes(5);
	}

	@Override
	public int getTokenDuration() {
		return 42;
	}

	@Override
	public int getDelayBeforeSendingNotificationEmailsInMinutes() {
		return 42;
	}

	@Override
	public String getMainDataLanguage() {
		return getString("mainDataLanguage", "fr");
	}

	private void validateConfigs(String email, String user, String password, Properties properties) {
		String emailPattern = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
		if (email == null || !email.matches(emailPattern)) {
			throw new PropertiesModelLayerConfigurationRuntimeException_InvalidEmail(email);
		}
		if (user == null || user.isEmpty()) {
			throw new PropertiesModelLayerConfigurationRuntimeException_InvalidUser(user);
		}
		if (password == null || password.isEmpty()) {
			throw new PropertiesModelLayerConfigurationRuntimeException_InvalidPassword(password);
		}
		if (!properties.getProperty("mail.smtp.auth").toLowerCase().matches("true|false")) {
			throw new PropertiesModelLayerConfigurationRuntimeException_NotABooleanValue("mail.smtp.auth",
					properties.getProperty("mail.smtp.auth").toString());
		}
		if (!properties.getProperty("mail.smtp.starttls.enable").toLowerCase().matches("true|false")) {
			throw new PropertiesModelLayerConfigurationRuntimeException_NotABooleanValue("mail.smtp.starttls.enable",
					properties.getProperty("mail.smtp.starttls.enable").toString());
		}
		if (properties.getProperty("mail.smtp.host") == null || properties.getProperty("mail.smtp.host").isEmpty()) {
			throw new PropertiesModelLayerConfigurationRuntimeException_InvalidHost(
					properties.getProperty("mail.smtp.host").toString());
		}
		try {
			Integer.parseInt(properties.getProperty("mail.smtp.port"));
		} catch (NumberFormatException e) {
			throw new PropertiesModelLayerConfigurationRuntimeException_InvalidPort(properties.getProperty("mail.smtp.port"));
		}
	}

}