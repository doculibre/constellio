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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.data.conf.DataLayerConfiguration;
import com.constellio.model.conf.PropertiesModelLayerConfigurationRuntimeException.PropertiesModelLayerConfigurationRuntimeException_InvalidEmail;
import com.constellio.model.conf.PropertiesModelLayerConfigurationRuntimeException.PropertiesModelLayerConfigurationRuntimeException_InvalidPassword;
import com.constellio.model.conf.PropertiesModelLayerConfigurationRuntimeException.PropertiesModelLayerConfigurationRuntimeException_InvalidPort;
import com.constellio.model.conf.PropertiesModelLayerConfigurationRuntimeException.PropertiesModelLayerConfigurationRuntimeException_NotABooleanValue;
import com.constellio.model.conf.ldap.LDAPDirectoryType;
import com.constellio.model.conf.ldap.LDAPServerConfiguration;
import com.constellio.model.conf.ldap.LDAPUserSyncConfiguration;
import com.constellio.model.services.notifications.SmtpServerConfig;
import com.constellio.sdk.tests.ConstellioTest;

public class PropertiesModelLayerConfigurationAcceptanceTest extends ConstellioTest {

	SmtpServerConfig smtpServerConfig;
	PropertiesModelLayerConfiguration propertiesModelLayerConfiguration;
	Map<String, String> sdkProperties;
	@Mock DataLayerConfiguration dataLayerConfiguration;

	@Before
	public void setup()
			throws Exception {

		sdkProperties = new HashMap<>();
		sdkProperties.put("smtpServer.mail.smtp.account.user", "Doculibre");
		sdkProperties.put("smtpServer.mail.smtp.account.email", "noreply.doculibre@gmail.com");
		sdkProperties.put("smtpServer.mail.smtp.account.password", "ncix123$");
		sdkProperties.put("smtpServer.mail.smtp.auth", "true");
		sdkProperties.put("smtpServer.mail.smtp.starttls.enable", "true");
		sdkProperties.put("smtpServer.mail.smtp.host", "smtp.gmail.com");
		sdkProperties.put("smtpServer.mail.smtp.port", "587");

		propertiesModelLayerConfiguration = new PropertiesModelLayerConfiguration(sdkProperties, dataLayerConfiguration,
				getFoldersLocator());
	}



	@Test
	public void whenGetSmtpServerThenItIsCreatedWithConfigInformations()
			throws Exception {

		smtpServerConfig = propertiesModelLayerConfiguration.getSmtpServerConfig();

		assertThat(smtpServerConfig.getUser()).isEqualTo("Doculibre");
		assertThat(smtpServerConfig.getEmail()).isEqualTo("noreply.doculibre@gmail.com");
		assertThat(smtpServerConfig.getPassword()).isEqualTo("ncix123$");
		assertThat(smtpServerConfig.getProperties().get("mail.smtp.auth")).isEqualTo("true");
		assertThat(smtpServerConfig.getProperties().get("mail.smtp.starttls.enable")).isEqualTo("true");
		assertThat(smtpServerConfig.getProperties().get("mail.smtp.host")).isEqualTo("smtp.gmail.com");
		assertThat(smtpServerConfig.getProperties().get("mail.smtp.port")).isEqualTo("587");
	}

	@Test
	public void givenInvalidEmailWhenGetSmtpServerThenException()
			throws Exception {

		try {
			sdkProperties.put("smtpServer.mail.smtp.account.email", "noreply.doculibregmail.com");
			propertiesModelLayerConfiguration.getSmtpServerConfig();
		} catch (PropertiesModelLayerConfigurationRuntimeException_InvalidEmail e) {
			assertThat(e.getMessage()).isEqualTo("Invalid email: noreply.doculibregmail.com");
		}
	}

	@Test
	public void givenMissingUserWhenGetSmtpServerThenException()
			throws Exception {

		try {
			sdkProperties.remove("smtpServer.mail.smtp.account.user");
			propertiesModelLayerConfiguration.getSmtpServerConfig();
		} catch (Exception e) {
			assertThat(e.getMessage()).isEqualTo("Property 'smtpServer.mail.smtp.account.user' must be defined");
		}
	}

	@Test
	public void givenEmptyPasswordWhenGetSmtpServerThenException()
			throws Exception {

		try {
			sdkProperties.put("smtpServer.mail.smtp.account.password", "");
			propertiesModelLayerConfiguration.getSmtpServerConfig();
		} catch (PropertiesModelLayerConfigurationRuntimeException_InvalidPassword e) {
			assertThat(e.getMessage()).isEqualTo("Invalid password: ");
		}
	}

	@Test
	public void givenNotACorrespondentBooleanValueToAuthWhenGetSmtpServerThenException()
			throws Exception {

		try {
			sdkProperties.put("smtpServer.mail.smtp.auth", "notABooleanCorrespondentValue");
			propertiesModelLayerConfiguration.getSmtpServerConfig();
		} catch (PropertiesModelLayerConfigurationRuntimeException_NotABooleanValue e) {
			assertThat(e.getMessage())
					.isEqualTo("mail.smtp.auth is not a boolean value: notABooleanCorrespondentValue");
		}
	}

	@Test
	public void givenNullValueToAuthWhenGetSmtpServerThenException()
			throws Exception {
		try {
			sdkProperties.put("smtpServer.mail.smtp.auth", null);
			propertiesModelLayerConfiguration.getSmtpServerConfig();
		} catch (Exception e) {
			assertThat(e.getMessage()).isEqualTo("Property 'smtpServer.mail.smtp.auth' must be defined");
		}
	}

	@Test
	public void givenNotACorrespondentBooleanValueToStartTlsWhenGetSmtpServerThenException()
			throws Exception {

		try {
			sdkProperties.put("smtpServer.mail.smtp.starttls.enable", "notABooleanCorrespondentValue");
			propertiesModelLayerConfiguration.getSmtpServerConfig();
		} catch (PropertiesModelLayerConfigurationRuntimeException_NotABooleanValue e) {
			assertThat(e.getMessage())
					.isEqualTo("mail.smtp.starttls.enable is not a boolean value: notABooleanCorrespondentValue");
		}
	}

	@Test
	public void givenNullHostWhenGetSmtpServerThenException()
			throws Exception {

		try {
			sdkProperties.put("smtpServer.mail.smtp.host", null);
			propertiesModelLayerConfiguration.getSmtpServerConfig();
		} catch (Exception e) {
			assertThat(e.getMessage()).isEqualTo("Property 'smtpServer.mail.smtp.host' must be defined");
		}
	}

	@Test
	public void givenNotANumberToPortWhenGetSmtpServerThenException()
			throws Exception {

		try {
			sdkProperties.put("smtpServer.mail.smtp.port", "a");
			propertiesModelLayerConfiguration.getSmtpServerConfig();
		} catch (PropertiesModelLayerConfigurationRuntimeException_InvalidPort e) {
			assertThat(e.getMessage()).isEqualTo("Invalid port: a");
		}
	}

	@Test
	public void givenBatchProcessPartSizeWhenItThenReturnIt()
			throws Exception {

		sdkProperties.put("batchProcess.partSize", "500");

		assertThat(propertiesModelLayerConfiguration.getBatchProcessesPartSize()).isEqualTo(500);
	}
}
