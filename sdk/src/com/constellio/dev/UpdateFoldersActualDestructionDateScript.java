package com.constellio.dev;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.dto.records.OptimisticLockingResolution;
import com.constellio.model.entities.records.ActionExecutorInBatch;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.extractions.RecordPopulateServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.sdk.SDKScriptUtils;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.joda.time.LocalDate;

import java.io.File;
import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class UpdateFoldersActualDestructionDateScript {

	private static Logger LOG = Logger.getLogger(UpdateFoldersActualDestructionDateScript.class);
	static int BATCH_SIZE = 500;

	static String currentCollection;
	static AppLayerFactory appLayerFactory;
	static ModelLayerFactory modelLayerFactory;
	static SearchServices searchServices;
	static RecordServices recordServices;
	static RMSchemasRecordsServices rm;

	private static void startBackend() {
		appLayerFactory = SDKScriptUtils.startApplicationWithoutBackgroundProcessesAndAuthentication();
	}

	private static void runScriptForCurrentCollection(final List<String> targetObjectIds) throws Exception {

		for (MetadataSchema folderSchema : rm.folderSchemaType().getAllSchemas()) {

			new ActionExecutorInBatch(searchServices, "The name of the task", BATCH_SIZE) {

				@Override
				public void doActionOnBatch(List<Record> records) {

					List<Folder> folders = rm.wrapFolders(records);

					Transaction transaction = new Transaction();
					transaction.setSkippingRequiredValuesValidation(true);
					transaction.setOptimisticLockingResolution(OptimisticLockingResolution.EXCEPTION);

					for (Folder folder : folders) {
						System.out.println(folder.<String>get(rm.folderSchemaType().getMetadata("legacyId")));
						//if (targetObjectIds.contains(folder.get(rm.folderSchemaType().getMetadata("legacyId")))) {

						System.out.println(folder.getId() + ":" + folder.getTitle());

						try {
							LocalDate metadataValue = folder.get(Folder.EXPECTED_DESTRUCTION_DATE);
							folder.set(Folder.ACTUAL_DESTRUCTION_DATE, metadataValue);

							transaction.add(folder);

						} catch (Exception e) {
							LOG.error("Identifiant document: " + folder.getId(), e);
							e.printStackTrace();
						}

					}

					try {
						recordServices.execute(transaction);
					} catch (RecordServicesException e) {
						throw new RuntimeException(e);
					}

					//}
				}

			}.execute(new LogicalSearchQuery(from(folderSchema).returnAll()));
		}
	}

	public static void main(String argv[]) throws Exception {

		if (argv.length < 1) {
			System.out.println(String.format("Sample call : sudo java -Xmx5120m -classpath ./classes:./lib/* " +
											 "com.constellio.dev.<nom-de-la-classe> <fichier-des-idenifiants>"));
			return;
		}

		RecordPopulateServices.LOG_CONTENT_MISSING = false;

		startBackend();

		modelLayerFactory = appLayerFactory.getModelLayerFactory();
		searchServices = modelLayerFactory.newSearchServices();
		recordServices = modelLayerFactory.newRecordServices();

		List<String> targetObjectIds = FileUtils.readLines(new File(argv[0]));

		for (String collection : modelLayerFactory.getCollectionsListManager().getCollections()) {
			currentCollection = collection;
			rm = new RMSchemasRecordsServices(collection, appLayerFactory);
			runScriptForCurrentCollection(targetObjectIds);
		}
	}
}
