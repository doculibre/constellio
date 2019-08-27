package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.services.bigVault.SearchResponseIterator;
import com.constellio.model.entities.calculators.SavedSearchRestrictedCalculator2;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.SavedSearch;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.entries.CalculatedDataEntry;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class CoreMigrationTo_8_3_1 implements MigrationScript {

	@Override
	public String getVersion() {
		return "8.3.1";
	}


	@Override
	public void migrate(String collection, MigrationResourcesProvider provider,
						AppLayerFactory appLayerFactory)
			throws Exception {

		if (!Collection.SYSTEM_COLLECTION.equals(collection)) {
			RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);

			if (!(((CalculatedDataEntry) rm.savedSearch.isRestricted().getDataEntry()).getCalculator() instanceof SavedSearchRestrictedCalculator2)) {

				SearchServices searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
				LogicalSearchQuery allSavedSearchQuery = new LogicalSearchQuery()
						.setCondition(from(rm.savedSearch.schemaType()).returnAll());
				SearchResponseIterator<Record> recordIterator = searchServices.recordsIterator(allSavedSearchQuery, 1000);

				Map<String, List<String>> sharedGroups = new HashMap<>();
				Map<String, List<String>> sharedUsers = new HashMap<>();
				while (recordIterator.hasNext()) {
					SavedSearch savedSearch = rm.wrapSavedSearch(recordIterator.next());
					sharedGroups.put(savedSearch.getId(), savedSearch.getSharedGroups());
					sharedUsers.put(savedSearch.getId(), savedSearch.getSharedUsers());
				}

				new CoreSchemaAlterationFor8_3_1_delete(collection, provider, appLayerFactory).migrate();
				new CoreSchemaAlterationFor8_3_1_recreate(collection, provider, appLayerFactory).migrate();

				Transaction transaction = new Transaction();
				SearchResponseIterator<List<Record>> recordBatchesIterator = searchServices.recordsIterator(allSavedSearchQuery, 1000).inBatches();
				while (recordBatchesIterator.hasNext()) {
					for (SavedSearch savedSearch : rm.wrapSavedSearches(recordBatchesIterator.next())) {
						savedSearch.setSharedGroups(sharedGroups.get(savedSearch.getId()));
						savedSearch.setSharedUsers(sharedUsers.get(savedSearch.getId()));
						transaction.add(savedSearch);
					}
					rm.executeTransaction(transaction);
				}
			}
		}
	}


	private class CoreSchemaAlterationFor8_3_1_delete extends MetadataSchemasAlterationHelper {
		public CoreSchemaAlterationFor8_3_1_delete(String collection,
												   MigrationResourcesProvider migrationResourcesProvider,
												   AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder builder) {
			MetadataSchemaBuilder savedSearchSchema = typesBuilder.getDefaultSchema(SavedSearch.SCHEMA_TYPE);
			savedSearchSchema.deleteMetadataWithoutValidation(SavedSearch.RESTRICTED);
			savedSearchSchema.deleteMetadataWithoutValidation(SavedSearch.SHARED_USERS);
			savedSearchSchema.deleteMetadataWithoutValidation(SavedSearch.SHARED_GROUPS);
		}
	}

	private class CoreSchemaAlterationFor8_3_1_recreate extends MetadataSchemasAlterationHelper {
		public CoreSchemaAlterationFor8_3_1_recreate(String collection,
													 MigrationResourcesProvider migrationResourcesProvider,
													 AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder builder) {
			MetadataSchemaBuilder savedSearchSchema = typesBuilder.getDefaultSchema(SavedSearch.SCHEMA_TYPE);
			savedSearchSchema.createUndeletable(SavedSearch.SHARED_USERS).setType(MetadataValueType.REFERENCE)
					.setMultivalue(true).defineReferencesTo(typesBuilder.getSchemaType(User.SCHEMA_TYPE));
			savedSearchSchema.createUndeletable(SavedSearch.SHARED_GROUPS).setType(MetadataValueType.REFERENCE)
					.setMultivalue(true).defineReferencesTo(typesBuilder.getSchemaType(Group.SCHEMA_TYPE));
			savedSearchSchema.createUndeletable(SavedSearch.RESTRICTED).setType(MetadataValueType.BOOLEAN)
					.setEssential(false).defineDataEntry().asCalculated(SavedSearchRestrictedCalculator2.class);
		}
	}
}
