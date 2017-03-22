package com.constellio.model.services.records;

import java.util.ArrayList;
import java.util.List;

import com.constellio.model.entities.batchprocess.BatchProcess;
import com.constellio.model.entities.batchprocess.BatchProcessAction;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.ModificationImpact;
import com.constellio.model.services.batch.actions.ReindexMetadatasBatchProcessAction;
import com.constellio.model.services.batch.manager.BatchProcessesManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

public class AddToBatchProcessImpactHandler implements RecordModificationImpactHandler {

	BatchProcessesManager manager;

	SearchServices searchServices;

	List<BatchProcess> createdBatchProcesses = new ArrayList<>();

	public AddToBatchProcessImpactHandler(BatchProcessesManager manager, SearchServices searchServices) {
		this.manager = manager;
		this.searchServices = searchServices;
	}

	@Override
	public void prepareToHandle(ModificationImpact modificationImpact) {
		LogicalSearchCondition condition = modificationImpact.getLogicalSearchCondition();
		if (searchServices.hasResults(condition)) {

			String title = "reindex.transaction";
			if (modificationImpact.getTransactionTitle() != null) {
				title += " " + modificationImpact.getTransactionTitle();
			}

			BatchProcessAction action = newBatchProcessAction(modificationImpact.getMetadataToReindex());
			String collection = modificationImpact.getMetadataToReindex().get(0).getCollection();
			BatchProcess batchProcess = this.manager.addBatchProcessInStandby(condition, action, title);
			createdBatchProcesses.add(batchProcess);

		}
	}

	@Override
	public void handle() {
		for (BatchProcess batchProcess : createdBatchProcesses) {
			this.manager.markAsPending(batchProcess);
		}
	}

	@Override
	public void cancel() {
		for (BatchProcess batchProcess : createdBatchProcesses) {
			this.manager.cancelStandByBatchProcess(batchProcess);
		}
		this.createdBatchProcesses.clear();
	}

	BatchProcessAction newBatchProcessAction(List<Metadata> metadatas) {
		return ReindexMetadatasBatchProcessAction.forMetadatas(metadatas);
	}

	public List<BatchProcess> getAllCreatedBatchProcesses() {
		return createdBatchProcesses;
	}
}
