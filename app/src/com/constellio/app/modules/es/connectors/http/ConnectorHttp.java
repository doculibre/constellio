package com.constellio.app.modules.es.connectors.http;

import com.constellio.app.modules.es.connectors.http.fetcher.HttpURLFetchingService;
import com.constellio.app.modules.es.connectors.spi.Connector;
import com.constellio.app.modules.es.connectors.spi.ConnectorJob;
import com.constellio.app.modules.es.model.connectors.AuthenticationScheme;
import com.constellio.app.modules.es.model.connectors.ConnectorDocument;
import com.constellio.app.modules.es.model.connectors.http.ConnectorHttpDocument;
import com.constellio.app.modules.es.model.connectors.http.ConnectorHttpInstance;
import com.constellio.app.modules.es.services.mapping.ConnectorField;
import com.constellio.app.modules.es.ui.pages.ConnectorReportView;
import com.constellio.data.utils.BatchBuilderIterator;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.gargoylesoftware.htmlunit.DefaultCredentialsProvider;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.auth.AuthScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;

public class ConnectorHttp extends Connector {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConnectorHttp.class);

	private ConnectorHttpContext context;

	private long lastVersion = -1;

	String connectorId;

	ConnectorHttpContextServices contextServices;

	@Override
	public void initialize(Record instanceRecord) {
		this.connectorId = instanceRecord.getId();
		this.contextServices = new ConnectorHttpContextServices(es);
	}

	public HttpURLFetchingService newFetchingService() {
		int timeout = 60_000;
		ConnectorHttpInstance connectorInstance = getConnectorInstance();
		DefaultCredentialsProvider credentialProvider = new DefaultCredentialsProvider();
		AuthScope authScope = new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT);
		if (AuthenticationScheme.NTLM.equals(connectorInstance.getAuthenticationScheme())) {
			String username = connectorInstance.getUsername();
			String password = connectorInstance.getPasssword();
			String domain = connectorInstance.getDomain();

			//FIXME
			//Credentials credentials = new NTCredentials(username, password, "contellio", domain);
			credentialProvider.addNTLMCredentials(username, password, null, -1, null, domain);
		}
		return new HttpURLFetchingService(timeout, credentialProvider);
	}

	@Override
	public List<String> fetchTokens(String username) {
		return new ArrayList<>();
	}

	@Override
	public List<String> getConnectorDocumentTypes() {
		return asList(ConnectorHttpDocument.SCHEMA_TYPE);
	}

	public ConnectorHttpDocument newUnfetchedURLDocument(String url, int level) {
		return getEs().newConnectorHttpDocument(getConnectorInstance()).setURL(url).setFetched(false).setSearchable(false)
				.setLevel(level);
	}

	private ConnectorHttpInstance getConnectorInstance() {
		return es.getConnectorHttpInstance(connectorId);
	}

	public void start() {
		context = contextServices.createContext(connectorId);

		ConnectorHttpInstance connectorInstance = getConnectorInstance();
		List<ConnectorDocument> documents = new ArrayList<>();
		Set<String> urls = new HashSet<>();
		urls.addAll(connectorInstance.getSeedsList());
		urls.removeAll(connectorInstance.getOnDemandsList());
		for (String url : urls) {
			ConnectorHttpDocument httpDocument = newUnfetchedURLDocument(url, 0);
			httpDocument.setInlinks(Arrays.asList(url));
			documents.add(httpDocument);
			context.markAsFetched(url);

		}
		eventObserver.addUpdateEvents(documents);
		es.getRecordServices().flush();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	private List<ConnectorHttpDocument> getOnDemandDocuments() {

		List<ConnectorHttpDocument> documents = new ArrayList<>();
		for (String url : getConnectorInstance().getOnDemandsList()) {
			if (context.isNewUrl(url)) {
				ConnectorHttpDocument httpDocument = newUnfetchedURLDocument(url, 0);
				httpDocument.setInlinks(Arrays.asList(url));
				documents.add(httpDocument);
				context.markAsFetched(url);
			} else {
				documents.add(es.getConnectorHttpDocumentByUrl(url));
			}
		}
		return documents;
	}

	private void removeOnDemandUrls() {
		ConnectorHttpInstance connectorInstance = getConnectorInstance();
		if (StringUtils.isNotBlank(connectorInstance.getOnDemands())) {
			connectorInstance.setOnDemands(null);
			try {
				es.getRecordServices().update(connectorInstance);
			} catch (RecordServicesException e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public void stop() {
		LOGGER.info("Stopping connector on instance '" + connectorId + "'");
	}

	@Override
	public void afterJobs(List<ConnectorJob> jobs) {
		if (context.getVersion() != lastVersion) {
			LOGGER.info("Saving context on instance '" + connectorId + "'");
			contextServices.save(context);
			lastVersion = context.getVersion();
		}
	}

	public void resume() {

		//		if (es.getModelLayerFactory().getDataLayerFactory().isDistributed()) {
		//			LOGGER.warn("Loading context of connector " + connectorId + " from solr...");
		//			loadFromSolr();
		//
		//		} else {

		LOGGER.info("Resuming connector on instance '" + connectorId + "'");
		try {
			context = contextServices.loadContext(connectorId);

		} catch (Exception e) {
			LOGGER.warn("Context of connector " + connectorId + " does not existe, recreating it...");

			loadFromSolr();

		}
		//		}
	}

	protected void loadFromSolr() {
		context = contextServices.createContext(connectorId);

		Iterator<Record> iterator = es.getModelLayerFactory().newSearchServices().recordsIterator(
				from(es.connectorHttpDocument.schemaType()).where(es.connectorHttpDocument.connector()).isEqualTo(connectorId), 5000);

		while (iterator.hasNext()) {
			ConnectorHttpDocument connectorHttpDocument = es.wrapConnectorHttpDocument(iterator.next());
			context.addDocumentDigest(connectorHttpDocument.getDigest(), connectorHttpDocument.getURL());
		}

		contextServices.save(context);
	}

	@Override
	public List<String> getReportMetadatas(String reportMode) {
		if (ConnectorReportView.INDEXING.equals(reportMode)) {
			return Arrays.asList(ConnectorHttpDocument.URL, ConnectorHttpDocument.DOWNLOAD_TIME,
					ConnectorHttpDocument.FETCHED_DATETIME);
		} else if (ConnectorReportView.ERRORS.equals(reportMode)) {
			return Arrays.asList(ConnectorHttpDocument.URL, ConnectorHttpDocument.ERROR_CODE, ConnectorHttpDocument.ERROR_MESSAGE,
					ConnectorHttpDocument.FETCHED_DATETIME, ConnectorHttpDocument.COPY_OF);
		}
		return new ArrayList<>();
	}

	@Override
	public String getMainConnectorDocumentType() {
		return ConnectorHttpDocument.SCHEMA_TYPE;
	}

	@Override
	public void onAllDocumentsDeleted() {
		contextServices.deleteContext(connectorId);
	}

	@Override
	public synchronized List<ConnectorJob> getJobs() {
		ConnectorHttpInstance connectorInstance = getConnectorInstance();
		for (String url : connectorInstance.getSeedsList()) {
			if (context.isNewUrl(url)) {
				try {
					ConnectorHttpDocument connectorHttpDocument = newUnfetchedURLDocument(url, 0);
					connectorHttpDocument.setInlinks(Arrays.asList(url));

					es.getRecordServices().add(connectorHttpDocument.getWrappedRecord());
				} catch (RecordServicesException e) {
					throw new RuntimeException(e);
				}
				context.markAsFetched(url);
			}
		}

		es.getRecordServices().refreshUsingCache(connectorInstance);
		List<ConnectorHttpDocument> onDemands = getOnDemandDocuments();

		LogicalSearchQuery query = es.connectorDocumentsToFetchQuery(connectorInstance);

		query.clearSort();
		query.sortAsc(es.connectorHttpDocument.fetched());
		query.sortAsc(es.connectorHttpDocument.level());
		query.setNumberOfRows(connectorInstance.getNumberOfJobsInParallel() * connectorInstance.getDocumentsPerJobs());

		List<ConnectorJob> jobs = new ArrayList<>();
		List<ConnectorHttpDocument> documentsToFetch = new ArrayList<>();
		documentsToFetch.addAll(onDemands);
		documentsToFetch.addAll(es.searchConnectorHttpDocuments(query));
		Iterator<List<ConnectorHttpDocument>> documentBatchsIterator = new BatchBuilderIterator<>(documentsToFetch.iterator(),
				connectorInstance.getDocumentsPerJobs());

		while (documentBatchsIterator.hasNext()) {
			jobs.add(new ConnectorHttpFetchJob(this, connectorInstance, documentBatchsIterator.next(), context, logger));
		}

		if (!onDemands.isEmpty()) {
			removeOnDemandUrls();
		}
		return jobs;
	}

	@Override
	public List<ConnectorField> getDefaultConnectorFields() {
		List<ConnectorField> defaultConnectorFields = new ArrayList<>();
		defaultConnectorFields.add(new ConnectorField());
		return defaultConnectorFields;
	}
}
