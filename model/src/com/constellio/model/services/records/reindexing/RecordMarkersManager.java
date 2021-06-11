package com.constellio.model.services.records.reindexing;

import com.constellio.data.dao.dto.records.RecordId;
import com.constellio.data.dao.managers.StatefulService;

import java.util.HashSet;
import java.util.Set;

public class RecordMarkersManager implements StatefulService {

	Set<RecordId> ids = new HashSet<>();

	public synchronized void markForHierarchyReindexing(RecordId recordId) {
		ids.add(recordId);
	}

	public synchronized void markAsHierarchyReindexed(RecordId recordId) {
		ids.remove(recordId);
	}

	public synchronized RecordId getRecordRequiringHierarchyReindexing() {
		return ids.isEmpty() ? null : ids.iterator().next();
	}

	@Override
	public void initialize() {

	}

	@Override
	public void close() {

	}
}
