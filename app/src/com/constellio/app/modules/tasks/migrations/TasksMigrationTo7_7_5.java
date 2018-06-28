package com.constellio.app.modules.tasks.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.tasks.TasksEmailTemplates;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.managers.config.ConfigManagerException.OptimisticLockingConfiguration;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

public class TasksMigrationTo7_7_5 extends MigrationHelper implements MigrationScript {

	private String collection;

	private MigrationResourcesProvider migrationResourcesProvider;

	private AppLayerFactory appLayerFactory;

	@Override
	public String getVersion() {
		return "7.7.5";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory)
			throws Exception {
		this.collection = collection;
		this.migrationResourcesProvider = migrationResourcesProvider;
		this.appLayerFactory = appLayerFactory;

		new SchemaAlterationFor7_7_5(collection, migrationResourcesProvider, appLayerFactory).migrate();
		reloadEmailTemplates();

	}

	private void reloadEmailTemplates() {
		if (appLayerFactory.getModelLayerFactory().getCollectionsListManager()
                .getCollectionLanguages(collection).get(0).equals("en")) {
			reloadEmailTemplate("taskAssigneeToYouTemplate_en.html", TasksEmailTemplates.TASK_ASSIGNED_TO_YOU);
			reloadEmailTemplate("taskReminderTemplate_en.html", TasksEmailTemplates.TASK_REMINDER);
			reloadEmailTemplate("subTasksModificationTemplate_en.html", TasksEmailTemplates.TASK_SUB_TASKS_MODIFIED);
			reloadEmailTemplate("taskAssigneeModificationTemplate_en.html", TasksEmailTemplates.TASK_ASSIGNEE_MODIFIED);
			reloadEmailTemplate("taskStatusModificationTemplate_en.html", TasksEmailTemplates.TASK_STATUS_MODIFIED);
			reloadEmailTemplate("taskStatusModificationToCompletedTemplate_en.html", TasksEmailTemplates.TASK_COMPLETED);
			reloadEmailTemplate( "taskDeletionTemplate_en.html", TasksEmailTemplates.TASK_DELETED);
		} else {
			reloadEmailTemplate("taskAssigneeToYouTemplate.html", TasksEmailTemplates.TASK_ASSIGNED_TO_YOU);
			reloadEmailTemplate("taskReminderTemplate.html", TasksEmailTemplates.TASK_REMINDER);
			reloadEmailTemplate("subTasksModificationTemplate.html", TasksEmailTemplates.TASK_SUB_TASKS_MODIFIED);
			reloadEmailTemplate("taskAssigneeModificationTemplate.html", TasksEmailTemplates.TASK_ASSIGNEE_MODIFIED);
			reloadEmailTemplate("taskStatusModificationTemplate.html", TasksEmailTemplates.TASK_STATUS_MODIFIED);
			reloadEmailTemplate("taskStatusModificationToCompletedTemplate.html", TasksEmailTemplates.TASK_COMPLETED);
			reloadEmailTemplate( "taskDeletionTemplate.html", TasksEmailTemplates.TASK_DELETED);
		}
	}

	private void reloadEmailTemplate(final String templateFileName, final String templateId) {
		final InputStream templateInputStream = migrationResourcesProvider.getStream(templateFileName);

		try {
			appLayerFactory.getModelLayerFactory().getEmailTemplatesManager()
					.replaceCollectionTemplate(templateId, collection, templateInputStream);
		} catch (IOException | OptimisticLockingConfiguration e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(templateInputStream);
		}
	}

	class SchemaAlterationFor7_7_5 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationFor7_7_5(String collection, MigrationResourcesProvider migrationResourcesProvider,
				AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		public String getVersion() {
			return "7.7.5";
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
		}
	}
}
