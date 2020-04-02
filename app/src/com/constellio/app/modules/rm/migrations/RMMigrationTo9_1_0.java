package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.entities.schemasDisplay.SchemaDisplayConfig;
import com.constellio.app.modules.rm.model.ExternalLinkType;
import com.constellio.app.modules.rm.model.calculators.document.DocumentCheckedOutDateCalculator;
import com.constellio.app.modules.rm.model.calculators.document.DocumentExpectedTransferDateCalculator;
import com.constellio.app.modules.rm.model.calculators.document.DocumentFilenameCalculator;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.ValueListItemSchemaTypeBuilder;
import com.constellio.app.modules.rm.services.ValueListItemSchemaTypeBuilder.ValueListItemSchemaTypeBuilderOptions;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.ExternalLink;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RMTask;
import com.constellio.app.modules.rm.wrappers.triggers.Trigger;
import com.constellio.app.modules.rm.wrappers.triggers.TriggerAction;
import com.constellio.app.modules.rm.wrappers.triggers.TriggerActionType;
import com.constellio.app.modules.rm.wrappers.triggers.TriggerType;
import com.constellio.app.modules.rm.wrappers.triggers.actions.MoveInFolderTriggerAction;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.migrations.MigrationUtil;
import com.constellio.app.services.schemasDisplay.SchemaTypesDisplayTransactionBuilder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.app.ui.pages.search.criteria.CriterionFactory;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.records.wrappers.SavedSearch;
import com.constellio.model.entities.records.wrappers.SearchEvent;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.schemas.entries.CalculatedDataEntry;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.services.schemas.builders.CommonMetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.schemas.calculators.AttachedAncestorsCalculator2;

import java.util.Locale;
import java.util.Map;

import static com.constellio.model.entities.schemas.MetadataValueType.REFERENCE;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static com.constellio.model.entities.schemas.MetadataValueType.STRUCTURE;
import static com.constellio.model.entities.schemas.MetadataValueType.TEXT;
import static org.apache.ignite.internal.util.lang.GridFunc.asList;

public class RMMigrationTo9_1_0 implements MigrationScript {
	@Override
	public String getVersion() {
		return "9.1.0";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory) throws Exception {
		new SchemaAlterationFor9_1(collection, migrationResourcesProvider, appLayerFactory).migrate();
		setupDisplayConfig(collection, appLayerFactory);
		appLayerFactory.getSystemGlobalConfigsManager().setReindexingRequired(true);

		Transaction transaction = new Transaction();
		createRecords(collection, appLayerFactory, transaction);
		appLayerFactory.getModelLayerFactory().newRecordServices().execute(transaction);
	}

	private void createRecords(String collection, AppLayerFactory appLayerFactory, Transaction transaction) {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);

		SchemasDisplayManager schemasDisplayManager = appLayerFactory.getMetadataSchemasDisplayManager();
		SchemaDisplayConfig triggerSchemaDisplayConfig = schemasDisplayManager.getSchema(collection, Trigger.DEFAULT_SCHEMA);

		SchemaDisplayConfig newTriggerSchemaDisplayConfig = triggerSchemaDisplayConfig.withTableMetadataCodes(asList(getDefaultTriggerMetadataCode(Trigger.TITLE),
				getDefaultTriggerMetadataCode(Trigger.DESCRIPTION), getDefaultTriggerMetadataCode(Schemas.MODIFIED_ON.getLocalCode())));

		schemasDisplayManager.saveSchema(newTriggerSchemaDisplayConfig);

		TriggerType triggerType = rm.newTriggerType();
		triggerType.setCode("dtrigger");
		triggerType.setTitle(Locale.FRENCH, "Déclencheur par default");
		triggerType.setTitle(Locale.ENGLISH, "Default trigger");

		TriggerActionType triggerActionType = rm.newTriggerActionType();
		triggerActionType.setCode("MoveInAFolder");
		triggerActionType.setTitle(Locale.FRENCH, "Déplacer dans un dossier");
		triggerActionType.setTitle(Locale.ENGLISH, "Move into folder");
		triggerActionType.setLinkedSchema(MoveInFolderTriggerAction.SCHEMA);


		transaction.add(triggerType.getWrappedRecord());
		transaction.add(triggerActionType.getWrappedRecord());
	}

	private String getDefaultTriggerMetadataCode(String metadataCode) {
		return Trigger.DEFAULT_SCHEMA + "_" + metadataCode;
	}

	private class SchemaAlterationFor9_1 extends MetadataSchemasAlterationHelper {
		SchemaAlterationFor9_1(String collection, MigrationResourcesProvider migrationResourcesProvider,
							   AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			typesBuilder.getDefaultSchema(Document.SCHEMA_TYPE).get(Document.TITLE).setCacheIndex(true);

			MetadataSchemaBuilder taskSchema = typesBuilder.getSchemaType(RMTask.SCHEMA_TYPE).getDefaultSchema();
			taskSchema.get(RMTask.LINKED_DOCUMENTS).setEssentialInSummary(true).setCacheIndex(true);

			MetadataSchemaBuilder folderSchema = typesBuilder.getSchemaType(Folder.SCHEMA_TYPE).getDefaultSchema();
			folderSchema.get(Schemas.ATTACHED_ANCESTORS).setEssentialInSummary(false);
			folderSchema.get(Schemas.DESCRIPTION_TEXT).setEssentialInSummary(false);
			folderSchema.get(Schemas.PATH_PARTS).setEssentialInSummary(false).setCacheIndex(false);

			//TODO : Should not exist!
			folderSchema.get(Folder.ACTIVE_RETENTION_CODE).setEssentialInSummary(false);
			folderSchema.get(Folder.SEMIACTIVE_RETENTION_CODE).setEssentialInSummary(false);
			folderSchema.get(Schemas.PRINCIPAL_PATH).setEssentialInSummary(true);

			folderSchema.get(Schemas.TOKENS_OF_HIERARCHY).setEssentialInSummary(true);
			folderSchema.get(Schemas.MODIFIED_ON).setEssentialInSummary(true);
			folderSchema.get(Folder.FORM_CREATED_ON).setEssentialInSummary(true);
			folderSchema.get(Folder.FORM_MODIFIED_ON).setEssentialInSummary(true);
			folderSchema.get(Folder.OPENING_DATE).setEssentialInSummary(true);
			folderSchema.get(Folder.CLOSING_DATE).setEssentialInSummary(true);
			folderSchema.get(Folder.CATEGORY).setTaxonomyRelationship(false);
			folderSchema.get(Folder.CATEGORY_ENTERED).setTaxonomyRelationship(true);

			folderSchema.get(Folder.ADMINISTRATIVE_UNIT).setTaxonomyRelationship(false);
			folderSchema.get(Folder.ADMINISTRATIVE_UNIT_ENTERED).setTaxonomyRelationship(true);
			folderSchema.get(Schemas.ALL_REMOVED_AUTHS).setEnabled(true).setEssentialInSummary(true);
			folderSchema.get(Folder.TITLE).setCacheIndex(false);

			MetadataSchemaBuilder documentSchema = typesBuilder.getSchemaType(Document.SCHEMA_TYPE).getDefaultSchema();
			documentSchema.get(Schemas.ATTACHED_ANCESTORS).setEssentialInSummary(false);
			documentSchema.get(Document.FOLDER_CATEGORY).setCacheIndex(false);
			documentSchema.get(Schemas.PATH_PARTS).setEssentialInSummary(false).setCacheIndex(false);
			documentSchema.get(Schemas.PRINCIPAL_PATH).setEssentialInSummary(true);
			documentSchema.get(Schemas.TOKENS_OF_HIERARCHY).setEssentialInSummary(true);
			documentSchema.get(Schemas.MODIFIED_ON).setEssentialInSummary(true);
			documentSchema.get(Document.FORM_CREATED_ON).setEssentialInSummary(true);
			documentSchema.get(Document.FORM_MODIFIED_ON).setEssentialInSummary(true);

			documentSchema.get(Document.FOLDER_CATEGORY).setTaxonomyRelationship(false);
			documentSchema.get(Document.FOLDER_ADMINISTRATIVE_UNIT).setTaxonomyRelationship(false);
			documentSchema.get(Schemas.ALL_REMOVED_AUTHS).setEnabled(true).setEssentialInSummary(true);
			documentSchema.get(Document.TITLE).setCacheIndex(false);

			for (MetadataSchemaTypeBuilder typeBuilder : typesBuilder.getTypes()) {
				if (typeBuilder.getDefaultSchema().hasMetadata(CommonMetadataBuilder.ATTACHED_ANCESTORS)) {
					if (!typeBuilder.getCode().equals(Event.SCHEMA_TYPE)
						&& !typeBuilder.getCode().equals(SavedSearch.SCHEMA_TYPE)
						&& !typeBuilder.getCode().equals(SearchEvent.SCHEMA_TYPE)) {
						typeBuilder.getDefaultSchema().getMetadata(CommonMetadataBuilder.ATTACHED_ANCESTORS)
								.defineDataEntry().asCalculated(AttachedAncestorsCalculator2.class);
					}
				}
			}

			if (Toggle.DOCUMENT_RETENTION_RULES.isEnabled() && hasCalculator(
					documentSchema.getMetadata(Document.FOLDER_EXPECTED_TRANSFER_DATE), DocumentExpectedTransferDateCalculator.class)) {
				documentSchema.getMetadata(Document.FOLDER_EXPECTED_TRANSFER_DATE).defineDataEntry()
						.asCopied(documentSchema.get(Document.FOLDER), folderSchema.get(Folder.EXPECTED_TRANSFER_DATE));
				documentSchema.getMetadata(Document.FOLDER_EXPECTED_DEPOSIT_DATE).defineDataEntry()
						.asCopied(documentSchema.get(Document.FOLDER), folderSchema.get(Folder.EXPECTED_DEPOSIT_DATE));
				documentSchema.getMetadata(Document.FOLDER_EXPECTED_DESTRUCTION_DATE).defineDataEntry()
						.asCopied(documentSchema.get(Document.FOLDER), folderSchema.get(Folder.EXPECTED_DESTRUCTION_DATE));
			}

			if (!documentSchema.hasMetadata(Document.IS_CHECKOUT_ALERT_SENT)) {
				documentSchema.createUndeletable(Document.IS_CHECKOUT_ALERT_SENT).setType(MetadataValueType.BOOLEAN)
						.setDefaultValue(false).setSystemReserved(true);

			}

			if (!documentSchema.hasMetadata(Document.CONTENT_CHECKED_OUT_DATE)) {
				documentSchema.createUndeletable(Document.CONTENT_CHECKED_OUT_DATE)
						.setType(MetadataValueType.DATE_TIME)
						.defineDataEntry().asCalculated(DocumentCheckedOutDateCalculator.class);
			}

			documentSchema.create(Document.LINKED_TO)
					.setType(MetadataValueType.REFERENCE)
					.defineReferencesTo(types().getDefaultSchema(Folder.SCHEMA_TYPE))
					.setMultivalue(true)
					.setCacheIndex(true);

			MetadataSchemaBuilder externalLinkTypeSchema = setupExternalLinkTypeSchema().getDefaultSchema();
			MetadataSchemaTypeBuilder externalLinkSchemaType = typesBuilder.createNewSchemaType(ExternalLink.SCHEMA_TYPE).setSecurity(false);
			MetadataSchemaBuilder externalLinkSchema = externalLinkSchemaType.getDefaultSchema();

			externalLinkSchema.createUndeletable(ExternalLink.TYPE)
					.setType(MetadataValueType.REFERENCE)
					.defineReferencesTo(externalLinkTypeSchema);
			externalLinkSchema.createUndeletable(ExternalLink.IMPORTED_ON).setType(MetadataValueType.DATE_TIME);

			folderSchema.createUndeletable(Folder.EXTERNAL_LINKS).setType(MetadataValueType.REFERENCE)
					.defineReferencesTo(externalLinkSchemaType).setMultivalue(true);

			documentSchema.createUndeletable(Document.FILENAME).setType(STRING)
					.defineDataEntry().asCalculated(new DocumentFilenameCalculator());

			MetadataSchemaTypeBuilder triggerActionTypeSchemaType = typesBuilder.createNewSchemaType(TriggerActionType.SCHEMA_TYPE).setSecurity(false);
			MetadataSchemaBuilder triggerActionTypeSchema = triggerActionTypeSchemaType.getDefaultSchema();
			triggerActionTypeSchema.createUndeletable(TriggerActionType.LINKED_SCHEMA).setType(STRING)
					.setDefaultRequirement(true);
			triggerActionTypeSchema.createUndeletable(TriggerActionType.CODE).setType(STRING).setSystemReserved(true).setDefaultRequirement(true);

			MetadataSchemaTypeBuilder triggerTypeSchemaType = typesBuilder.createNewSchemaType(TriggerType.SCHEMA_TYPE).setSecurity(false);
			MetadataSchemaBuilder triggerTypeSchema = triggerTypeSchemaType.getDefaultSchema();
			triggerTypeSchema.createUndeletable(TriggerType.CODE).setType(STRING).setSystemReserved(true).setDefaultRequirement(true);


			MetadataSchemaTypeBuilder triggerActionSchemaType = typesBuilder.createNewSchemaType(TriggerAction.SCHEMA_TYPE).setSecurity(false);
			MetadataSchemaBuilder triggerActionSchema = triggerActionSchemaType.getDefaultSchema();
			triggerActionSchema.getMetadata(TriggerAction.TITLE).required();
			triggerActionSchema.createUndeletable(TriggerAction.TYPE).setType(REFERENCE).defineReferencesTo(triggerActionTypeSchemaType).required();

			triggerActionSchemaType.createCustomSchema(MoveInFolderTriggerAction.SCHEMA_LOCAL_CODE);
			triggerActionSchemaType.getCustomSchema(MoveInFolderTriggerAction.SCHEMA_LOCAL_CODE).createUndeletable(MoveInFolderTriggerAction.DATE).setType(MetadataValueType.DATE);

			MetadataSchemaTypeBuilder triggerSchemaType = typesBuilder.createNewSchemaType(Trigger.SCHEMA_TYPE).setSecurity(false);
			MetadataSchemaBuilder triggerSchema = triggerSchemaType.getDefaultSchema();

			triggerSchema.getMetadata(Trigger.TITLE).required();
			triggerSchema.createUndeletable(Trigger.DESCRIPTION).setType(TEXT);
			triggerSchema.createUndeletable(Trigger.TYPE).setType(REFERENCE).defineReferencesTo(triggerTypeSchema).required();
			triggerSchema.createUndeletable(Trigger.ACTIONS).setType(REFERENCE).defineReferencesTo(triggerActionSchemaType).setMultivalue(true).required();
			triggerSchema.createUndeletable(Trigger.CRITERIA).setType(STRUCTURE).defineStructureFactory(CriterionFactory.class).setMultivalue(true).required();
			triggerSchema.createUndeletable(Trigger.TARGET).setType(REFERENCE).setMultivalue(true).defineReferencesTo(typesBuilder.getSchemaType(Folder.SCHEMA_TYPE)).setSystemReserved(true);
		}


		private MetadataSchemaTypeBuilder setupExternalLinkTypeSchema() {
			Map<Language, String> mapLanguage = MigrationUtil.getLabelsByLanguage(collection, modelLayerFactory,
					migrationResourcesProvider, "init.externalLinkType");

			MetadataSchemaTypeBuilder schemaType = new ValueListItemSchemaTypeBuilder(types())
					.createValueListItemSchema(ExternalLinkType.SCHEMA_TYPE, mapLanguage,
							ValueListItemSchemaTypeBuilderOptions.codeMetadataRequiredAndUnique())
					.setSecurity(false);

			MetadataSchemaBuilder defaultSchema = schemaType.getDefaultSchema();
			defaultSchema.createUndeletable(ExternalLinkType.LINKED_SCHEMA).setType(MetadataValueType.STRING);

			return schemaType;
		}

		private boolean hasCalculator(MetadataBuilder metadata, Class<?> expectedCalculatorClass) {
			return metadata.getDataEntry().getType() == DataEntryType.CALCULATED
				   && ((CalculatedDataEntry) metadata.getDataEntry()).getCalculator().getClass().equals(expectedCalculatorClass);

		}

	}

	private void setupDisplayConfig(String collection, AppLayerFactory appLayerFactory) {
		SchemasDisplayManager displayManager = appLayerFactory.getMetadataSchemasDisplayManager();
		SchemaTypesDisplayTransactionBuilder transactionBuilder = displayManager.newTransactionBuilderFor(collection);

		transactionBuilder.in(Document.SCHEMA_TYPE)
				.addToForm(Document.LINKED_TO)
				.atTheEnd();
		transactionBuilder.in(Document.SCHEMA_TYPE)
				.addToDisplay(Document.LINKED_TO)
				.atTheEnd();

		displayManager.execute(transactionBuilder.build());

		displayManager.saveSchema(displayManager.getSchema(collection, Event.DEFAULT_SCHEMA)
				.withNewTableMetadatas(Event.DEFAULT_SCHEMA + "_" + Schemas.CREATED_ON.getLocalCode()).withRemovedTableMetadatas(Event.DEFAULT_SCHEMA + "_" + Schemas.MODIFIED_ON.getLocalCode()));

		SchemaDisplayConfig externalLinkConfig = displayManager.getSchema(collection, ExternalLink.DEFAULT_SCHEMA);

		displayManager.saveSchema(externalLinkConfig
				.withNewTableMetadatas(ExternalLink.DEFAULT_SCHEMA + "_" + ExternalLink.TYPE)
				.withRemovedTableMetadatas(ExternalLink.DEFAULT_SCHEMA + "_" + Schemas.MODIFIED_ON));
	}
}
