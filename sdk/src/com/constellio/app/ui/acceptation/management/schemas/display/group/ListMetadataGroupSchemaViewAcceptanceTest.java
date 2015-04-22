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
package com.constellio.app.ui.acceptation.management.schemas.display.group;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.ui.tools.RecordContainerWebElement;
import com.constellio.app.ui.tools.RecordContainerWebElementRuntimeException.RecordContainerWebElementRuntimeException_NoSuchRowWithValueInColumn;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.UiTest;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;

@UiTest
public class ListMetadataGroupSchemaViewAcceptanceTest extends ConstellioTest {
	MetadataSchemasManager schemasManager;
	RMTestRecords records;
	ConstellioWebDriver driver;
	MetadataGroupSchemaPage page;
	MetadataSchemaType type;

	@Before
	public void setUp()
			throws Exception {
		givenCollection(zeCollection).withConstellioRMModule().withAllTestUsers();
		records = new RMTestRecords(zeCollection).setup(getModelLayerFactory());
		schemasManager = getModelLayerFactory().getMetadataSchemasManager();
		driver = newWebDriver(loggedAsUserInCollection(gandalf, zeCollection));
		type = schemasManager.getSchemaTypes(zeCollection).getSchemaTypes().get(0);
		page = new MetadataGroupSchemaPage(driver).navigateToPage(type.getCode());
	}

	@Test
	public void givenBaseConfigurationThenDisplayNoGroup()
			throws Exception {
		RecordContainerWebElement typeTable = page.getGroupTable();
		assertThat(typeTable.countRows()).isEqualTo(1);

		assertThat(page.getGroupWithName("Métadonnées")).isNotNull();
	}

	@Test
	public void givenBaseConfigurationThenAddingGroupThenDisplayGroup()
			throws Exception {
		page.addGroupWithName("zeGroup");
		page.addGroupWithName("zeOtherGroup");

		RecordContainerWebElement typeTable = page.getGroupTable();
		assertThat(typeTable.countRows()).isEqualTo(3);

		assertThat(page.getGroupWithName("Métadonnées")).isNotNull();
		assertThat(page.getGroupWithName("zeGroup")).isNotNull();
		assertThat(page.getGroupWithName("zeOtherGroup")).isNotNull();
	}

	@Test
	public void givenBaseConfigurationThenAddingTwoGroupWithTheSameNameThenOnlyOneAdded()
			throws Exception {
		page.addGroupWithName("zeGroup");
		page.addGroupWithName("zeGroup");

		RecordContainerWebElement typeTable = page.getGroupTable();
		assertThat(typeTable.countRows()).isEqualTo(2);

		assertThat(page.getGroupWithName("Métadonnées")).isNotNull();
		assertThat(page.getGroupWithName("zeGroup")).isNotNull();
	}

	@Test
	public void givenBaseConfigurationWhenRemoveLastGroupThenNotAble()
			throws Exception {
		RecordContainerWebElement typeTable = page.getGroupTable();

		page.removeGroupWithName("Métadonnées");
		assertThat(typeTable.countRows()).isEqualTo(1);
		assertThat(page.getGroupWithName("Métadonnées")).isNotNull();
	}

	@Test(expected = RecordContainerWebElementRuntimeException_NoSuchRowWithValueInColumn.class)
	public void givenBaseConfigurationWithGroupThenDeleteThenDisplayGoodGroup()
			throws Exception {
		page.addGroupWithName("zeGroup");

		RecordContainerWebElement typeTable = page.getGroupTable();
		assertThat(typeTable.countRows()).isEqualTo(2);

		page.removeGroupWithName("zeGroup");

		//exception thrown here if it's correctly removed
		page.getGroupWithName("zeGroup");
	}

	@Test
	public void givenBaseConfigurationWhenAddingNoNameGroupThenNotAdded()
			throws Exception {
		page.addGroupWithName("");

		RecordContainerWebElement typeTable = page.getGroupTable();
		assertThat(typeTable.countRows()).isEqualTo(1);

		assertThat(page.getGroupWithName("Métadonnées")).isNotNull();
	}
}
