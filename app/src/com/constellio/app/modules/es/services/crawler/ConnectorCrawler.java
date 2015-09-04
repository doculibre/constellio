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
package com.constellio.app.modules.es.services.crawler;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.joda.time.Duration;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.modules.es.connectors.spi.Connector;
import com.constellio.app.modules.es.connectors.spi.ConnectorEventObserver;
import com.constellio.app.modules.es.connectors.spi.ConnectorJob;
import com.constellio.app.modules.es.connectors.spi.ConnectorLogger;
import com.constellio.app.modules.es.connectors.spi.ConsoleConnectorLogger;
import com.constellio.app.modules.es.model.connectors.ConnectorInstance;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.data.utils.Factory;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

public class ConnectorCrawler {

	private static Logger LOGGER = LoggerFactory.getLogger(ConnectorCrawler.class);

	ESSchemasRecordsServices es;
	ConnectorJobCrawler jobCrawler;
	ConnectorLogger logger;
	ConnectorEventObserver eventObserver;
	RecordServices recordServices;

	List<CrawledConnector> crawledConnectors = new ArrayList<>();

	ConnectorCrawler(ESSchemasRecordsServices es, ConnectorJobCrawler jobCrawler,
			ConnectorLogger logger,
			ConnectorEventObserver eventObserver) {
		this.es = es;
		this.jobCrawler = jobCrawler;
		this.logger = logger;
		this.eventObserver = eventObserver;
		this.recordServices = es.getModelLayerFactory().newRecordServices();
	}

	private Connector createConnectorFor(ConnectorInstance instance) {

		Connector connector = es.instanciate(instance);
		connector.initialize(logger, instance.getWrappedRecord(), eventObserver, es);

		if (instance.getTraversalCode() == null) {
			instance.setTraversalCode(UUID.randomUUID().toString());
			connector.start();
		} else {
			connector.resume();
		}

		return connector;
	}

	boolean crawlAllConnectors() {

		List<ConnectorInstance> connectorInstances = getConnectorInstances();
		initializeStartAndStopConnectors(connectorInstances);

		boolean executedJobs = false;
		for (CrawledConnector crawledConnector : crawledConnectors) {

			LOGGER.info("**** Get jobs of '" + crawledConnector.connectorInstance.getIdTitle() + "' ****");
			List<ConnectorJob> jobs = crawledConnector.connector.getJobs();

			if (!jobs.isEmpty()) {
				try {
					jobCrawler.crawl(jobs);
				} catch (Exception e) {
					e.printStackTrace();
				}
				executedJobs = true;
			}

		}
		recordServices.flush();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		return executedJobs;
	}

	private List<ConnectorInstance> getConnectorInstances() {
		LogicalSearchQuery query = new LogicalSearchQuery(
				from(es.connectorInstance.schemaType()).where(es.connectorInstance.enabled()).isTrue());
		query.sortAsc(es.connectorInstance.code());
		return es.wrapConnectorInstances(es.getSearchServices().search(query));
	}

	private void initializeStartAndStopConnectors(List<ConnectorInstance> connectorInstances) {
		List<String> enabledConnectorInstanceIds = new ArrayList<>();
		for (ConnectorInstance connectorInstance : connectorInstances) {
			enabledConnectorInstanceIds.add(connectorInstance.getId());
			CrawledConnector crawledConnector = getCrawledConnector(connectorInstance);
			if (crawledConnector == null || connectorInstance.getTraversalCode() == null) {
				if (crawledConnector != null) {
					removedCrawledConnectorsFromCrawlingList(connectorInstance.getId());
				}
				Connector connector = createConnectorFor(connectorInstance);
				crawledConnectors.add(new CrawledConnector(connector, connectorInstance.getWrappedRecord()));
				if (connectorInstance.getWrappedRecord().isDirty()) {
					try {
						recordServices.add(connectorInstance);
					} catch (RecordServicesException e) {
						throw new RuntimeException(e);
					}
				}
			}
		}

		removedDisabledConnectors(enabledConnectorInstanceIds);

		Collections.sort(crawledConnectors, new Comparator<CrawledConnector>() {
			@Override
			public int compare(CrawledConnector o1, CrawledConnector o2) {
				String code1 = o1.connectorInstance.get(Schemas.CODE);
				String code2 = o2.connectorInstance.get(Schemas.CODE);
				return code1.compareTo(code2);
			}
		});
	}

	private void removedDisabledConnectors(List<String> enabledConnectorInstanceIds) {
		Iterator<CrawledConnector> connectors = crawledConnectors.iterator();
		while (connectors.hasNext()) {
			if (!enabledConnectorInstanceIds.contains(connectors.next().connectorInstance.getId())) {
				connectors.remove();
			}
		}
	}

	private void removedCrawledConnectorsFromCrawlingList(String id) {
		Iterator<CrawledConnector> connectors = crawledConnectors.iterator();
		while (connectors.hasNext()) {
			if (connectors.next().connectorInstance.getId().equals(id)) {
				connectors.remove();
			}
		}
	}

	private CrawledConnector getCrawledConnector(ConnectorInstance connectorInstance) {
		for (CrawledConnector crawledConnector : crawledConnectors) {
			if (crawledConnector.connectorInstance.getId().endsWith(connectorInstance.getId())) {
				return crawledConnector;
			}
		}
		return null;
	}

	public void shutdown() {
		eventObserver.close();
	}

	public synchronized ConnectorCrawler stopCrawlingConnector(ConnectorInstance instance) {
		return this;
	}

	public static ConnectorCrawler runningJobsSequentially(ESSchemasRecordsServices es, ConnectorLogger logger,
			ConnectorEventObserver eventObserver) {
		return new ConnectorCrawler(es, new SimpleConnectorJobCrawler(), logger, eventObserver);
	}

	public static ConnectorCrawler runningJobsSequentially(ESSchemasRecordsServices es, ConnectorEventObserver eventObserver) {
		return runningJobsSequentially(es, new ConsoleConnectorLogger(), eventObserver);
	}

	public static ConnectorCrawler runningJobsInParallel(ESSchemasRecordsServices es, ConnectorLogger logger,
			ConnectorEventObserver eventObserver) {
		return new ConnectorCrawler(es, new MultithreadConnectorJobCrawler(), logger, eventObserver);
	}

	public static ConnectorCrawler runningJobsInParallel(ESSchemasRecordsServices es, ConnectorEventObserver eventObserver) {
		return runningJobsSequentially(es, new ConsoleConnectorLogger(), eventObserver);
	}

	public void crawlUntilRecordsFound(final LogicalSearchCondition condition) {
		crawlUntil(new Factory<Boolean>() {

			@Override
			public Boolean get() {
				return es.getSearchServices().hasResults(condition);
			}
		});

	}

	public void crawlUntilNoMoreRecordsFoundWithinTimeoutForSchemaTypes(Duration duration, String... schemaTypeCodes) {
		//Not using TimeProvider
		List<MetadataSchemaType> types = es.getTypes().getSchemaTypesWithCode(asList(schemaTypeCodes));

		LocalDateTime timeoutTime = new LocalDateTime().plus(duration);
		boolean recordsFound = true;

		LocalDateTime lastModification = null;
		while (new LocalDateTime().isBefore(timeoutTime) && !getLastModificationDate(types).equals(lastModification)) {
			if (!crawlAllConnectors()) {
				break;
			}
		}

	}

	private LocalDateTime getLastModificationDate(List<MetadataSchemaType> types) {
		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(from(types).returnAll());
		query.sortDesc(Schemas.MODIFIED_ON);
		query.setNumberOfRows(1);
		List<Record> records = es.getSearchServices().search(query);
		if (records.isEmpty()) {
			return new LocalDateTime(2010, 11, 4, 0, 0);
		} else {
			return records.get(0).get(Schemas.MODIFIED_ON);
		}
	}

	public void crawlUntil(Factory<Boolean> condition) {
		while (!condition.get()) {
			crawlAllConnectors();
			if (crawledConnectors.isEmpty()) {
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	private static class CrawledConnector {

		Connector connector;

		Record connectorInstance;

		private CrawledConnector(Connector connector, Record connectorInstance) {
			this.connector = connector;
			this.connectorInstance = connectorInstance;
		}
	}

}
