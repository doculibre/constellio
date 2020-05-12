package com.constellio.model.conf.ldap;

import com.constellio.model.conf.LDAPTestConfig;
import com.constellio.model.conf.ldap.config.AzureADServerConfig;
import com.constellio.model.conf.ldap.config.AzureADUserSynchConfig;
import com.constellio.model.conf.ldap.config.LDAPServerConfiguration;
import com.constellio.model.conf.ldap.config.LDAPUserSyncConfiguration;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.InDevelopmentTest;
import com.constellio.sdk.tests.annotations.InternetTest;
import org.joda.time.Duration;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@InternetTest
public class LDAPConfigurationManagerAcceptanceTest extends ConstellioTest {

	private LDAPConfigurationManager ldapConfigManager;
	private RegexFilter azurUsersRegex = new RegexFilter("zAcceptUser", "zRejectUser"), azurGroupsRegex = new RegexFilter("zAccG",
			"zRejectGroups");
	private Duration azurDuration = new Duration(120000 * 60);
	private List<String> azurCollections = Arrays.asList("zAzurColl1", "zAzurColl2");

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

	private void saveValidAzurConfig() {
		AzureADServerConfig serverConfig = new AzureADServerConfig().setClientId("zclientId").setAuthorityTenantId("zTanentId");
		LDAPServerConfiguration ldapServerConfiguration = new LDAPServerConfiguration(serverConfig, false);
		AzureADUserSynchConfig azurConf = new AzureADUserSynchConfig().setApplicationKey("zApplicationKey").setClientId("synchClientId");
		LDAPUserSyncConfiguration ldapUserSyncConfiguration = new LDAPUserSyncConfiguration(azurConf, azurUsersRegex,
				azurGroupsRegex, azurDuration, null, azurCollections);
		ldapConfigManager.saveLDAPConfiguration(ldapServerConfiguration, ldapUserSyncConfiguration);
	}

	@Test
	@InDevelopmentTest
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
	@InDevelopmentTest
	public void givenConfigWithAShortDurationWhenSaveConfigurationThenException()
			throws Exception {
		LDAPServerConfiguration ldapServerConfiguration = LDAPTestConfig.getLDAPServerConfiguration();
		LDAPUserSyncConfiguration ldapUserSyncConfiguration = LDAPTestConfig.getLDAPUserSyncConfiguration();
		ldapUserSyncConfiguration.setDurationBetweenExecution(new Duration(LDAPConfigurationManager.MIN_DURATION - 1));
		ldapConfigManager.saveLDAPConfiguration(ldapServerConfiguration, ldapUserSyncConfiguration);
	}

	@Test
	@InDevelopmentTest
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
	@InDevelopmentTest
	public void givenLDAPSavedAfterAzurWhenGetLDAPServerConfigurationThenItIsCreatedWithConfigInformation()
			throws Exception {
		saveValidAzurConfig();
		saveValidLDAPConfig();
		assertThat(ldapConfigManager.isLDAPAuthentication()).isEqualTo(true);
		LDAPServerConfiguration ldapServerConfiguration = ldapConfigManager.getLDAPServerConfiguration();

		assertThat(ldapServerConfiguration.getDirectoryType()).isEqualTo(LDAPDirectoryType.ACTIVE_DIRECTORY);
		assertThat(ldapServerConfiguration.getUrls()).containsAll(LDAPTestConfig.getUrls());
		assertThat(ldapServerConfiguration.getDomains()).containsAll(LDAPTestConfig.getDomains());

		assertThat(ldapServerConfiguration.getTenantName()).isNull();
		assertThat(ldapServerConfiguration.getClientId()).isNull();
	}

	@Test
	@InDevelopmentTest
	public void givenLDAPSavedAfterAzurWhenGetLDAPSyncConfigurationThenItIsCreatedWithConfigInformation()
			throws Exception {
		saveValidAzurConfig();
		saveValidLDAPConfig();
		LDAPUserSyncConfiguration ldapUserSyncConfiguration = ldapConfigManager.getLDAPUserSyncConfiguration(true);

		//assertThat(ldapUserSyncConfiguration.getDurationBetweenExecution().getStandardDays()).isEqualTo(1l);
		assertThat(ldapUserSyncConfiguration.getGroupBaseContextList())
				.containsAll(Arrays.asList("OU=Groupes,DC=test,DC=doculibre,DC=ca"));
		assertThat(ldapUserSyncConfiguration.getUsersWithoutGroupsBaseContextList())
				.containsAll(Arrays.asList("CN=Users,DC=test,DC=doculibre,DC=ca"));
		assertThat(ldapUserSyncConfiguration.getUser()).isEqualTo(LDAPTestConfig.getUser());
		assertThat(ldapUserSyncConfiguration.getPassword()).isEqualTo(LDAPTestConfig.getPassword());
		assertThat(ldapUserSyncConfiguration.getGroupFilter().getAcceptedRegex())
				.isEqualTo(LDAPTestConfig.getGroupFiler().getAcceptedRegex());
		assertThat(ldapUserSyncConfiguration.getGroupFilter().getRejectedRegex())
				.isEqualTo(LDAPTestConfig.getGroupFiler().getRejectedRegex());
		assertThat(ldapUserSyncConfiguration.getUserFilter().getAcceptedRegex())
				.isEqualTo(LDAPTestConfig.getUserFiler().getAcceptedRegex());
		assertThat(ldapUserSyncConfiguration.getUserFilter().getRejectedRegex())
				.isEqualTo(LDAPTestConfig.getUserFiler().getRejectedRegex());
		assertThat(ldapUserSyncConfiguration.isGroupAccepted("GGS-SEC-ALF_SCEC_ext")).isFalse();
		assertThat(ldapUserSyncConfiguration.isGroupAccepted("GGS-SEC-ALF_SCEC")).isTrue();
		assertThat(ldapUserSyncConfiguration.isGroupAccepted("GGS-SEC-ALF_SCEC_ext")).isFalse();
		assertThat(ldapUserSyncConfiguration.isGroupAccepted("GGS-SEC-ALF_tous_centres_SCEC")).isFalse();
		assertThat(ldapUserSyncConfiguration.isUserAccepted("testuser")).isTrue();
		assertThat(ldapUserSyncConfiguration.isUserAccepted("testAuj")).isFalse();
		assertThat(ldapUserSyncConfiguration.isUserAccepted("admin")).isFalse();

		assertThat(ldapUserSyncConfiguration.getClientSecret()).isNull();
	}

	@Test
	@InDevelopmentTest
	public void givenAzurSavedAfterLDAPWhenGetLDAPServerConfigurationThenItIsCreatedWithConfigInformation()
			throws Exception {
		saveValidLDAPConfig();
		saveValidAzurConfig();

		assertThat(ldapConfigManager.isLDAPAuthentication()).isEqualTo(false);
		LDAPServerConfiguration ldapServerConfiguration = ldapConfigManager.getLDAPServerConfiguration();

		assertThat(ldapServerConfiguration.getClientId()).isEqualTo("zclientId");
		assertThat(ldapServerConfiguration.getTenantName()).isEqualTo("zTanentId");

		assertThat(ldapServerConfiguration.getDirectoryType()).isEqualTo(LDAPDirectoryType.AZURE_AD);
		assertThat(ldapServerConfiguration.getUrls()).isNull();
		assertThat(ldapServerConfiguration.getDomains()).isNull();

	}

	@Test
	@InDevelopmentTest
	public void givenAzurSavedAfterLDAPWhenGetLDAPSyncConfigurationThenItIsCreatedWithConfigInformation()
			throws Exception {
		saveValidLDAPConfig();
		saveValidAzurConfig();

		LDAPUserSyncConfiguration ldapUserSyncConfiguration = ldapConfigManager.getLDAPUserSyncConfiguration(true);

		assertThat(ldapUserSyncConfiguration.getClientSecret()).isEqualTo("zApplicationKey");
		assertThat(ldapUserSyncConfiguration.getClientId()).isEqualTo("synchClientId");
		assertThat(ldapUserSyncConfiguration.getGroupFilter().getAcceptedRegex()).isEqualTo(azurGroupsRegex.getAcceptedRegex());
		assertThat(ldapUserSyncConfiguration.getGroupFilter().getRejectedRegex()).isEqualTo(azurGroupsRegex.getRejectedRegex());
		assertThat(ldapUserSyncConfiguration.getUserFilter().getAcceptedRegex()).isEqualTo(azurUsersRegex.getAcceptedRegex());
		assertThat(ldapUserSyncConfiguration.getUserFilter().getRejectedRegex()).isEqualTo(azurUsersRegex.getRejectedRegex());
		assertThat(ldapUserSyncConfiguration.getSelectedCollectionsCodes()).containsExactlyElementsOf(azurCollections);

		assertThat(ldapUserSyncConfiguration.getGroupBaseContextList()).isNull();
		assertThat(ldapUserSyncConfiguration.getUsersWithoutGroupsBaseContextList()).isNull();
		assertThat(ldapUserSyncConfiguration.getUser()).isNull();
		assertThat(ldapUserSyncConfiguration.getPassword()).isNull();
	}
}
