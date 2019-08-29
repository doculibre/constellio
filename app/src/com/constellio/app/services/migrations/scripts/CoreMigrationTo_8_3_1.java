package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServer;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServerTransaction;
import com.constellio.data.dao.services.bigVault.solr.SolrUtils;
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
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import org.apache.solr.common.SolrInputDocument;

import java.util.ArrayList;
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
			ModelLayerFactory modelLayerFactory = appLayerFactory.getModelLayerFactory();
			final MetadataSchemasManager schemasManager = modelLayerFactory.getMetadataSchemasManager();
			MetadataSchemaTypes schemaTypes = schemasManager.getSchemaTypes(collection);

			LogicalSearchQuery allSavedSearchQuery = new LogicalSearchQuery()
					.setCondition(from(schemaTypes.getSchemaType(SavedSearch.SCHEMA_TYPE)).returnAll());
			List<Record> savedSearches = modelLayerFactory.newSearchServices().search(allSavedSearchQuery);

			List<SolrInputDocument> solrInputDocuments = new ArrayList<>();
			final Map<String, List<String>> sharedGroups = new HashMap<>();
			final Map<String, List<String>> sharedUsers = new HashMap<>();
			for (Record savedSearch : savedSearches) {
				SolrInputDocument inputDocument = new SolrInputDocument();
				Metadata searchGroups = schemaTypes.getMetadata(SAVED_SEACH_SHARED_GROUPS_CODE);
				Metadata searchUsers = schemaTypes.getMetadata(SAVED_SEACH_SHARED_USERS_CODE);
				List<String> searchGroupsValues = savedSearch.getValues(searchGroups);
				List<String> searchUsersValues = savedSearch.getValues(searchUsers);

				inputDocument.setField("id", savedSearch.getId());
				if (!searchGroupsValues.isEmpty()) {
					inputDocument.setField("sharedGroupsId_ss", SolrUtils.atomicSet(searchGroupsValues));
					inputDocument.setField("sharedGroups_ss", SolrUtils.atomicSet(null));
				}
				if (!searchUsersValues.isEmpty()) {
					inputDocument.setField("sharedUsersId_ss", SolrUtils.atomicSet(searchUsersValues));
					inputDocument.setField("sharedUsers_ss", SolrUtils.atomicSet(null));
				}

				sharedGroups.put(savedSearch.getId(), searchGroupsValues);
				sharedUsers.put(savedSearch.getId(), searchUsersValues);

				if (inputDocument.get("sharedGroupsId_ss") != null || inputDocument.get("sharedUsersId_ss") != null) {
					solrInputDocuments.add(inputDocument);
				}
			}

			BigVaultServerTransaction transaction = new BigVaultServerTransaction(RecordsFlushing.NOW())
					.setUpdatedDocuments(solrInputDocuments);
			BigVaultServer vaultServer = modelLayerFactory.getDataLayerFactory().getRecordsVaultServer();
			vaultServer.addAll(transaction);

			new CoreSchemaAlterationFor8_3_1_delete(collection, provider, appLayerFactory).migrate();
			new CoreSchemaAlterationFor8_3_1_recreate(collection, provider, appLayerFactory).migrate();

			new ConditionnedActionExecutorInBatchBuilder(modelLayerFactory, allSavedSearchQuery.getCondition())
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
