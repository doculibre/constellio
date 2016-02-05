package com.constellio.model.services.batch.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.constellio.data.dao.dto.records.OptimisticLockingResolution;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.services.records.BulkRecordTransactionHandler;
import com.constellio.model.services.records.BulkRecordTransactionHandlerOptions;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.sdk.tests.setups.TwoTaxonomiesContainingFolderAndDocumentsSetup.FolderSchema;
import com.constellio.sdk.tests.setups.TwoTaxonomiesContainingFolderAndDocumentsSetup.TaxonomyRecords;

public class BatchProcessControllerSetupWithTaxonomies {

	public int add10_10_10_X_HierarchyOfFoldersWith(final RecordServices recordServices, final TaxonomyRecords taxonomyRecords,
			final FolderSchema folderSchema, final int nbOfFoldersPerLevel2Node)
			throws RecordServicesException, InterruptedException {

		final AtomicInteger nbFolders = new AtomicInteger();
		final List<Record> level2s = new ArrayList<>();
		Transaction transaction = new Transaction();
		for (int folderLevel0 = 0; folderLevel0 < 10; folderLevel0++) {
			Record level0 = recordServices.newRecordWithSchema(folderSchema.instance());
			transaction.addUpdate(level0);
			level0.set(folderSchema.taxonomy1(), taxonomyRecords.taxo1_firstTypeItem2_secondTypeItem1);

			for (int folderLevel1 = 0; folderLevel1 < 10; folderLevel1++) {
				Record level1 = recordServices.newRecordWithSchema(folderSchema.instance());
				level1.set(folderSchema.parent(), level0);
				transaction.addUpdate(level1);

				for (int folderLevel2 = 0; folderLevel2 < 10; folderLevel2++) {
					Record level2 = recordServices.newRecordWithSchema(folderSchema.instance());
					level2.set(folderSchema.parent(), level1);
					transaction.addUpdate(level2);
					level2s.add(level2);
				}
			}
		}
		nbFolders.addAndGet(transaction.getRecords().size());
		transaction.setOptimisticLockingResolution(OptimisticLockingResolution.EXCEPTION);
		recordServices.execute(transaction);

		BulkRecordTransactionHandlerOptions options = new BulkRecordTransactionHandlerOptions().withRecordsPerBatch(10000);
		BulkRecordTransactionHandler bulkTransactionsHandler = new BulkRecordTransactionHandler(recordServices,
				"testRecordsSetup", options);
		for (Record level2 : level2s) {
			for (int j = 0; j < nbOfFoldersPerLevel2Node; j++) {
				Record level3 = recordServices.newRecordWithSchema(folderSchema.instance());
				level3.set(folderSchema.parent(), level2);
				bulkTransactionsHandler.append(level3);
			}
			nbFolders.addAndGet(nbOfFoldersPerLevel2Node);
		}
		bulkTransactionsHandler.closeAndJoin();
		return nbFolders.get();
	}

}
