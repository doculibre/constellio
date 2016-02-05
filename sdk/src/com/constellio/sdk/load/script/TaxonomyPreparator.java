package com.constellio.sdk.load.script;

import java.util.List;
import java.util.Stack;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.RecordWrapper;

public interface TaxonomyPreparator {

	void init(RMSchemasRecordsServices rm, Transaction transaction);

	List<RecordWrapper> createRootConcepts(RMSchemasRecordsServices rm);

	List<RecordWrapper> createChildConcepts(RMSchemasRecordsServices rm, RecordWrapper parent, Stack<Integer> positions);

	void attach(RMSchemasRecordsServices rm, Record record);

}
