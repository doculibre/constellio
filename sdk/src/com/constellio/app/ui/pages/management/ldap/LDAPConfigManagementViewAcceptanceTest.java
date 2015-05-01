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
package com.constellio.app.ui.pages.management.ldap;

import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.model.conf.LDAPTestConfig;
import com.constellio.model.conf.ldap.LDAPServerConfiguration;
import com.constellio.model.conf.ldap.LDAPUserSyncConfiguration;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.InDevelopmentTest;
import com.constellio.sdk.tests.annotations.UiTest;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;
import org.joda.time.Duration;
import org.junit.Before;
import org.junit.Test;

@UiTest
@InDevelopmentTest
public class LDAPConfigManagementViewAcceptanceTest extends ConstellioTest {
    ConstellioWebDriver driver;

    @Before
    public void setUp()
            throws Exception {
        givenCollection(zeCollection).withConstellioRMModule().withAllTestUsers();
        saveValidLDAPConfig();

        driver = newWebDriver(loggedAsUserInCollection("admin", zeCollection));
    }

    private void saveValidLDAPConfig() {
        LDAPServerConfiguration ldapServerConfiguration = LDAPTestConfig.getLDAPServerConfiguration();
        LDAPUserSyncConfiguration ldapUserSyncConfiguration = LDAPTestConfig.getLDAPUserSyncConfiguration(new Duration(1000*60*12));
        getModelLayerFactory().getLdapConfigurationManager().saveLDAPConfiguration(ldapServerConfiguration, ldapUserSyncConfiguration);
    }

    @Test
    public void navigateToLDAP() {
        driver.navigateTo().url(NavigatorConfigurationService.LDAP_CONFIG_MANAGEMENT);
        waitUntilICloseTheBrowsers();
    }
}
