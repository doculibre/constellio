package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.model.CopyRetentionRuleBuilder;
import com.constellio.app.modules.rm.model.calculators.FolderCalendarYearCalculator;
import com.constellio.app.modules.rm.model.calculators.document.DocumentCalendarYearCalculator;
import com.constellio.app.modules.rm.model.calculators.document.DocumentMainCopyRuleCalculator2;
import com.constellio.app.modules.rm.model.calculators.folder.FolderMainCopyRuleCalculator2;
import com.constellio.app.modules.rm.model.calculators.rule.RuleDocumentTypesCalculator2;
import com.constellio.app.modules.rm.model.calculators.rule.RuleFolderTypesCalculator;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.modules.rm.wrappers.type.FolderType;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaTypesDisplayTransactionBuilder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.records.ActionExecutorInBatch;
import com.constellio.model.entities.records.ImpactHandlingMode;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;

import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class RMMigrationTo6_2 implements MigrationScript {
	@Override
	public String getVersion() {
		return "6.2";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {
		new SchemaAlterationsFor6_2(collection, migrationResourcesProvider, appLayerFactory).migrate();
		setupDisplayConfigs(collection, appLayerFactory);
		addIdsToCopyRetentionRules(collection, appLayerFactory);
	}

	private void setupDisplayConfigs(String collection, AppLayerFactory factory) {
		SchemasDisplayManager schemaDisplayManager = factory.getMetadataSchemasDisplayManager();

		SchemaTypesDisplayTransactionBuilder transaction = schemaDisplayManager.newTransactionBuilderFor(collection);

		transaction
				.in(Folder.SCHEMA_TYPE).addToForm(Folder.MAIN_COPY_RULE_ID_ENTERED).afterMetadata(Folder.COPY_STATUS_ENTERED)
				.in(Document.SCHEMA_TYPE).addToForm(Document.MAIN_COPY_RULE_ID_ENTERED).afterMetadata(Document.TITLE);

		schemaDisplayManager.execute(transaction.build());
	}

	private void addIdsToCopyRetentionRules(String collection, final AppLayerFactory appLayerFactory)
			throws Exception {
		SearchServices searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
		final RecordServices recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
		final RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
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
				transaction.getRecordUpdateOptions().setImpactHandlingMode(ImpactHandlingMode.NEXT_SYSTEM_REINDEXING);
				recordServices.execute(transaction);
			}
		}.execute(from(rm.retentionRule.schemaType()).returnAll());
	}

	public static class SchemaAlterationsFor6_2 extends MetadataSchemasAlterationHelper {
		protected SchemaAlterationsFor6_2(String collection, MigrationResourcesProvider provider,
										  AppLayerFactory factory) {
			super(collection, provider, factory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			updateDocumentSchema(typesBuilder.getSchemaType(Document.SCHEMA_TYPE).getDefaultSchema());
			updateFolderSchema(typesBuilder.getSchemaType(Folder.SCHEMA_TYPE).getDefaultSchema());
			updateRetentionRuleSchema(typesBuilder.getSchema(RetentionRule.DEFAULT_SCHEMA));
		}

		private void updateRetentionRuleSchema(MetadataSchemaBuilder schema) {
			MetadataSchemaTypeBuilder folderTypeSchemaType = types().getSchemaType(FolderType.SCHEMA_TYPE);
			schema.get(RetentionRule.DESCRIPTION).setSchemaAutocomplete(true);
			schema.get(RetentionRule.DOCUMENT_TYPES).defineDataEntry().asCalculated(RuleDocumentTypesCalculator2.class);
			schema.create(RetentionRule.FOLDER_TYPES).setMultivalue(true)
					.defineReferencesTo(folderTypeSchemaType)
					.defineDataEntry().asCalculated(RuleFolderTypesCalculator.class);
		}

		private void updateDocumentSchema(MetadataSchemaBuilder documentSchemaType) {
			documentSchemaType.createUndeletable(Document.MAIN_COPY_RULE_ID_ENTERED).setType(MetadataValueType.STRING);

			documentSchemaType.get(Document.MAIN_COPY_RULE).defineDataEntry().asCalculated(DocumentMainCopyRuleCalculator2.class)
					.setDefaultRequirement(true);
			//TODO remove in new script
			documentSchemaType.createUndeletable("calendarYearEntered").setType(MetadataValueType.STRING);

			documentSchemaType.createUndeletable("calendarYear").setType(MetadataValueType.DATE).defineDataEntry()
					.asCalculated(DocumentCalendarYearCalculator.class);
		}

		private void updateFolderSchema(MetadataSchemaBuilder folderSchemaType) {
			folderSchemaType.createUndeletable(Folder.MAIN_COPY_RULE_ID_ENTERED).setType(MetadataValueType.STRING);
			folderSchemaType.get(Folder.MAIN_COPY_RULE).defineDataEntry().asCalculated(FolderMainCopyRuleCalculator2.class);

			//FIXME remove in another
			folderSchemaType.createUndeletable("calendarYearEntered").setType(MetadataValueType.STRING);

			folderSchemaType.createUndeletable("calendarYear").setType(MetadataValueType.DATE).defineDataEntry()
					.asCalculated(FolderCalendarYearCalculator.class);
		}
	}
}
