package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.entities.schemasDisplay.SchemaDisplayConfig;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.triggers.Trigger;
import com.constellio.app.modules.rm.wrappers.triggers.TriggerAction;
import com.constellio.app.modules.rm.wrappers.triggers.TriggerActionType;
import com.constellio.app.modules.rm.wrappers.triggers.TriggerType;
import com.constellio.app.modules.rm.wrappers.triggers.actions.MoveInFolderTriggerAction;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.app.ui.pages.search.criteria.CriterionFactory;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

import java.util.Locale;

import static com.constellio.model.entities.schemas.MetadataValueType.REFERENCE;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static com.constellio.model.entities.schemas.MetadataValueType.STRUCTURE;
import static com.constellio.model.entities.schemas.MetadataValueType.TEXT;
import static org.apache.ignite.internal.util.lang.GridFunc.asList;

public class RMMigrationTo_9_2 extends MigrationHelper implements MigrationScript {
	@Override
	public String getVersion() {
		return "9.2";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory) throws Exception {
		new SchemaAlterationForTo9_2(collection, migrationResourcesProvider, appLayerFactory).migrate();

		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		RecordServices recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();

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


		try {
			recordServices.add(triggerType.getWrappedRecord());
			recordServices.add(triggerActionType.getWrappedRecord());
		} catch (RecordServicesException e) {
			throw new ImpossibleRuntimeException("Simple record creation should not throw");
		}
	}

	private String getDefaultTriggerMetadataCode(String metadataCode) {
		return Trigger.DEFAULT_SCHEMA + "_" + metadataCode;
	}

	class SchemaAlterationForTo9_2 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationForTo9_2(String collection,
										   MigrationResourcesProvider migrationResourcesProvider,
										   AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {

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
	}
}
