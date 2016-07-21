package com.constellio.model.conf;

import static java.util.Arrays.asList;

import org.joda.time.Duration;

import com.constellio.model.conf.ldap.config.AzureADServerConfig;
import com.constellio.model.conf.ldap.config.AzureADUserSynchConfig;
import com.constellio.model.conf.ldap.config.LDAPServerConfiguration;
import com.constellio.model.conf.ldap.config.LDAPUserSyncConfiguration;
import com.constellio.sdk.SDKPasswords;

public class AzureADTestConf {
	public static LDAPServerConfiguration getLDAPServerConfiguration() {
		AzureADServerConfig azurServerConf = new AzureADServerConfig()
				.setClientId(SDKPasswords.testAzureAuthenticationApplicationId())
				.setAuthorityTenantId(SDKPasswords.testAzureTenantName());
		return new LDAPServerConfiguration(azurServerConf, false);
	}

	public static LDAPUserSyncConfiguration getLDAPUserSyncConfiguration(String... collections) {
		AzureADUserSynchConfig azurUserSynch = new AzureADUserSynchConfig()
				.setClientId(SDKPasswords.testAzureSynchClientId())
				.setApplicationKey(SDKPasswords.testAzureSynchApplicationKey());
		return new LDAPUserSyncConfiguration(azurUserSynch, null, null, new Duration(10000000), asList(collections));

	}
}
