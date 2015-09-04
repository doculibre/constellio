/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.modules.es.services;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.where;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.Duration;

import com.constellio.app.modules.es.connectors.spi.Connector;
import com.constellio.app.modules.es.connectors.spi.ConnectorEventObserver;
import com.constellio.app.modules.es.connectors.spi.ConnectorLogger;
import com.constellio.app.modules.es.connectors.spi.ConsoleConnectorLogger;
import com.constellio.app.modules.es.model.connectors.ConnectorDocument;
import com.constellio.app.modules.es.model.connectors.ConnectorInstance;
import com.constellio.app.modules.es.services.crawler.ConnectorCrawler;
import com.constellio.app.modules.es.services.crawler.ConnectorManagerRuntimeException.ConnectorManagerRuntimeException_InParallelFlagMustBeSetBeforeFirstCrawl;
import com.constellio.app.modules.es.services.crawler.DefaultConnectorEventObserver;
import com.constellio.data.dao.managers.StatefulService;
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

	boolean paused;

	boolean inParallel = true;

	public ConnectorManager(ESSchemasRecordsServices es) {
		this.recordServices = es.getModelLayerFactory().newRecordServices();
		this.metadataSchemasManager = es.getModelLayerFactory().getMetadataSchemasManager();
		this.es = es;
	}

	public void setCrawlerInParallel(boolean parallel) {
		if (crawler != null) {
			throw new ConnectorManagerRuntimeException_InParallelFlagMustBeSetBeforeFirstCrawl();
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
			typesBuilder.getSchemaType(schemaType).createCustomSchema(schema);
		}

		try {
			metadataSchemasManager.saveUpdateSchemaTypes(typesBuilder);
		} catch (OptimistickLocking optimistickLocking) {
			throw new RuntimeException(optimistickLocking);
		}

		return connectorInstance;
	}

	<T extends ConnectorInstance> Connector instanciate(T connectorInstance) {
		return es.instanciate(connectorInstance);
	}

	public synchronized ConnectorCrawler getCrawler() {
		if (crawler == null) {
			ConnectorLogger connectorLogger = new ConsoleConnectorLogger();
			String resourceName = "crawlerObserver-" + es.getCollection();
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
				.andWhere(es.connectorDocument.traversalCode()).isEqualTo(connectorInstance.getTraversalCode()));
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
}
