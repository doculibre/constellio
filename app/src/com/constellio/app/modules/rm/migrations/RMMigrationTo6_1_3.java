package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.ActionExecutorInBatch;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;

import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class RMMigrationTo6_1_3 implements MigrationScript {
	@Override
	public String getVersion() {
		return "6.1.3";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory)
			throws Exception {
		setSubFoldersEnteredFieldsToNull(collection,appLayerFactory);
	}

	private void setSubFoldersEnteredFieldsToNull(String collection,AppLayerFactory appLayerFactory) throws Exception {
		// TODO Fetch all folders with a parent
		// TODO Set all entered values to null in a way that scales and doesn't blow up the computer

		SearchServices searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
		final RecordServices recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
		final RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory.getModelLayerFactory());
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

				recordServices.execute(transaction);

			}
		}.execute(from(rm.folderSchemaType()).where(rm.folderParentFolder()).isNotNull());

	}
}
