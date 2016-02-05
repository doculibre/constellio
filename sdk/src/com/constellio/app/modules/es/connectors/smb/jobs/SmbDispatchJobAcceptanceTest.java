package com.constellio.app.modules.es.connectors.smb.jobs;

import static com.constellio.app.modules.es.sdk.ESTestUtils.assertThatEventsObservedBy;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.constellio.app.modules.es.connectors.smb.ConnectorSmb;
import com.constellio.app.modules.es.connectors.smb.jobmanagement.SmbDocumentOrFolderUpdater;
import com.constellio.app.modules.es.connectors.smb.jobmanagement.SmbJobFactory;
import com.constellio.app.modules.es.connectors.smb.jobmanagement.SmbJobFactoryImpl.SmbJobCategory;
import com.constellio.app.modules.es.connectors.smb.service.SmbRecordService;
import com.constellio.app.modules.es.connectors.smb.service.SmbService;
import com.constellio.app.modules.es.connectors.smb.testutils.FakeSmbService;
import com.constellio.app.modules.es.connectors.smb.testutils.SmbTestParams;
import com.constellio.app.modules.es.connectors.spi.ConnectorJob;
import com.constellio.app.modules.es.connectors.spi.ConnectorLogger;
import com.constellio.app.modules.es.connectors.spi.ConsoleConnectorLogger;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbInstance;
import com.constellio.app.modules.es.sdk.TestConnectorEventObserver;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.app.modules.es.services.crawler.DefaultConnectorEventObserver;
import com.constellio.sdk.tests.ConstellioTest;

public class SmbDispatchJobAcceptanceTest extends ConstellioTest {
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
		jobFactory = new FakeSmbJobFactory(SmbNullJob.getInstance(connector));

		SmbDispatchJob dispatchJob = new SmbDispatchJob(connector, SHARE_URL, smbService, jobFactory, "");
		dispatchJob.execute(connector);

		assertThatEventsObservedBy(eventObserver).comparingRecordsUsing(es.connectorSmbFolder.url())
				.isEmpty();
	}

	@Test
	public void givenDeleteJobInsteadOfRetrievalJobWhenExecutingDispatchThenQueueDeleteJob() {
		SmbDeleteJob deleteJob = new SmbDeleteJob(connector, SHARE_URL, eventObserver, smbRecordService, connectorInstance, smbService);
		jobFactory = new FakeSmbJobFactory(deleteJob);

		ArgumentCaptor<ConnectorJob> argumentCaptor = ArgumentCaptor.forClass(ConnectorJob.class);

		SmbDispatchJob dispatchJob = new SmbDispatchJob(connector, SHARE_URL, smbService, jobFactory, "");
		dispatchJob.execute(connector);

		verify(connector, times(1)).queueJob(argumentCaptor.capture());
		assertThat(argumentCaptor.getAllValues()).containsOnly(deleteJob);

		assertThatEventsObservedBy(eventObserver).comparingRecordsUsing(es.connectorSmbFolder.url())
				.isEmpty();
	}

	@Test
	public void givenRetrievalJobAndUrlIsFileWhenExecutingDispatchThenQueueRetrievalJob() {
		SmbNewDocumentRetrievalJob retrievalJob = new SmbNewDocumentRetrievalJob(connector, FILE_URL, smbService, eventObserver, smbRecordService, updater, "",
				jobFactory);
		jobFactory = new FakeSmbJobFactory(retrievalJob);

		ArgumentCaptor<ConnectorJob> argumentCaptor = ArgumentCaptor.forClass(ConnectorJob.class);

		SmbDispatchJob dispatchJob = new SmbDispatchJob(connector, FILE_URL, smbService, jobFactory, "");
		dispatchJob.execute(connector);

		verify(connector, times(1)).queueJob(argumentCaptor.capture());
		assertThat(argumentCaptor.getAllValues()).containsOnly(retrievalJob);

		assertThatEventsObservedBy(eventObserver).comparingRecordsUsing(es.connectorSmbDocument.url())
				.isEmpty();
	}

	@Test
	public void givenRetrievalJobAndUrlIsShareWithContentWhenExecutingDispatchThenQueueRetrievalJobsAndDispatchJobs() {
		smbService = new FakeSmbService(Arrays.asList(FILE_URL, FOLDER_URL));

		SmbNewFolderRetrievalJob shareRetrievalJob = new SmbNewFolderRetrievalJob(connector, SHARE_URL, smbService, eventObserver, smbRecordService, updater,
				"", jobFactory);

		SmbNewDocumentRetrievalJob fileRetrievalJob = new SmbNewDocumentRetrievalJob(connector, FILE_URL, smbService, eventObserver, smbRecordService, updater,
				SHARE_URL, jobFactory);

		SmbNewFolderRetrievalJob folderRetrievalJob = new SmbNewFolderRetrievalJob(connector, FOLDER_URL, smbService, eventObserver, smbRecordService, updater,
				FOLDER_URL, jobFactory);

		jobFactory = new FakeSmbJobFactory(Arrays.asList(shareRetrievalJob, fileRetrievalJob, folderRetrievalJob));

		ArgumentCaptor<ConnectorJob> argumentCaptor = ArgumentCaptor.forClass(ConnectorJob.class);

		SmbDispatchJob dispatchJob = new SmbDispatchJob(connector, SHARE_URL, smbService, jobFactory, "");
		dispatchJob.execute(connector);

		verify(connector, times(3)).queueJob(argumentCaptor.capture());
		List<ConnectorJob> jobs = argumentCaptor.getAllValues();
		assertThat(jobs).containsOnly(shareRetrievalJob, fileRetrievalJob, folderRetrievalJob);

		assertThatEventsObservedBy(eventObserver).comparingRecordsUsing(es.connectorSmbFolder.url())
				.isEmpty();

		assertThatEventsObservedBy(eventObserver).comparingRecordsUsing(es.connectorSmbDocument.url())
				.isEmpty();
	}

	@Test
	public void givenRetrievalJobAndUrlIsShareWithoutContentWhenExecutingDispatchThenQueueRetrievalJob() {
		SmbNewFolderRetrievalJob shareRetrievalJob = new SmbNewFolderRetrievalJob(connector, SHARE_URL, smbService, eventObserver, smbRecordService, updater,
				"", jobFactory);
		smbService = new FakeSmbService(new ArrayList<String>());
		jobFactory = new FakeSmbJobFactory(shareRetrievalJob);

		ArgumentCaptor<ConnectorJob> argumentCaptor = ArgumentCaptor.forClass(ConnectorJob.class);

		SmbDispatchJob dispatchJob = new SmbDispatchJob(connector, SHARE_URL, smbService, jobFactory, "");
		dispatchJob.execute(connector);

		verify(connector, times(1)).queueJob(argumentCaptor.capture());
		assertThat(argumentCaptor.getAllValues()).containsOnly(shareRetrievalJob);

		assertThatEventsObservedBy(eventObserver).comparingRecordsUsing(es.connectorSmbFolder.url())
				.isEmpty();
	}

	@Test
	public void givenRetrievalJobAndUrlIsFolderWithContentWhenExecutingDispatchThenQueueRetrievalJobsAndDispatchJobs() {
		smbService = new FakeSmbService(Arrays.asList(FILE1_URL, FOLDER2_URL));

		SmbNewFolderRetrievalJob shareRetrievalJob = new SmbNewFolderRetrievalJob(connector, FOLDER_URL, smbService, eventObserver, smbRecordService, updater,
				SHARE_URL, jobFactory);

		SmbNewDocumentRetrievalJob fileRetrievalJob = new SmbNewDocumentRetrievalJob(connector, FILE1_URL, smbService, eventObserver, smbRecordService,
				updater, FOLDER_URL, jobFactory);

		SmbNewFolderRetrievalJob folderRetrievalJob = new SmbNewFolderRetrievalJob(connector, FOLDER2_URL, smbService, eventObserver, smbRecordService,
				updater, FOLDER_URL, jobFactory);

		jobFactory = new FakeSmbJobFactory(Arrays.asList(shareRetrievalJob, fileRetrievalJob, folderRetrievalJob));

		ArgumentCaptor<ConnectorJob> argumentCaptor = ArgumentCaptor.forClass(ConnectorJob.class);

		SmbDispatchJob dispatchJob = new SmbDispatchJob(connector, FOLDER_URL, smbService, jobFactory, SHARE_URL);
		dispatchJob.execute(connector);

		verify(connector, times(3)).queueJob(argumentCaptor.capture());
		List<ConnectorJob> jobs = argumentCaptor.getAllValues();
		assertThat(jobs).contains(shareRetrievalJob, fileRetrievalJob, folderRetrievalJob);

		assertThatEventsObservedBy(eventObserver).comparingRecordsUsing(es.connectorSmbFolder.url())
				.isEmpty();

		assertThatEventsObservedBy(eventObserver).comparingRecordsUsing(es.connectorSmbDocument.url())
				.isEmpty();
	}

	@Test
	public void givenRetrievalJobAndUrlIsFolderWithoutContentWhenExecutingDispatchThenQueueRetrievalJob() {
		SmbNewFolderRetrievalJob retrievalJob = new SmbNewFolderRetrievalJob(connector, FOLDER_URL, smbService, eventObserver, smbRecordService, updater,
				SHARE_URL, jobFactory);
		smbService = new FakeSmbService(new ArrayList<String>());
		jobFactory = new FakeSmbJobFactory(retrievalJob);

		ArgumentCaptor<ConnectorJob> argumentCaptor = ArgumentCaptor.forClass(ConnectorJob.class);

		SmbDispatchJob dispatchJob = new SmbDispatchJob(connector, FOLDER_URL, smbService, jobFactory, SHARE_URL);
		dispatchJob.execute(connector);

		verify(connector, times(1)).queueJob(argumentCaptor.capture());
		assertThat(argumentCaptor.getAllValues()).containsOnly(retrievalJob);

		assertThatEventsObservedBy(eventObserver).comparingRecordsUsing(es.connectorSmbFolder.url())
				.isEmpty();
	}

	@Test
	public void givenResumeIgnoreJobInsteadOfRetrievalJobWhenExecutingDispatchThenDoNothing() {
		jobFactory = new FakeSmbJobFactory(SmbResumeIgnoreJob.getInstance(connector));
		smbService = new FakeSmbService(Arrays.asList(FILE_URL, FOLDER_URL));

		SmbDispatchJob dispatchJob = new SmbDispatchJob(connector, SHARE_URL, smbService, jobFactory, "");
		dispatchJob.execute(connector);

		assertThatEventsObservedBy(eventObserver).comparingRecordsUsing(es.connectorSmbFolder.url())
				.isEmpty();
	}

	@After
	public void after() {
		eventObserver.close();
	}

	private class FakeSmbJobFactory implements SmbJobFactory {
		private Deque<ConnectorJob> jobs = new ArrayDeque<>();

		public FakeSmbJobFactory(ConnectorJob job) {
			this.jobs.add(job);
		}

		public FakeSmbJobFactory(List<? extends ConnectorJob> jobs) {
			this.jobs.addAll(jobs);
		}

		@Override
		public ConnectorJob get(SmbJobCategory jobType, String url, String parentUrl) {
			return jobs.poll();
		}

		@Override
		public void reset() {
			throw new UnsupportedOperationException("TODO");
		}

		@Override
		public void updateResumeUrl(String resumeUrl) {
			throw new UnsupportedOperationException("TODO");
		}
	}
}