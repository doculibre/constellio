package com.constellio.app.modules.es.connectors.smb.jobs;

import com.constellio.app.modules.es.connectors.smb.ConnectorSmb;
import com.constellio.app.modules.es.connectors.smb.cache.SmbConnectorContext;
import com.constellio.app.modules.es.connectors.smb.cache.SmbConnectorContextServices;
import com.constellio.app.modules.es.connectors.smb.jobmanagement.SmbDocumentOrFolderUpdater;
import com.constellio.app.modules.es.connectors.smb.jobmanagement.SmbJobFactory;
import com.constellio.app.modules.es.connectors.smb.jobmanagement.SmbJobFactoryImpl;
import com.constellio.app.modules.es.connectors.smb.service.SmbFileDTO;
import com.constellio.app.modules.es.connectors.smb.service.SmbFileDTO.SmbFileDTOStatus;
import com.constellio.app.modules.es.connectors.smb.service.SmbRecordService;
import com.constellio.app.modules.es.connectors.smb.service.SmbShareService;
import com.constellio.app.modules.es.connectors.smb.testutils.FakeSmbService;
import com.constellio.app.modules.es.connectors.smb.testutils.SmbTestParams;
import com.constellio.app.modules.es.connectors.smb.utils.ConnectorSmbUtils;
import com.constellio.app.modules.es.connectors.spi.ConnectorLogger;
import com.constellio.app.modules.es.connectors.spi.ConsoleConnectorLogger;
import com.constellio.app.modules.es.model.connectors.ConnectorDocument;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbInstance;
import com.constellio.app.modules.es.sdk.TestConnectorEventObserver;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.app.modules.es.services.crawler.DefaultConnectorEventObserver;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static com.constellio.app.modules.es.sdk.ESTestUtils.assertThatEventsObservedBy;
import static com.constellio.app.modules.es.sdk.TestConnectorEvent.addEvent;
import static java.util.Arrays.asList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class SmbNewDocumentRetrievalJobAcceptanceTest extends ConstellioTest {
	@Mock private ConnectorSmb connector;
	private ESSchemasRecordsServices es;
	private ConnectorSmbInstance connectorInstance;
	private SmbShareService smbService;
	private ConnectorLogger logger;
	private TestConnectorEventObserver eventObserver;
	private SmbRecordService smbRecordService;
	private SmbDocumentOrFolderUpdater updater;
	private ConnectorSmbUtils smbUtils;
	private SmbJobFactory jobFactory;
	private SmbNewDocumentRetrievalJob retrievalJob;
	@Mock private SmbConnectorContext context;

	private String SHARE_URL = SmbTestParams.EXISTING_SHARE;
	private String FILE_URL = SHARE_URL + SmbTestParams.EXISTING_FILE;

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

		SmbConnectorContextServices contextServices = new SmbConnectorContextServices(es);
		context = contextServices.createContext(connectorInstance.getId());
		when(connector.getContext()).thenReturn(context);
		jobFactory = new SmbJobFactoryImpl(connector, connectorInstance, eventObserver, smbService, smbUtils, smbRecordService, updater);


	}

	@Test
	public void givenFullDTOWhenExecutingThenSendFullDocumentToObserver() {
		SmbFileDTO smbFileDTO = new SmbFileDTO();
		smbFileDTO.setUrl(FILE_URL);
		smbFileDTO.setIsFile(true);
		smbFileDTO.setStatus(SmbFileDTOStatus.FULL_DTO);
		smbService = new FakeSmbService(smbFileDTO);

		JobParams jobParams = new JobParams(connector, eventObserver,smbUtils, connectorInstance, smbService,smbRecordService, updater, jobFactory, FILE_URL, null);
		retrievalJob = new SmbNewDocumentRetrievalJob(jobParams);
		retrievalJob.execute(connector);

		assertThatEventsObservedBy(eventObserver).comparingRecordsUsing(es.connectorSmbDocument.url())
				.containsOnly(addEvent(es.newConnectorSmbDocument(connectorInstance)
						.setUrl(FILE_URL)));

		verify(updater, times(1)).updateDocumentOrFolder(any(SmbFileDTO.class), any(ConnectorDocument.class), anyString());
		verify(smbRecordService, times(1)).getFolder(anyString());
		verify(smbRecordService, times(1)).updateResumeUrl(FILE_URL);
	}

	@Test
	public void givenFailedDTOWhenExecutingThenSendFailedDocumentToObserver() {
		SmbFileDTO smbFileDTO = new SmbFileDTO();
		smbFileDTO.setUrl(FILE_URL);
		smbFileDTO.setIsFile(true);
		smbFileDTO.setStatus(SmbFileDTOStatus.FAILED_DTO);
		smbService = new FakeSmbService(smbFileDTO);

		JobParams jobParams = new JobParams(connector, eventObserver,smbUtils, connectorInstance, smbService,smbRecordService, updater, jobFactory, FILE_URL, null);
		retrievalJob = new SmbNewDocumentRetrievalJob(jobParams);
		retrievalJob.execute(connector);

		assertThatEventsObservedBy(eventObserver).comparingRecordsUsing(es.connectorSmbDocument.url())
				.containsOnly(addEvent(es.newConnectorSmbDocument(connectorInstance)
						.setUrl(FILE_URL)));

		verify(updater, times(1)).updateFailedDocumentOrFolder(any(SmbFileDTO.class), any(ConnectorDocument.class), anyString());
		verify(smbRecordService, times(1)).getFolder(anyString());
	}

	@Test
	public void givenDeleteDTOWhenExecutingThenQueueDeleteJob()
			throws RecordServicesException {
		SmbFileDTO smbFileDTO = new SmbFileDTO();
		smbFileDTO.setUrl(FILE_URL);
		smbFileDTO.setStatus(SmbFileDTOStatus.DELETE_DTO);
		smbService = new FakeSmbService(smbFileDTO);

		JobParams jobParams = new JobParams(connector, eventObserver,smbUtils, connectorInstance, smbService,smbRecordService, updater, jobFactory, FILE_URL, null);
		retrievalJob = new SmbNewDocumentRetrievalJob(jobParams);
		retrievalJob.execute(connector);

		verify(connector, times(1)).queueJob(any(SmbDeleteJob.class));
	}

	@After
	public void after() {
		eventObserver.close();
	}
}