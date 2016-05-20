package com.constellio.dev;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import java.util.Collections;
import java.util.List;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.dto.records.OptimisticLockingResolution;
import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.model.entities.records.ActionExecutorInBatch;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.ModificationImpact;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordModificationImpactHandler;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.extractions.RecordPopulateServices;
import com.constellio.model.services.records.reindexing.ReindexationMode;
import com.constellio.model.services.records.reindexing.ReindexingServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.security.AuthorizationDetailsManager;
import com.constellio.sdk.SDKScriptUtils;

public class DeleteAllAuthorizationsMain {

	static String currentCollection;
	static AppLayerFactory appLayerFactory;
	static ModelLayerFactory modelLayerFactory;
	static SearchServices searchServices;
	static RecordServices recordServices;
	static RMSchemasRecordsServices rm;

	private static final int BATCH_SIZE = 5000;

	private static void startBackend() {
		//TODO

		//Only enable this line to run in production
		//appLayerFactory = startLayerFactoriesWithoutBackgroundThreads();

		//Only enable this line to run on developer workstation
		appLayerFactory = SDKScriptUtils.startApplicationWithoutBackgroundProcessesAndAuthentication();

	}

	public static void main(String argv[])
			throws Exception {

		RecordPopulateServices.LOG_CONTENT_MISSING = false;
		startBackend();
		modelLayerFactory = appLayerFactory.getModelLayerFactory();
		searchServices = modelLayerFactory.newSearchServices();
		recordServices = modelLayerFactory.newRecordServices();
		CollectionsListManager collectionsListManager = modelLayerFactory.getCollectionsListManager();

		//for (String collection : collectionsListManager.getCollections()) {
		//currentCollection = collection;
		currentCollection = "collection20151002";
		rm = new RMSchemasRecordsServices(currentCollection, appLayerFactory);
		runScriptForCurrentCollection();
		//}

		ReindexingServices reindexingServices = modelLayerFactory.newReindexingServices();
		reindexingServices.reindexCollections(ReindexationMode.RECALCULATE_AND_REWRITE);

	}

	private static class ResetRecordAuthorizationAction extends ActionExecutorInBatch {

		public ResetRecordAuthorizationAction(String actionName) {
			super(searchServices, actionName, BATCH_SIZE);
		}

		@Override
		public void doActionOnBatch(List<Record> records)
				throws Exception {
			Transaction transaction = new Transaction();
			transaction.setSkippingRequiredValuesValidation(true);
			transaction.setSkippingReferenceToLogicallyDeletedValidation(true);
			transaction.setOptimisticLockingResolution(OptimisticLockingResolution.EXCEPTION);

			for (Record record : records) {

				record.set(Schemas.REMOVED_AUTHORIZATIONS, Collections.emptyList());
				record.set(Schemas.AUTHORIZATIONS, Collections.emptyList());
				record.set(Schemas.IS_DETACHED_AUTHORIZATIONS, null);
				if (record.isDirty()) {
					transaction.add(record);
				}
			}

			recordServices.executeWithImpactHandler(transaction, new RecordModificationImpactHandler() {
				@Override
				public void prepareToHandle(ModificationImpact modificationImpact) {
				}

				@Override
				public void handle() {
				}

				@Override
				public void cancel() {
				}
			});
		}
	}

	private static void runScriptForCurrentCollection()
			throws Exception {

		new ResetRecordAuthorizationAction("Collection " + currentCollection + " - Delete group authorisations")
				.execute(from(rm.groupSchemaType()).returnAll());

		new ResetRecordAuthorizationAction("Collection " + currentCollection + " - Delete user authorisations")
				.execute(from(rm.userSchemaType()).returnAll());

		new ResetRecordAuthorizationAction("Collection " + currentCollection + " - Delete administrative units authorisations")
				.execute(from(rm.administrativeUnitSchemaType()).returnAll());

		new ResetRecordAuthorizationAction("Collection " + currentCollection + " - Delete folders authorisations")
				.execute(from(rm.folderSchemaType()).returnAll());

		new ResetRecordAuthorizationAction("Collection " + currentCollection + " - Delete documents authorisations")
				.execute(from(rm.documentSchemaType()).returnAll());

		AuthorizationDetailsManager authorizationDetailsManager = modelLayerFactory.getAuthorizationDetailsManager();

		ConfigManager configManager = modelLayerFactory.getDataLayerFactory().getConfigManager();
		configManager.delete("/" + currentCollection + "/authorizations.xml");

		authorizationDetailsManager.createCollectionAuthorizationDetail(currentCollection);

	}

}
