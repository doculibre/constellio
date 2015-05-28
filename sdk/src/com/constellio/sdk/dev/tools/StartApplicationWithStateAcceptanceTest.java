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
package com.constellio.sdk.dev.tools;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import org.junit.Test;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.reindexing.ReindexationMode;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.InDevelopmentTest;
import com.constellio.sdk.tests.annotations.UiTest;

@UiTest
public class StartApplicationWithStateAcceptanceTest extends ConstellioTest {

	@InDevelopmentTest
	@Test
	public void testName()
			throws Exception {
		givenTransactionLogIsEnabled();
		getCurrentTestSession().getFactoriesTestFeatures().givenSystemInState(
				new File(
						"/Users/francisbaril/À traiter/Téléchargements du 15-05-26/constellio_doculibre/Doculibre26_05_2015.zip"))
				.withPasswordsReset();

		RecordServices recordServices = getModelLayerFactory().newRecordServices();

		//		newWebDriver(loggedAsUserInCollection("fbaril", "myCollection"));
		//		waitUntilICloseTheBrowsers();

		Record record = recordServices.getDocumentById("00000007028");
		assertThat(record.getList(Schemas.PATH_PARTS)).isNotEmpty();

		getModelLayerFactory().newReindexingServices().reindexCollections(ReindexationMode.RECALCULATE_AND_REWRITE);

		record = recordServices.getDocumentById("00000007028");
		assertThat(record.getList(Schemas.PATH_PARTS)).isNotEmpty();

		newWebDriver(loggedAsUserInCollection("fbaril", "myCollection"));
		waitUntilICloseTheBrowsers();

	}
}
