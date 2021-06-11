package com.constellio.app.services.migrations.scripts;


import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.ActionExecutorInBatch;
import com.constellio.model.entities.records.ImpactHandlingMode;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.SavedSearch;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;

import java.util.List;

import static com.constellio.model.entities.schemas.MetadataValueType.BOOLEAN;
import static com.constellio.model.entities.schemas.MetadataValueType.NUMBER;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class CoreMigrationTo_6_3 implements MigrationScript {
	@Override
	public String getVersion() {
		return "6.3";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider provider, AppLayerFactory appLayerFactory)
			throws Exception {
		//FIXME is called several times for the different installed modules!!
		new CoreSchemaAlterationFor6_3(collection, provider, appLayerFactory).migrate();
		appLayerFactory.getSystemGlobalConfigsManager().setReindexingRequired(true);
		//initializeUsersLanguages(collection, appLayerFactory);
	}

	private void initializeUsersLanguages(final String collection, AppLayerFactory appLayerFactory) throws Exception {
		//FIXME not possible since collection not created yet!
		final String collectionLanguageCode = appLayerFactory.getCollectionsManager().getCollection(collection).getLanguages().get(0);
		SearchServices searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
		final RecordServices recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
		final RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		new ActionExecutorInBatch(searchServices, "Create users login languages.", 1000) {
			@Override
			public void doActionOnBatch(List<Record> records)
					throws Exception {
				Transaction transaction = new Transaction();

				for (User user : rm.wrapUsers(records)) {
					user.setLoginLanguageCode(collectionLanguageCode);
					transaction.add(user);
				}

				transaction.setSkippingRequiredValuesValidation(true);
				transaction.getRecordUpdateOptions().setImpactHandlingMode(ImpactHandlingMode.NEXT_SYSTEM_REINDEXING);
				recordServices.execute(transaction);
			}
		}.execute(from(rm.userSchema()).returnAll());
	}

	private class CoreSchemaAlterationFor6_3 extends MetadataSchemasAlterationHelper {
		public CoreSchemaAlterationFor6_3(String collection, MigrationResourcesProvider migrationResourcesProvider,
										  AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaBuilder user = typesBuilder.getSchemaType(User.SCHEMA_TYPE).getDefaultSchema();
			user.createUndeletable(User.LOGIN_LANGUAGE_CODE).setType(MetadataValueType.STRING);
			MetadataSchemaTypeBuilder type = typesBuilder.getSchemaType(SavedSearch.SCHEMA_TYPE);
			MetadataSchemaBuilder defaultSchema = type.getDefaultSchema();
			defaultSchema.createUndeletable(SavedSearch.TEMPORARY).setType(BOOLEAN);
			defaultSchema.createUndeletable(SavedSearch.PAGE_NUMBER).setType(NUMBER);

		}
	}
}
