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

public class TasksMigrationTo9_2_7 extends MigrationHelper implements MigrationScript {

	@Override
	public String getVersion() {
		return "9.2.7";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory) throws Exception {
		if (appLayerFactory.getModelLayerFactory().getCollectionsListManager().getCollectionLanguages(collection).get(0)
				.equals("en")) {
			addEmailTemplate(appLayerFactory, migrationResourcesProvider, collection, "collaboratorAddedTemplate_en.html",
					TasksEmailTemplates.TASK_COLLABORATOR_ADDED);
		} else {
			addEmailTemplate(appLayerFactory, migrationResourcesProvider, collection, "collaboratorAddedTemplate.html",
					TasksEmailTemplates.TASK_COLLABORATOR_ADDED);
		}
	}

	private void addEmailTemplate(AppLayerFactory appLayerFactory,
								  MigrationResourcesProvider migrationResourcesProvider,
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
