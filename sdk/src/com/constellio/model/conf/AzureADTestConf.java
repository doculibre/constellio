package com.constellio.model.conf;

import com.constellio.model.conf.ldap.config.AzureADServerConfig;
import com.constellio.model.conf.ldap.config.AzureADUserSynchConfig;
import com.constellio.model.conf.ldap.config.LDAPServerConfiguration;
import com.constellio.model.conf.ldap.config.LDAPUserSyncConfiguration;
import com.constellio.sdk.SDKPasswords;
import org.joda.time.Duration;

import static java.util.Arrays.asList;

public class AzureADTestConf {
	public static LDAPServerConfiguration getLDAPServerConfiguration() {
		AzureADServerConfig azurServerConf = new AzureADServerConfig()
				.setClientId(SDKPasswords.testAzureAuthenticationApplicationId())
				.setAuthorityTenantId(SDKPasswords.testAzureTenantName());
		return new LDAPServerConfiguration(azurServerConf, true);
	}

	public static LDAPUserSyncConfiguration getLDAPUserSyncConfiguration(String... collections) {
		AzureADUserSynchConfig azurUserSynch = new AzureADUserSynchConfig()
				.setClientId(SDKPasswords.testAzureSynchClientId())
				.setApplicationKey(SDKPasswords.testAzureSynchApplicationKey());
		return new LDAPUserSyncConfiguration(azurUserSynch, null, null, new Duration(10000000), null, asList(collections));

	}
}
