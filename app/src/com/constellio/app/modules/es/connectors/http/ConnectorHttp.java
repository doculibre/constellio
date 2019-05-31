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
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataListFilter;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.gargoylesoftware.htmlunit.DefaultCredentialsProvider;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.auth.AuthScope;
import org.jetbrains.annotations.NotNull;
import org.apache.solr.client.solrj.SolrServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;

public class ConnectorHttp extends Connector {


	private static final Logger LOGGER = LoggerFactory.getLogger(ConnectorHttp.class);

	private ConnectorHttpDocumentURLCache cache;

	//private ConnectorHttpContext context;

	private long lastVersion = -1;

	String connectorId;

	//ConnectorHttpContextServices contextServices;


	@Override
	public void initialize(Record instanceRecord) {
		this.connectorId = instanceRecord.getId();
		cache = new ConnectorHttpDocumentURLCache(es.wrapConnectorInstance(instanceRecord), es.getAppLayerFactory());
		es.getModelLayerFactory().getCachesManager().register(instanceRecord.getCollection(), connectorId, cache);

	}

	public HttpURLFetchingService newFetchingService() {
		int timeout = 60_000;
		ConnectorHttpInstance connectorInstance = getConnectorInstance();

		return getHttpURLFetchingService(timeout, connectorInstance);
	}

	@NotNull
	public static HttpURLFetchingService getHttpURLFetchingService(int timeout, ConnectorHttpInstance connectorInstance) {
		AuthScope authScope = new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT);
		DefaultCredentialsProvider credentialProvider = new DefaultCredentialsProvider();
		if (AuthenticationScheme.NTLM.equals(connectorInstance.getAuthenticationScheme())) {
			String username = connectorInstance.getUsername();
			String password = connectorInstance.getPasssword();
			String domain = connectorInstance.getDomain();

			//FIXME
			//Credentials credentials = new NTCredentials(username, password, "contellio", domain);
			credentialProvider.addNTLMCredentials(username, password, null, -1, null, domain);
		} else {
			if (connectorInstance.getUsername() != null && connectorInstance.getPasssword() != null) {
				credentialProvider.addCredentials(connectorInstance.getUsername(), connectorInstance.getPasssword());
			}
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
		//context = contextServices.createContext(connectorId);
		cache.onConnectorStart();

		ConnectorHttpInstance connectorInstance = getConnectorInstance();
		List<ConnectorDocument> documents = new ArrayList<>();
		Set<String> urls = new HashSet<>();
		urls.addAll(connectorInstance.getSeedsList());
		urls.removeAll(connectorInstance.getOnDemandsList());

		for (String url : urls) {
			if (!cache.exists(url)) {
				ConnectorHttpDocument httpDocument = newUnfetchedURLDocument(url, 0);
				httpDocument.setInlinks(Arrays.asList(url));
				documents.add(httpDocument);
				//context.markAsFetched(url);
			}

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
			if (!cache.exists(url)) {
				if (cache.tryLockingDocumentForFetching(url)) {
					ConnectorHttpDocument httpDocument = newUnfetchedURLDocument(url, 0);
					httpDocument.setInlinks(Arrays.asList(url));
					documents.add(httpDocument);
				}
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
		cache.onConnectorStop();
	}

	@Override
	public void afterJobs(List<ConnectorJob> jobs) {
	}

	public void resume() {
		LOGGER.info("Resuming connector on instance '" + connectorId + "'");
		cache.onConnectorResume();

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
		cache.onAllDocumentsDeleted();
	}

	@Override
	public synchronized List<ConnectorJob> getJobs() {
		try {
			es.getModelLayerFactory().getDataLayerFactory().getRecordsVaultServer().flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (SolrServerException e) {
			throw new RuntimeException(e);
		}
		cache.onConnectorGetJobsCalled();
		ConnectorHttpInstance connectorInstance = getConnectorInstance();
		for (String url : connectorInstance.getSeedsList()) {

			if (!cache.exists(url) && cache.tryLockingDocumentForFetching(url)) {
				try {
					ConnectorHttpDocument connectorHttpDocument = newUnfetchedURLDocument(url, 0);
					connectorHttpDocument.setInlinks(Arrays.asList(url));

					es.getRecordServices().add(connectorHttpDocument.getWrappedRecord());
				} catch (RecordServicesException e) {
					throw new RuntimeException(e);
				}
			}
		}

		es.getRecordServices().refreshUsingCache(connectorInstance);
		List<ConnectorHttpDocument> onDemands = getOnDemandDocuments();

		LogicalSearchQuery query = es.connectorDocumentsToFetchQuery(connectorInstance);

		List<Metadata> metadatas = es.connectorHttpDocument.schema().getMetadatas().only(new MetadataListFilter() {
			@Override
			public boolean isReturned(Metadata metadata) {
				return !metadata.getLocalCode().equals(ConnectorHttpDocument.PARSED_CONTENT)
					   && !metadata.getLocalCode().equals(ConnectorHttpDocument.DESCRIPTION)
					   && !metadata.getLocalCode().equals(ConnectorHttpDocument.THESAURUS_MATCH);
			}
		});
		query.setReturnedMetadatas(ReturnedMetadatasFilter.onlyMetadatas(metadatas));

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
			jobs.add(new ConnectorHttpFetchJob(this, connectorInstance, documentBatchsIterator.next(), cache, logger));
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
