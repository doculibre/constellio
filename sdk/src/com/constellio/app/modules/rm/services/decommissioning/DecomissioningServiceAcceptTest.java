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
package com.constellio.app.modules.rm.services.decommissioning;

import static org.assertj.core.api.Assertions.assertThat;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.sdk.tests.ConstellioTest;

public class DecomissioningServiceAcceptTest extends ConstellioTest {

	DecommissioningService service;
	RMSchemasRecordsServices rm;
	RMTestRecords records = new RMTestRecords(zeCollection);

	@Before
	public void setUp()
			throws Exception {

		givenCollection(zeCollection).withConstellioRMModule().withAllTestUsers();
		records.setup(getModelLayerFactory()).withFoldersAndContainersOfEveryStatus();

		rm = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());
		service = new DecommissioningService(zeCollection, getModelLayerFactory());
	}

	@Test
	public void whenHasFolderToDepositThenOk()
			throws Exception {

		assertThat(service.hasFolderToDeposit(records.getContainerBac01())).isTrue();
		assertThat(service.hasFolderToDeposit(records.getContainerBac13())).isFalse();
	}

	@Test
	public void whenGetMediumTypesOfContainerThenOk()
			throws Exception {

		assertThat(service.getMediumTypesOf(records.getContainerBac11())).containsOnly(rm.PA(), rm.DM());
	}

	@Test
	public void whenGetDispositionYearThenOk()
			throws Exception {

		assertThat(service.getDispositionDate(records.getContainerBac10())).isEqualTo(new LocalDate(2007, 10, 31));
	}

	@Test
	public void whenGetUniformRuleForContainer13ThenReturnIt()
			throws Exception {

		assertThat(service.getUniformRuleOf(records.getContainerBac13())).isEqualTo("ruleId_2");
	}

	@Test
	public void whenGetUniformRuleForContainer10ThenReturnNull()
			throws Exception {

		assertThat(service.getUniformRuleOf(records.getContainerBac10())).isNull();
	}
}
