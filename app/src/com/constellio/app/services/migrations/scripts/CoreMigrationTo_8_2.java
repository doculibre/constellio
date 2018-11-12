package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.core.CoreTypes;
import com.constellio.app.modules.rm.model.calculators.UserDocumentContentSizeCalculator;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.utils.KeyListMap;
import com.constellio.model.entities.records.ActionExecutorInBatch;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.RecordUpdateOptions;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Authorization;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.records.wrappers.UserDocument;
import com.constellio.model.entities.schemas.LegacyGlobalMetadatas;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class CoreMigrationTo_8_2 implements MigrationScript {
	@Override
	public String getVersion() {
		return "8.2";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {
		new CoreSchemaAlterationFor_8_2(collection, migrationResourcesProvider, appLayerFactory).migrate();

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
				for (Authorization detail : schemas.wrapSolrAuthorizationDetailss(records)) {
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

	class CoreSchemaAlterationFor_8_2 extends MetadataSchemasAlterationHelper {

		protected CoreSchemaAlterationFor_8_2(String collection,
											  MigrationResourcesProvider migrationResourcesProvider,
											  AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {

			if (!typesBuilder.getSchema(Authorization.DEFAULT_SCHEMA).hasMetadata(Authorization.PRINCIPALS)) {
				typesBuilder.getSchema(Authorization.DEFAULT_SCHEMA).create(Authorization.PRINCIPALS)
						.setType(STRING).setMultivalue(true);
			}

			for (MetadataSchemaTypeBuilder schemaBuilder : typesBuilder.getTypes()) {
				if (schemaBuilder.getDefaultSchema().hasMetadata(LegacyGlobalMetadatas.AUTHORIZATIONS.getLocalCode())) {
					schemaBuilder.getDefaultSchema().get(LegacyGlobalMetadatas.AUTHORIZATIONS.getLocalCode())
							.setEnabled(false).setMarkedForDeletion(true).defineDataEntry().asManual();
				}

				if (schemaBuilder.getDefaultSchema().hasMetadata(LegacyGlobalMetadatas.ALL_AUTHORIZATIONS.getLocalCode())) {
					schemaBuilder.getDefaultSchema().get(LegacyGlobalMetadatas.ALL_AUTHORIZATIONS.getLocalCode())
							.setEnabled(false).setMarkedForDeletion(true).defineDataEntry().asManual();
				}

				if (schemaBuilder.getDefaultSchema().hasMetadata(LegacyGlobalMetadatas.INHERITED_AUTHORIZATIONS.getLocalCode())) {
					schemaBuilder.getDefaultSchema().get(LegacyGlobalMetadatas.INHERITED_AUTHORIZATIONS.getLocalCode())
							.setEnabled(false).setMarkedForDeletion(true).defineDataEntry().asManual();
				}

				if (schemaBuilder.getDefaultSchema().hasMetadata(LegacyGlobalMetadatas.FOLLOWERS.getLocalCode())) {
					schemaBuilder.getDefaultSchema().get(LegacyGlobalMetadatas.FOLLOWERS.getLocalCode())
							.setEnabled(false).setMarkedForDeletion(true).defineDataEntry().asManual();
				}

				if (schemaBuilder.getDefaultSchema().hasMetadata(LegacyGlobalMetadatas.PARENT_PATH.getLocalCode())) {
					schemaBuilder.getDefaultSchema().get(LegacyGlobalMetadatas.PARENT_PATH.getLocalCode())
							.setEnabled(false).setMarkedForDeletion(true).defineDataEntry().asManual();
				}

				if (schemaBuilder.getDefaultSchema().hasMetadata(LegacyGlobalMetadatas.NON_TAXONOMY_AUTHORIZATIONS.getLocalCode())) {
					schemaBuilder.getDefaultSchema().get(LegacyGlobalMetadatas.NON_TAXONOMY_AUTHORIZATIONS.getLocalCode())
							.setEnabled(false).setMarkedForDeletion(true).defineDataEntry().asManual();
				}


			}

			for (MetadataSchemaTypeBuilder coreType : CoreTypes.coreSchemaTypes(typesBuilder)) {
				coreType.setSecurity(false);
			}

			MetadataSchemaBuilder userDocumentSchema = typesBuilder.getDefaultSchema(UserDocument.SCHEMA_TYPE);

			if (!userDocumentSchema.hasMetadata(UserDocument.CONTENT_SIZE)) {
				MetadataBuilder userDocumentContentSize = userDocumentSchema.createUndeletable(UserDocument.CONTENT_SIZE)
						.setType(MetadataValueType.NUMBER).defineDataEntry()
						.asCalculated(UserDocumentContentSizeCalculator.class);

				MetadataSchemaBuilder userSchema = typesBuilder.getDefaultSchema(User.SCHEMA_TYPE);
				userSchema.createUndeletable(User.USER_DOCUMENT_SIZE_SUM)
						.setType(MetadataValueType.NUMBER).setEssential(false).defineDataEntry()
						.asSum(userDocumentSchema.getMetadata(UserDocument.USER), userDocumentContentSize);
			}
		}
	}
}
