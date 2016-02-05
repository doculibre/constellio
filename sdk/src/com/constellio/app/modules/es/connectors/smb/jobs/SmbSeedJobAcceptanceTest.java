package com.constellio.app.modules.es.connectors.smb.jobs;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.constellio.app.modules.es.connectors.smb.ConnectorSmb;
import com.constellio.app.modules.es.connectors.smb.jobmanagement.SmbDocumentOrFolderUpdater;
import com.constellio.app.modules.es.connectors.smb.jobmanagement.SmbJobFactory;
import com.constellio.app.modules.es.connectors.smb.jobmanagement.SmbJobFactoryImpl.SmbJobType;
import com.constellio.app.modules.es.connectors.smb.service.SmbRecordService;
import com.constellio.app.modules.es.connectors.smb.service.SmbService;
import com.constellio.app.modules.es.connectors.smb.testutils.SmbTestParams;
import com.constellio.app.modules.es.connectors.spi.ConnectorLogger;
import com.constellio.app.modules.es.connectors.spi.ConsoleConnectorLogger;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbInstance;
import com.constellio.app.modules.es.sdk.TestConnectorEventObserver;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.app.modules.es.services.crawler.DefaultConnectorEventObserver;
import com.constellio.sdk.tests.ConstellioTest;

public class SmbSeedJobAcceptanceTest extends ConstellioTest {
	@Mock private ConnectorSmb connector;
	private ConnectorSmbInstance connectorInstance;
	private ESSchemasRecordsServices es;
	private SmbService smbService;
	private SmbJobFactory jobFactory;
	private ConnectorLogger logger;
	private TestConnectorEventObserver eventObserver;
	private SmbDocumentOrFolderUpdater updater;

	private SmbRecordService smbRecordService;

	private String SHARE_URL = SmbTestParams.EXISTING_SHARE;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		prepareSystem(withZeCollection().withConstellioESModule()
				.withAllTestUsers());

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
		es.getConnectorManager()
				.createConnector(connectorInstance);

		logger = new ConsoleConnectorLogger();
		when(connector.getLogger()).thenReturn(logger);
		eventObserver = new TestConnectorEventObserver(es, new DefaultConnectorEventObserver(es, logger, SmbTestParams.CONNECTOR_OBSERVER));

		smbRecordService = new SmbRecordService(es, connectorInstance);
		updater = new SmbDocumentOrFolderUpdater(connectorInstance, smbRecordService);
	}

	@Test
	public void givenSeedJobWhenVerifyingJobTypeThenGetSeedJobType() {
		SmbSeedJob smbSeedJob = new SmbSeedJob(connector, SHARE_URL, smbService, jobFactory, "");
		assertThat(smbSeedJob.getType()).isEqualTo(SmbJobType.SEED_JOB);
	}

	@Test
	public void givenSeedJobWhenVerifyingToStringThenGetSeedJobName() {
		SmbSeedJob smbSeedJob = new SmbSeedJob(connector, SHARE_URL, smbService, jobFactory, "");
		assertThat(smbSeedJob.toString()).contains(SmbSeedJob.class.getSimpleName());
	}

	@After
	public void after() {
		eventObserver.close();
	}
}