package com.constellio.app.modules.es.connectors.smb.testutils;

import com.constellio.model.conf.ldap.LDAPDirectoryType;
import com.constellio.model.conf.ldap.RegexFilter;
import com.constellio.model.conf.ldap.config.LDAPServerConfiguration;
import com.constellio.model.conf.ldap.config.LDAPUserSyncConfiguration;
import com.constellio.sdk.SDKPasswords;
import org.joda.time.Duration;

import java.util.Arrays;
import java.util.List;

public class LDAPTokenTestConfig {

	public static List<String> getUrls() {
		return Arrays.asList(new String[]{getLDAPDevServerUrl()});
	}

	public static String getLDAPDevServerUrl() {
		return SDKPasswords.testLDAPServer();
	}

	public static List<String> getDomains() {
		return Arrays.asList(new String[]{"test.doculibre.ca"});
	}

	public static LDAPDirectoryType getDirectoryType() {
		return LDAPDirectoryType.ACTIVE_DIRECTORY;
	}

	public static List<String> getGroupBaseContextList() {
		return Arrays.asList("OU=Groupes,DC=test,DC=doculibre,DC=ca");
	}

	public static String getPassword() {
		return SDKPasswords.testSmbPassword();
	}

	public static String getUser() {
		return SDKPasswords.testSmbUsername();
	}

	public static List<String> getScheduleTimeList() {
		return Arrays.asList();
	}

	public static List<String> getUsersWithoutGroupsBaseContextList() {
		return Arrays.asList("CN=Users,DC=test,DC=doculibre,DC=ca");
	}

	public static List<String> getUserFilterGroupsList() {
		return Arrays.asList();
	}

	public static boolean isMembershipAutomaticDerivationActivated() {
		return true;
	}

	public static RegexFilter getUserFiler() {
		return new RegexFilter(".*", "testAuj");
	}

	public static RegexFilter getGroupFiler() {
		return new RegexFilter(".*", "GGS\\-SEC\\-ALF_tous|GGS\\-SEC\\-ALF_tous_centres_SCEC|.*_ext");
	}

	public static LDAPServerConfiguration getLDAPServerConfiguration() {
		return new LDAPServerConfiguration(getUrls(), getDomains(), getDirectoryType(), true, true);
	}

	public static LDAPServerConfiguration getLDAPServerConfigurationInactive() {
		return new LDAPServerConfiguration(getUrls(), getDomains(), getDirectoryType(), false, true);
	}

	public static LDAPUserSyncConfiguration getLDAPUserSyncConfigurationWithSelectedCollections(
			List<String> selectedCollectionsCodes) {
		return new LDAPUserSyncConfiguration(getUser(),
				getPassword(),
				getUserFiler(),
				getGroupFiler(),
				null,
				getScheduleTimeList(),
				getGroupBaseContextList(),
				getUsersWithoutGroupsBaseContextList(),
				getUserFilterGroupsList(),
				isMembershipAutomaticDerivationActivated(),
				selectedCollectionsCodes, false, false, false);
	}

	public static LDAPUserSyncConfiguration getLDAPUserSyncConfiguration() {
		return getLDAPUserSyncConfiguration(null);
	}

	public static LDAPUserSyncConfiguration getLDAPUserSyncConfiguration(Duration duration) {
		return new LDAPUserSyncConfiguration(getUser(),
				getPassword(),
				getUserFiler(),
				getGroupFiler(),
				duration,
				getScheduleTimeList(),
				getGroupBaseContextList(),
				getUsersWithoutGroupsBaseContextList(),
				getUserFilterGroupsList(),
				isMembershipAutomaticDerivationActivated(), false, false, false);
	}
}
