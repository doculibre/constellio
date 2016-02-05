package com.constellio.app.modules.tasks.migrations;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.tasks.TasksEmailTemplates;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.managers.config.ConfigManagerException.OptimisticLockingConfiguration;
import com.constellio.model.services.emails.EmailTemplatesManager;

public class TasksMigrationTo5_1_2 extends MigrationHelper implements MigrationScript {
	@Override
	public String getVersion() {
		return "5.1.2";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory)
			throws Exception {
		reloadEmailTemplates(appLayerFactory, migrationResourcesProvider, collection);

	}

	private void reloadEmailTemplates(AppLayerFactory appLayerFactory, MigrationResourcesProvider migrationResourcesProvider,
			String collection) {
		reloadEmailTemplate(appLayerFactory, migrationResourcesProvider, collection, "taskAssigneeModificationTemplate.html",
				TasksEmailTemplates.TASK_ASSIGNEE_MODIFIED);
		reloadEmailTemplate(appLayerFactory, migrationResourcesProvider, collection,
				"taskStatusModificationToCompletedTemplate.html",
				TasksEmailTemplates.TASK_COMPLETED);
		reloadEmailTemplate(appLayerFactory, migrationResourcesProvider, collection, "taskDeletionTemplate.html",
				TasksEmailTemplates.TASK_DELETED);
		reloadEmailTemplate(appLayerFactory, migrationResourcesProvider, collection, "taskStatusModificationTemplate.html",
				TasksEmailTemplates.TASK_STATUS_MODIFIED);
		reloadEmailTemplate(appLayerFactory, migrationResourcesProvider, collection, "subTasksModificationTemplate.html",
				TasksEmailTemplates.TASK_SUB_TASKS_MODIFIED);
		reloadEmailTemplate(appLayerFactory, migrationResourcesProvider, collection, "taskReminderTemplate.html",
				TasksEmailTemplates.TASK_REMINDER);
		addEmailTemplate(appLayerFactory, migrationResourcesProvider, collection, "taskAssigneeTuYouTemplate.html",
				TasksEmailTemplates.TASK_ASSIGNED_TO_YOU);
	}

	private void addEmailTemplate(AppLayerFactory appLayerFactory, MigrationResourcesProvider migrationResourcesProvider,
			String collection,
			String templateFileName, String templateId) {
		InputStream remindReturnBorrowedFolderTemplate = migrationResourcesProvider.getStream(templateFileName);
		try {
			appLayerFactory.getModelLayerFactory().getEmailTemplatesManager()
					.addCollectionTemplateIfInexistent(templateId, collection, remindReturnBorrowedFolderTemplate);
		} catch (IOException | OptimisticLockingConfiguration e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(remindReturnBorrowedFolderTemplate);
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
