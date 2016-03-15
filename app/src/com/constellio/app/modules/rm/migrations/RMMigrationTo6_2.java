package com.constellio.app.modules.rm.migrations;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import java.util.List;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.model.CopyRetentionRuleBuilder;
import com.constellio.app.modules.rm.model.calculators.FolderCalendarYearCalculator;
import com.constellio.app.modules.rm.model.calculators.document.DocumentCalendarYearCalculator;
import com.constellio.app.modules.rm.model.calculators.folder.FolderMainCopyRuleCalculator2;
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
import com.constellio.model.services.records.UnhandledRecordModificationImpactHandler;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;

public class RMMigrationTo6_2 implements MigrationScript {
	@Override
	public String getVersion() {
		return "6.2";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
			final AppLayerFactory appLayerFactory)
			throws Exception {
		new SchemaAlterationsFor6_2(collection, migrationResourcesProvider, appLayerFactory).migrate();

		SearchServices searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
		final RecordServices recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
		final RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory.getModelLayerFactory());
		new ActionExecutorInBatch(searchServices, "Set copy retention rule ids", 250) {

			@Override
			public void doActionOnBatch(List<Record> records)
					throws Exception {
				Transaction transaction = new Transaction();

				for (RetentionRule rule : rm.wrapRetentionRules(records)) {

					CopyRetentionRuleBuilder builder = CopyRetentionRuleBuilder.sequential(appLayerFactory);
					builder.addIdsTo(rule.getDocumentCopyRetentionRules());
					builder.addIdsTo(rule.getSecondaryCopy());
					builder.addIdsTo(rule.getPrincipalCopies());
					builder.addIdsTo(rule.getPrincipalDefaultDocumentCopyRetentionRule());
					builder.addIdsTo(rule.getSecondaryDefaultDocumentCopyRetentionRule());

					transaction.add(rule);
				}

				transaction.setSkippingRequiredValuesValidation(true);

				recordServices.executeWithImpactHandler(transaction, new UnhandledRecordModificationImpactHandler());

			}
		}.execute(from(rm.retentionRuleSchemaType()).returnAll());

	}

	public static class SchemaAlterationsFor6_2 extends MetadataSchemasAlterationHelper {
		protected SchemaAlterationsFor6_2(String collection, MigrationResourcesProvider provider, AppLayerFactory factory) {
			super(collection, provider, factory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			updateDocumentSchema(typesBuilder.getSchemaType(Document.SCHEMA_TYPE).getDefaultSchema());
			updateFolderSchema(typesBuilder.getSchemaType(Folder.SCHEMA_TYPE).getDefaultSchema());

		}

		private void updateDocumentSchema(MetadataSchemaBuilder documentSchemaType) {
			documentSchemaType.createUndeletable(Document.MAIN_COPY_RULE_ID_ENTERED).setType(MetadataValueType.STRING);

			documentSchemaType.createUndeletable(Document.CALENDAR_YEAR_ENTERED).setType(MetadataValueType.STRING);

			documentSchemaType.createUndeletable(Document.CALENDAR_YEAR).setType(MetadataValueType.DATE).defineDataEntry()
					.asCalculated(DocumentCalendarYearCalculator.class);
		}

		private void updateFolderSchema(MetadataSchemaBuilder folderSchemaType) {
			folderSchemaType.createUndeletable(Folder.MAIN_COPY_RULE_ID_ENTERED).setType(MetadataValueType.STRING);
			folderSchemaType.get(Folder.MAIN_COPY_RULE).defineDataEntry().asCalculated(FolderMainCopyRuleCalculator2.class);

			folderSchemaType.createUndeletable(Folder.CALENDAR_YEAR_ENTERED).setType(MetadataValueType.STRING);

			folderSchemaType.createUndeletable(Folder.CALENDAR_YEAR).setType(MetadataValueType.DATE).defineDataEntry()
					.asCalculated(FolderCalendarYearCalculator.class);
		}
	}
}
