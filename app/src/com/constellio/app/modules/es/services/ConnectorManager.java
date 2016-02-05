package com.constellio.app.modules.es.services;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.isFalse;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.where;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.solr.common.params.ModifiableSolrParams;
import org.joda.time.Duration;

import com.constellio.app.modules.es.connectors.ConnectorUtilsServices;
import com.constellio.app.modules.es.connectors.spi.Connector;
import com.constellio.app.modules.es.connectors.spi.ConnectorEventObserver;
import com.constellio.app.modules.es.connectors.spi.ConnectorInstanciator;
import com.constellio.app.modules.es.connectors.spi.ConnectorLogger;
import com.constellio.app.modules.es.connectors.spi.ConsoleConnectorLogger;
import com.constellio.app.modules.es.model.connectors.ConnectorDocument;
import com.constellio.app.modules.es.model.connectors.ConnectorInstance;
import com.constellio.app.modules.es.model.connectors.RegisteredConnector;
import com.constellio.app.modules.es.services.crawler.ConnectorCrawler;
import com.constellio.app.modules.es.services.crawler.ConnectorManagerRuntimeException.ConnectorManagerRuntimeException_CrawlerCannotBeChangedAfterItIsStarted;
import com.constellio.app.modules.es.services.crawler.DefaultConnectorEventObserver;
import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.data.dao.dto.records.TransactionDTO;
import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.dao.services.bigVault.RecordDaoException.OptimisticLocking;
import com.constellio.data.dao.services.idGenerator.UUIDV1Generator;
import com.constellio.data.dao.services.records.RecordDao;
import com.constellio.data.threads.BackgroundThreadConfiguration;
import com.constellio.data.threads.BackgroundThreadExceptionHandling;
import com.constellio.data.threads.BackgroundThreadsManager;
import com.constellio.data.utils.Factory;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.MetadataSchemasManagerException.OptimistickLocking;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

public class ConnectorManager implements StatefulService {

	public static final String ID = "connectorManager";

	RecordServices recordServices;

	MetadataSchemasManager metadataSchemasManager;

	ESSchemasRecordsServices es;

	ConnectorCrawler crawler;

	ConnectorInstanciator connectorInstanciator;

	boolean paused;

	boolean inParallel = true;

	private final List<RegisteredConnector> registeredConnectors = new ArrayList<>();

	public ConnectorManager(ESSchemasRecordsServices es) {
		this.recordServices = es.getModelLayerFactory().newRecordServices();
		this.metadataSchemasManager = es.getModelLayerFactory().getMetadataSchemasManager();
		this.es = es;
		this.connectorInstanciator = es;
	}

	public void setCrawler(ConnectorCrawler crawler) {
		if (this.crawler != null) {
			throw new ConnectorManagerRuntimeException_CrawlerCannotBeChangedAfterItIsStarted();
		}
		this.crawler = crawler;
	}

	public void setCrawlerInParallel(boolean parallel) {
		if (crawler != null) {
			throw new ConnectorManagerRuntimeException_CrawlerCannotBeChangedAfterItIsStarted();
		}
		this.inParallel = parallel;
	}

	@Override
	public void initialize() {
		BackgroundThreadsManager backgroundThreadsManager = es.getModelLayerFactory().getDataLayerFactory()
				.getBackgroundThreadsManager();

		Runnable crawlAction = new Runnable() {
			@Override
			public void run() {
				if (!paused) {
					getCrawler().crawlUntil(new Factory<Boolean>() {
						@Override
						public Boolean get() {
							return paused;
						}
					});
				}
			}
		};

		//May takes up to 5 seconds to resume the connector manager after it was paused
		backgroundThreadsManager.configure(BackgroundThreadConfiguration
				.repeatingAction("ConnectorManagerCrawler", crawlAction)
				.handlingExceptionWith(BackgroundThreadExceptionHandling.CONTINUE)
				.executedEvery(Duration.standardSeconds(5)));
	}

	@Override
	public void close() {
		if (crawler != null) {
			crawler.shutdown();
		}
	}

	public synchronized void pauseConnectorManager() {
		paused = true;
	}

	public synchronized void resumeConnectorManager() {
		paused = false;
	}

	public void save(ConnectorInstance connectorInstance) {

		try {
			recordServices.add(connectorInstance);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
	}

	public void restartAllConnectorTraversals() {
		for (ConnectorInstance connectorInstance : getConnectorInstances()) {
			save(connectorInstance.setTraversalCode(null));
		}
	}

	public void restartConnectorTraversal(String connectorId) {
		ConnectorInstance connectorInstance = es.getConnectorInstance(connectorId);
		save(connectorInstance.setTraversalCode(null).setEnabled(true));
	}

	public List<ConnectorInstance> getConnectorInstances() {
		return es.searchConnectorInstances(where(Schemas.LOGICALLY_DELETED_STATUS).isFalseOrNull());
	}

	public ConnectorInstance getConnectorInstance(String connectorId) {
		return es.getConnectorInstance(connectorId);
	}

	public <T extends ConnectorInstance> T createConnector(T connectorInstance) {

		try {
			recordServices.add(connectorInstance);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}

		String schema = connectorInstance.getDocumentsCustomSchemaCode();

		MetadataSchemaTypesBuilder typesBuilder = metadataSchemasManager.modify(connectorInstance.getCollection());

		Connector connector = instanciate(connectorInstance);

		for (String schemaType : connector.getConnectorDocumentTypes()) {
			typesBuilder.getSchemaType(schemaType).createCustomSchema(schema).setLabel(connectorInstance.getTitle());
		}

		try {
			metadataSchemasManager.saveUpdateSchemaTypes(typesBuilder);
		} catch (OptimistickLocking optimistickLocking) {
			throw new RuntimeException(optimistickLocking);
		}

		return connectorInstance;
	}

	public <T extends ConnectorInstance> Connector instanciate(T connectorInstance) {
		return connectorInstanciator.instanciate(connectorInstance);
	}

	public synchronized ConnectorCrawler getCrawler() {
		if (crawler == null) {
			ConnectorLogger connectorLogger = new ConsoleConnectorLogger();
			String resourceName = "crawlerObserver-" + UUIDV1Generator.newRandomId() + "-" + es.getCollection();
			System.out.println("Starting crawler '" + resourceName + "'");
			ConnectorEventObserver connectorEventObserver = new DefaultConnectorEventObserver(es, connectorLogger, resourceName);
			if (inParallel) {
				this.crawler = ConnectorCrawler.runningJobsInParallel(es, connectorLogger, connectorEventObserver);
			} else {
				this.crawler = ConnectorCrawler.runningJobsSequentially(es, connectorLogger, connectorEventObserver);
			}
		}
		return crawler;
	}

	public long getFetchedDocumentsCount(String connectorId) {
		ConnectorInstance<?> connectorInstance = es.getConnectorInstance(connectorId);
		return es.getSearchServices().getResultsCount(es.fromAllFetchedDocumentsOf(connectorId)
				.andWhere(es.connectorDocument.searchable()).isNot(isFalse()));
	}

	public List<ConnectorDocument<?>> getLastFetchedDocuments(String connectorId, int qty) {
		ConnectorInstance<?> connectorInstance = es.getConnectorInstance(connectorId);

		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(es.fromAllFetchedDocumentsOf(connectorId)
				.andWhere(es.connectorDocument.traversalCode()).isEqualTo(connectorInstance.getTraversalCode()));
		query.setNumberOfRows(qty);
		query.sortDesc(es.connectorDocument.modifiedOn());

		return es.wrapConnectorDocuments(es.getSearchServices().search(query));

	}

	public void updateUserTokens(User user) {
		List<String> newUserTokens = new ArrayList<>();
		for (ConnectorInstance connectorInstance : getConnectorInstances()) {
			Connector connector = instanciate(connectorInstance);
			List<String> connectorTokens = connector.fetchTokens(user.getUsername());
			newUserTokens.addAll(connectorTokens);
		}
		try {
			user.setManualTokens(newUserTokens);
			recordServices.update(user);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
	}

	private boolean isConnectorToken(String connectorId, String token) {
		return token.startsWith("r" + connectorId) ||
				token.startsWith("w" + connectorId) ||
				token.startsWith("d" + connectorId);
	}

	public void delete(ConnectorInstance<?> instance) {
		recordServices.logicallyDelete(instance.getWrappedRecord(), User.GOD);
		recordServices.physicallyDelete(instance.getWrappedRecord(), User.GOD);
	}

	public void setConnectorInstanciator(ConnectorInstanciator connectorInstanciator) {
		this.connectorInstanciator = connectorInstanciator;
	}

	public void totallyDeleteConnectorRecordsSkippingValidation(RecordDao recordDao,
			ConnectorInstance connectorInstance) {
		stopConnectorAndWaitUntilStopped(connectorInstance);
		ModifiableSolrParams params = new ModifiableSolrParams();
		params.set("q", "connectorId_s:" + connectorInstance.getId());

		try {
			recordDao.execute(new TransactionDTO(RecordsFlushing.NOW).withDeletedByQueries(params));
			Connector connector = instanciate(connectorInstance);
			connector.initialize(new ConsoleConnectorLogger(), connectorInstance.getWrappedRecord(), null, es);
			connector.onAllDocumentsDeleted();
			connectorInstance.setTraversalCode(null);
			recordServices.update(connectorInstance.getWrappedRecord());
		} catch (OptimisticLocking optimisticLocking) {
			throw new RuntimeException(optimisticLocking);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}

	}

	public void stopConnectorAndWaitUntilStopped(ConnectorInstance connectorInstance) {
		if (connectorInstance.isEnabled()) {
			// TODO Francis replace sleep with proper waiting
			connectorInstance.setEnabled(false);
			try {
				recordServices.update(connectorInstance.getWrappedRecord());
				Thread.sleep(30000);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			} catch (RecordServicesException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public List<RegisteredConnector> getRegisteredConnectors() {
		return Collections.unmodifiableList(registeredConnectors);
	}

	public ConnectorManager register(String connectorTypeCode, String connectorInstanceSchemaCode, ConnectorUtilsServices services) {
		registeredConnectors.add(new RegisteredConnector(connectorTypeCode, connectorInstanceSchemaCode, services));
		return this;
	}
}
