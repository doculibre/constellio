package com.constellio.dev;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.dto.records.OptimisticLockingResolution;
import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.data.utils.BatchBuilderIterator;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static com.constellio.app.utils.ScriptsUtils.startLayerFactoriesWithoutBackgroundThreads;
import static com.constellio.model.entities.schemas.Schemas.LEGACY_ID;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class ModifyLegacyIdScript {

	private static final String COLLECTION = "collection";

	public static void main(String argv[]) {

		AppLayerFactory appLayerFactory = startLayerFactoriesWithoutBackgroundThreads();
		ModelLayerFactory modelLayerFactory = appLayerFactory.getModelLayerFactory();
		SearchServices searchServices = modelLayerFactory.newSearchServices();
		RecordServices recordServices = modelLayerFactory.newRecordServices();
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(COLLECTION, appLayerFactory);

		long foldersCount = searchServices.getResultsCount(from(rm.folderSchemaType()).returnAll());
		long documentsCount = searchServices.getResultsCount(from(rm.documentSchemaType()).returnAll());
		System.out.println("Nombre de dossiers : " + foldersCount);
		System.out.println("Nombre de documents : " + documentsCount);

		LogicalSearchQuery allDocumentsWithLegacyId = new LogicalSearchQuery(
				from(rm.documentSchemaType()).where(LEGACY_ID).isNotNull());
		allDocumentsWithLegacyId.setReturnedMetadatas(ReturnedMetadatasFilter.onlyMetadatas(Schemas.LEGACY_ID));
		Iterator<Record> documentsIterator = searchServices.recordsIterator(allDocumentsWithLegacyId, 1000);
		Iterator<List<Record>> documentsBatchIterator = new BatchBuilderIterator<>(documentsIterator, 1000);

		List<String> allSavedLegacyIds = new ArrayList<>();
		Set<String> legacyIdsWithoutDuplicates = new HashSet<>();

		int currentBatchStart = 0;
		while (documentsBatchIterator.hasNext()) {
			System.out.println("Batch " + currentBatchStart + "-" + (currentBatchStart + 1000));
			List<Record> batchRecords = new ArrayList<>(documentsBatchIterator.next());
			List<Record> modifiedRecords = new ArrayList<>();
			for (Record batchRecord : batchRecords) {
				String legacyId = batchRecord.get(LEGACY_ID);

				int semiColonIndex = legacyId.indexOf(";");

				if (semiColonIndex != -1) {
					legacyId = StringUtils.substringBefore(legacyId, ";");
					batchRecord.set(Schemas.LEGACY_ID, legacyId);
					modifiedRecords.add(batchRecord);
				} else {
					legacyId = legacyId + "pouet";
					batchRecord.set(Schemas.LEGACY_ID, legacyId);
					modifiedRecords.add(batchRecord);
				}

				allSavedLegacyIds.add(legacyId);
				legacyIdsWithoutDuplicates.add(legacyId);
			}

			Transaction transaction = new Transaction();
			transaction.setSkippingReferenceToLogicallyDeletedValidation(true);
			transaction.setOptimisticLockingResolution(OptimisticLockingResolution.TRY_MERGE);
			transaction.setSkippingRequiredValuesValidation(true);
			transaction.getRecordUpdateOptions().setSkipReferenceValidation(true);
			transaction.getRecordUpdateOptions().setUpdateModificationInfos(false);
			transaction.getRecordUpdateOptions().setValidationsEnabled(false);
			transaction.addUpdate(modifiedRecords);
			transaction.setRecordFlushing(RecordsFlushing.WITHIN_MINUTES(5));

			try {
				recordServices.execute(transaction);
			} catch (RecordServicesException e) {
				throw new RuntimeException(e);
			}
			currentBatchStart += 1000;

		}
		recordServices.flush();

		if (legacyIdsWithoutDuplicates.size() == allSavedLegacyIds.size()) {
			System.out.println("No duplicates");
		} else {

			System.out.println("Finished!... Now detecting duplicate legacy ids");

			for (String legacyId : legacyIdsWithoutDuplicates) {
				allSavedLegacyIds.remove(legacyId);
			}

			System.out.println("Duplicate legacy ids : " + allSavedLegacyIds);
		}
	}

}
