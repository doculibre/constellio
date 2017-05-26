package com.constellio.app.modules.es.connectors.smb;

import com.constellio.app.modules.es.connectors.http.ConnectorHttpContext;
import com.constellio.app.modules.es.connectors.smb.ConnectorSmbRuntimeException.ConnectorSmbRuntimeException_CannotDelete;
import com.constellio.app.modules.es.connectors.smb.cache.SmbConnectorContext;
import com.constellio.app.modules.es.connectors.smb.cache.SmbConnectorContextServices;
import com.constellio.app.modules.es.connectors.smb.config.SmbRetrievalConfiguration;
import com.constellio.app.modules.es.connectors.smb.config.SmbSchemaDisplayConfiguration;
import com.constellio.app.modules.es.connectors.smb.jobmanagement.SmbConnectorJob;
import com.constellio.app.modules.es.connectors.smb.jobmanagement.SmbDocumentOrFolderUpdater;
import com.constellio.app.modules.es.connectors.smb.jobmanagement.SmbJobFactory;
import com.constellio.app.modules.es.connectors.smb.jobmanagement.SmbJobFactoryImpl;
import com.constellio.app.modules.es.connectors.smb.jobmanagement.SmbJobFactoryImpl.SmbJobCategory;
import com.constellio.app.modules.es.connectors.smb.jobs.SmbNullJob;
import com.constellio.app.modules.es.connectors.smb.queue.SmbJobQueue;
import com.constellio.app.modules.es.connectors.smb.queue.SmbJobQueueSortedImpl;
import com.constellio.app.modules.es.connectors.smb.security.Credentials;
import com.constellio.app.modules.es.connectors.smb.service.*;
import com.constellio.app.modules.es.connectors.smb.utils.ConnectorSmbUtils;
import com.constellio.app.modules.es.connectors.smb.utils.SmbUrlComparator;
import com.constellio.app.modules.es.connectors.spi.Connector;
import com.constellio.app.modules.es.connectors.spi.ConnectorJob;
import com.constellio.app.modules.es.model.connectors.ConnectorDocument;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbDocument;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbFolder;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbInstance;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.app.modules.es.ui.pages.ConnectorReportView;
import com.constellio.data.dao.managers.config.ConfigManagerRuntimeException;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.records.RecordServicesException;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import org.joda.time.DateTime;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;

import static java.util.Arrays.asList;

public class ConnectorSmb extends Connector {

	static {
		System.setProperty("jcifs.smb.client.soTimeout","150000");
		System.setProperty("jcifs.smb.client.responseTimeout","120000");
		System.setProperty("jcifs.resolveOrder","LMHOSTS,DNS,WINS");
		System.setProperty("jcifs.smb.client.listSize","1200");
		System.setProperty("jcifs.smb.client.listCount","15");
		System.setProperty("jcifs.smb.client.dfs.strictView","true");
	}

	static final String START_OF_TRAVERSAL = "Start of traversal";
	static final String RESUME_OF_TRAVERSAL = "Resume of traversal";
	static final String END_OF_TRAVERSAL = "End of traversal";

	private static final int MAX_JOBS_PER_GET_JOBS_CALL = 500;

	private ConnectorSmbInstance connectorInstance;
	private ConnectorSmbUtils smbUtils;
	private SmbSchemaDisplayConfiguration schemaDisplayConfig;
	private SmbJobQueue jobsQueue;
	private SmbShareService smbShareService;
	private SmbJobFactory smbJobFactory;
	private SmbRecordService smbRecordService;
	private SmbDocumentOrFolderUpdater updater;
	private SmbUrlComparator urlComparator;

	private String connectorId;
	private SmbConnectorContext context;
	private SmbConnectorContextServices contextServices;

	private DateTime lastSave;

	public ConnectorSmb() {
		urlComparator = new SmbUrlComparator();
	}

	public ConnectorSmb(SmbShareService smbShareService) {
		this.smbShareService = smbShareService;
		urlComparator = new SmbUrlComparator();
	}

	@Override
	protected void initialize(Record instanceRecord) {
		this.connectorId = instanceRecord.getId();
		this.connectorInstance = getEs().wrapConnectorSmbInstance(instanceRecord);
		this.smbUtils = new ConnectorSmbUtils();
		schemaDisplayConfig = new SmbSchemaDisplayConfiguration(getEs(), connectorInstance);
		schemaDisplayConfig.setupMetadatasDisplay();
		jobsQueue = new SmbJobQueueSortedImpl();

		Credentials credentials = new Credentials(connectorInstance.getDomain(), connectorInstance.getUsername(),
				connectorInstance.getPassword());

		SmbRetrievalConfiguration smbRetrievalConfiguration = new SmbRetrievalConfiguration(connectorInstance.getSeeds(),
				connectorInstance.getInclusions(),
				connectorInstance.getExclusions(),
				connectorInstance.isSkipShareAccessControl());

		if (smbShareService == null) {
			smbShareService = new SmbShareServiceSimpleImpl(credentials, smbRetrievalConfiguration, smbUtils, logger, es);
		}
		smbRecordService = new SmbRecordService(es, connectorInstance);
		updater = new SmbDocumentOrFolderUpdater(connectorInstance, smbRecordService);
		contextServices = new SmbConnectorContextServices(es);

		smbJobFactory = new SmbJobFactoryImpl(this, connectorInstance, eventObserver, smbShareService, smbUtils, smbRecordService,
				updater);
	}

	@Override
	public void start() {
		getLogger().info(START_OF_TRAVERSAL, "Current TraversalCode : " + connectorInstance.getTraversalCode(),
				new LinkedHashMap<String, String>());
		try {
			context = contextServices.createContext(connectorId);
		} catch (ConfigManagerRuntimeException.ConfigurationAlreadyExists e) {
			contextServices.deleteContext(connectorId);
			context = contextServices.createContext(connectorId);
		}

		jobsQueue.clear();
		String resumeUrl = connectorInstance.getResumeUrl();
		queueSeeds();
	}

	private void queueSeeds() {
		List<String> sortedSeeds = new ArrayList(connectorInstance.getSeeds());
		Collections.sort(sortedSeeds, urlComparator);
		for (String seed : sortedSeeds) {
			SmbConnectorJob smbDispatchJob = smbJobFactory.get(SmbJobCategory.SEED, seed, "");
			if (!(smbDispatchJob instanceof SmbNullJob)) {
				queueJob(smbDispatchJob);
			}
		}
	}

	@Override
	public void resume() {
		getLogger().info(RESUME_OF_TRAVERSAL, "Current TraversalCode : " + connectorInstance.getTraversalCode(),
				new LinkedHashMap<String, String>());
		context = contextServices.loadContext(connectorId);
		jobsQueue.clear();
		String resumeUrl = connectorInstance.getResumeUrl();
		queueSeeds();
	}

	@Override
	public void stop() {
		try {
			es.getRecordServices()
					.update(connectorInstance.setResumeUrl(""));
		} catch (Exception e) {
			logger.errorUnexpected(e);
		}
	}

	@Override
	public List<String> fetchTokens(String username) {
		// Ne pas modifier cette méthode!
		return new ArrayList<>();
	}

	@Override
	public void afterJobs(List<ConnectorJob> jobs) {
		if (lastSave == null || lastSave.plusMinutes(15).isBeforeNow()) {
			contextServices.save(context);
			lastSave = new DateTime();
		}
	}

	@Override
	public List<String> getReportMetadatas(String reportMode) {
		if (ConnectorReportView.INDEXING.equals(reportMode)) {
			return Arrays.asList(ConnectorSmbDocument.URL, ConnectorSmbDocument.FETCHED_DATETIME);
		} else if (ConnectorReportView.ERRORS.equals(reportMode)) {
			return Arrays.asList(ConnectorSmbDocument.URL, ConnectorSmbDocument.ERROR_CODE, ConnectorSmbDocument.ERROR_MESSAGE,
					ConnectorSmbDocument.FETCHED_DATETIME);
		}
		return new ArrayList<>();
	}

	@Override
	public String getMainConnectorDocumentType() {
		return ConnectorSmbDocument.SCHEMA_TYPE;
	}

	@Override
	public void onAllDocumentsDeleted() {
		// TODO Pat delete config folder for this connector.
	}

	@Override
	public List<ConnectorJob> getJobs() {
		List<ConnectorJob> jobs = new ArrayList<>();

		while (!jobsQueue.isEmpty() && jobs.size() < MAX_JOBS_PER_GET_JOBS_CALL) {
			SmbConnectorJob queuedJob = jobsQueue.poll();
			jobs.add(queuedJob);
		}

		if (jobsQueue.isEmpty() && jobs.isEmpty()) {
			List<String> urlsOfDocumentsToDelete = this.context.staleUrls(this.connectorInstance.getTraversalCode());
			// Delete jobs are currently not limited to MAX_JOBS_PER_GET_JOBS_CALL
			for (String url : urlsOfDocumentsToDelete) {
				ConnectorJob deleteJob = smbJobFactory.get(SmbJobCategory.DELETE, url, "");
				jobs.add(deleteJob);
			}

			changeTraversalCodeToMarkEndOfTraversal();
			queueSeeds();
		}
		return jobs;
	}

	public void queueJob(SmbConnectorJob job) {
		logger.debug("Queueing job : ", job.toString(), new LinkedHashMap<String, String>());
		jobsQueue.add(job);
	}

	private void changeTraversalCodeToMarkEndOfTraversal() {
		String oldTraversalCode = connectorInstance.getTraversalCode();
		String newTraversalCode = UUID.randomUUID()
				.toString();

		connectorInstance.setTraversalCode(newTraversalCode);
		connectorInstance.setResumeUrl("");

		try {
			es.getRecordServices().update(connectorInstance);
		} catch (RecordServicesException e) {
			logger.errorUnexpected(e);
		}

		getLogger().info(END_OF_TRAVERSAL, "Connector instance " + connectorInstance.getId() +
				" Old TraversalCode : \"" + oldTraversalCode + "\" New TraversalCode : \"" + newTraversalCode + "\"",
				new LinkedHashMap<String, String>());
	}

	@Override
	public List<String> getConnectorDocumentTypes() {
		return asList(ConnectorSmbDocument.SCHEMA_TYPE, ConnectorSmbFolder.SCHEMA_TYPE);
	}

	public void setEs(ESSchemasRecordsServices es) {
		this.es = es;
	}

	private ConnectorSmbUtils getSmbUtils() {
		return smbUtils;
	}

	public InputStream getInputStream(ConnectorSmbDocument document, String resourceName) {
		if (Toggle.SIMULATE_CONNECTOR_DOWNLOAD_CONTENT.isEnabled()) {
			String dummyContent = "This is not the content you are looking for";
			return new ByteArrayInputStream(dummyContent.getBytes());
		}

		SmbFile smbFile = getSmbFile(document);
		try {
			InputStream is = smbFile.getInputStream();
			return es.getIOServices().newBufferedInputStream(is, resourceName);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void deleteFile(ConnectorDocument document) {
		SmbFile smbFile = getSmbFile(document);
		try {
			smbFile.delete();
			es.getRecordServices().logicallyDelete(document.getWrappedRecord(), User.GOD);
		} catch (SmbException e) {
			throw new ConnectorSmbRuntimeException_CannotDelete(document, e);
		}
	}

	public boolean exists(ConnectorDocument document) {
		SmbFile smbFile = getSmbFile(document);
		try {
			return smbFile.exists();
		} catch (SmbException e) {
			throw new RuntimeException(e);
		}
	}

	private SmbFile getSmbFile(ConnectorDocument document) {
		SmbFileFactory smbFactory = new SmbFileFactoryImpl();

		String connectorId = document.getConnector();
		if (es == null) {
			throw new IllegalStateException("Must call setEs(es) before getInputStream()");
		}

		ConnectorSmbInstance connectorInstance = es
				.getConnectorSmbInstance(connectorId);

		NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(
				connectorInstance.getDomain(), connectorInstance.getUsername(),
				connectorInstance.getPassword());
		try {
			return smbFactory.getSmbFile(document.getURL(), auth);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	public SmbConnectorContext getContext() {
		return this.context;
	}
}