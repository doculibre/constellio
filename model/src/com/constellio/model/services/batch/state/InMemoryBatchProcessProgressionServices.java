package com.constellio.model.services.batch.state;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.constellio.model.entities.batchprocess.BatchProcess;
import com.constellio.model.services.batch.state.BatchProcessProgressionServicesException.BatchProcessProgressionServicesException_OptimisticLocking;

public class InMemoryBatchProcessProgressionServices implements BatchProcessProgressionServices {

	Map<String, List<StoredBatchProcessPart>> batchProcessesParts = new HashMap<>();

	private List<StoredBatchProcessPart> getParts(String batchProcessId) {
		List<StoredBatchProcessPart> parts = batchProcessesParts.get(batchProcessId);
		if (parts == null) {
			parts = new ArrayList<>();
			batchProcessesParts.put(batchProcessId, parts);
		}
		return parts;
	}

	@Override
	public void markNewPartAsStarted(StoredBatchProcessPart part)
			throws BatchProcessProgressionServicesException_OptimisticLocking {
		List<StoredBatchProcessPart> parts = getParts(part.getBatchProcessId());
		if (parts.size() != part.getIndex()) {
			throw new BatchProcessProgressionServicesException_OptimisticLocking(part.getIndex());
		}
		parts.add(part);
	}

	@Override
	public void markPartAsFinished(StoredBatchProcessPart part) {
		List<StoredBatchProcessPart> parts = getParts(part.getBatchProcessId());
		parts.set(part.getIndex(), part.whichIsFinished());
	}

	@Override
	public void markStartedPartsHasInStandby() {
		for (List<StoredBatchProcessPart> parts : batchProcessesParts.values()) {
			for (int i = 0; i < parts.size(); i++) {
				StoredBatchProcessPart part = parts.get(i);
				if (part.isStarted()) {
					parts.set(i, part.whichIsInStandby());
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
}
