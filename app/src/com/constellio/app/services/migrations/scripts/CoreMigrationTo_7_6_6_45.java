package com.constellio.app.services.migrations.scripts;

import static com.constellio.data.dao.dto.records.OptimisticLockingResolution.EXCEPTION;
import static com.constellio.model.entities.records.RecordUpdateOptions.validationExceptionSafeOptions;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.ActionExecutorInBatch;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;

public class CoreMigrationTo_7_6_6_45 implements MigrationScript {

	private static final Logger LOGGER = LoggerFactory.getLogger(CoreMigrationTo_7_6_6_45.class);

	@Override
	public String getVersion() {
		return "7.6.6.45";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory)
			throws Exception {
		new CoreSchemaAlterationFor_7_6_6_1(collection, migrationResourcesProvider, appLayerFactory).migrate();

		final SchemasRecordsServices schemas = new SchemasRecordsServices(collection, appLayerFactory.getModelLayerFactory());
		SearchServices searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
		final RecordServices recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();

		new ActionExecutorInBatch(searchServices, "set auth's target schema type", 10000) {

			@Override
			public void doActionOnBatch(List<Record> records)
					throws Exception {

				Transaction tx = new Transaction();
				tx.setOptions(validationExceptionSafeOptions().setOptimisticLockingResolution(EXCEPTION));

				for (Record record : records) {
					User user = schemas.wrapUser(record);
					user.set("alluserauthorizations", new ArrayList<>());
					user.set("groupsauthorizations", new ArrayList<>());
					tx.add(user);
				}

				recordServices.execute(tx);

			}
		}.execute(from(schemas.user.schemaType())
				.where(schemas.user.schema().get("groupsauthorizations")).isNotNull());
	}

	class CoreSchemaAlterationFor_7_6_6_1 extends MetadataSchemasAlterationHelper {

		protected CoreSchemaAlterationFor_7_6_6_1(String collection, MigrationResourcesProvider migrationResourcesProvider,
				AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaBuilder userSchema = typesBuilder.getSchema(User.DEFAULT_SCHEMA);
			if (userSchema.hasMetadata("groupsauthorizations")) {
				MetadataBuilder groupsauthorizations = userSchema.get("groupsauthorizations");
				groupsauthorizations.defineDataEntry().asManual();
				groupsauthorizations.setEnabled(false).setMarkedForDeletion(true);
			}

			if (userSchema.hasMetadata("alluserauthorizations")) {
				MetadataBuilder alluserauthorizations = userSchema.get("alluserauthorizations");
				alluserauthorizations.defineDataEntry().asManual();
				alluserauthorizations.setEnabled(false).setMarkedForDeletion(true);
			}

		}
	}
}
