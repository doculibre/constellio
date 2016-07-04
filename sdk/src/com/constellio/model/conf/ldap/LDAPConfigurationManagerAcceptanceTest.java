package com.constellio.model.conf.ldap;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.HashMap;

import org.joda.time.Duration;
import org.junit.Before;
import org.junit.Test;

import com.constellio.model.conf.LDAPTestConfig;
import com.constellio.model.conf.ldap.config.LDAPServerConfiguration;
import com.constellio.model.conf.ldap.config.LDAPUserSyncConfiguration;
import com.constellio.sdk.tests.ConstellioTest;

public class LDAPConfigurationManagerAcceptanceTest extends ConstellioTest {

	private LDAPConfigurationManager ldapConfigManager;

	@Before
	public void setup()
			throws Exception {
		//pour avoir le fichier d encryptage
		prepareSystem(
				withZeCollection()
		);

		sdkProperties = new HashMap<>();
		ldapConfigManager = getModelLayerFactory().getLdapConfigurationManager();
	}

	private void saveValidLDAPConfig() {
		LDAPServerConfiguration ldapServerConfiguration = LDAPTestConfig.getLDAPServerConfiguration();
		LDAPUserSyncConfiguration ldapUserSyncConfiguration = LDAPTestConfig.getLDAPUserSyncConfiguration();
		ldapConfigManager.saveLDAPConfiguration(ldapServerConfiguration, ldapUserSyncConfiguration);
	}

	@Test
	public void givenConfigWithDurationWhenSaveConfigurationThenDurationIsSavedAsNull()
			throws Exception {
		LDAPServerConfiguration ldapServerConfiguration = LDAPTestConfig.getLDAPServerConfiguration();
		LDAPUserSyncConfiguration ldapUserSyncConfiguration = LDAPTestConfig.getLDAPUserSyncConfiguration();
		ldapUserSyncConfiguration.setDurationBetweenExecution(new Duration(0));
		ldapConfigManager.saveLDAPConfiguration(ldapServerConfiguration, ldapUserSyncConfiguration);

		assertThat(ldapConfigManager.isLDAPAuthentication()).isEqualTo(true);
		assertThat(ldapConfigManager.idUsersSynchActivated()).isEqualTo(false);
		ldapUserSyncConfiguration = ldapConfigManager.getLDAPUserSyncConfiguration();

		assertThat(ldapUserSyncConfiguration.getDurationBetweenExecution()).isNull();
	}

	@Test(expected = TooShortDurationRuntimeException.class)
	public void givenConfigWithAShortDurationWhenSaveConfigurationThenException()
			throws Exception {
		LDAPServerConfiguration ldapServerConfiguration = LDAPTestConfig.getLDAPServerConfiguration();
		LDAPUserSyncConfiguration ldapUserSyncConfiguration = LDAPTestConfig.getLDAPUserSyncConfiguration();
		ldapUserSyncConfiguration.setDurationBetweenExecution(new Duration(LDAPConfigurationManager.MIN_DURATION - 1));
		ldapConfigManager.saveLDAPConfiguration(ldapServerConfiguration, ldapUserSyncConfiguration);
	}

	@Test
	public void givenConfigWithANonShortDurationWhenSaveConfigurationSavedCorrectly()
			throws Exception {
		LDAPServerConfiguration ldapServerConfiguration = LDAPTestConfig.getLDAPServerConfiguration();
		LDAPUserSyncConfiguration ldapUserSyncConfiguration = LDAPTestConfig.getLDAPUserSyncConfiguration();

		Duration nonShortDuration = new Duration(LDAPConfigurationManager.MIN_DURATION + 1);
		ldapUserSyncConfiguration.setDurationBetweenExecution(nonShortDuration);
		ldapConfigManager.saveLDAPConfiguration(ldapServerConfiguration, ldapUserSyncConfiguration);
		ldapUserSyncConfiguration = ldapConfigManager.getLDAPUserSyncConfiguration();

		assertThat(ldapUserSyncConfiguration.getDurationBetweenExecution().toStandardMinutes())
				.isEqualTo(nonShortDuration.toStandardMinutes());
	}

	@Test
	public void whenGetLDAPServerConfigurationThenItIsCreatedWithConfigInformation()
			throws Exception {
		saveValidLDAPConfig();
		assertThat(ldapConfigManager.isLDAPAuthentication()).isEqualTo(true);
		LDAPServerConfiguration ldapServerConfiguration = ldapConfigManager.getLDAPServerConfiguration();

		assertThat(ldapServerConfiguration.getDirectoryType()).isEqualTo(LDAPDirectoryType.ACTIVE_DIRECTORY);
		assertThat(ldapServerConfiguration.getUrls()).containsAll(LDAPTestConfig.getUrls());
		assertThat(ldapServerConfiguration.getDomains()).containsAll(LDAPTestConfig.getDomains());
	}

	@Test
	public void whenGetLDAPSyncConfigurationThenItIsCreatedWithConfigInformation()
			throws Exception {
		saveValidLDAPConfig();
		LDAPUserSyncConfiguration ldapUserSyncConfiguration = ldapConfigManager.getLDAPUserSyncConfiguration(true);

		//assertThat(ldapUserSyncConfiguration.getDurationBetweenExecution().getStandardDays()).isEqualTo(1l);
		assertThat(ldapUserSyncConfiguration.getGroupBaseContextList())
				.containsAll(Arrays.asList("OU=Groupes,DC=test,DC=doculibre,DC=ca"));
		assertThat(ldapUserSyncConfiguration.getUsersWithoutGroupsBaseContextList())
				.containsAll(Arrays.asList("CN=Users,DC=test,DC=doculibre,DC=ca"));
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
