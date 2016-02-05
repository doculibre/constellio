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
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

public class ConnectorCrawler {

	int timeWaitedWhenNoJobs = 10000;

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

	public Connector createConnectorFor(ConnectorInstance instance) {

		Connector connector = es.getConnectorManager().instanciate(instance);
		connector.initialize(logger, instance.getWrappedRecord(), eventObserver, es);

		if (instance.getTraversalCode() == null) {
			instance.setTraversalCode(UUID.randomUUID().toString());
			try {
				recordServices.update(instance);
			} catch (RecordServicesException e) {
				throw new RuntimeException(e);
			}
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

			ConnectorInstance instance = es.getConnectorInstance(crawledConnector.connectorInstance.getId());
			if (instance.isCurrentlyRunning()) {
				LOGGER.info("**** Get jobs of '" + crawledConnector.connectorInstance.getIdTitle() + "' ****");
				List<ConnectorJob> jobs = crawledConnector.connector.getJobs();

				if (!jobs.isEmpty()) {
					try {
						jobCrawler.crawl(jobs);
					} catch (Exception e) {
						e.printStackTrace();
					}
					executedJobs = true;
					crawledConnector.connector.afterJobs(jobs);
				} else {
					try {
						recordServices.add(instance.setLastTraversalOn(TimeProvider.getLocalDateTime()));
					} catch (RecordServicesException e) {
						LOGGER.warn("last traversal date not updated", e);
					}
					waitSinceNoJobs();
				}
			}

		}
		eventObserver.flush();

		if (crawledConnectors.isEmpty()) {
			waitSinceNoJobs();
		}

		return executedJobs;
	}

	void waitSinceNoJobs() {
		if (timeWaitedWhenNoJobs > 0) {
			try {
				Thread.sleep(timeWaitedWhenNoJobs);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
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
			CrawledConnector crawledConnector = connectors.next();
			if (!enabledConnectorInstanceIds.contains(crawledConnector.connectorInstance.getId())) {
				connectors.remove();
				crawledConnector.connector.stop();
			}
		}
	}

	private void removedCrawledConnectorsFromCrawlingList(String id) {
		Iterator<CrawledConnector> connectors = crawledConnectors.iterator();
		while (connectors.hasNext()) {
			CrawledConnector crawledConnector = connectors.next();
			if (crawledConnector.connectorInstance.getId().equals(id)) {
				connectors.remove();
				crawledConnector.connector.stop();
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

	public void crawlNTimes(int times) {
		for (int i = 0; i < times; i++) {
			crawlAllConnectors();
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

	public ConnectorCrawler withoutSleeps() {
		timeWaitedWhenNoJobs = 0;
		return this;
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
