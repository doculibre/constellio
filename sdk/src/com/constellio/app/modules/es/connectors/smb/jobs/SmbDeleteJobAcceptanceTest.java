package com.constellio.app.modules.es.connectors.smb.jobs;

import com.constellio.app.modules.es.connectors.smb.ConnectorSmb;
import com.constellio.app.modules.es.connectors.smb.cache.SmbConnectorContext;
import com.constellio.app.modules.es.connectors.smb.cache.SmbConnectorContextServices;
import com.constellio.app.modules.es.connectors.smb.service.SmbFileDTO;
import com.constellio.app.modules.es.connectors.smb.service.SmbFileDTO.SmbFileDTOStatus;
import com.constellio.app.modules.es.connectors.smb.service.SmbRecordService;
import com.constellio.app.modules.es.connectors.smb.service.SmbShareService;
import com.constellio.app.modules.es.connectors.smb.testutils.FakeSmbService;
import com.constellio.app.modules.es.connectors.smb.testutils.SmbTestParams;
import com.constellio.app.modules.es.connectors.smb.utils.ConnectorSmbUtils;
import com.constellio.app.modules.es.connectors.spi.ConnectorLogger;
import com.constellio.app.modules.es.connectors.spi.ConsoleConnectorLogger;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbDocument;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbFolder;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbInstance;
import com.constellio.app.modules.es.sdk.TestConnectorEventObserver;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.app.modules.es.services.crawler.DefaultConnectorEventObserver;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static com.constellio.app.modules.es.sdk.ESTestUtils.assertThatEventsObservedBy;
import static com.constellio.app.modules.es.sdk.TestConnectorEvent.deleteEvent;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

public class SmbDeleteJobAcceptanceTest extends ConstellioTest {
	@Mock private ConnectorSmb connector;
	private ESSchemasRecordsServices es;
	private ConnectorSmbInstance connectorInstance;
	private ConnectorLogger logger;
	private TestConnectorEventObserver eventObserver;
	private SmbRecordService smbRecordService;
	@Mock private ConnectorSmbUtils smbUtils;
	@Mock private SmbConnectorContext context;

	private String SHARE_URL = SmbTestParams.EXISTING_SHARE;
	private String FILE_URL = SHARE_URL + SmbTestParams.FILE_NAME;
	private String FOLDER_URL = SHARE_URL + SmbTestParams.FOLDER_NAME;

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
		SmbConnectorContextServices contextServices = new SmbConnectorContextServices(es);
		context = contextServices.createContext(connectorInstance.getId());
		when(connector.getContext()).thenReturn(context);

		eventObserver = new TestConnectorEventObserver(es, new DefaultConnectorEventObserver(es, logger, SmbTestParams.CONNECTOR_OBSERVER));
		smbRecordService = new SmbRecordService(es, connectorInstance);
	}

	@Test
	public void givenNoRecordWhenExecutingDeleteJobThenDoNothing()
			throws RecordServicesException {
		SmbFileDTO smbFileDTO = new SmbFileDTO();
		smbFileDTO.setStatus(SmbFileDTOStatus.DELETE_DTO);
		SmbShareService smbService = new FakeSmbService(smbFileDTO);
		JobParams jobParams = new JobParams(connector, eventObserver,smbUtils, connectorInstance, smbService,smbRecordService, null, null, FILE_URL, null);
		SmbDeleteJob deleteJob = new SmbDeleteJob(jobParams);
		deleteJob.execute(connector);

		assertThatEventsObservedBy(eventObserver).comparingRecordsUsing(es.connectorSmbDocument.url())
				.isEmpty();
	}

	@Test
	public void givenExistingFileRecordWhenExecutingDeleteJobThenDeleteRecord()
			throws RecordServicesException {

		ConnectorSmbDocument connectorSmbDocument = es.newConnectorSmbDocument(connectorInstance)
				.setUrl(FILE_URL)
				.setConnector(connectorInstance)
				.setTraversalCode(SmbTestParams.TRAVERSAL_CODE)
				.setFetched(true);

		es.getRecordServices()
				.add(connectorSmbDocument);

		SmbFileDTO smbFileDTO = new SmbFileDTO();
		smbFileDTO.setStatus(SmbFileDTOStatus.DELETE_DTO);
		SmbShareService smbService = new FakeSmbService(smbFileDTO);
		JobParams jobParams = new JobParams(connector, eventObserver,smbUtils, connectorInstance, smbService,smbRecordService, null, null, FILE_URL, null);
		SmbDeleteJob deleteJob = new SmbDeleteJob(jobParams);
		deleteJob.execute(connector);

		assertThatEventsObservedBy(eventObserver).comparingRecordsUsing(es.connectorSmbDocument.url())
				.containsOnly(deleteEvent(es.newConnectorSmbDocument(connectorInstance)
						.setUrl(FILE_URL)));
	}

	@Test
	public void givenExistingFolderRecordWithContentWhenExecutingDeleteJobThenDeleteRecordAndChildren()
			throws RecordServicesException {
		ConnectorSmbFolder connectorSmbFolder = es.newConnectorSmbFolder(connectorInstance)
				.setUrl(FOLDER_URL)
				.setConnector(connectorInstance)
				.setTraversalCode(SmbTestParams.TRAVERSAL_CODE)
				.setFetched(true);

		es.getRecordServices()
				.add(connectorSmbFolder);

		ConnectorSmbDocument connectorSmbDocument = es.newConnectorSmbDocument(connectorInstance)
				.setUrl(FILE_URL)
				.setConnector(connectorInstance)
				.setTraversalCode(SmbTestParams.TRAVERSAL_CODE)
				.setFetched(true)
				.setParent(connectorSmbFolder.getId());

		es.getRecordServices()
				.add(connectorSmbDocument);

		SmbFileDTO smbFileDTO = new SmbFileDTO();
		smbFileDTO.setStatus(SmbFileDTOStatus.DELETE_DTO);
		SmbShareService smbService = new FakeSmbService(smbFileDTO);
		JobParams jobParams = new JobParams(connector, eventObserver,smbUtils, connectorInstance, smbService,smbRecordService, null, null, FILE_URL, null);
		SmbDeleteJob deleteJob = new SmbDeleteJob(jobParams);
		deleteJob.execute(connector);

		List<ConnectorSmbDocument> documents = es.searchConnectorSmbDocuments(LogicalSearchQueryOperators.from(es.connectorSmbDocument.schema())
				.where(es.connectorSmbDocument.url())
				.isEqualTo(FILE_URL));

		assertThat(documents).isEmpty();
	}

	@Test
	public void givenRejectedUrlWhenDeletingThenDeleteRecord()
			throws RecordServicesException {
		when(smbUtils.isAccepted(anyString(), any(ConnectorSmbInstance.class))).thenReturn(false);

		ConnectorSmbDocument connectorSmbDocument = es.newConnectorSmbDocument(connectorInstance)
				.setUrl(FILE_URL)
				.setConnector(connectorInstance)
				.setTraversalCode(SmbTestParams.TRAVERSAL_CODE)
				.setFetched(true);

		es.getRecordServices()
				.add(connectorSmbDocument);
		es.getRecordServices().flush();

		SmbFileDTO smbFileDTO = new SmbFileDTO();
		smbFileDTO.setStatus(SmbFileDTOStatus.FULL_DTO);
		SmbShareService smbService = new FakeSmbService(smbFileDTO);
		JobParams jobParams = new JobParams(connector, eventObserver,smbUtils, connectorInstance, smbService,smbRecordService, null, null, FILE_URL, null);
		SmbDeleteJob deleteJob = new SmbDeleteJob(jobParams);
		deleteJob.execute(connector);

		assertThatEventsObservedBy(eventObserver).comparingRecordsUsing(es.connectorSmbDocument.url())
				.containsOnly(deleteEvent(es.newConnectorSmbDocument(connectorInstance)
						.setUrl(FILE_URL)));
	}

	@After
	public void after() {
		eventObserver.close();
	}

}