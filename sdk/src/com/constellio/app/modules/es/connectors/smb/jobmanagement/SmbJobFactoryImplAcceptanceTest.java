package com.constellio.app.modules.es.connectors.smb.jobmanagement;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.joda.time.LocalDateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.constellio.app.modules.es.connectors.smb.ConnectorSmb;
import com.constellio.app.modules.es.connectors.smb.jobmanagement.SmbJobFactoryImpl.SmbJobCategory;
import com.constellio.app.modules.es.connectors.smb.jobs.SmbDeleteJob;
import com.constellio.app.modules.es.connectors.smb.jobs.SmbDispatchJob;
import com.constellio.app.modules.es.connectors.smb.jobs.SmbModifiedDocumentRetrievalJob;
import com.constellio.app.modules.es.connectors.smb.jobs.SmbNewDocumentRetrievalJob;
import com.constellio.app.modules.es.connectors.smb.jobs.SmbNewFolderRetrievalJob;
import com.constellio.app.modules.es.connectors.smb.jobs.SmbNullJob;
import com.constellio.app.modules.es.connectors.smb.jobs.SmbResumeIgnoreJob;
import com.constellio.app.modules.es.connectors.smb.jobs.SmbSeedJob;
import com.constellio.app.modules.es.connectors.smb.jobs.SmbUnmodifiedDocumentRetrievalJob;
import com.constellio.app.modules.es.connectors.smb.service.SmbRecordService;
import com.constellio.app.modules.es.connectors.smb.service.SmbService;
import com.constellio.app.modules.es.connectors.smb.service.SmbService.SmbModificationIndicator;
import com.constellio.app.modules.es.connectors.smb.testutils.SmbTestParams;
import com.constellio.app.modules.es.connectors.smb.utils.ConnectorSmbUtils;
import com.constellio.app.modules.es.connectors.spi.ConnectorJob;
import com.constellio.app.modules.es.connectors.spi.ConnectorLogger;
import com.constellio.app.modules.es.connectors.spi.ConsoleConnectorLogger;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbDocument;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbInstance;
import com.constellio.app.modules.es.sdk.TestConnectorEventObserver;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.app.modules.es.services.crawler.DefaultConnectorEventObserver;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.InDevelopmentTest;

public class SmbJobFactoryImplAcceptanceTest extends ConstellioTest {
	@Mock private ConnectorSmb connector;
	private ESSchemasRecordsServices es;
	private ConnectorSmbInstance connectorInstance;
	@Mock private SmbService smbService;
	private ConnectorLogger logger;
	private TestConnectorEventObserver eventObserver;
	private ConnectorSmbUtils smbUtils;
	private SmbRecordService smbRecordService;
	private SmbDocumentOrFolderUpdater updater;

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

		jobFactory = new SmbJobFactoryImpl(connector, connectorInstance, eventObserver, smbService, smbUtils, smbRecordService, updater);
	}

	@Test
	public void givenAnAcceptedUrlWhenCreatingDispatchJobThenGetDispatchJob() {
		ConnectorJob job = jobFactory.get(SmbJobCategory.DISPATCH, SmbTestParams.EXISTING_SHARE + SmbTestParams.EXISTING_FOLDER, "");
		assertThat(job).isInstanceOf(SmbDispatchJob.class);
	}

	@Test
	public void givenAnAcceptedNewDocumentUrlWhenCreatingRetrievalJobThenGetRetrievalJob() {
		ConnectorJob job = jobFactory.get(SmbJobCategory.RETRIEVAL, SmbTestParams.EXISTING_SHARE + "newFile.txt", "");
		assertThat(job).isInstanceOf(SmbNewDocumentRetrievalJob.class);
	}

	@Test
	public void givenEmptyResumeUrlWhenResumingThenCreateRetrievalJob() {
		String resumeUrl = "";
		jobFactory.updateResumeUrl(resumeUrl);
		ConnectorJob job = jobFactory.get(SmbJobCategory.RETRIEVAL, SmbTestParams.EXISTING_SHARE, "");
		assertThat(job).isInstanceOf(SmbNewFolderRetrievalJob.class);
	}

	@Test
	public void givenUrlLowerThanResumeUrlWhenResumingThenDoNotCreateRetrievalJob() {
		String resumeUrl = SmbTestParams.EXISTING_SHARE + SmbTestParams.EXISTING_FOLDER;
		jobFactory.updateResumeUrl(resumeUrl);
		ConnectorJob job = jobFactory.get(SmbJobCategory.RETRIEVAL, SmbTestParams.EXISTING_SHARE, "");
		assertThat(job).isEqualTo(SmbResumeIgnoreJob.getInstance(connector));
	}

	@Test
	public void givenUrlGreaterThanResumeUrlWhenResumingThenCreateRetrievalJob() {
		String resumeUrl = SmbTestParams.EXISTING_SHARE + SmbTestParams.EXISTING_FOLDER;
		jobFactory.updateResumeUrl(resumeUrl);
		ConnectorJob job = jobFactory.get(SmbJobCategory.RETRIEVAL, SmbTestParams.EXISTING_SHARE + SmbTestParams.EXISTING_FOLDER, "");
		assertThat(job).isInstanceOf(SmbNewFolderRetrievalJob.class);
	}

	@Test
	public void givenAnAcceptedModifiedDocumentUrlWhenCreatingRetrievalJobThenGetRetrievalJob()
			throws RecordServicesException {
		String url = SmbTestParams.EXISTING_SHARE + "modifiedFile.txt";
		String permissionsHash = "someHash";
		long size = 10;
		long lastModified = System.currentTimeMillis();

		SmbModificationIndicator modInfo = new SmbModificationIndicator();
		modInfo.setLastModified(lastModified);
		modInfo.setPermissionsHash(permissionsHash);
		modInfo.setSize(15);

		ConnectorSmbDocument document = es.newConnectorSmbDocument(connectorInstance);
		document.setUrl(url);
		document.setPermissionsHash(permissionsHash);
		document.setSize(size);
		document.setLastModified(new LocalDateTime(lastModified));

		when(smbService.getModificationIndicator(anyString())).thenReturn(modInfo);

		es.getRecordServices()
				.update(document.getWrappedRecord());
		es.getRecordServices()
				.flush();

		ConnectorJob job = jobFactory.get(SmbJobCategory.RETRIEVAL, url, "");

		assertThat(job).isInstanceOf(SmbModifiedDocumentRetrievalJob.class);
	}

	@Test
	public void givenAnAcceptedNonModifiedDocumentUrlWhenCreatingRetrievalJobThenGetRetrievalJob()
			throws RecordServicesException {

		String url = SmbTestParams.EXISTING_SHARE + "nonModifiedFile.txt";
		String permissionsHash = "someHash";
		long size = 10;
		long lastModified = System.currentTimeMillis();

		SmbModificationIndicator modInfo = new SmbModificationIndicator();
		modInfo.setLastModified(lastModified);
		modInfo.setPermissionsHash(permissionsHash);
		modInfo.setSize(size);

		ConnectorSmbDocument document = es.newConnectorSmbDocument(connectorInstance);
		document.setUrl(url);
		document.setPermissionsHash(permissionsHash);
		document.setSize(size);
		document.setLastModified(new LocalDateTime(lastModified));

		when(smbService.getModificationIndicator(anyString())).thenReturn(modInfo);

		es.getRecordServices()
				.update(document.getWrappedRecord());
		es.getRecordServices()
				.flush();

		ConnectorJob job = jobFactory.get(SmbJobCategory.RETRIEVAL, url, "");
		assertThat(job).isInstanceOf(SmbUnmodifiedDocumentRetrievalJob.class);
	}

	@Test
	public void givenAnAcceptedFolderUrlWhenCreatingRetrievalJobThenGetRetrievalJob() {
		ConnectorJob job = jobFactory.get(SmbJobCategory.RETRIEVAL, SmbTestParams.EXISTING_SHARE + SmbTestParams.EXISTING_FOLDER, "");
		assertThat(job).isInstanceOf(SmbNewFolderRetrievalJob.class);
	}

	@Test
	public void givenAnAcceptedUrlWhenCreatingDeleteJobThenGetDeleteJob() {
		ConnectorJob job = jobFactory.get(SmbJobCategory.DELETE, SmbTestParams.EXISTING_SHARE + SmbTestParams.EXISTING_FOLDER, "");
		assertThat(job).isInstanceOf(SmbDeleteJob.class);
	}

	@Test
	public void givenANonAcceptedUrlWhenCreatingDispatchJobThenGetDispatchJob() {
		ConnectorJob job = jobFactory.get(SmbJobCategory.DISPATCH, SmbTestParams.DIFFERENT_SHARE + SmbTestParams.EXISTING_FOLDER, "");
		assertThat(job).isInstanceOf(SmbDeleteJob.class);
	}

	@Test
	public void givenANonAcceptedUrlWhenCreatingRetrievalJobThenGetDeleteJob() {
		ConnectorJob job = jobFactory.get(SmbJobCategory.RETRIEVAL, SmbTestParams.DIFFERENT_SHARE + SmbTestParams.EXISTING_FILE, "");
		assertThat(job).isInstanceOf(SmbDeleteJob.class);
	}

	@Test
	public void givenANonAcceptedUrlWhenCreatingDeleteJobThenGetDeleteJob() {
		ConnectorJob job = jobFactory.get(SmbJobCategory.DELETE, SmbTestParams.DIFFERENT_SHARE + SmbTestParams.EXISTING_FILE, "");
		assertThat(job).isInstanceOf(SmbDeleteJob.class);
	}

	@Test
	public void givenFactoryResetWhenGettingJobForGivenUrlThenGetJobForGivenUrl() {

		ConnectorJob job = jobFactory.get(SmbJobCategory.RETRIEVAL, SmbTestParams.EXISTING_SHARE + SmbTestParams.EXISTING_FILE, "");
		assertThat(job).isNotNull()
				.isNotEqualTo(SmbNullJob.getInstance(connector));
		jobFactory.reset();
		ConnectorJob job2 = jobFactory.get(SmbJobCategory.RETRIEVAL, SmbTestParams.EXISTING_SHARE + SmbTestParams.EXISTING_FILE, "");
		assertThat(job2).isNotEqualTo(SmbNullJob.getInstance(connector));
		assertThat(job2).isInstanceOf(SmbNewDocumentRetrievalJob.class);
	}

	@Test
	public void givenMultipleThreadsWhenGettingDispatchJobThenGetOnlyOneDispatchJob()
			throws InterruptedException, ExecutionException {
		// TODO Benoit. Revalidate if there is a Constellio facility for thread tests.
		ExecutorService executorService = Executors.newFixedThreadPool(5);

		Callable<ConnectorJob> callable = new Callable<ConnectorJob>() {
			@Override
			public ConnectorJob call()
					throws Exception {
				return jobFactory.get(SmbJobCategory.DISPATCH, SmbTestParams.EXISTING_SHARE, "");
			}
		};

		List<Callable<ConnectorJob>> callables = Collections.nCopies(5, callable);

		List<Future<ConnectorJob>> results = executorService.invokeAll(callables);

		executorService.awaitTermination(1, TimeUnit.SECONDS);

		int nullJobsCount = 0;
		int dispatchJobsCount = 0;
		for (Future<ConnectorJob> future : results) {
			ConnectorJob job = future.get();

			if (job instanceof SmbNullJob) {
				nullJobsCount++;
			}

			if (job instanceof SmbDispatchJob) {
				dispatchJobsCount++;
			}
		}

		assertThat(dispatchJobsCount).isEqualTo(1);
		assertThat(nullJobsCount).isEqualTo(4);
	}

	@Test
	public void givenUrlLowerThanResumeUrlWhenCreatingDispatchJobThenGetResumeIgnoreJob() {
		jobFactory.updateResumeUrl(SmbTestParams.EXISTING_SHARE + SmbTestParams.EXISTING_FILE);
		ConnectorJob job = jobFactory.get(SmbJobCategory.DISPATCH, SmbTestParams.EXISTING_SHARE, "");
		assertThat(job).isInstanceOf(SmbResumeIgnoreJob.class);
	}

	@Test
	public void whenUpdatingResumeUrlWithNullThenStillGetJob() {
		jobFactory.updateResumeUrl(null);

		ConnectorJob job = jobFactory.get(SmbJobCategory.RETRIEVAL, SmbTestParams.EXISTING_SHARE + SmbTestParams.EXISTING_FILE, "");
		assertThat(job).isInstanceOf(SmbNewDocumentRetrievalJob.class);
	}

	@Test
	public void givenAnAcceptedUrlWhenCreatingSeedJobThenGetSeedJob() {
		ConnectorJob job = jobFactory.get(SmbJobCategory.SEED, SmbTestParams.EXISTING_SHARE, "");
		assertThat(job).isInstanceOf(SmbSeedJob.class);
	}

	@Test
	public void whenResumingThenDoNotCreateDeleteJobForSkippedRecords() {
		jobFactory.updateResumeUrl(SmbTestParams.EXISTING_SHARE + SmbTestParams.EXISTING_FOLDER);
		ConnectorJob job = jobFactory.get(SmbJobCategory.DELETE, SmbTestParams.EXISTING_SHARE, "");
		assertThat(job).isInstanceOf(SmbNullJob.class);
	}

	@Test
	@InDevelopmentTest
	@Ignore
	public void givenMultipleThreadsWhenResettingJobFactoryThenSystemStaysCoherent()
			throws InterruptedException, ExecutionException {
		// TODO Benoit. Test to make sure the system stays coherent.
		// Case is pending dispatch job executed after smb job factory is reset.

		fail("To implement!");

		// ConnectorJob initialJob = jobFactory.get(SmbJobType.DISPATCH, SmbTestParams.EXISTING_SHARE);
		//
		// // TODO Benoit. Revalidate if there is a Constellio facility for thread tests.
		// ExecutorService executorService = Executors.newFixedThreadPool(5);
		//
		// Callable<ConnectorJob> callable = new Callable<ConnectorJob>() {
		// @Override
		// public ConnectorJob call()
		// throws Exception {
		// return jobFactory.get(SmbJobType.DISPATCH, SmbTestParams.EXISTING_SHARE);
		// }
		// };
		//
		// Callable<ConnectorJob> resetGet = new Callable<ConnectorJob>() {
		// @Override
		// public ConnectorJob call()
		// throws Exception {
		// jobFactory.reset();
		// return jobFactory.get(SmbJobType.DELETE, SmbTestParams.EXISTING_SHARE);
		// }
		// };
		//
		// List<Callable<ConnectorJob>> callables = new ArrayList<>();
		// callables.addAll(Collections.nCopies(2, callable));
		// callables.add(resetGet);
		// callables.addAll(Collections.nCopies(2, callable));
		//
		// List<Future<ConnectorJob>> results = executorService.invokeAll(callables);
		// executorService.awaitTermination(1, TimeUnit.SECONDS);
		//
		// int nullJobsCount = 0;
		// int deleteJobsCount = 0;
		// int dispatchJobsCount = 0;
		// for (Future<ConnectorJob> future : results) {
		// ConnectorJob job = future.get();
		//
		// if (job instanceof SmbDeleteJob) {
		// deleteJobsCount++;
		// }
		//
		// if (job instanceof SmbDispatchJob) {
		// dispatchJobsCount++;
		// }
		//
		// if (job instanceof SmbNullJob) {
		// nullJobsCount++;
		// }
		//
		// }
		//
		// assertThat(deleteJobsCount).isEqualTo(1);
		// assertThat(dispatchJobsCount).isEqualTo(0);
		// assertThat(nullJobsCount).isEqualTo(4);
	}

	@After
	public void after() {
		eventObserver.close();
	}
}
