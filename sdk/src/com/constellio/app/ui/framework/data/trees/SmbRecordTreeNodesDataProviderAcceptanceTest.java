package com.constellio.app.ui.framework.data.trees;

import com.constellio.app.modules.es.connectors.smb.testutils.SmbTestParams;
import com.constellio.app.modules.es.model.connectors.ConnectorInstance;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbDocument;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbFolder;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.services.records.RecordServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import org.junit.Before;
import org.junit.Test;

import static java.util.Arrays.asList;

public class SmbRecordTreeNodesDataProviderAcceptanceTest extends ConstellioTest {
	RMTestRecords records = new RMTestRecords(zeCollection);
	private ESSchemasRecordsServices es;
	private ConnectorInstance connectorInstance;
	RecordServices recordService;
	SessionContext sessionContext;

	@Before
	public void setup() throws Exception {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withRMTest(records).withConstellioESModule()
		);
		recordService = getAppLayerFactory().getModelLayerFactory().newRecordServices();
		es = new ESSchemasRecordsServices(zeCollection, getAppLayerFactory());

		connectorInstance = es.newConnectorSmbInstance()
				.setDomain(SmbTestParams.DOMAIN)
				.setUsername(SmbTestParams.USERNAME)
				.setPassword(SmbTestParams.PASSWORD)
				.setSeeds(asList(SmbTestParams.EXISTING_SHARE))
				.setCode(SmbTestParams.INSTANCE_CODE)
				.setTraversalCode(SmbTestParams.TRAVERSAL_CODE)
				.setInclusions(asList(SmbTestParams.EXISTING_SHARE))
				.setExclusions(asList(""))
				.setTitle(SmbTestParams.CONNECTOR_TITLE);

		recordService.update(connectorInstance);
		recordService.flush();

		es.getConnectorManager()
				.createConnector(connectorInstance);

		ConnectorSmbFolder connectorSmbFolder1 = es.newConnectorSmbFolder(connectorInstance).setUrl("smb://127.0.0.1/share/folerTest1");
		ConnectorSmbFolder connectorSmbFolder2 = es.newConnectorSmbFolder(connectorInstance).setUrl("smb://127.0.0.1/share/folderTest2");
		ConnectorSmbDocument connectorSmbDocument1 = es.newConnectorSmbDocument(connectorInstance).setUrl("smb://127.0.0.1/share/fichierTest1");
		ConnectorSmbDocument connectorSmbDocument2 = es.newConnectorSmbDocument(connectorInstance).setUrl("smb://127.0.0.1/share/folerTest1/fichierTest1").setParentUrl("smb://127.0.0.1/share/folerTest1");

		recordService.add(connectorSmbFolder1);
		recordService.add(connectorSmbFolder2);
		recordService.add(connectorSmbDocument1);
		recordService.add(connectorSmbDocument2);

		sessionContext = FakeSessionContext.adminInCollection(zeCollection);
	}

	@Test
	public void getChildrenNodesReturnRowsThenOk() {

	}

	@Test
	public void getRootNodesReturnRowsThenOk() {

	}
}
