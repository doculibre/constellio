package com.constellio.app.modules.es.connectors.smb.jobmanagement;

import com.constellio.app.modules.es.connectors.smb.ConnectorSmb;
import com.constellio.app.modules.es.connectors.smb.cache.SmbConnectorContext;
import com.constellio.app.modules.es.connectors.smb.cache.SmbConnectorContextServices;
import com.constellio.app.modules.es.connectors.smb.jobmanagement.SmbJobFactoryImpl.SmbJobCategory;
import com.constellio.app.modules.es.connectors.smb.jobs.*;
import com.constellio.app.modules.es.connectors.smb.service.SmbModificationIndicator;
import com.constellio.app.modules.es.connectors.smb.service.SmbRecordService;
import com.constellio.app.modules.es.connectors.smb.service.SmbShareService;
import com.constellio.app.modules.es.connectors.smb.testutils.SmbTestParams;
import com.constellio.app.modules.es.connectors.smb.utils.ConnectorSmbUtils;
import com.constellio.app.modules.es.connectors.spi.ConnectorJob;
import com.constellio.app.modules.es.connectors.spi.ConnectorLogger;
import com.constellio.app.modules.es.connectors.spi.ConsoleConnectorLogger;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbDocument;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbFolder;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbInstance;
import com.constellio.app.modules.es.sdk.TestConnectorEventObserver;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.app.modules.es.services.crawler.DefaultConnectorEventObserver;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.sdk.tests.ConstellioTest;
import org.joda.time.LocalDateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

public class SmbJobFactoryImplAcceptanceTest extends ConstellioTest {
	@Mock private ConnectorSmb connector;
	private ESSchemasRecordsServices es;
	private ConnectorSmbInstance connectorInstance;
	@Mock private SmbShareService smbService;
	private ConnectorLogger logger;
	private TestConnectorEventObserver eventObserver;
	private ConnectorSmbUtils smbUtils;
	private SmbRecordService smbRecordService;
	private SmbDocumentOrFolderUpdater updater;
	private SmbJobFactory jobFactory;
	private SmbConnectorContext context;

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

		SmbConnectorContextServices contextServices = new SmbConnectorContextServices(es);
		context = contextServices.createContext(connectorInstance.getId());

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
	public void givenAnAcceptedModifiedDocumentUrlWhenCreatingRetrievalJobThenGetRetrievalJob()
			throws RecordServicesException {
		String url = SmbTestParams.EXISTING_SHARE + "modifiedFile.txt";
		String permissionsHash = "someHash";
		long size = 10;
		long lastModified = System.currentTimeMillis();

		SmbModificationIndicator modInfo = new SmbModificationIndicator(permissionsHash, 15D, lastModified);

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

		assertThat(job).isInstanceOf(SmbNewDocumentRetrievalJob.class);
	}

	@Test
	public void givenAnAcceptedNonModifiedDocumentUrlWhenCreatingRetrievalJobThenGetRetrievalJob()
			throws RecordServicesException {

		ConnectorSmbFolder folder = es.newConnectorSmbFolder(connectorInstance);
		folder.setUrl(SmbTestParams.EXISTING_SHARE);

		es.getRecordServices()
				.update(folder.getWrappedRecord());
		es.getRecordServices()
				.flush();

		String url = SmbTestParams.EXISTING_SHARE + "nonModifiedFile.txt";
		String permissionsHash = "someHash";
		long size = 10;
		long lastModified = System.currentTimeMillis();

		SmbModificationIndicator modInfo = new SmbModificationIndicator(permissionsHash, 10, lastModified);
		modInfo.setParentId(folder.getId());

		ConnectorSmbDocument document = es.newConnectorSmbDocument(connectorInstance);
		document.setUrl(url);
		document.setPermissionsHash(permissionsHash);
		document.setSize(size);
		document.setLastModified(new LocalDateTime(lastModified));
		document.setParent(folder.getId());

		when(smbService.getModificationIndicator(anyString())).thenReturn(modInfo);

		es.getRecordServices()
				.update(document.getWrappedRecord());
		es.getRecordServices()
				.flush();

		context.traverseModified(url, modInfo, folder.getUrl(), null);

		ConnectorJob job = jobFactory.get(SmbJobCategory.RETRIEVAL, url, folder.getUrl());
		assertThat(job).isInstanceOf(SmbUnmodifiedRetrievalJob.class);
	}

	@Test
	public void givenNonModifiedDocumentWithoutParentWhenCreatingRetrievalJobThenGetRetrievalJob()
			throws RecordServicesException {

		String url = SmbTestParams.EXISTING_SHARE + "nonModifiedFile.txt";
		String permissionsHash = "someHash";
		long size = 10;
		long lastModified = System.currentTimeMillis();

		SmbModificationIndicator modInfo = new SmbModificationIndicator("someHash", 10D, System.currentTimeMillis());

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
		assertThat(job).isInstanceOf(SmbNewDocumentRetrievalJob.class);
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
				.isNotInstanceOf(SmbNullJob.class);
		ConnectorJob job2 = jobFactory.get(SmbJobCategory.RETRIEVAL, SmbTestParams.EXISTING_SHARE + SmbTestParams.EXISTING_FILE, "");
		assertThat(job2).isNotInstanceOf(SmbNullJob.class);
		assertThat(job2).isInstanceOf(SmbNewDocumentRetrievalJob.class);
	}

	@Test
	public void givenAnAcceptedUrlWhenCreatingSeedJobThenGetSeedJob() {
		ConnectorJob job = jobFactory.get(SmbJobCategory.SEED, SmbTestParams.EXISTING_SHARE, "");
		assertThat(job).isInstanceOf(SmbSeedJob.class);
	}

	@After
	public void after() {
		eventObserver.close();
	}
}
