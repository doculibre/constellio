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
package com.constellio.app.modules.rm.model;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.constants.RMRoles;
import com.constellio.app.modules.rm.constants.RMTaxonomies;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;

public class RMUserAcceptanceTest extends ConstellioTest {
	RMSchemasRecordsServices rm;
	RMTestRecords records = new RMTestRecords(zeCollection);
	RecordServices recordServices;
	UserServices userServices;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withRMTest(records)
		);

		assertThat(getModelLayerFactory().getTaxonomiesManager().getPrincipalTaxonomy(zeCollection).getCode())
				.isEqualTo(RMTaxonomies.ADMINISTRATIVE_UNITS);

		rm = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();
		userServices = getModelLayerFactory().newUserServices();

	}

	private UserCredential newJackBauerUserCredential() {
		return new UserCredential("jack.bauer", "Jack", "Bauer", "jack.bauer@constellio.com", new ArrayList<String>(),
				asList(zeCollection), UserCredentialStatus.ACTIVE);
	}

	@Test
	public void whenAddUpdatingUserWithoutRolesThenAddDefaultUserRole()
			throws Exception {

		userServices.addUpdateUserCredential(newJackBauerUserCredential());
		assertThat(jackBauerInZeCollection().getAllRoles()).containsOnly(RMRoles.USER);

		recordServices.update(jackBauerInZeCollection().setUserRoles(asList(RMRoles.MANAGER)));
		assertThat(jackBauerInZeCollection().getAllRoles()).containsOnly(RMRoles.MANAGER);

		recordServices.update(jackBauerInZeCollection().setUserRoles(new ArrayList<String>()));
		assertThat(jackBauerInZeCollection().getAllRoles()).containsOnly(RMRoles.USER);
	}

	private User jackBauerInZeCollection() {
		return userServices.getUserInCollection("jack.bauer", zeCollection);
	}
}
