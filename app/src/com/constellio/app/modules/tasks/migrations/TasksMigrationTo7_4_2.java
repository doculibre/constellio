package com.constellio.app.modules.tasks.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.model.wrappers.types.TaskStatus;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaDisplayManagerTransaction;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class TasksMigrationTo7_4_2 implements MigrationScript {
	@Override
	public String getVersion() {
		return "7.4.2";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory)
			throws Exception {

		new TaskSchemaAlterationFor7_4_2(collection, migrationResourcesProvider, appLayerFactory).migrate();
		SchemasDisplayManager manager = appLayerFactory.getMetadataSchemasDisplayManager();
		manager.saveSchema(manager.getSchema(collection, Task.DEFAULT_SCHEMA).withNewFormAndDisplayMetadatas(
				Task.DEFAULT_SCHEMA + "_" + Task.REMINDER_FREQUENCY,
				Task.DEFAULT_SCHEMA + "_" + Task.ESCALATION_ASSIGNEE
				));

		String remindersTab = "init.userTask.remindersTab";
		SchemaDisplayManagerTransaction transaction = new SchemaDisplayManagerTransaction();
		transaction.add(manager.getMetadata(collection, Task.DEFAULT_SCHEMA, Task.REMINDER_FREQUENCY)
				.withMetadataGroup(remindersTab));
		manager.execute(transaction);
	}

	private class TaskSchemaAlterationFor7_4_2 extends MetadataSchemasAlterationHelper {

		protected TaskSchemaAlterationFor7_4_2(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			typesBuilder.getDefaultSchema(Task.SCHEMA_TYPE).createUndeletable(Task.REMINDER_FREQUENCY).setType(MetadataValueType.STRING).addLabel(Language.French,"Fréquence de rappel").addLabel(Language.English,"Reminder frequency");
			typesBuilder.getDefaultSchema(Task.SCHEMA_TYPE).createUndeletable(Task.LAST_REMINDER).setType(MetadataValueType.DATE_TIME)
					.setSystemReserved(true);
			typesBuilder.getDefaultSchema(Task.SCHEMA_TYPE).createUndeletable(Task.NUMBER_OF_REMINDERS).setType(MetadataValueType.INTEGER)
					.setSystemReserved(true).setDefaultValue(0);
			typesBuilder.getDefaultSchema(Task.SCHEMA_TYPE).createUndeletable(Task.ESCALATION_ASSIGNEE).setType(MetadataValueType.REFERENCE)
					.defineReferencesTo(typesBuilder.getSchemaType(User.SCHEMA_TYPE)).addLabel(Language.French,"Personne assignée à l'escalade").addLabel(Language.English,"Escalation assignee");
		}
	}

}
