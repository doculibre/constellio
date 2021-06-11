package com.constellio.app.modules.tasks.migrations;

import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.tasks.TasksEmailTemplates;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.managers.config.ConfigManagerException.OptimisticLockingConfiguration;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

public class TasksMigrationTo9_2_10 extends MigrationHelper implements MigrationScript {

	private String collection;

	private MigrationResourcesProvider migrationResourcesProvider;

	private AppLayerFactory appLayerFactory;

	@Override
	public String getVersion() {
		return "9.2.10";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {
		this.collection = collection;
		this.migrationResourcesProvider = migrationResourcesProvider;
		this.appLayerFactory = appLayerFactory;

		reloadEmailTemplates();
	}

	private void reloadEmailTemplates() {
		if (appLayerFactory.getModelLayerFactory().getCollectionsListManager()
				.getCollectionLanguages(collection).get(0).equals("en")) {
			reloadEmailTemplate("taskDeletionTemplate_en.html", TasksEmailTemplates.TASK_DELETED);
			reloadEmailTemplate("taskStatusModificationTemplate_en.html", TasksEmailTemplates.TASK_STATUS_MODIFIED);
			reloadEmailTemplate("taskAssigneeModificationTemplate_en.html", TasksEmailTemplates.TASK_ASSIGNEE_MODIFIED);
			reloadEmailTemplate("taskCollaboratorAddedTemplate_en.html", TasksEmailTemplates.TASK_COLLABORATOR_ADDED);
			reloadEmailTemplate("taskAssigneeToYouTemplate_en.html", TasksEmailTemplates.TASK_ASSIGNED_TO_YOU);
			reloadEmailTemplate("subTasksModificationTemplate_en.html", TasksEmailTemplates.TASK_SUB_TASKS_MODIFIED);
			reloadEmailTemplate("taskStatusModificationToCompletedTemplate_en.html", TasksEmailTemplates.TASK_COMPLETED);
			reloadEmailTemplate("taskReminderTemplate_en.html", TasksEmailTemplates.TASK_REMINDER);
		} else {
			reloadEmailTemplate("taskDeletionTemplate.html", TasksEmailTemplates.TASK_DELETED);
			reloadEmailTemplate("taskStatusModificationTemplate.html", TasksEmailTemplates.TASK_STATUS_MODIFIED);
			reloadEmailTemplate("taskAssigneeModificationTemplate.html", TasksEmailTemplates.TASK_ASSIGNEE_MODIFIED);
			reloadEmailTemplate("taskCollaboratorAddedTemplate.html", TasksEmailTemplates.TASK_COLLABORATOR_ADDED);
			reloadEmailTemplate("taskAssigneeToYouTemplate.html", TasksEmailTemplates.TASK_ASSIGNED_TO_YOU);
			reloadEmailTemplate("subTasksModificationTemplate.html", TasksEmailTemplates.TASK_SUB_TASKS_MODIFIED);
			reloadEmailTemplate("taskStatusModificationToCompletedTemplate.html", TasksEmailTemplates.TASK_COMPLETED);
			reloadEmailTemplate("taskReminderTemplate.html", TasksEmailTemplates.TASK_REMINDER);
		}
	}

	private void reloadEmailTemplate(final String templateFileName, final String templateId) {
		final InputStream templateInputStream = migrationResourcesProvider.getStream(templateFileName);

		try {
			appLayerFactory.getModelLayerFactory().getEmailTemplatesManager().replaceCollectionTemplate(templateId, collection, templateInputStream);
		} catch (IOException | OptimisticLockingConfiguration e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(templateInputStream);
		}
	}
}
