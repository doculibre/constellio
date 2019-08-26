package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.calculators.SavedSearchRestrictedCalculator2;
import com.constellio.model.entities.records.ConditionnedActionExecutorInBatchBuilder;
import com.constellio.model.entities.records.ConditionnedActionExecutorInBatchBuilder.RecordScript;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.RecordUpdateOptions;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.SavedSearch;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class CoreMigrationTo_8_3_1 implements MigrationScript {

	private final String SAVED_SEACH_SHARED_GROUPS_CODE = SavedSearch.DEFAULT_SCHEMA + "_" + SavedSearch.SHARED_GROUPS;
	private final String SAVED_SEACH_SHARED_USERS_CODE = SavedSearch.DEFAULT_SCHEMA + "_" + SavedSearch.SHARED_USERS;

	@Override
	public String getVersion() {
		return "8.3.1";
	}


	@Override
	public void migrate(final String collection, MigrationResourcesProvider provider,
						AppLayerFactory appLayerFactory)
			throws Exception {

		if (!Collection.SYSTEM_COLLECTION.equals(collection)) {
			final MetadataSchemasManager schemasManager = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager();
			MetadataSchemaTypes schemaTypes = schemasManager.getSchemaTypes(collection);
			SearchServices searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
			LogicalSearchQuery allSavedSearchQuery = new LogicalSearchQuery()
					.setCondition(from(schemaTypes.getSchemaType(SavedSearch.SCHEMA_TYPE)).returnAll());
			List<Record> savedSearches = searchServices.search(allSavedSearchQuery);

			final Map<String, List<String>> sharedGroups = new HashMap<>();
			final Map<String, List<String>> sharedUsers = new HashMap<>();
			for (Record savedSearch : savedSearches) {
				sharedGroups.put(savedSearch.getId(), savedSearch.<String>getValues(schemaTypes.getMetadata(SAVED_SEACH_SHARED_GROUPS_CODE)));
				sharedUsers.put(savedSearch.getId(), savedSearch.<String>getValues(schemaTypes.getMetadata(SAVED_SEACH_SHARED_USERS_CODE)));
			}

			new CoreSchemaAlterationFor8_3_1_delete(collection, provider, appLayerFactory).migrate();
			new CoreSchemaAlterationFor8_3_1_recreate(collection, provider, appLayerFactory).migrate();

			new ConditionnedActionExecutorInBatchBuilder(appLayerFactory.getModelLayerFactory(), allSavedSearchQuery.getCondition())
					.setOptions(RecordUpdateOptions.validationExceptionSafeOptions())
					.modifyingRecordsWithImpactHandling(new RecordScript() {

						MetadataSchemaTypes schemaTypes = schemasManager.getSchemaTypes(collection);
						Metadata newSharedGroups = schemaTypes.getMetadata(SAVED_SEACH_SHARED_GROUPS_CODE);
						Metadata newSharedUsers = schemaTypes.getMetadata(SAVED_SEACH_SHARED_USERS_CODE);

						@Override
						public void modifyRecord(Record record) {
							record.set(newSharedGroups, sharedGroups.get(record.getId()));
							record.set(newSharedUsers, sharedUsers.get(record.getId()));
						}
					});
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
