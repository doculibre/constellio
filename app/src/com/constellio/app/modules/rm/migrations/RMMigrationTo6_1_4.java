package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.ActionExecutorInBatch;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.SearchServices;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class RMMigrationTo6_1_4 implements MigrationScript {
	@Override
	public String getVersion() {
		return "6.1.4";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {
		setSubFoldersEnteredFieldsToNull(collection, appLayerFactory);
	}

	private void setSubFoldersEnteredFieldsToNull(String collection, AppLayerFactory appLayerFactory)
			throws Exception {

		SearchServices searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
		final RecordServices recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
		final RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		final AtomicBoolean recordFixed = new AtomicBoolean(false);

		new ActionExecutorInBatch(searchServices, "Set sub-folders entered values to null", 250) {

			@Override
			public void doActionOnBatch(List<Record> records)
					throws Exception {
				Transaction transaction = new Transaction();

				for (Folder folder : rm.wrapFolders(records)) {

					folder.setAdministrativeUnitEntered((String) null);
					folder.setCategoryEntered((String) null);
					folder.setRetentionRuleEntered((String) null);
					folder.setCopyStatusEntered(null);

					transaction.add(folder);
				}

				transaction.setSkippingRequiredValuesValidation(true);

				recordServices.executeWithoutImpactHandling(transaction);

			}
		}.execute(from(rm.folder.schemaType()).where(rm.folder.parentFolder()).isNotNull());

		if (recordFixed.get()) {
			appLayerFactory.getSystemGlobalConfigsManager().setReindexingRequired(true);
		}
	}
}
