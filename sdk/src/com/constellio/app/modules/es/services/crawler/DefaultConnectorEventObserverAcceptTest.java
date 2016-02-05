package com.constellio.app.modules.es.services.crawler;

import static java.util.Arrays.asList;

import org.junit.Before;

import com.constellio.app.modules.es.connectors.spi.ConnectorLogger;
import com.constellio.app.modules.es.connectors.spi.ConsoleConnectorLogger;
import com.constellio.app.modules.es.model.connectors.ConnectorInstance;
import com.constellio.app.modules.es.services.ConnectorManager;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.taxonomies.TaxonomiesSearchOptions;
import com.constellio.sdk.SDKPasswords;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;

public class DefaultConnectorEventObserverAcceptTest extends ConstellioTest {

	private Users users = new Users();

	private ConnectorLogger logger = new ConsoleConnectorLogger();
	private ConnectorInstance<?> connector1, connector2;
	private ConnectorManager connectorManager;
	private RecordServices recordServices;
	private ESSchemasRecordsServices es;
	private DefaultConnectorEventObserver observer;

	private String share, domain, username, password;

	private TaxonomiesSearchOptions defaultOptions = new TaxonomiesSearchOptions();

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(withZeCollection().withConstellioESModule().withAllTest(users));

		es = new ESSchemasRecordsServices(zeCollection, getAppLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();
		connectorManager = es.getConnectorManager();

		share = SDKPasswords.testSmbShare();
		domain = SDKPasswords.testSmbDomain();
		username = SDKPasswords.testSmbUsername();
		password = SDKPasswords.testSmbPassword();

		recordServices.update(users.bobIn(zeCollection).setManualTokens("rtoken1"));
		recordServices.update(users.chuckNorrisIn(zeCollection).setManualTokens("rtoken1", "rtoken2"));

		connector1 = connectorManager.createConnector(es.newConnectorSmbInstance().setCode("connector1").setEnabled(false)
				.setSeeds(asList(share)).setUsername(username).setPassword(password).setDomain(domain));

		connector2 = connectorManager.createConnector(es.newConnectorSmbInstance().setCode("connector2").setEnabled(false)
				.setSeeds(asList(share)).setUsername(username).setPassword(password).setDomain(domain));

		observer = new DefaultConnectorEventObserver(es, logger, "observer");
	}

}
