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

public class TasksMigrationTo9_2_11 extends MigrationHelper implements MigrationScript {

	private String collection;

	private MigrationResourcesProvider migrationResourcesProvider;

	private AppLayerFactory appLayerFactory;

	@Override
	public String getVersion() {
		return "9.2.11";
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
			reloadEmailTemplate("taskCollaboratorAddedTemplate_en.html", TasksEmailTemplates.TASK_COLLABORATOR_ADDED);
			addEmailTemplate("taskFollowerAddedTemplate_en.html", TasksEmailTemplates.TASK_FOLLOWER_ADDED);
		} else {
			reloadEmailTemplate("taskCollaboratorAddedTemplate.html", TasksEmailTemplates.TASK_COLLABORATOR_ADDED);
			addEmailTemplate("taskFollowerAddedTemplate.html", TasksEmailTemplates.TASK_FOLLOWER_ADDED);
		}
	}

	private void addEmailTemplate(String templateFileName, String templateId) {
		InputStream templateInputStream = migrationResourcesProvider.getStream(templateFileName);
		try {
			appLayerFactory.getModelLayerFactory().getEmailTemplatesManager()
					.addCollectionTemplateIfInexistent(templateId, collection, templateInputStream);
		} catch (IOException | OptimisticLockingConfiguration e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(templateInputStream);
		}
	}

	private void reloadEmailTemplate(String templateFileName, String templateId) {
		InputStream templateInputStream = migrationResourcesProvider.getStream(templateFileName);
		try {
			appLayerFactory.getModelLayerFactory().getEmailTemplatesManager()
					.replaceCollectionTemplate(templateId, collection, templateInputStream);
		} catch (IOException | OptimisticLockingConfiguration e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(templateInputStream);
		}
	}
}
