package com.constellio.model.services.records;

import com.constellio.model.entities.batchprocess.AsyncTask;
import com.constellio.model.entities.batchprocess.AsyncTaskCreationRequest;
import com.constellio.model.entities.batchprocess.BatchProcess;
import com.constellio.model.entities.batchprocess.BatchProcessAction;
import com.constellio.model.entities.batchprocess.ReindexingAsyncTask;
import com.constellio.model.entities.schemas.HierarchyReindexingRecordsModificationImpact;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.ModificationImpact;
import com.constellio.model.entities.schemas.QueryBasedReindexingBatchProcessModificationImpact;
import com.constellio.model.services.batch.actions.ReindexMetadatasBatchProcessAction;
import com.constellio.model.services.batch.manager.BatchProcessesManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class AddToBatchProcessImpactHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(AddToBatchProcessImpactHandler.class);

	BatchProcessesManager manager;

	SearchServices searchServices;

	RecordServices recordServices;

	List<BatchProcess> createdBatchProcesses = new ArrayList<>();

	public AddToBatchProcessImpactHandler(BatchProcessesManager manager, SearchServices searchServices,
										  RecordServices recordServices) {
		this.manager = manager;
		this.searchServices = searchServices;
		this.recordServices = recordServices;
	}

	public void prepareToHandle(ModificationImpact modificationImpact) {
		if (modificationImpact instanceof QueryBasedReindexingBatchProcessModificationImpact) {
			QueryBasedReindexingBatchProcessModificationImpact queryImpact =
					(QueryBasedReindexingBatchProcessModificationImpact) modificationImpact;
			LogicalSearchCondition condition = queryImpact.getLogicalSearchCondition();
			if (searchServices.hasResults(condition)) {

				String title = "reindex.transaction";
				if (queryImpact.getTransactionTitle() != null) {
					title += " " + queryImpact.getTransactionTitle();
				}

				BatchProcessAction action = newBatchProcessAction(modificationImpact.getMetadataToReindex());
				createdBatchProcesses.add(this.manager.addBatchProcessInStandby(condition, action, title));

			}
		}

		if (modificationImpact instanceof HierarchyReindexingRecordsModificationImpact) {
			HierarchyReindexingRecordsModificationImpact hierarchyImpact =
					(HierarchyReindexingRecordsModificationImpact) modificationImpact;

			String id = hierarchyImpact.getRootIdToReindex().stringValue();
			AsyncTask reindexingAsyncTask = new ReindexingAsyncTask(hierarchyImpact.getCollection(), id);

			String recordCaptions = recordServices.get(id).getTitle();

			String title = "reindex.hierarchy.of " + recordCaptions;
			if (!this.manager.getPendingBatchProcesses().stream().anyMatch(batchProcess -> batchProcess.getTitle().equals(title))) {
				LOGGER.info("Adding hierarchy reindexing batch process : " + title);
				createdBatchProcesses.add(this.manager.addAsyncTask(new AsyncTaskCreationRequest(
						reindexingAsyncTask, modificationImpact.getCollection(), title).setInStandby(true)));
			}

		}
	}


	public void handle() {
		for (BatchProcess batchProcess : createdBatchProcesses) {
			this.manager.markAsPending(batchProcess);
		}
	}

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
