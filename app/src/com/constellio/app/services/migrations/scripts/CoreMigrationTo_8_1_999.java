package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.utils.KeyListMap;
import com.constellio.model.entities.records.ActionExecutorInBatch;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.RecordUpdateOptions;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.SolrAuthorizationDetails;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.LegacyGlobalMetadatas;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class CoreMigrationTo_8_1_999 implements MigrationScript {
	@Override
	public String getVersion() {
		return "8.1.999";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {
		new CoreSchemaAlterationFor_8_1_999(collection, migrationResourcesProvider, appLayerFactory).migrate();

		final SchemasRecordsServices schemas = new SchemasRecordsServices(collection, appLayerFactory.getModelLayerFactory());

		final KeyListMap<String, String> authsPrincipals = new KeyListMap<>();

		final Metadata userAuthsMetadata = schemas.user.schema().get("authorizations");
		final Metadata userAllAuthsMetadata = schemas.user.schema().get("allauthorizations");
		final Metadata groupAuthsMetadata = schemas.group.schema().get("authorizations");

		for (User user : schemas.getAllUsers()) {
			for (String auth : user.<String>getList(userAuthsMetadata)) {
				authsPrincipals.add(auth, user.getId());
			}
		}

		for (Group group : schemas.getAllGroups()) {
			for (String auth : group.<String>getList(groupAuthsMetadata)) {
				authsPrincipals.add(auth, group.getId());
			}
		}

		SearchServices searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
		final RecordServices recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
		new ActionExecutorInBatch(searchServices, "Reverse auth/principals relations - Modify authorizations", 1000) {

			@Override
			public void doActionOnBatch(List<Record> records) throws Exception {
				Transaction tx = new Transaction();
				for (SolrAuthorizationDetails detail : schemas.wrapSolrAuthorizationDetailss(records)) {
					tx.add(detail.setPrincipals(authsPrincipals.get(detail.getId())));
				}

				recordServices.executeWithoutImpactHandling(tx);
			}
		}.execute(from(schemas.authorizationDetails.schemaType()).returnAll());

		new ActionExecutorInBatch(searchServices, "Reverse auth/principals relations - Modify users", 1000) {

			@Override
			public void doActionOnBatch(List<Record> records) throws Exception {
				Transaction tx = new Transaction();
				tx.setOptions(RecordUpdateOptions.validationExceptionSafeOptions());

				for (User user : schemas.wrapUsers(records)) {
					tx.add(user.set(userAllAuthsMetadata, new ArrayList<>()).set(userAuthsMetadata, new ArrayList<>()));
				}

				recordServices.executeWithoutImpactHandling(tx);
			}
		}.execute(from(schemas.user.schemaType()).returnAll());

		new ActionExecutorInBatch(searchServices, "Reverse auth/principals relations - Modify groups", 1000) {

			@Override
			public void doActionOnBatch(List<Record> records) throws Exception {
				Transaction tx = new Transaction();
				tx.setOptions(RecordUpdateOptions.validationExceptionSafeOptions());

				for (Group group : schemas.wrapGroups(records)) {
					tx.add(group.set(groupAuthsMetadata, new ArrayList<>()));
				}

				recordServices.executeWithoutImpactHandling(tx);
			}
		}.execute(from(schemas.group.schemaType()).returnAll());

	}

	class CoreSchemaAlterationFor_8_1_999 extends MetadataSchemasAlterationHelper {

		protected CoreSchemaAlterationFor_8_1_999(String collection,
												  MigrationResourcesProvider migrationResourcesProvider,
												  AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {

			if (!typesBuilder.getSchema(SolrAuthorizationDetails.DEFAULT_SCHEMA).hasMetadata(SolrAuthorizationDetails.PRINCIPALS)) {
				typesBuilder.getSchema(SolrAuthorizationDetails.DEFAULT_SCHEMA).create(SolrAuthorizationDetails.PRINCIPALS)
						.setType(STRING).setMultivalue(true);
			}

			for (MetadataSchemaTypeBuilder schemaBuilder : typesBuilder.getTypes()) {
				if (schemaBuilder.getDefaultSchema().hasMetadata("authorizations")) {
					schemaBuilder.getDefaultSchema().get("authorizations")
							.setEnabled(false).setMarkedForDeletion(true).defineDataEntry().asManual();
				}

				if (schemaBuilder.getDefaultSchema().hasMetadata("allauthorizations")) {
					schemaBuilder.getDefaultSchema().get("allauthorizations")
							.setEnabled(false).setMarkedForDeletion(true).defineDataEntry().asManual();
				}

				if (schemaBuilder.getDefaultSchema().hasMetadata("inheritedauthorizations")) {
					schemaBuilder.getDefaultSchema().get("inheritedauthorizations")
							.setEnabled(false).setMarkedForDeletion(true).defineDataEntry().asManual();
				}

				if (schemaBuilder.getDefaultSchema().hasMetadata(LegacyGlobalMetadatas.FOLLOWERS.getLocalCode())) {
					schemaBuilder.getDefaultSchema().get(LegacyGlobalMetadatas.FOLLOWERS.getLocalCode())
							.setEnabled(false).setMarkedForDeletion(true).defineDataEntry().asManual();
				}



			}

		}
	}
}
