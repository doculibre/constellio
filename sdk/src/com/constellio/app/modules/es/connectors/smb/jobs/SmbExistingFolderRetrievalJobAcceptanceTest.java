package com.constellio.app.modules.es.connectors.smb.jobs;

import static com.constellio.app.modules.es.sdk.ESTestUtils.assertThatEventsObservedBy;
import static com.constellio.app.modules.es.sdk.TestConnectorEvent.modifyEvent;
import static java.util.Arrays.asList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.constellio.app.modules.es.connectors.smb.ConnectorSmb;
import com.constellio.app.modules.es.connectors.smb.jobmanagement.SmbDocumentOrFolderUpdater;
import com.constellio.app.modules.es.connectors.smb.jobmanagement.SmbJobFactory;
import com.constellio.app.modules.es.connectors.smb.jobmanagement.SmbJobFactoryImpl;
import com.constellio.app.modules.es.connectors.smb.service.SmbFileDTO;
import com.constellio.app.modules.es.connectors.smb.service.SmbFileDTO.SmbFileDTOStatus;
import com.constellio.app.modules.es.connectors.smb.service.SmbRecordService;
import com.constellio.app.modules.es.connectors.smb.service.SmbService;
import com.constellio.app.modules.es.connectors.smb.testutils.FakeSmbService;
import com.constellio.app.modules.es.connectors.smb.testutils.SmbTestParams;
import com.constellio.app.modules.es.connectors.smb.utils.ConnectorSmbUtils;
import com.constellio.app.modules.es.connectors.spi.ConnectorLogger;
import com.constellio.app.modules.es.connectors.spi.ConsoleConnectorLogger;
import com.constellio.app.modules.es.model.connectors.ConnectorDocument;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbFolder;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbInstance;
import com.constellio.app.modules.es.sdk.TestConnectorEventObserver;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.app.modules.es.services.crawler.DefaultConnectorEventObserver;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.sdk.tests.ConstellioTest;

public class SmbExistingFolderRetrievalJobAcceptanceTest extends ConstellioTest {
	@Mock private ConnectorSmb connector;
	private ESSchemasRecordsServices es;
	private ConnectorSmbInstance connectorInstance;
	private SmbService smbService;
	private ConnectorLogger logger;
	private TestConnectorEventObserver eventObserver;
	private ConnectorSmbFolder connectorSmbFolder;
	private SmbRecordService smbRecordService;
	private SmbDocumentOrFolderUpdater updater;
	private ConnectorSmbUtils smbUtils;
	private SmbJobFactory jobFactory;

	private String SHARE_URL = SmbTestParams.EXISTING_SHARE;

	private SmbExistingFolderRetrievalJob retrievalJob;

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
		smbRecordService = Mockito.spy(new SmbRecordService(es, connectorInstance));
		updater = Mockito.spy(new SmbDocumentOrFolderUpdater(connectorInstance, smbRecordService));
		smbUtils = new ConnectorSmbUtils();
		jobFactory = new SmbJobFactoryImpl(connector, connectorInstance, eventObserver, smbService, smbUtils, smbRecordService, updater);
	}

	@Test
	public void givenFullDTOWhenExecutingThenSendNothingToObserver()
			throws RecordServicesException {
		SmbFileDTO smbFileDTO = new SmbFileDTO();
		smbFileDTO.setUrl(SHARE_URL);
		smbFileDTO.setIsDirectory(true);
		smbFileDTO.setStatus(SmbFileDTOStatus.FULL_DTO);
		smbService = new FakeSmbService(smbFileDTO);

		connectorSmbFolder = es.newConnectorSmbFolder(connectorInstance)
				.setUrl(SHARE_URL)
				.setConnector(connectorInstance)
				.setTraversalCode(SmbTestParams.TRAVERSAL_CODE)
				.setFetched(false);

		getModelLayerFactory().newRecordServices()
				.add(connectorSmbFolder);

		retrievalJob = new SmbExistingFolderRetrievalJob(connector, SHARE_URL, connectorSmbFolder, smbService, eventObserver, smbRecordService, updater, "", jobFactory);
		retrievalJob.execute(connector);

		assertThatEventsObservedBy(eventObserver).comparingRecordsUsing(es.connectorSmbFolder.url())
				.isEmpty();

		verify(smbRecordService, times(1)).updateResumeUrl(SHARE_URL);
	}

	@Test
	public void givenFailedDTOWhenExecutingThenSendNothingToObserver()
			throws RecordServicesException {
		SmbFileDTO smbFileDTO = new SmbFileDTO();
		smbFileDTO.setUrl(SHARE_URL);
		smbFileDTO.setIsDirectory(true);
		smbFileDTO.setStatus(SmbFileDTOStatus.FAILED_DTO);
		smbService = new FakeSmbService(smbFileDTO);

		connectorSmbFolder = es.newConnectorSmbFolder(connectorInstance)
				.setUrl(SHARE_URL)
				.setConnector(connectorInstance)
				.setTraversalCode(SmbTestParams.TRAVERSAL_CODE)
				.setFetched(false);

		getModelLayerFactory().newRecordServices()
				.add(connectorSmbFolder);

		retrievalJob = new SmbExistingFolderRetrievalJob(connector, SHARE_URL, connectorSmbFolder, smbService, eventObserver, smbRecordService, updater, "", jobFactory);
		retrievalJob.execute(connector);

		assertThatEventsObservedBy(eventObserver).comparingRecordsUsing(es.connectorSmbFolder.url())
				.isEmpty();
	}

	@Test
	public void givenDeleteDTOWhenExecutingThenQueueDeleteJob()
			throws RecordServicesException {
		SmbFileDTO smbFileDTO = new SmbFileDTO();
		smbFileDTO.setUrl(SHARE_URL);
		smbFileDTO.setStatus(SmbFileDTOStatus.DELETE_DTO);
		smbService = new FakeSmbService(smbFileDTO);

		retrievalJob = new SmbExistingFolderRetrievalJob(connector, SHARE_URL, es.newConnectorSmbFolder(connectorInstance), smbService, eventObserver, smbRecordService, updater, "", jobFactory);
		retrievalJob.execute(connector);

		verify(connector, times(1)).queueJob(any(SmbDeleteJob.class));
	}

	@After
	public void after() {
		eventObserver.close();
	}
}