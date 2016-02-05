package com.constellio.app.ui.pages.management.ldap;

import org.joda.time.Duration;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.model.conf.LDAPTestConfig;
import com.constellio.model.conf.ldap.LDAPServerConfiguration;
import com.constellio.model.conf.ldap.LDAPUserSyncConfiguration;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.InDevelopmentTest;
import com.constellio.sdk.tests.annotations.UiTest;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;

@UiTest
@InDevelopmentTest
public class LDAPConfigManagementViewAcceptanceTest extends ConstellioTest {
	ConstellioWebDriver driver;

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers()
		);
		saveValidLDAPConfig();

		driver = newWebDriver(loggedAsUserInCollection("admin", zeCollection));
	}

	private void saveValidLDAPConfig() {
		LDAPServerConfiguration ldapServerConfiguration = LDAPTestConfig.getLDAPServerConfiguration();
		LDAPUserSyncConfiguration ldapUserSyncConfiguration = LDAPTestConfig
				.getLDAPUserSyncConfiguration(new Duration(1000 * 60 * 12));
		getModelLayerFactory().getLdapConfigurationManager()
				.saveLDAPConfiguration(ldapServerConfiguration, ldapUserSyncConfiguration);
	}

	@Test
	public void navigateToLDAP() {
		driver.navigateTo().url(NavigatorConfigurationService.LDAP_CONFIG_MANAGEMENT);
		waitUntilICloseTheBrowsers();
	}
}
