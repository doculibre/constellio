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

import com.constellio.model.conf.ldap.LDAPDirectoryType;
import com.constellio.model.conf.ldap.LDAPServerConfiguration;
import com.constellio.model.conf.ldap.LDAPUserSyncConfiguration;
import com.constellio.model.conf.ldap.RegexFilter;
import com.constellio.sdk.SDKPasswords;
import org.joda.time.Duration;

import java.util.Arrays;
import java.util.List;

public class LDAPTestConfig {

	public static List<String> getUrls(){
		return Arrays.asList(new String[]{getLDAPDevServerUrl()});
	}

	public static String getLDAPDevServerUrl(){
		return "ldap://sp2010.constellio.com:389";
	}

	public static List<String>getDomains() {
		return Arrays.asList(new String[]{"test.doculibre.ca"});
	}

	public static LDAPDirectoryType getDirectoryType() {
		return LDAPDirectoryType.ACTIVE_DIRECTORY;
	}

	public static List<String> getGroupBaseContextList() {
		return Arrays.asList("OU=Groupes,DC=test,DC=doculibre,DC=ca");
	}

	public static String getPassword() {
		return SDKPasswords.testLDAPServer();
	}

	public static String getUser() {
		return "administrator";
	}

	public static List<String> getUsersWithoutGroupsBaseContextList() {
		return  Arrays.asList("CN=Users,DC=test,DC=doculibre,DC=ca");
	}

	public static RegexFilter getUserFiler() {
		return new RegexFilter(".*", "testAuj");
	}

	public static RegexFilter getGroupFiler() {
		return new RegexFilter(".*", "GGS\\-SEC\\-ALF_tous|GGS\\-SEC\\-ALF_tous_centres_SCEC|.*_ext");
	}

	public static LDAPServerConfiguration getLDAPServerConfiguration() {
		return new LDAPServerConfiguration(getUrls(), getDomains(), getDirectoryType(), true);
	}

	public static LDAPServerConfiguration getLDAPServerConfigurationInactive() {
		return new LDAPServerConfiguration(getUrls(), getDomains(), getDirectoryType(), false);
	}

	public static LDAPUserSyncConfiguration getLDAPUserSyncConfigurationWithSelectedCollections(List<String> selectedCollectionsCodes){
		return new LDAPUserSyncConfiguration(getUser(), getPassword(), getUserFiler(), getGroupFiler(), null, getGroupBaseContextList(),
				getUsersWithoutGroupsBaseContextList(), selectedCollectionsCodes);
	}

	public static LDAPUserSyncConfiguration getLDAPUserSyncConfiguration(){
		return getLDAPUserSyncConfiguration(null);
	}

	public static LDAPUserSyncConfiguration getLDAPUserSyncConfiguration(Duration duration) {
		return new LDAPUserSyncConfiguration(getUser(), getPassword(), getUserFiler(), getGroupFiler(), duration, getGroupBaseContextList(), getUsersWithoutGroupsBaseContextList());
	}
}
