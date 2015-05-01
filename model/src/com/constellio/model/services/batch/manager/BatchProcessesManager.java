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
package com.constellio.model.services.batch.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.jdom2.Document;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.managers.config.ConfigManagerException;
import com.constellio.data.dao.managers.config.ConfigManagerException.OptimisticLockingConfiguration;
import com.constellio.data.dao.managers.config.DocumentAlteration;
import com.constellio.data.dao.managers.config.events.ConfigUpdatedEventListener;
import com.constellio.data.dao.managers.config.values.XMLConfiguration;
import com.constellio.model.entities.batchprocess.BatchProcess;
import com.constellio.model.entities.batchprocess.BatchProcessAction;
import com.constellio.model.entities.batchprocess.BatchProcessPart;
import com.constellio.model.entities.batchprocess.BatchProcessStatus;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.services.batch.actions.ReindexMetadatasBatchProcessAction;
import com.constellio.model.services.batch.xml.detail.BatchProcessReader;
import com.constellio.model.services.batch.xml.detail.BatchProcessWriter;
import com.constellio.model.services.batch.xml.list.BatchProcessListReader;
import com.constellio.model.services.batch.xml.list.BatchProcessListWriter;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

public class BatchProcessesManager implements StatefulService, ConfigUpdatedEventListener {

	static final String BATCH_PROCESS_LIST_PATH = "/batchProcesses/list.xml";
	private static final Logger LOGGER = LoggerFactory.getLogger(BatchProcessesManager.class);
	private final String computerName;
	private final ConfigManager configManager;
	private final int partsSize;
	private final SearchServices searchServices;
	private final RecordServices recordServices;
	private final List<BatchProcessesListUpdatedEventListener> listeners = new ArrayList<>();

	public BatchProcessesManager(String computerName, int partsSize, RecordServices recordServices, SearchServices searchServices,
			ConfigManager configManager) {
		super();
		this.partsSize = partsSize;
		this.computerName = computerName;
		this.recordServices = recordServices;
		this.searchServices = searchServices;
		this.configManager = configManager;
	}

	@Override
	public void initialize() {
		if (!configManager.exist(BATCH_PROCESS_LIST_PATH)) {
			saveEmptyProcessListXMLDocument();
		}
		configManager.registerListener(BATCH_PROCESS_LIST_PATH, this);
		markAllStandbyAsPending();
	}

	public BatchProcess add(List<String> records, String collection, BatchProcessAction action) {

		String id = newBatchProcessId();
		LocalDateTime requestDateTime = getCurrentTime();

		Document document = newDocument();
		newBatchProcessWriter(document).newBatchProcess(id, requestDateTime, records);
		configManager.add("/batchProcesses/" + id + ".xml", document);

		updateBatchProcesses(newAddBatchProcessDocumentAlteration(id, collection, requestDateTime, records.size(), action));

		return newBatchProcessListReader(getProcessListXMLDocument()).read(id);
	}

	public void markAsPending(List<BatchProcess> batchProcesses) {
		for (BatchProcess batchProcess : batchProcesses) {
			markAsPending(batchProcess);
		}
	}

	public void markAsPending(BatchProcess batchProcess) {
		updateBatchProcesses(markBatchProcessAsPendingDocumentAlteration(batchProcess.getId()));
	}

	public void cancelStandByBatchProcesses(List<BatchProcess> batchProcesses) {
		for (BatchProcess batchProcess : batchProcesses) {
			cancelStandByBatchProcess(batchProcess);
		}
	}

	public void cancelStandByBatchProcess(BatchProcess batchProcess) {
		updateBatchProcesses(cancelStandByBatchProcessDocumentAlteration(batchProcess.getId()));
	}

	public void markAllStandbyAsPending() {
		updateBatchProcesses(markAllBatchProcessAsPendingDocumentAlteration());
	}

	private void updateBatchProcesses(DocumentAlteration documentAlteration) {
		configManager.updateXML(BATCH_PROCESS_LIST_PATH, documentAlteration);
	}

	public BatchProcess get(String id) {

		BatchProcessListReader reader = newBatchProcessListReader(getProcessListXMLDocument());
		return reader.read(id);
	}

	BatchProcessPart getBatchProcessPartWithPossibleOptimisticLocking()
			throws ConfigManagerException.OptimisticLockingConfiguration {
		BatchProcessPart nextPart = null;
		BatchProcess batchProcess = getCurrentBatchProcess();
		if (batchProcess != null) {
			XMLConfiguration xmlConfiguration = getExistingXMLConfiguration(batchProcess.getId());

			Document batchProcessDocument = xmlConfiguration.getDocument();
			BatchProcessWriter batchProcessWriter = newBatchProcessWriter(batchProcessDocument);
			List<String> records = batchProcessWriter.assignBatchProcessPartTo(computerName, partsSize);
			if (!records.isEmpty()) {
				updateBatchProcessDocument(batchProcess, xmlConfiguration, batchProcessDocument);
				nextPart = new BatchProcessPart(batchProcess, records);
			}
		}

		return nextPart;
	}

	public BatchProcess getCurrentBatchProcess() {
		try {
			return getCurrentBatchProcessWithPossibleOptimisticLocking();
		} catch (ConfigManagerException.OptimisticLockingConfiguration e) {
			LOGGER.info("Optimistic locking while getting current batch process, retrying...", e);
			return getCurrentBatchProcess();
		}
	}

	public BatchProcessPart getCurrentBatchProcessPart() {
		try {
			return getBatchProcessPartWithPossibleOptimisticLocking();
		} catch (ConfigManagerException.OptimisticLockingConfiguration e) {
			LOGGER.info("Optimistic locking while getting batch process part, retrying...", e);
			return getCurrentBatchProcessPart();
		}
	}

	BatchProcess getCurrentBatchProcessWithPossibleOptimisticLocking()
			throws ConfigManagerException.OptimisticLockingConfiguration {
		XMLConfiguration xmlConfiguration = getProcessListXMLConfiguration();
		// Document processList = xmlConfiguration.getDocument();
		Document processList = getProcessListXMLDocument();
		BatchProcessListReader reader = newBatchProcessListReader(processList);
		BatchProcess batchProcess = reader.readCurrent();
		List<BatchProcess> pendingBatchProcesses = reader.readPendingBatchProcesses();

		if (batchProcess == BatchProcessListReader.NO_CURRENT_BATCH_PROCESS && !pendingBatchProcesses.isEmpty()) {
			startNextBatchProcess(xmlConfiguration, processList);
			batchProcess = getCurrentBatchProcess();
		}
		return batchProcess;
	}

	LocalDateTime getCurrentTime() {
		return new LocalDateTime();
	}

	XMLConfiguration getExistingXMLConfiguration(String id) {
		return configManager.getXML("/batchProcesses/" + id + ".xml");
	}

	public List<BatchProcess> getFinishedBatchProcesses() {
		BatchProcessListReader reader = newBatchProcessListReader(getProcessListXMLDocument());
		return reader.readFinishedBatchProcesses();
	}

	public List<BatchProcess> getPendingBatchProcesses() {
		BatchProcessListReader reader = newBatchProcessListReader(getProcessListXMLDocument());
		return reader.readPendingBatchProcesses();
	}

	public List<BatchProcess> getStandbyBatchProcesses() {
		BatchProcessListReader reader = newBatchProcessListReader(getProcessListXMLDocument());
		return reader.readStandbyBatchProcesses();
	}

	public int getAllBatchProcessesCount() {
		BatchProcessListReader reader = newBatchProcessListReader(getProcessListXMLDocument());
		return reader.getAllBatchProcessesCount();
	}

	XMLConfiguration getProcessListXMLConfiguration() {
		XMLConfiguration config = configManager.getXML(BATCH_PROCESS_LIST_PATH);
		if (config == null) {
			saveEmptyProcessListXMLDocument();

			config = getProcessListXMLConfiguration();
		}
		return config;
	}

	Document getProcessListXMLDocument() {
		return getProcessListXMLConfiguration().getDocument();
	}

	public List<String> getRecordsWithError(BatchProcess batchProcess) {

		XMLConfiguration batchProcessConfiguration = getExistingXMLConfiguration(batchProcess.getId());
		Document document = batchProcessConfiguration.getDocument();

		return newBatchProcessReader(document).getRecordsWithError();
	}

	public BatchProcessPart markBatchProcessPartAsFinishedAndGetAnotherPart(BatchProcessPart previousPart, List<String> errors) {
		try {
			return markBatchProcessPartAsFinishedAndGetAnotherPartWithPossibleOptimisticLocking(previousPart, errors);
		} catch (ConfigManagerException.OptimisticLockingConfiguration e) {
			LOGGER.info("Optimistic locking while getting batch process part, retrying...", e);
			return markBatchProcessPartAsFinishedAndGetAnotherPart(previousPart, errors);
		}
	}

	BatchProcessPart markBatchProcessPartAsFinishedAndGetAnotherPartWithPossibleOptimisticLocking(BatchProcessPart previousPart,
			List<String> errors)
			throws ConfigManagerException.OptimisticLockingConfiguration {

		final BatchProcess batchProcess = getCurrentBatchProcess();
		XMLConfiguration xmlConfiguration = getExistingXMLConfiguration(batchProcess.getId());

		Document batchProcessDocument = xmlConfiguration.getDocument();
		BatchProcessWriter batchProcessWriter = newBatchProcessWriter(batchProcessDocument);
		batchProcessWriter.markHasDone(computerName, errors);
		List<String> records = batchProcessWriter.assignBatchProcessPartTo(computerName, partsSize);
		updateBatchProcessDocument(batchProcess, xmlConfiguration, batchProcessDocument);

		configManager.updateXML(BATCH_PROCESS_LIST_PATH,
				newIncrementProgressionDocumentAlteration(batchProcess, previousPart.getRecordIds().size(), errors.size()));

		if (!records.isEmpty()) {
			return new BatchProcessPart(batchProcess, records);
		} else {
			return getCurrentBatchProcessPart();
		}
	}

	DocumentAlteration newIncrementProgressionDocumentAlteration(final BatchProcess batchProcess, final int increment,
			final int errors) {
		return new DocumentAlteration() {

			@Override
			public void alter(Document document) {
				newBatchProcessListWriter(document).incrementProgression(batchProcess, increment, errors);
			}
		};
	}

	DocumentAlteration newAddBatchProcessDocumentAlteration(final String id, final String collection,
			final LocalDateTime requestDateTime,
			final int recordsCount, final BatchProcessAction action) {
		return new DocumentAlteration() {

			@Override
			public void alter(Document document) {
				newBatchProcessListWriter(document).addBatchProcess(id, collection, requestDateTime, recordsCount, action);
			}
		};
	}

	DocumentAlteration markAllBatchProcessAsPendingDocumentAlteration() {
		return new DocumentAlteration() {

			@Override
			public void alter(Document document) {
				newBatchProcessListWriter(document).markAllBatchProcessAsPending();
			}
		};
	}

	DocumentAlteration markBatchProcessAsPendingDocumentAlteration(final String id) {
		return new DocumentAlteration() {

			@Override
			public void alter(Document document) {
				newBatchProcessListWriter(document).markBatchProcessAsPending(id);
			}
		};
	}

	DocumentAlteration cancelStandByBatchProcessDocumentAlteration(final String id) {
		return new DocumentAlteration() {

			@Override
			public void alter(Document document) {
				newBatchProcessListWriter(document).cancelStandByBatchProcess(id);
			}
		};
	}

	String newBatchProcessId() {
		return UUID.randomUUID().toString();
	}

	BatchProcessListReader newBatchProcessListReader(Document document) {
		return new BatchProcessListReader(document);
	}

	BatchProcessListWriter newBatchProcessListWriter(Document document) {
		return new BatchProcessListWriter(document);
	}

	BatchProcessReader newBatchProcessReader(Document document) {
		return new BatchProcessReader(document);
	}

	BatchProcessWriter newBatchProcessWriter(Document document) {
		return new BatchProcessWriter(document);
	}

	Document newDocument() {
		return new Document();
	}

	void saveEmptyProcessListXMLDocument() {
		Document document = new Document();

		BatchProcessListWriter writer = newBatchProcessListWriter(document);
		writer.createEmptyProcessList();

		configManager.add(BATCH_PROCESS_LIST_PATH, document);

	}

	private void startNextBatchProcess(XMLConfiguration xmlConfiguration, Document processList)
			throws OptimisticLockingConfiguration {
		BatchProcessListWriter writer = newBatchProcessListWriter(processList);
		writer.startNextBatchProcess(getCurrentTime());
		configManager.update(BATCH_PROCESS_LIST_PATH, xmlConfiguration.getHash(), processList);
	}

	void updateBatchProcessDocument(BatchProcess batchProcess, XMLConfiguration xmlConfiguration, Document batchProcessDocument)
			throws OptimisticLockingConfiguration {
		String path = "/batchProcesses/" + batchProcess.getId() + ".xml";
		configManager.update(path, xmlConfiguration.getHash(), batchProcessDocument);
	}

	public void registerBatchProcessesListUpdatedEvent(BatchProcessesListUpdatedEventListener listener) {
		listeners.add(listener);
	}

	@Override
	public void onConfigUpdated(String configPath) {
		for (BatchProcessesListUpdatedEventListener listener : listeners) {
			listener.onBatchProcessesListUpdated();
		}
	}

	public List<BatchProcess> getAllNonFinishedBatchProcesses() {
		BatchProcessListReader reader = newBatchProcessListReader(getProcessListXMLDocument());
		List<BatchProcess> batchProcesses = new ArrayList<>();
		if (reader.readCurrent() != null) {
			batchProcesses.add(reader.readCurrent());
		}
		batchProcesses.addAll(reader.readPendingBatchProcesses());
		batchProcesses.addAll(reader.readStandbyBatchProcesses());

		return batchProcesses;
	}

	@Override
	public void close() {

	}

	public void waitUntilAllFinished() {
		for (BatchProcess batchProcess : getAllNonFinishedBatchProcesses()) {
			waitUntilFinished(batchProcess);
		}
	}

	public void waitUntilFinished(BatchProcess batchProcess) {
		while (get(batchProcess.getId()).getStatus() != BatchProcessStatus.FINISHED) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public BatchProcess prepareToReindex(List<Metadata> metadatas, LogicalSearchCondition condition) {
		String collection = metadatas.get(0).getCollection();
		List<String> metadataCodes = new SchemaUtils().toMetadataCodes(metadatas);

		//TODO not viable!
		List<String> ids = searchServices.searchRecordIds(new LogicalSearchQuery(condition));

		return add(ids, collection, new ReindexMetadatasBatchProcessAction(metadataCodes));

	}
}
