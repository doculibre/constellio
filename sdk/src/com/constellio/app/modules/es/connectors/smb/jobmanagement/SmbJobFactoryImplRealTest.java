package com.constellio.app.modules.es.connectors.smb.jobmanagement;

import com.constellio.app.modules.es.connectors.smb.ConnectorSmb;
import com.constellio.app.modules.es.connectors.smb.jobmanagement.SmbJobFactoryImpl.SmbJobCategory;
import com.constellio.app.modules.es.connectors.smb.service.SmbRecordService;
import com.constellio.app.modules.es.connectors.smb.service.SmbShareService;
import com.constellio.app.modules.es.connectors.smb.service.SmbShareServiceSimpleImpl;
import com.constellio.app.modules.es.connectors.smb.testutils.SmbServiceTestUtils;
import com.constellio.app.modules.es.connectors.smb.testutils.SmbTestParams;
import com.constellio.app.modules.es.connectors.smb.utils.ConnectorSmbUtils;
import com.constellio.app.modules.es.connectors.spi.ConnectorLogger;
import com.constellio.app.modules.es.connectors.spi.ConsoleConnectorLogger;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbFolder;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbInstance;
import com.constellio.app.modules.es.sdk.TestConnectorEventObserver;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.app.modules.es.services.crawler.DefaultConnectorEventObserver;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.InDevelopmentTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class SmbJobFactoryImplRealTest extends ConstellioTest {
	@Mock private ConnectorSmb connector;
	private ESSchemasRecordsServices es;
	private ConnectorSmbInstance connectorInstance;
	@Mock private SmbShareService smbService;
	private ConnectorLogger logger;
	private TestConnectorEventObserver eventObserver;
	private ConnectorSmbUtils smbUtils;
	private SmbRecordService smbRecordService;
	private SmbDocumentOrFolderUpdater updater;
	private SmbServiceTestUtils testUtils;

	private SmbJobFactory jobFactory;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		prepareSystem(withZeCollection().withConstellioESModule()
				.withAllTestUsers());

		es = new ESSchemasRecordsServices(zeCollection, getAppLayerFactory());
		smbUtils = new ConnectorSmbUtils();

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

		es.getConnectorManager()
				.createConnector(connectorInstance);

		logger = new ConsoleConnectorLogger();
		when(connector.getLogger()).thenReturn(logger);
		eventObserver = new TestConnectorEventObserver(es, new DefaultConnectorEventObserver(es, logger, SmbTestParams.CONNECTOR_OBSERVER));

		smbRecordService = new SmbRecordService(es, connectorInstance);
		updater = new SmbDocumentOrFolderUpdater(connectorInstance, smbRecordService);
	}

	@Test
	// Confirm @SlowTest
	@InDevelopmentTest
	public void givenWorstCaseOfSlowRetrievalOfModificationIndicatorsWhenUsingFactoryThenAverageFactoryLockTimeIsLessThan1Second()
			throws RecordServicesException, IOException, InterruptedException {
		testUtils = new SmbServiceTestUtils();
		smbService = new SmbShareServiceSimpleImpl(testUtils.getValidCredentials(), testUtils.getFetchValidShare(), smbUtils, logger, es);

		jobFactory = new SmbJobFactoryImpl(connector, connectorInstance, eventObserver, smbService, smbUtils, smbRecordService, updater);

		ConnectorSmbFolder folder = es.newConnectorSmbFolder(connectorInstance)
				.setUrl(SmbTestParams.EXISTING_SHARE);
		es.getRecordServices()
				.update(folder.getWrappedRecord());
		es.getRecordServices()
				.flush();

		long average = 0;
		long firstLockTime = 0;
		for (int i = 0; i < 10; i++) {
			long startTime = System.currentTimeMillis();
			jobFactory.get(SmbJobCategory.RETRIEVAL, SmbTestParams.EXISTING_SHARE, "");
			long endTime = System.currentTimeMillis();
			if (i == 0) {
				firstLockTime = endTime - startTime;
			}
			average += (endTime - startTime);
		}

		System.out.println("First lock time : " + firstLockTime + " ms");
		assertThat(firstLockTime).isLessThan(10_000);

		average = (average / 10);
		System.out.println("Average lock time : " + average + " ms");
		assertThat(average).isLessThan(1_000);
	}

	@After
	public void after() {
		eventObserver.close();
	}
}