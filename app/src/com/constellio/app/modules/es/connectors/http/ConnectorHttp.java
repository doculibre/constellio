package com.constellio.app.modules.es.connectors.http;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.auth.AuthScope;

import com.constellio.app.modules.es.connectors.http.fetcher.HttpURLFetchingService;
import com.constellio.app.modules.es.connectors.spi.Connector;
import com.constellio.app.modules.es.connectors.spi.ConnectorJob;
import com.constellio.app.modules.es.model.connectors.AuthenticationScheme;
import com.constellio.app.modules.es.model.connectors.ConnectorDocument;
import com.constellio.app.modules.es.model.connectors.http.ConnectorHttpDocument;
import com.constellio.app.modules.es.model.connectors.http.ConnectorHttpInstance;
import com.constellio.app.modules.es.services.mapping.ConnectorField;
import com.constellio.app.modules.es.ui.pages.ConnectorReportView;
import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.managers.config.ConfigManagerRuntimeException.ConfigurationAlreadyExists;
import com.constellio.data.utils.BatchBuilderIterator;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.gargoylesoftware.htmlunit.DefaultCredentialsProvider;

public class ConnectorHttp extends Connector {

	private ConnectorHttpContext context;

	ConfigManager configManager;

	String connectorId;

	ConnectorHttpContextServices contextServices;

	@Override
	public void initialize(Record instanceRecord) {
		this.connectorId = instanceRecord.getId();
		this.configManager = es.getModelLayerFactory().getDataLayerFactory().getConfigManager();
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
		try {
			context = contextServices.createContext(connectorId);

		} catch (ConfigurationAlreadyExists e) {
			contextServices.deleteContext(connectorId);
			context = contextServices.createContext(connectorId);
		}

		ConnectorHttpInstance connectorInstance = getConnectorInstance();
		List<ConnectorDocument> documents = new ArrayList<>();
		Set<String> urls = new HashSet<>();
		urls.addAll(connectorInstance.getSeedsList());
		urls.removeAll(connectorInstance.getOnDemandsList());
		for (String url : urls) {
			documents.add(newUnfetchedURLDocument(url, 0));
			context.markAsFetched(url);

		}
		eventObserver.addUpdateEvents(documents);
		es.getRecordServices().flush();
	}

	private List<ConnectorHttpDocument> getOnDemandDocuments() {

		List<ConnectorHttpDocument> documents = new ArrayList<>();
		for (String url : getConnectorInstance().getOnDemandsList()) {
			if (context.isNewUrl(url)) {
				documents.add(newUnfetchedURLDocument(url, 0));
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

	}

	@Override
	public void afterJobs(List<ConnectorJob> jobs) {
		contextServices.save(context);
	}

	public void resume() {
		context = contextServices.loadContext(connectorId);

		es.getRecordServices().flush();
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
		String configFolderPath = "connectors/http/" + connectorId + "/";
		ConfigManager configManager = es.getModelLayerFactory().getDataLayerFactory().getConfigManager();
		if (configManager.folderExist(configFolderPath)) {
			configManager.deleteFolder(configFolderPath);
		}
	}

	@Override
	public synchronized List<ConnectorJob> getJobs() {
		ConnectorHttpInstance connectorInstance = getConnectorInstance();
		for (String url : connectorInstance.getSeedsList()) {
			if (context.isNewUrl(url)) {
				try {
					es.getRecordServices().add(newUnfetchedURLDocument(url, 0).getWrappedRecord());
				} catch (RecordServicesException e) {
					throw new RuntimeException(e);
				}
				context.markAsFetched(url);
			}
		}

		es.getRecordServices().refresh(connectorInstance);
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
