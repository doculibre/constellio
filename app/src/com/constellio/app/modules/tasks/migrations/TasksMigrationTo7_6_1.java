package com.constellio.app.modules.tasks.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.entities.schemasDisplay.SchemaTypeDisplayConfig;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.Language;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

import java.util.HashMap;
import java.util.Map;

public class TasksMigrationTo7_6_1 implements MigrationScript {
	@Override
	public String getVersion() {
		return "7.6.1";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {
		new TaskSchemaAlterationFor7_6_1(collection, migrationResourcesProvider, appLayerFactory).migrate();

		String remindersTab = "init.userTask.remindersTab";
		String detailsTab = "init.userTask.details";

		SchemasDisplayManager manager = appLayerFactory.getMetadataSchemasDisplayManager();
		SchemaTypeDisplayConfig taskSchemaType = manager.getType(collection, Task.SCHEMA_TYPE);
		Map<String, Map<Language, String>> metadataGroup = new HashMap<>(taskSchemaType.getMetadataGroup());
		if (metadataGroup.containsKey(remindersTab)) {
			Map<Language, String> tabLabels = new HashMap<>();
			tabLabels.put(Language.French, "\uF0A2 Rappels");
			tabLabels.put(Language.English, "\uF0A2 Reminders");
			metadataGroup.put(remindersTab, tabLabels);
		}
		if (metadataGroup.containsKey(detailsTab)) {
			Map<Language, String> tabLabels = new HashMap<>();
			tabLabels.put(Language.French, "\uF0EA Fichiers");
			tabLabels.put(Language.English, "\uF0EA Files");
			metadataGroup.put(detailsTab, tabLabels);
		}

		manager.saveType(taskSchemaType.withMetadataGroup(metadataGroup));
	}

	private class TaskSchemaAlterationFor7_6_1 extends MetadataSchemasAlterationHelper {

		public TaskSchemaAlterationFor7_6_1(String collection, MigrationResourcesProvider migrationResourcesProvider,
											AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			typesBuilder.getDefaultSchema(Task.SCHEMA_TYPE).getMetadata(Task.LAST_REMINDER)
					.addLabel(Language.French, "Dernier rappel").addLabel(Language.English, "Last reminder");

			typesBuilder.getDefaultSchema(Task.SCHEMA_TYPE).getMetadata(Task.NUMBER_OF_REMINDERS)
					.addLabel(Language.French, "Nombre de rappels").addLabel(Language.English, "Number of reminders");
		}
	}
}
