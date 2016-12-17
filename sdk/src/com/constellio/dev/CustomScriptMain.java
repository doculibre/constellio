package com.constellio.dev;

import static com.constellio.app.utils.ScriptsUtils.startLayerFactoriesWithoutBackgroundThreads;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.RMObject;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.dto.records.OptimisticLockingResolution;
import com.constellio.model.entities.records.ActionExecutorInBatch;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.extractions.RecordPopulateServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

public class CustomScriptMain {

	static String currentCollection;
	static AppLayerFactory appLayerFactory;
	static ModelLayerFactory modelLayerFactory;
	static SearchServices searchServices;
	static RecordServices recordServices;
	static RMSchemasRecordsServices rm;

	public static void main(String argv[])
			throws Exception {

		RecordPopulateServices.LOG_CONTENT_MISSING = false;
		appLayerFactory = startLayerFactoriesWithoutBackgroundThreads();
		modelLayerFactory = appLayerFactory.getModelLayerFactory();
		searchServices = modelLayerFactory.newSearchServices();
		recordServices = modelLayerFactory.newRecordServices();
		CollectionsListManager collectionsListManager = modelLayerFactory.getCollectionsListManager();
		for (String collection : collectionsListManager.getCollections()) {
			currentCollection = collection;
			rm = new RMSchemasRecordsServices(collection, appLayerFactory);
			runScriptForCurrentCollection();
		}

	}

	private static void runScriptForCurrentCollection()
			throws Exception {
		final Map<String, String> usernamesIdsMap = buildUsernamesIdsMap();

		for (MetadataSchema folderSchema : rm.folderSchemaType().getAllSchemas()) {
			runScriptForRMObject(usernamesIdsMap, folderSchema);
		}

		for (MetadataSchema documentSchema : rm.documentSchemaType().getAllSchemas()) {
			runScriptForRMObject(usernamesIdsMap, documentSchema);
		}

	}

	private static void runScriptForRMObject(final Map<String, String> usernamesIdsMap, final MetadataSchema schema)
			throws Exception {

		final Metadata formCreatedBy = schema.get(RMObject.FORM_CREATED_BY);
		final Metadata formModifiedBy = schema.get(RMObject.FORM_MODIFIED_BY);

		if (!schema.hasMetadataWithCode("USRancienUtilisateurCreation") || !schema
				.hasMetadataWithCode("USRancienUtilisateurModification")) {
			return;
		}

		final Metadata ancienUtilisateurCreation = schema.get("USRancienUtilisateurCreation");
		final Metadata ancienUtilisateurModification = schema.get("USRancienUtilisateurModification");

		LogicalSearchCondition condition = LogicalSearchQueryOperators.from(schema).whereAnyCondition(
				LogicalSearchQueryOperators.allConditions(
						LogicalSearchQueryOperators.where(ancienUtilisateurCreation).isNotNull(),
						LogicalSearchQueryOperators.where(ancienUtilisateurCreation).isNotEqual("__NULL__")
				),
				LogicalSearchQueryOperators.allConditions(
						LogicalSearchQueryOperators.where(ancienUtilisateurModification).isNotNull(),
						LogicalSearchQueryOperators.where(ancienUtilisateurModification).isNotEqual("__NULL__")
				)
		);

		new ActionExecutorInBatch(searchServices, "Update '" + schema.getCode() + "'", 5000) {

			@Override
			public void doActionOnBatch(List<Record> records)
					throws Exception {
				Transaction transaction = new Transaction();
				transaction.setSkippingRequiredValuesValidation(true);
				transaction.setSkippingReferenceToLogicallyDeletedValidation(true);
				transaction.setOptimisticLockingResolution(OptimisticLockingResolution.EXCEPTION);

				for (Record record : records) {
					boolean add = false;
					String ancienUtilisateurCreationValue = record.get(ancienUtilisateurCreation);
					if (ancienUtilisateurCreationValue != null) {
						String userId = usernamesIdsMap.get(ancienUtilisateurCreationValue);
						if (ancienUtilisateurCreationValue.equals("__NULL__") || userId != null) {
							record.set(ancienUtilisateurCreation, null);
							record.set(formCreatedBy, userId);
							add = true;
						}
					}

					String ancienUtilisateurModificationValue = record.get(ancienUtilisateurModification);
					if (ancienUtilisateurModificationValue != null) {
						String userId = usernamesIdsMap.get(ancienUtilisateurModificationValue);
						if (ancienUtilisateurModificationValue.equals("__NULL__") || userId != null) {
							record.set(ancienUtilisateurModification, null);
							record.set(formModifiedBy, userId);
							add = true;
						}
					}
					if (add) {
						transaction.add(record);
					}
				}

				recordServices.execute(transaction);
				modelLayerFactory.getDataLayerFactory().newRecordDao().flush();
			}
		}.execute(condition);

	}

	private static Map<String, String> buildUsernamesIdsMap() {

		List<User> users = rm.wrapUsers(searchServices.search(new LogicalSearchQuery(from(rm.userSchemaType()).returnAll())));
		Map<String, String> usernamesIdsMap = new HashMap<>();
		for (User user : users) {
			usernamesIdsMap.put(user.getUsername(), user.getId());
		}

		return usernamesIdsMap;
	}

}
