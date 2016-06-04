package com.constellio.dev;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.dto.records.OptimisticLockingResolution;
import com.constellio.model.entities.records.ActionExecutorInBatch;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.extractions.RecordPopulateServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

import java.util.List;

import static com.constellio.app.utils.ScriptsUtils.startLayerFactoriesWithoutBackgroundThreads;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

/*
 * Script exemple
 *
 * Compile and copy classes on production server
 *
 */
public class ScriptExemple {

	static int BATCH_SIZE = 5000;

	static String currentCollection;
	static AppLayerFactory appLayerFactory;
	static ModelLayerFactory modelLayerFactory;
	static SearchServices searchServices;
	static RecordServices recordServices;
	static RMSchemasRecordsServices rm;

	private static void startBackend() {
		//TODO

		//Only enable this line to run in production
		appLayerFactory = startLayerFactoriesWithoutBackgroundThreads();

		//Only enable this line to run on developer workstation
		//appLayerFactory = SDKScriptUtils.startApplicationWithoutBackgroundProcessesAndAuthentication();

	}

	private static LogicalSearchQuery getQuery() {
		//TODO Build a query to find records to modify or to return all records
		return new LogicalSearchQuery(from(rm.folderSchemaType())
				.where(rm.containerRecord.administrativeUnit()).isEqualTo("42"));

		//return new LogicalSearchQuery(from(rm.folderSchemaType()).returnAll());

	}

	private static void runScriptForCurrentCollection()
			throws Exception {

		new ActionExecutorInBatch(searchServices, "The name of the task", BATCH_SIZE) {

			@Override
			public void doActionOnBatch(List<Record> records) {

				//TODO Wrap the records
				List<Folder> folders = rm.wrapFolders(records);

				Transaction transaction = new Transaction();
				transaction.setSkippingRequiredValuesValidation(true);
				transaction.setOptimisticLockingResolution(OptimisticLockingResolution.EXCEPTION);


				for (Folder folder : folders) {

					//TODO Do the modification on a record
					folder.setBorrowed(false);

					transaction.add(folder);
				}

				try {
					recordServices.execute(transaction);
				} catch (RecordServicesException e) {
					throw new RuntimeException(e);
				}
			}

		}.execute(getQuery());

	}

	public static void main(String argv[])
			throws Exception {

		RecordPopulateServices.LOG_CONTENT_MISSING = false;

		startBackend();

		modelLayerFactory = appLayerFactory.getModelLayerFactory();
		searchServices = modelLayerFactory.newSearchServices();
		recordServices = modelLayerFactory.newRecordServices();

		for (String collection : modelLayerFactory.getCollectionsListManager().getCollections()) {
			currentCollection = collection;
			rm = new RMSchemasRecordsServices(collection, appLayerFactory);
			runScriptForCurrentCollection();
		}

	}

}
