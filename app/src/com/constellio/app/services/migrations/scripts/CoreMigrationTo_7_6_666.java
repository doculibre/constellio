package com.constellio.app.services.migrations.scripts;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import java.util.List;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.ActionExecutorInBatch;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.SolrAuthorizationDetails;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;

public class CoreMigrationTo_7_6_666 implements MigrationScript {
	@Override
	public String getVersion() {
		return "7.6.666";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
			final AppLayerFactory appLayerFactory)
			throws Exception {
		new CoreSchemaAlterationFor_7_6_666(collection, migrationResourcesProvider, appLayerFactory).migrate();
		appLayerFactory.getSystemGlobalConfigsManager().setReindexingRequired(true);

		final SchemasRecordsServices schemas = new SchemasRecordsServices(collection, appLayerFactory.getModelLayerFactory());
		SearchServices searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
		final RecordServices recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
		new ActionExecutorInBatch(searchServices, "set auth's target schema type", 10000) {

			@Override
			public void doActionOnBatch(List<Record> records)
					throws Exception {

				Transaction tx = new Transaction();

				for (Record record : records) {
					SolrAuthorizationDetails auth = schemas.wrapSolrAuthorizationDetails(record);

					try {
						Record targetRecord = recordServices.getDocumentById(auth.getTarget());
						auth.setTargetSchemaType(targetRecord.getTypeCode());
					} catch (Exception e) {

					}

					tx.add(auth);
				}

				recordServices.execute(tx);

			}
		}.execute(from(schemas.authorizationDetails.schemaType()).returnAll());

	}

	class CoreSchemaAlterationFor_7_6_666 extends MetadataSchemasAlterationHelper {

		protected CoreSchemaAlterationFor_7_6_666(String collection, MigrationResourcesProvider migrationResourcesProvider,
				AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaBuilder authorizationSchema = typesBuilder.getSchema(SolrAuthorizationDetails.DEFAULT_SCHEMA);
			authorizationSchema.createUndeletable(SolrAuthorizationDetails.TARGET_SCHEMA_TYPE).setType(MetadataValueType.STRING);

//			for (MetadataSchemaTypeBuilder typeBuilder : typesBuilder.getTypes()) {
			//				MetadataBuilder tokens = typeBuilder.getDefaultSchema().get(Schemas.TOKENS);
			//				if (!asList(Collection.SCHEMA_TYPE, User.SCHEMA_TYPE, Group.SCHEMA_TYPE).contains(typeBuilder.getCode())
			//						&& ((CalculatedDataEntry) tokens.getDataEntry()).getCalculator().getClass()
			//						.equals(TokensCalculator2.class)) {
			//					tokens.defineDataEntry().asCalculated(TokensCalculator4.class);
			//				}
			//
			//			}

		}
	}
}
