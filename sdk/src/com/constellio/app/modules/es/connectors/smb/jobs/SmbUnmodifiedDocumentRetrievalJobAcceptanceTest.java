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

import org.joda.time.LocalDateTime;
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
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbDocument;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbInstance;
import com.constellio.app.modules.es.sdk.TestConnectorEventObserver;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.app.modules.es.services.crawler.DefaultConnectorEventObserver;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.sdk.tests.ConstellioTest;

public class SmbUnmodifiedDocumentRetrievalJobAcceptanceTest extends ConstellioTest {

	@Mock private ConnectorSmb connector;
	private ESSchemasRecordsServices es;
	private ConnectorSmbInstance connectorInstance;
	private SmbService smbService;
	private ConnectorLogger logger;
	private TestConnectorEventObserver eventObserver;
	private SmbRecordService smbRecordService;
	private SmbDocumentOrFolderUpdater updater;
	private ConnectorSmbUtils smbUtils;
	private SmbJobFactory jobFactory;

	private String SHARE_URL = SmbTestParams.EXISTING_SHARE;
	private String FILE_URL = SHARE_URL + SmbTestParams.EXISTING_FILE;

	private SmbUnmodifiedDocumentRetrievalJob retrievalJob;

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
	public void givenFullDTOWhenExecutingThenSendFullDocumentToObserver()
			throws RecordServicesException {
		LocalDateTime time1 = new LocalDateTime();
		givenTimeIs(time1);
		SmbFileDTO smbFileDTO = new SmbFileDTO();
		smbFileDTO.setUrl(FILE_URL);
		smbFileDTO.setLastFetchAttempt(time1);
		smbFileDTO.setStatus(SmbFileDTOStatus.FULL_DTO);
		smbService = new FakeSmbService(smbFileDTO);

		ConnectorSmbDocument document = es.newConnectorSmbDocument(connectorInstance);
		document.setUrl(FILE_URL);
		es.getRecordServices()
				.update(document.getWrappedRecord());

		retrievalJob = new SmbUnmodifiedDocumentRetrievalJob(connector, FILE_URL, smbService, eventObserver, smbRecordService, updater, "", jobFactory);
		retrievalJob.execute(connector);

		assertThatEventsObservedBy(eventObserver).comparingRecordsUsing(es.connectorSmbDocument.url())
				.containsOnly(modifyEvent(es.newConnectorSmbDocument(connectorInstance)
						.setUrl(FILE_URL)));

		verify(updater, times(1)).updateUnmodifiedDocument(any(SmbFileDTO.class), any(ConnectorDocument.class), anyString());
		verify(smbRecordService, times(1)).getRecordIdForFolder(anyString());
		verify(smbRecordService, times(1)).updateResumeUrl(FILE_URL);
	}

	@Test
	public void givenFailedDTOWhenExecutingThenSendFailedDocumentToObserver()
			throws RecordServicesException {
		LocalDateTime time1 = new LocalDateTime();
		givenTimeIs(time1);
		SmbFileDTO smbFileDTO = new SmbFileDTO();
		smbFileDTO.setUrl(FILE_URL);
		smbFileDTO.setLastFetchAttempt(time1);
		smbFileDTO.setStatus(SmbFileDTOStatus.FAILED_DTO);
		smbService = new FakeSmbService(smbFileDTO);

		ConnectorSmbDocument document = es.newConnectorSmbDocument(connectorInstance);
		document.setUrl(FILE_URL);
		es.getRecordServices()
				.update(document.getWrappedRecord());

		retrievalJob = new SmbUnmodifiedDocumentRetrievalJob(connector, FILE_URL, smbService, eventObserver, smbRecordService, updater, "", jobFactory);
		retrievalJob.execute(connector);

		assertThatEventsObservedBy(eventObserver).comparingRecordsUsing(es.connectorSmbDocument.url())
				.containsOnly(modifyEvent(es.newConnectorSmbDocument(connectorInstance)
						.setUrl(FILE_URL)));

		verify(updater, times(1)).updateFailedDocumentOrFolder(any(SmbFileDTO.class), any(ConnectorDocument.class), anyString());
		verify(smbRecordService, times(1)).getRecordIdForFolder(anyString());
	}

	@Test
	public void givenDeleteDTOWhenExecutingThenQueueDeleteJob()
			throws RecordServicesException {
		SmbFileDTO smbFileDTO = new SmbFileDTO();
		smbFileDTO.setUrl(SHARE_URL);
		smbFileDTO.setStatus(SmbFileDTOStatus.DELETE_DTO);
		smbService = new FakeSmbService(smbFileDTO);

		retrievalJob = new SmbUnmodifiedDocumentRetrievalJob(connector, SHARE_URL, smbService, eventObserver, smbRecordService, updater, "", jobFactory);
		retrievalJob.execute(connector);

		verify(connector, times(1)).queueJob(any(SmbDeleteJob.class));
	}

	@After
	public void after() {
		eventObserver.close();
	}
}