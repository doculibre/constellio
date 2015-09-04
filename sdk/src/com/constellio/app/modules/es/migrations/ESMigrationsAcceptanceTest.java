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
package com.constellio.app.modules.es.migrations;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.constellio.app.modules.es.connectors.http.ConnectorHttp;
import com.constellio.app.modules.es.model.connectors.ConnectorType;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.SDKFoldersLocator;

@RunWith(Parameterized.class)
public class ESMigrationsAcceptanceTest extends ConstellioTest {

	@Test
	public void testAll()
			throws Exception {
		whenMigratingToCurrentVersionThenValidSchemas();
		whenMigratingToCurrentVersionThenCreateConnectorTypes();
	}

	private void whenMigratingToCurrentVersionThenValidSchemas()
			throws Exception {
		assertThat(es.connectorType.schemaType()).isNotNull();
		assertThat(es.connectorType.schema()).isNotNull();
		assertThat(es.connectorType.code()).isNotNull();

		assertThat(es.connectorInstance.schemaType()).isNotNull();
		assertThat(es.connectorInstance.schema()).isNotNull();
		assertThat(es.connectorInstance.connectorType()).isNotNull();

		assertThat(es.connectorHttpDocument.schemaType()).isNotNull();
		assertThat(es.connectorHttpDocument.connector()).isNotNull();
		assertThat(es.connectorHttpDocument.schema()).isNotNull();
		assertThat(es.connectorHttpDocument.connectorType()).isNotNull();
		assertThat(es.connectorHttpDocument.url()).isNotNull();
	}

	private void whenMigratingToCurrentVersionThenCreateConnectorTypes() {

		ConnectorType httpConnectorType = es.getHttpConnectorType();
		assertThat(httpConnectorType.getConnectorClassName()).isEqualTo(ConnectorHttp.class.getName());
		assertThat(httpConnectorType.getLinkedSchema()).isEqualTo("connectorInstance_http");
		assertThat(httpConnectorType.getDefaultAvailableProperties())
				.containsEntry("mimetype", String.class.getName())
				.containsEntry("charset", String.class.getName())
				.containsEntry("language", String.class.getName())
				.containsEntry("lastModification", LocalDate.class.getName());
	}

	//--------------------------------------------------------------

	String testCase;

	ESSchemasRecordsServices es;

	public ESMigrationsAcceptanceTest(String testCase) {
		this.testCase = testCase;
	}

	@Parameterized.Parameters(name = "{0}")
	public static Collection<Object[]> testCases() {
		List<Object[]> states = new ArrayList<>();
		states.add(new Object[] { "givenNewInstallation" });

		for (String state : new SDKFoldersLocator().getInitialStatesFolder().list()) {
			if (state.endsWith(".zip") && state.contains("_es_")) {
				states.add(new Object[] { state.replace(".zip", "") });
			}
		}

		return states;

	}

	@Before
	public void setUp()
			throws Exception {
		if ("givenNewInstallation".equals(testCase)) {
			givenTransactionLogIsEnabled();
			givenCollection(zeCollection).withAllTestUsers().withConstellioESModule();
			//			prepareSystem(
			//					withZeCollection().withAllTestUsers().withConstellioESModule()
			//			);

		} else {

			givenTransactionLogIsEnabled();
			File statesFolder = new SDKFoldersLocator().getInitialStatesFolder();
			File state = new File(statesFolder, testCase + ".zip");

			getCurrentTestSession().getFactoriesTestFeatures().givenSystemInState(state);

		}

		es = new ESSchemasRecordsServices(zeCollection, getAppLayerFactory());
	}
}
