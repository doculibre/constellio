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
package com.constellio.app.ui.acceptation.management.schemas;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.ui.tools.RecordContainerWebElement;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.UiTest;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;

@UiTest
public class ListSchemaTypeViewAcceptanceTest extends ConstellioTest {
	MetadataSchemasManager schemasManager;
	RMTestRecords records;
	ConstellioWebDriver driver;
	SchemaTypePage page;
	List<MetadataSchemaType> types;

	@Before
	public void setUp()
			throws Exception {
		givenCollection(zeCollection).withConstellioRMModule().withAllTestUsers();
		records = new RMTestRecords(zeCollection).setup(getModelLayerFactory());
		schemasManager = getModelLayerFactory().getMetadataSchemasManager();
		driver = newWebDriver(loggedAsUserInCollection(admin, zeCollection));
		page = new SchemaTypePage(driver).navigateToPage();
		types = schemasManager.getSchemaTypes(zeCollection).getSchemaTypes();
	}

	@Test
	public void givenBaseConfigurationThenDisplayCorrectInformation() {
		RecordContainerWebElement typeTable = page.getTypeTable();
		assertThat(typeTable.countRows()).isEqualTo(10);

		assertThat(typeTable.hasRowWithValueInColumn("Rubrique du plans", 0)).isTrue();
		assertThat(typeTable.hasRowWithValueInColumn("Contenant", 0)).isTrue();
		assertThat(typeTable.hasRowWithValueInColumn("Types de contenants", 0)).isTrue();
		assertThat(typeTable.hasRowWithValueInColumn("Types de documents", 0)).isTrue();
		assertThat(typeTable.hasRowWithValueInColumn("Types de dossiers", 0)).isTrue();
		assertThat(typeTable.hasRowWithValueInColumn("Types de supports", 0)).isTrue();
		assertThat(typeTable.hasRowWithValueInColumn("Types d'emplacement", 0)).isTrue();
		assertThat(typeTable.hasRowWithValueInColumn("Document", 0)).isTrue();
		assertThat(typeTable.hasRowWithValueInColumn("Dossier", 0)).isTrue();
		assertThat(typeTable.hasRowWithValueInColumn("Emplacement", 0)).isTrue();

	}
}
