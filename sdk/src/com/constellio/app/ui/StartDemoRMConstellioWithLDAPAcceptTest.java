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

import org.joda.time.Duration;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.DemoTestRecords;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.model.conf.LDAPTestConfig;
import com.constellio.model.conf.ldap.LDAPServerConfiguration;
import com.constellio.model.conf.ldap.LDAPUserSyncConfiguration;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.InDevelopmentTest;
import com.constellio.sdk.tests.annotations.UiTest;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;

@UiTest
@InDevelopmentTest
public class StartDemoRMConstellioWithLDAPAcceptTest extends ConstellioTest {

	RecordServices recordServices;
	ConstellioWebDriver driver;
	RMTestRecords records;
	RMSchemasRecordsServices schemas;

	@Before
	public void setUp()
			throws Exception {
		givenCollectionWithTitle(zeCollection, "Collection de test").withConstellioRMModule().withAllTestUsers();
		givenCollectionWithTitle("LaCollectionDeRida", "Collection d'entreprise").withConstellioRMModule().withAllTestUsers();

		recordServices = getModelLayerFactory().newRecordServices();

		records = new RMTestRecords(zeCollection).setup(getModelLayerFactory()).withFoldersAndContainersOfEveryStatus()
				.withEvents();
		new DemoTestRecords("LaCollectionDeRida").setup(getModelLayerFactory()).withFoldersAndContainersOfEveryStatus();

		LDAPUserSyncConfiguration userSync = LDAPTestConfig.getLDAPUserSyncConfiguration(new Duration(1000*60*12));
		LDAPServerConfiguration serverConf = LDAPTestConfig.getLDAPServerConfiguration();
		getModelLayerFactory().getLdapConfigurationManager().saveLDAPConfiguration(serverConf, userSync);
		getModelLayerFactory().getLdapUserSyncManager().synchronize();

		UserServices userServices = getModelLayerFactory().newUserServices();
		UserCredential administrator = userServices.getUser("Administrator");
		userServices.addUserToCollection(administrator, zeCollection);
	}

	@Test
	public void startOnLoginPage()
			throws Exception {
		driver = newWebDriver();
		waitUntilICloseTheBrowsers();

	}

}
