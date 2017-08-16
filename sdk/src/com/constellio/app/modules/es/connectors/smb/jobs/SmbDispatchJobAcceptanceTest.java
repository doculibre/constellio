package com.constellio.app.modules.es.connectors.smb.jobs;

import com.constellio.app.modules.es.connectors.smb.ConnectorSmb;
import com.constellio.app.modules.es.connectors.smb.cache.SmbConnectorContext;
import com.constellio.app.modules.es.connectors.smb.jobmanagement.SmbConnectorJob;
import com.constellio.app.modules.es.connectors.smb.jobmanagement.SmbDocumentOrFolderUpdater;
import com.constellio.app.modules.es.connectors.smb.jobmanagement.SmbJobFactory;
import com.constellio.app.modules.es.connectors.smb.jobmanagement.SmbJobFactoryImpl.SmbJobCategory;
import com.constellio.app.modules.es.connectors.smb.service.SmbModificationIndicator;
import com.constellio.app.modules.es.connectors.smb.service.SmbRecordService;
import com.constellio.app.modules.es.connectors.smb.service.SmbShareService;
import com.constellio.app.modules.es.connectors.smb.testutils.FakeSmbService;
import com.constellio.app.modules.es.connectors.smb.testutils.SmbTestParams;
import com.constellio.app.modules.es.connectors.smb.utils.ConnectorSmbUtils;
import com.constellio.app.modules.es.connectors.spi.ConnectorJob;
import com.constellio.app.modules.es.connectors.spi.ConnectorLogger;
import com.constellio.app.modules.es.connectors.spi.ConsoleConnectorLogger;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbInstance;
import com.constellio.app.modules.es.sdk.TestConnectorEventObserver;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.app.modules.es.services.crawler.DefaultConnectorEventObserver;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static com.constellio.app.modules.es.sdk.ESTestUtils.assertThatEventsObservedBy;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class SmbDispatchJobAcceptanceTest extends ConstellioTest {
	@Mock private ConnectorSmb connector;
	private ConnectorSmbInstance connectorInstance;
	private ESSchemasRecordsServices es;
	private SmbShareService smbService;
	private SmbJobFactory jobFactory;
	private ConnectorLogger logger;
	private TestConnectorEventObserver eventObserver;
	private SmbDocumentOrFolderUpdater updater;

	private SmbRecordService smbRecordService;

	@Mock private ConnectorSmbUtils smbUtils;
	@Mock private SmbConnectorContext context;

	private String SHARE_URL = SmbTestParams.EXISTING_SHARE;
	private String FILE_URL = SHARE_URL + SmbTestParams.FILE_NAME;
	private String FOLDER_URL = SHARE_URL + SmbTestParams.FOLDER_NAME;
	private String FILE1_URL = FOLDER_URL + "file1.txt";
	private String FOLDER2_URL = FOLDER_URL + "folder2/";

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
	public void givenNullJobInsteadOfRetrievalJobWhenExecutingDispatchThenDoNothing() {
		JobParams jobParams = spy(new JobParams(connector, eventObserver, smbUtils, connectorInstance, smbService,smbRecordService, updater, null, FILE_URL, SHARE_URL));
		SmbNullJob nullJob = new SmbNullJob(jobParams);
		when(jobParams.getJobFactory()).thenReturn(new FakeSmbJobFactory(nullJob));
		SmbDispatchJob dispatchJob = new SmbDispatchJob(jobParams);
		dispatchJob.execute(connector);

		assertThatEventsObservedBy(eventObserver).comparingRecordsUsing(es.connectorSmbFolder.url())
				.isEmpty();
	}

	@Test
	public void givenDeleteJobInsteadOfRetrievalJobWhenExecutingDispatchThenQueueDeleteJob() {
		JobParams jobParams = spy(new JobParams(connector, eventObserver, smbUtils, connectorInstance, smbService,smbRecordService, updater, null, FILE_URL, SHARE_URL));
		SmbDeleteJob deleteJob = new SmbDeleteJob(jobParams);
		when(jobParams.getJobFactory()).thenReturn(new FakeSmbJobFactory(deleteJob));

		ArgumentCaptor<SmbConnectorJob> argumentCaptor = ArgumentCaptor.forClass(SmbConnectorJob.class);

		SmbDispatchJob dispatchJob = new SmbDispatchJob(jobParams);
		dispatchJob.execute(connector);

		verify(connector, times(1)).queueJob(argumentCaptor.capture());
		assertThat(argumentCaptor.getAllValues()).containsOnly(deleteJob);

		assertThatEventsObservedBy(eventObserver).comparingRecordsUsing(es.connectorSmbFolder.url())
				.isEmpty();
	}

	@Test
	public void givenRetrievalJobAndUrlIsFileWhenExecutingDispatchThenQueueRetrievalJob() {
		JobParams jobParams = spy(new JobParams(connector, eventObserver, smbUtils, connectorInstance, smbService,smbRecordService, updater, null, FILE_URL, SHARE_URL));
		SmbNewRetrievalJob retrievalJob = new SmbNewRetrievalJob(jobParams, mock(SmbModificationIndicator.class), false);
		when(jobParams.getJobFactory()).thenReturn(new FakeSmbJobFactory(retrievalJob));

		ArgumentCaptor<SmbConnectorJob> argumentCaptor = ArgumentCaptor.forClass(SmbConnectorJob.class);

		SmbDispatchJob dispatchJob = new SmbDispatchJob(jobParams);
		dispatchJob.execute(connector);

		verify(connector, times(1)).queueJob(argumentCaptor.capture());
		assertThat(argumentCaptor.getAllValues()).containsOnly(retrievalJob);

		assertThatEventsObservedBy(eventObserver).comparingRecordsUsing(es.connectorSmbDocument.url())
				.isEmpty();
	}

	@Test
	public void givenRetrievalJobAndUrlIsShareWithContentWhenExecutingDispatchThenQueueRetrievalJobsAndDispatchJobs() {
		smbService = new FakeSmbService(Arrays.asList(FOLDER_URL, FILE_URL));
		JobParams jobParams = spy(new JobParams(connector, eventObserver, smbUtils, connectorInstance, smbService, smbRecordService, null, null, SHARE_URL, null));
		when(jobParams.getUrl()).thenReturn(SHARE_URL, FOLDER_URL, FILE_URL, FILE1_URL);
		SmbNewRetrievalJob shareRetrievalJob = new SmbNewRetrievalJob(jobParams, mock(SmbModificationIndicator.class), true);
		SmbNewRetrievalJob fileRetrievalJob = new SmbNewRetrievalJob(jobParams, mock(SmbModificationIndicator.class), false);
		SmbNewRetrievalJob folderRetrievalJob = new SmbNewRetrievalJob(jobParams, mock(SmbModificationIndicator.class), true);
		when(jobParams.getJobFactory()).thenReturn(new FakeSmbJobFactory(Arrays.asList(shareRetrievalJob, fileRetrievalJob, folderRetrievalJob)));

		when(smbUtils.isFolder(SHARE_URL)).thenReturn(true);

		ArgumentCaptor<SmbConnectorJob> argumentCaptor = ArgumentCaptor.forClass(SmbConnectorJob.class);

		SmbDispatchJob dispatchJob = new SmbDispatchJob(jobParams);
		dispatchJob.execute(connector);

		verify(connector, times(3)).queueJob(argumentCaptor.capture());
		List<SmbConnectorJob> jobs = argumentCaptor.getAllValues();
		assertThat(jobs).containsOnly(shareRetrievalJob, fileRetrievalJob, folderRetrievalJob);

		assertThatEventsObservedBy(eventObserver).comparingRecordsUsing(es.connectorSmbFolder.url())
				.isEmpty();

		assertThatEventsObservedBy(eventObserver).comparingRecordsUsing(es.connectorSmbDocument.url())
				.isEmpty();
	}

	@Test
	public void givenRetrievalJobAndUrlIsShareWithoutContentWhenExecutingDispatchThenQueueRetrievalJob() {
		smbService = new FakeSmbService(new ArrayList<String>());
		JobParams jobParams = spy(new JobParams(connector, eventObserver,smbUtils, connectorInstance, smbService,smbRecordService, null, null, null, null));
		SmbNewRetrievalJob shareRetrievalJob = new SmbNewRetrievalJob(jobParams, mock(SmbModificationIndicator.class), false);
		when(jobParams.getJobFactory()).thenReturn(new FakeSmbJobFactory(shareRetrievalJob));

		ArgumentCaptor<SmbConnectorJob> argumentCaptor = ArgumentCaptor.forClass(SmbConnectorJob.class);

		SmbDispatchJob dispatchJob = new SmbDispatchJob(jobParams);
		dispatchJob.execute(connector);

		verify(connector, times(1)).queueJob(argumentCaptor.capture());
		assertThat(argumentCaptor.getAllValues()).containsOnly(shareRetrievalJob);

		assertThatEventsObservedBy(eventObserver).comparingRecordsUsing(es.connectorSmbFolder.url())
				.isEmpty();
	}

	@Test
	public void givenRetrievalJobAndUrlIsFolderWithContentWhenExecutingDispatchThenQueueRetrievalJobsAndDispatchJobs() {
		smbService = new FakeSmbService(Arrays.asList(FILE1_URL, FOLDER2_URL));
		JobParams jobParams = spy(new JobParams(connector, eventObserver,smbUtils, connectorInstance, smbService,smbRecordService, null, null, null, null));
		when(jobParams.getUrl()).thenReturn(FOLDER_URL, FILE1_URL, FOLDER2_URL);
		SmbNewRetrievalJob shareRetrievalJob = new SmbNewRetrievalJob(jobParams, mock(SmbModificationIndicator.class), true);
		SmbNewRetrievalJob fileRetrievalJob = new SmbNewRetrievalJob(jobParams, mock(SmbModificationIndicator.class), false);
		SmbNewRetrievalJob folderRetrievalJob = new SmbNewRetrievalJob(jobParams, mock(SmbModificationIndicator.class), true);
		jobFactory = new FakeSmbJobFactory(Arrays.asList(shareRetrievalJob, fileRetrievalJob, folderRetrievalJob));
		when(jobParams.getJobFactory()).thenReturn(new FakeSmbJobFactory(Arrays.asList(shareRetrievalJob, fileRetrievalJob, folderRetrievalJob)));

		ArgumentCaptor<SmbConnectorJob> argumentCaptor = ArgumentCaptor.forClass(SmbConnectorJob.class);

		when(smbUtils.isFolder(FOLDER_URL)).thenReturn(true);

		SmbDispatchJob dispatchJob = new SmbDispatchJob(jobParams);
		dispatchJob.execute(connector);


		verify(connector, times(3)).queueJob(argumentCaptor.capture());
		List<SmbConnectorJob> jobs = argumentCaptor.getAllValues();
		assertThat(jobs).contains(shareRetrievalJob, fileRetrievalJob, folderRetrievalJob);

		assertThatEventsObservedBy(eventObserver).comparingRecordsUsing(es.connectorSmbFolder.url())
				.isEmpty();

		assertThatEventsObservedBy(eventObserver).comparingRecordsUsing(es.connectorSmbDocument.url())
				.isEmpty();
	}

	@Test
	public void givenRetrievalJobAndUrlIsFolderWithoutContentWhenExecutingDispatchThenQueueRetrievalJob() {
		JobParams jobParams = spy(new JobParams(connector, eventObserver,smbUtils, connectorInstance, smbService,smbRecordService, null, null, FILE_URL, null));
		SmbNewRetrievalJob retrievalJob = new SmbNewRetrievalJob(jobParams, mock(SmbModificationIndicator.class), true);
		when(jobParams.getSmbShareService()).thenReturn(new FakeSmbService(new ArrayList<String>()));
		when(jobParams.getJobFactory()).thenReturn(new FakeSmbJobFactory(retrievalJob));

		ArgumentCaptor<SmbConnectorJob> argumentCaptor = ArgumentCaptor.forClass(SmbConnectorJob.class);

		SmbDispatchJob dispatchJob = new SmbDispatchJob(jobParams);
		dispatchJob.execute(connector);

		verify(connector, times(1)).queueJob(argumentCaptor.capture());
		assertThat(argumentCaptor.getAllValues()).containsOnly(retrievalJob);

		assertThatEventsObservedBy(eventObserver).comparingRecordsUsing(es.connectorSmbFolder.url())
				.isEmpty();
	}

	@Test
	public void givenResumeIgnoreJobInsteadOfRetrievalJobWhenExecutingDispatchThenDoNothing() {
		JobParams jobParams = spy(new JobParams(connector, eventObserver,smbUtils, connectorInstance, smbService,smbRecordService, null, null, FILE_URL, null));
		SmbUnmodifiedRetrievalJob retrievalJob = new SmbUnmodifiedRetrievalJob(jobParams);
		SmbJobFactory factory = spy(new FakeSmbJobFactory(retrievalJob));
		when(jobParams.getJobFactory()).thenReturn(factory);
		when(jobParams.getSmbShareService()).thenReturn(new FakeSmbService(new ArrayList<String>()));

		SmbDispatchJob dispatchJob = new SmbDispatchJob(jobParams);
		dispatchJob.execute(connector);

		assertThatEventsObservedBy(eventObserver).comparingRecordsUsing(es.connectorSmbFolder.url())
				.isEmpty();

		assertThat(connectorInstance.getResumeUrl()).isNull();
		verify(connector, times(1)).queueJob(retrievalJob);
	}

	@After
	public void after() {
		eventObserver.close();
	}

	private class FakeSmbJobFactory implements SmbJobFactory {
		private Deque<SmbConnectorJob> jobs = new ArrayDeque<>();

		public FakeSmbJobFactory(SmbConnectorJob job) {
			this.jobs.add(job);
		}

		public FakeSmbJobFactory(List<? extends SmbConnectorJob> jobs) {
			this.jobs.addAll(jobs);
		}

		@Override
		public SmbConnectorJob get(SmbJobCategory jobType, String url, String parentUrl) {
			return jobs.poll();
		}
	}
}