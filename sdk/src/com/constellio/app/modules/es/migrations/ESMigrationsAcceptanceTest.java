package com.constellio.app.modules.es.migrations;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.constellio.app.modules.es.connectors.http.ConnectorHttp;
import com.constellio.app.modules.es.model.connectors.ConnectorType;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.model.entities.Language;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.SDKFoldersLocator;

@RunWith(Parameterized.class)
public class ESMigrationsAcceptanceTest extends ConstellioTest {

	@Test
	public void testAll()
			throws Exception {
		givenDisabledAfterTestValidations();
		whenMigratingToCurrentVersionThenValidSchemas();
		whenMigratingToCurrentVersionThenCreateConnectorTypes();
	}

	private void whenMigratingToCurrentVersionThenValidSchemas()
			throws Exception {
		assertThat(es.userSchemaType()).isNotNull();
		assertThat(es.userSchemaType().isInTransactionLog()).isTrue();

		assertThat(es.connectorType.schemaType()).isNotNull();
		assertThat(es.connectorType.schema()).isNotNull();
		assertThat(es.connectorType.code()).isNotNull();
		assertThat(es.connectorType.schemaType().isInTransactionLog()).isTrue();

		assertThat(es.connectorInstance.schemaType()).isNotNull();
		assertThat(es.connectorInstance.schema()).isNotNull();
		assertThat(es.connectorInstance.connectorType()).isNotNull();
		assertThat(es.connectorInstance.schemaType().isInTransactionLog()).isTrue();

		assertThat(es.connectorHttpDocument.schemaType()).isNotNull();
		assertThat(es.connectorHttpDocument.connector()).isNotNull();
		assertThat(es.connectorHttpDocument.schema()).isNotNull();
		assertThat(es.connectorHttpDocument.connectorType()).isNotNull();
		assertThat(es.connectorHttpDocument.url()).isNotNull();
		assertThat(es.connectorHttpDocument.schemaType().isInTransactionLog()).isFalse();

		assertThat(es.connectorSmbFolder.schemaType()).isNotNull();
		assertThat(es.connectorSmbFolder.connector()).isNotNull();
		assertThat(es.connectorSmbFolder.schema()).isNotNull();
		assertThat(es.connectorSmbFolder.connectorType()).isNotNull();
		assertThat(es.connectorSmbFolder.url()).isNotNull();
		assertThat(es.connectorSmbFolder.schemaType().isInTransactionLog()).isFalse();

		assertThat(es.connectorSmbDocument.schemaType()).isNotNull();
		assertThat(es.connectorSmbDocument.schemaType().getLabel(Language.French)).isEqualTo("Document sur un partage réseau");
		assertThat(es.connectorSmbDocument.connector()).isNotNull();
		assertThat(es.connectorSmbDocument.schema()).isNotNull();
		assertThat(es.connectorSmbDocument.connectorType()).isNotNull();
		assertThat(es.connectorSmbDocument.url()).isNotNull();
		assertThat(es.connectorSmbDocument.schemaType().isInTransactionLog()).isFalse();

		assertThat(es.connectorLdapUserDocument.schemaType().getLabel(Language.French)).isEqualTo("Utilisateur");
	}

	private void whenMigratingToCurrentVersionThenCreateConnectorTypes() {

		ConnectorType httpConnectorType = es.getHttpConnectorType();
		assertThat(httpConnectorType.getConnectorClassName()).isEqualTo(ConnectorHttp.class.getName());
		assertThat(httpConnectorType.getLinkedSchema()).isEqualTo("connectorInstance_http");
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
			prepareSystem(withZeCollection().withAllTestUsers().withConstellioESModule());
			//prepareSystem(withZeCollection().withConstellioESModule().withAllTestUsers());
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
