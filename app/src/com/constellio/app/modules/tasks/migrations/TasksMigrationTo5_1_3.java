package com.constellio.app.modules.tasks.migrations;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.tasks.TasksEmailTemplates;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.managers.config.ConfigManagerException.OptimisticLockingConfiguration;
import com.constellio.model.services.emails.EmailTemplatesManager;

public class TasksMigrationTo5_1_3 extends MigrationHelper implements MigrationScript {
	@Override
	public String getVersion() {
		return "5.1.3";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory)
			throws Exception {
		addAndReloadEmailTemplates(appLayerFactory, migrationResourcesProvider, collection);

	}

	private void addAndReloadEmailTemplates(AppLayerFactory appLayerFactory,
			MigrationResourcesProvider migrationResourcesProvider,
			String collection) {

		String taskReminderTemplate;
		String subTasksModificationTemplate;
		String taskAssigneeModificationTemplate;
		String taskAssigneeToYouTemplate;
		String taskDeletionTemplate;
		String taskStatusModificationTemplate;
		String taskStatusModificationToCompletedTemplate;
		if (appLayerFactory.getModelLayerFactory().getCollectionsListManager().getCollectionLanguages(collection).get(0)
				.equals("en")) {
			taskReminderTemplate = "taskReminderTemplate_en.html";
			subTasksModificationTemplate = "subTasksModificationTemplate_en.html";
			taskAssigneeModificationTemplate = "taskAssigneeModificationTemplate_en.html";
			taskAssigneeToYouTemplate = "taskAssigneeToYouTemplate_en.html";
			taskStatusModificationTemplate = "taskStatusModificationTemplate_en.html";
			taskStatusModificationToCompletedTemplate = "taskStatusModificationToCompletedTemplate_en.html";
			taskDeletionTemplate = "taskDeletionTemplate_en.html";
		} else {
			taskReminderTemplate = "taskReminderTemplate.html";
			subTasksModificationTemplate = "subTasksModificationTemplate.html";
			taskAssigneeModificationTemplate = "taskAssigneeModificationTemplate.html";
			taskAssigneeToYouTemplate = "taskAssigneeToYouTemplate.html";
			taskStatusModificationTemplate = "taskStatusModificationTemplate.html";
			taskStatusModificationToCompletedTemplate = "taskStatusModificationToCompletedTemplate.html";
			taskDeletionTemplate = "taskDeletionTemplate.html";
		}
		reloadEmailTemplate(appLayerFactory, migrationResourcesProvider, collection, taskAssigneeModificationTemplate,
				TasksEmailTemplates.TASK_ASSIGNEE_MODIFIED);
		reloadEmailTemplate(appLayerFactory, migrationResourcesProvider, collection,
				taskStatusModificationToCompletedTemplate,
				TasksEmailTemplates.TASK_COMPLETED);
		reloadEmailTemplate(appLayerFactory, migrationResourcesProvider, collection, taskDeletionTemplate,
				TasksEmailTemplates.TASK_DELETED);
		reloadEmailTemplate(appLayerFactory, migrationResourcesProvider, collection, taskStatusModificationTemplate,
				TasksEmailTemplates.TASK_STATUS_MODIFIED);
		reloadEmailTemplate(appLayerFactory, migrationResourcesProvider, collection, subTasksModificationTemplate,
				TasksEmailTemplates.TASK_SUB_TASKS_MODIFIED);
		reloadEmailTemplate(appLayerFactory, migrationResourcesProvider, collection, taskReminderTemplate,
				TasksEmailTemplates.TASK_REMINDER);
		reloadEmailTemplate(appLayerFactory, migrationResourcesProvider, collection, taskAssigneeToYouTemplate,
				TasksEmailTemplates.TASK_ASSIGNED_TO_YOU);
	}

	private void addEmailTemplate(AppLayerFactory appLayerFactory, MigrationResourcesProvider migrationResourcesProvider,
			String collection,
			String templateFileName, String templateId) {
		InputStream stream = migrationResourcesProvider.getStream(templateFileName);
		try {
			appLayerFactory.getModelLayerFactory().getEmailTemplatesManager()
					.addCollectionTemplateIfInexistent(templateId, collection, stream);
		} catch (IOException | OptimisticLockingConfiguration e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(stream);
		}
	}

	private void reloadEmailTemplate(AppLayerFactory appLayerFactory, MigrationResourcesProvider migrationResourcesProvider,
			String collection,
			String templateFileName, String templateId) {
		InputStream templateInputStream = migrationResourcesProvider.getStream(templateFileName);
		EmailTemplatesManager emailTemplateManager = appLayerFactory.getModelLayerFactory()
				.getEmailTemplatesManager();
		try {
			emailTemplateManager.replaceCollectionTemplate(templateId, collection, templateInputStream);
		} catch (IOException | OptimisticLockingConfiguration e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(templateInputStream);
		}
	}
}
