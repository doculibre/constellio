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
package com.constellio.app.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.joda.time.Duration;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.DemoTestRecords;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.model.conf.ldap.LDAPDirectoryType;
import com.constellio.model.conf.ldap.LDAPServerConfiguration;
import com.constellio.model.conf.ldap.LDAPUserSyncConfiguration;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.MainTest;
import com.constellio.sdk.tests.annotations.MainTestDefaultStart;
import com.constellio.sdk.tests.annotations.UiTest;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;

@UiTest
@MainTest
public class StartDemoRMConstellioWithLDAPAcceptTest extends ConstellioTest {

	RecordServices recordServices;
	ConstellioWebDriver driver;
	RMTestRecords records = new RMTestRecords(zeCollection);
	DemoTestRecords records2 = new DemoTestRecords("LaCollectionDeRida");
	RMSchemasRecordsServices schemas;

	@Before
	public void setUp()
			throws Exception {

		givenTransactionLogIsEnabled();
		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(
						records).withFoldersAndContainersOfEveryStatus().withEvents(),
				withCollection("LaCollectionDeRida").withConstellioRMModule().withAllTestUsers().withRMTest(records2)
						.withFoldersAndContainersOfEveryStatus()
		);
		inCollection("LaCollectionDeRida").setCollectionTitleTo("Collection d'entreprise");
		inCollection(zeCollection).setCollectionTitleTo("Collection de test");

		recordServices = getModelLayerFactory().newRecordServices();

		LDAPUserSyncConfiguration userSync = new LDAPUserSyncConfiguration(getUser(), getPassword(), null, null,
				new Duration(1000 * 60 * 12), getGroupBaseContextList(), getUsersWithoutGroupsBaseContextList());
		LDAPServerConfiguration serverConf = new LDAPServerConfiguration(getUrls(), getDomains(),
				LDAPDirectoryType.ACTIVE_DIRECTORY, true);
		getModelLayerFactory().getLdapConfigurationManager().saveLDAPConfiguration(serverConf, userSync);
		UserServices userServices = getModelLayerFactory().newUserServices();
		System.out.println(userServices.getAllUserCredentials().size());
		getModelLayerFactory().getLdapUserSyncManager().synchronize();

		System.out.println(userServices.getAllUserCredentials().size());
		//		UserCredential administrator = userServices.getUser("Administrator");
		//		userServices.addUserToCollection(administrator, zeCollection);
	}

	private List<String> getUrls() {
		return Arrays.asList(new String[] { "ldap://127.0.0.1:3389" });
	}

	private List<String> getDomains() {
		return Arrays.asList(new String[] { "DC=mes,DC=reseau,DC=intra" });
	}

	private List<String> getUsersWithoutGroupsBaseContextList() {
		return new ArrayList<>();
	}

	private List<String> getGroupBaseContextList() {
		String groupCN = "CN=guMFA,OU=Groupes Users (gu),OU=Groupes,DC=mes,DC=reseau,DC=intra";
		return Arrays.asList(new String[] { groupCN });
	}

	private String getPassword() {
		return "R*tsQ5yzt2Zfb#Xd";
	}

	@Test
	@MainTestDefaultStart
	public void startOnLoginPage()
			throws Exception {
		driver = newWebDriver();
		waitUntilICloseTheBrowsers();

	}

	public String getUser() {
		return "cs_065_IntelliGid_de";
	}
}
