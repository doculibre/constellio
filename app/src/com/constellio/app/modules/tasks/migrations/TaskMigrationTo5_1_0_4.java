package com.constellio.app.modules.tasks.migrations;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.tasks.TasksEmailTemplates;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.managers.config.ConfigManagerException.OptimisticLockingConfiguration;

public class TaskMigrationTo5_1_0_4 implements MigrationScript {

	@Override
	public String getVersion() {
		return "5.1.0.4";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
			AppLayerFactory appLayerFactory)
			throws Exception {

		addEmailTemplates(appLayerFactory, migrationResourcesProvider, collection);

	}

	private void addEmailTemplates(AppLayerFactory appLayerFactory, MigrationResourcesProvider migrationResourcesProvider,
			String collection) {
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
}
