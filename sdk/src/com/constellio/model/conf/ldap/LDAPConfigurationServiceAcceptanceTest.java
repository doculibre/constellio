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
package com.constellio.model.conf.ldap;

import com.constellio.model.conf.LDAPTestConfig;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class LDAPConfigurationServiceAcceptanceTest extends ConstellioTest{
    //TODO

    LDAPConfigurationManager ldapConfigurationService;
    Map<String, String> sdkProperties;

    @Before
    public void setup()
            throws Exception {

        sdkProperties = new HashMap<>();
        saveValidLDAPConfig();
        ldapConfigurationService = getModelLayerFactory().getLdapConfigurationManager();
    }

    private void saveValidLDAPConfig() {
        LDAPServerConfiguration ldapServerConfiguration = LDAPTestConfig.getLDAPServerConfiguration();
        LDAPUserSyncConfiguration ldapUserSyncConfiguration = LDAPTestConfig.getLDAPUserSyncConfiguration();
        getModelLayerFactory().getLdapConfigurationManager().saveLDAPConfiguration(ldapServerConfiguration, ldapUserSyncConfiguration);
    }

    @Test
    public void whenGetLDAPServerConfigurationThenItIsCreatedWithConfigInformation()
            throws Exception {
        assertThat(ldapConfigurationService.isLDAPAuthentication()).isEqualTo(true);
        LDAPServerConfiguration ldapServerConfiguration = ldapConfigurationService.getLDAPServerConfiguration();

        assertThat(ldapServerConfiguration.getDirectoryType()).isEqualTo(LDAPDirectoryType.ACTIVE_DIRECTORY);
        assertThat(ldapServerConfiguration.getUrls()).containsAll(LDAPTestConfig.getUrls());
        assertThat(ldapServerConfiguration.getDomains()).containsAll(LDAPTestConfig.getDomains());
    }

    @Test
    public void whenGetLDAPSyncConfigurationThenItIsCreatedWithConfigInformation()
            throws Exception {
        LDAPUserSyncConfiguration ldapUserSyncConfiguration = ldapConfigurationService.getLDAPUserSyncConfiguration();

        //assertThat(ldapUserSyncConfiguration.getDurationBetweenExecution().getStandardDays()).isEqualTo(1l);
        assertThat(ldapUserSyncConfiguration.getGroupBaseContextList()).containsAll(Arrays.asList(new String[]{"OU=Groupes,DC=test,DC=doculibre,DC=ca"}));
        assertThat(ldapUserSyncConfiguration.getUsersWithoutGroupsBaseContextList()).containsAll(Arrays.asList(new String []{"CN=Users,DC=test,DC=doculibre,DC=ca"}));
        assertThat(ldapUserSyncConfiguration.getUser()).isEqualTo(LDAPTestConfig.getUser());
        assertThat(ldapUserSyncConfiguration.getPassword()).isEqualTo(LDAPTestConfig.getPassword());
        assertThat(ldapUserSyncConfiguration.isGroupAccepted("GGS-SEC-ALF_SCEC_ext1")).isTrue();
        assertThat(ldapUserSyncConfiguration.isGroupAccepted("GGS-SEC-ALF_SCEC_ext")).isFalse();
        assertThat(ldapUserSyncConfiguration.isGroupAccepted("GGS-SEC-ALF_SCEC")).isTrue();
        assertThat(ldapUserSyncConfiguration.isGroupAccepted("GGS-SEC-ALF_SCEC_ext")).isFalse();
        assertThat(ldapUserSyncConfiguration.isGroupAccepted("GGS-SEC-ALF_tous_centres_SCEC")).isFalse();
        assertThat(ldapUserSyncConfiguration.isUserAccepted("testuser")).isTrue();
        assertThat(ldapUserSyncConfiguration.isUserAccepted("testAuj")).isFalse();
        assertThat(ldapUserSyncConfiguration.isUserAccepted("admin")).isFalse();

    }
}
