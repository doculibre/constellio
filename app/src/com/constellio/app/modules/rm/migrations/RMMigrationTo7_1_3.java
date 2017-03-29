package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.model.wrappers.request.BorrowRequest;
import com.constellio.app.modules.tasks.model.wrappers.request.ExtensionRequest;
import com.constellio.app.modules.tasks.model.wrappers.request.ReactivationRequest;
import com.constellio.app.modules.tasks.model.wrappers.request.ReturnRequest;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class RMMigrationTo7_1_3 extends MigrationHelper implements MigrationScript {
	@Override
	public String getVersion() {
		return "7.1.3";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory)
			throws Exception {
		new SchemaAlterationFor7_1_3(collection, migrationResourcesProvider, appLayerFactory).migrate();
		TasksSchemasRecordsServices taskSchemas = new TasksSchemasRecordsServices(collection, appLayerFactory);
		Transaction transaction = new Transaction();
		transaction.add(taskSchemas.newTaskType().setCode("borrowRequest").setTitle("Demande d'emprunt").setLinkedSchema(Task.SCHEMA_TYPE + "_" + BorrowRequest.SCHEMA_NAME));
		transaction.add(taskSchemas.newTaskType().setCode("returnRequest").setTitle("Demande de retour").setLinkedSchema(Task.SCHEMA_TYPE + "_" + ReturnRequest.SCHEMA_NAME));
		transaction.add(taskSchemas.newTaskType().setCode("reactivationRequest").setTitle("Demande de réactivation").setLinkedSchema(Task.SCHEMA_TYPE + "_" + ReactivationRequest.SCHEMA_NAME));
		transaction.add(taskSchemas.newTaskType().setCode("borrowExtensionRequest").setTitle("Demande de prolongation d'emprunt").setLinkedSchema(Task.SCHEMA_TYPE + "_" + ExtensionRequest.SCHEMA_NAME));
		appLayerFactory.getModelLayerFactory().newRecordServices().execute(transaction);
	}

	private class SchemaAlterationFor7_1_3 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationFor7_1_3(String collection, MigrationResourcesProvider migrationResourcesProvider,
				AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaTypeBuilder taskSchemaType = typesBuilder.getSchemaType(Task.SCHEMA_TYPE);
			taskSchemaType.createCustomSchema(BorrowRequest.SCHEMA_NAME);
			taskSchemaType.createCustomSchema(ReturnRequest.SCHEMA_NAME);
			taskSchemaType.createCustomSchema(ReactivationRequest.SCHEMA_NAME);
			taskSchemaType.createCustomSchema(ExtensionRequest.SCHEMA_NAME);

			typesBuilder.getDefaultSchema(ContainerRecord.SCHEMA_TYPE).createUndeletable(ContainerRecord.BORROW_RETURN_DATE)
					.setType(MetadataValueType.DATE_TIME);

			typesBuilder.getSchema(Task.SCHEMA_TYPE + "_" + ExtensionRequest.SCHEMA_NAME).create(ExtensionRequest.EXTENSION_VALUE).defineDataEntry().asManual().setType(MetadataValueType.DATE);
		}
	}
}
