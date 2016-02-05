package com.constellio.model.services.batch.state;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Document;

import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.managers.config.DocumentAlteration;
import com.constellio.data.dao.managers.config.events.ConfigUpdatedEventListener;
import com.constellio.model.entities.batchprocess.BatchProcess;
import com.constellio.model.services.batch.state.BatchProcessProgressionServicesException.BatchProcessProgressionServicesException_OptimisticLocking;

public class StoredBatchProcessProgressionServices implements BatchProcessProgressionServices, StatefulService,
															  ConfigUpdatedEventListener {

	private static final String BATCH_PROCESS_PROGRESSION_CONFIG = "/batchProcessProgression.xml";

	private final ConfigManager configManager;

	Map<String, List<StoredBatchProcessPart>> cache = new HashMap<>();

	public StoredBatchProcessProgressionServices(ConfigManager configManager) {
		this.configManager = configManager;
	}

	@Override
	public void initialize() {
		registerListener(configManager);
		refreshCache();

	}

	private void refreshCache() {
		Document document = configManager.getXML(BATCH_PROCESS_PROGRESSION_CONFIG).getDocument();
		StoredBatchProcessReader reader = new StoredBatchProcessReader(document);
		cache = Collections.unmodifiableMap(reader.readAll());
	}

	void registerListener(ConfigManager configManager) {
		if (!configManager.exist(BATCH_PROCESS_PROGRESSION_CONFIG)) {
			createEmptyBatchProcessProgressionConfig();
		}
		configManager.registerListener(BATCH_PROCESS_PROGRESSION_CONFIG, this);
	}

	void createEmptyBatchProcessProgressionConfig() {
		Document document = new Document();
		StoredBatchProcessWriter writer = newStoredBatchProcessWriter(document);
		writer.createEmptyBatchProcessProgression();
		configManager.add(BATCH_PROCESS_PROGRESSION_CONFIG, document);
	}

	private StoredBatchProcessWriter newStoredBatchProcessWriter(Document document) {
		return new StoredBatchProcessWriter(document);
	}

	//
	private List<StoredBatchProcessPart> getParts(String batchProcessId) {
		List<StoredBatchProcessPart> parts = cache.get(batchProcessId);
		if (parts == null) {
			parts = new ArrayList<>();
		}
		return parts;
	}

	public void addUpdate(StoredBatchProcessPart storedBatchProcessPart) {
		configManager.updateXML(BATCH_PROCESS_PROGRESSION_CONFIG,
				newAddUpdateBatchProcessPartDocumentAlteration(storedBatchProcessPart));
	}

	DocumentAlteration newAddUpdateBatchProcessPartDocumentAlteration(final StoredBatchProcessPart storedBatchProcessPart) {
		return new DocumentAlteration() {
			@Override
			public void alter(Document document) {
				newStoredBatchProcessWriter(document).addUpdate(storedBatchProcessPart);
			}
		};
	}

	@Override
	public void markNewPartAsStarted(StoredBatchProcessPart part)
			throws BatchProcessProgressionServicesException_OptimisticLocking {
		List<StoredBatchProcessPart> parts = getParts(part.getBatchProcessId());
		if (parts.size() != part.getIndex()) {
			throw new BatchProcessProgressionServicesException_OptimisticLocking(part.getIndex());
		}
		addUpdate(part.whichIsStarted());
	}

	@Override
	public void markPartAsFinished(StoredBatchProcessPart part) {
		addUpdate(part.whichIsFinished());
	}

	@Override
	public void markStartedPartsHasInStandby() {
		for (List<StoredBatchProcessPart> parts : cache.values()) {
			for (int i = 0; i < parts.size(); i++) {
				StoredBatchProcessPart part = parts.get(i);
				if (part.isStarted()) {
					addUpdate(part.whichIsInStandby());
				}
			}
		}
	}

	@Override
	public List<StoredBatchProcessPart> getPartsInStandby(BatchProcess batchProcess) {
		List<StoredBatchProcessPart> parts = getParts(batchProcess.getId());
		List<StoredBatchProcessPart> partsInStandby = new ArrayList<>();

		for (StoredBatchProcessPart part : parts) {
			while (!part.isStarted()) {
				partsInStandby.add(part);
			}
		}
		return parts;
	}

	@Override
	public StoredBatchProcessPart getLastBatchProcessPart(BatchProcess batchProcess) {
		List<StoredBatchProcessPart> parts = getParts(batchProcess.getId());
		return parts.isEmpty() ? null : parts.get(parts.size() - 1);
	}

	//
	@Override
	public void onConfigUpdated(String configPath) {
		refreshCache();
	}

	@Override
	public void close() {
	}
}
