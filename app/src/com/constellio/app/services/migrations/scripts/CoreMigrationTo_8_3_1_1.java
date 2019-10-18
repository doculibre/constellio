package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.model.enums.DefaultTabInFolderDisplay;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.ActionExecutorInBatch;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.RecordUpdateOptions;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.search.SearchServices;

import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class CoreMigrationTo_8_3_1_1 implements MigrationScript {

	@Override
	public String getVersion() {
		return "8.3.1.1";
	}

	@Override
	public void migrate(final String collection, MigrationResourcesProvider provider,
						final AppLayerFactory appLayerFactory)
			throws Exception {

		final RecordServices recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
		final SearchServices searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
		final SchemasRecordsServices schemas = new SchemasRecordsServices(collection, appLayerFactory.getModelLayerFactory());
		final Metadata defaultTabInFolderDisplay = schemas.user.schema().get(User.DEFAULT_TAB_IN_FOLDER_DISPLAY);

		new ActionExecutorInBatch(searchServices, "Set defaut tab in folder to C if was D or S", 1000) {

			@Override
			public void doActionOnBatch(List<Record> records) throws Exception {
				Transaction tx = new Transaction();
				tx.setOptions(RecordUpdateOptions.validationExceptionSafeOptions());

				for (User user : schemas.wrapUsers(records)) {
					Object setting = user.get(defaultTabInFolderDisplay);
					if (setting != null && setting.toString().matches("D|SF")) {
						tx.add((User) user.set(defaultTabInFolderDisplay, DefaultTabInFolderDisplay.CONTENT.getCode()));
					}
				}

				recordServices.executeWithoutImpactHandling(tx);
			}
		}.execute(from(schemas.user.schemaType()).returnAll());
	}
}
