package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaDisplayManagerTransaction;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.ActionExecutorInBatch;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.RecordAuthorization;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.constellio.data.dao.dto.records.OptimisticLockingResolution.EXCEPTION;
import static com.constellio.model.entities.records.RecordUpdateOptions.validationExceptionSafeOptions;
import static com.constellio.model.entities.schemas.MetadataValueType.DATE;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class CoreMigrationTo_7_6_6 implements MigrationScript {

	private static final Logger LOGGER = LoggerFactory.getLogger(CoreMigrationTo_7_6_6.class);

	@Override
	public String getVersion() {
		return "7.6.6";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {

		new CoreSchemaAlterationFor_7_6_6(collection, migrationResourcesProvider, appLayerFactory).migrate();
		SchemasDisplayManager displayManager = appLayerFactory.getMetadataSchemasDisplayManager();
		SchemaDisplayManagerTransaction transaction = new SchemaDisplayManagerTransaction();
		transaction.add(displayManager.getSchema(collection, User.DEFAULT_SCHEMA)
				.withNewDisplayMetadataQueued(User.DEFAULT_SCHEMA + "_" + User.DEFAULT_PAGE_LENGTH));
		displayManager.execute(transaction);

		appLayerFactory.getSystemGlobalConfigsManager().setReindexingRequired(true);

		final SchemasRecordsServices schemas = new SchemasRecordsServices(collection, appLayerFactory.getModelLayerFactory());
		SearchServices searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
		final RecordServices recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();

		final Taxonomy principalTaxonomy = appLayerFactory.getModelLayerFactory().getTaxonomiesManager()
				.getPrincipalTaxonomy(collection);

		if (principalTaxonomy != null) {
			new ActionExecutorInBatch(searchServices, "set auth's target schema type", 10000) {

				@Override
				public void doActionOnBatch(List<Record> records)
						throws Exception {

					Transaction tx = new Transaction();
					tx.setOptions(validationExceptionSafeOptions().setOptimisticLockingResolution(EXCEPTION));

					for (Record record : records) {
						RecordAuthorization auth = (RecordAuthorization) schemas.wrapAuthorization(record);

						try {
							Record targetRecord = recordServices.getDocumentById(auth.getTarget());
							auth.setTargetSchemaType(targetRecord.getTypeCode());
							if (!principalTaxonomy.getSchemaTypes().contains(targetRecord.getTypeCode())) {
								auth.setLastTokenRecalculate(TimeProvider.getLocalDate());
							}
							tx.add(auth);
						} catch (Exception e) {
							LOGGER.info("Auth target is deleted, a case fixed with a system check", e);
						}

					}

					recordServices.execute(tx);

				}
			}.execute(from(schemas.authorizationDetails.schemaType()).returnAll());
		}
	}

	class CoreSchemaAlterationFor_7_6_6 extends MetadataSchemasAlterationHelper {

		protected CoreSchemaAlterationFor_7_6_6(String collection,
												MigrationResourcesProvider migrationResourcesProvider,
												AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaBuilder authorizationSchema = typesBuilder.getSchema(RecordAuthorization.DEFAULT_SCHEMA);
			if (!authorizationSchema.hasMetadata(RecordAuthorization.TARGET_SCHEMA_TYPE)) {
				authorizationSchema.createUndeletable(RecordAuthorization.TARGET_SCHEMA_TYPE).setType(STRING);
				authorizationSchema.createUndeletable(RecordAuthorization.LAST_TOKEN_RECALCULATE).setType(DATE);
			}
			typesBuilder.getSchema(RecordAuthorization.DEFAULT_SCHEMA).create(RecordAuthorization.PRINCIPALS)
					.setType(STRING).setMultivalue(true);

		}
	}
}
