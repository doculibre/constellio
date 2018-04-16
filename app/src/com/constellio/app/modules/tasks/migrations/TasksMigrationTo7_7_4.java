package com.constellio.app.modules.tasks.migrations;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.tasks.TasksEmailTemplates;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.managers.config.ConfigManagerException.OptimisticLockingConfiguration;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class TasksMigrationTo7_7_4 extends MigrationHelper implements MigrationScript {

	private String collection;

	private MigrationResourcesProvider migrationResourcesProvider;

	private AppLayerFactory appLayerFactory;

	@Override
	public String getVersion() {
		return "7.7.4";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory)
			throws Exception {
		this.collection = collection;
		this.migrationResourcesProvider = migrationResourcesProvider;
		this.appLayerFactory = appLayerFactory;

		new SchemaAlterationFor7_7_4(collection, migrationResourcesProvider, appLayerFactory).migrate();
		reloadEmailTemplates();

	}

	private void reloadEmailTemplates() {
		if (appLayerFactory.getModelLayerFactory().getCollectionsListManager().getCollectionLanguages(collection).get(0)
				.equals("en")) {
			reloadEmailTemplate("taskStatusModificationToCompletedTemplate_en.html", TasksEmailTemplates.TASK_COMPLETED);
		} else {
			reloadEmailTemplate("taskStatusModificationToCompletedTemplate.html", TasksEmailTemplates.TASK_COMPLETED);
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

	class SchemaAlterationFor7_7_4 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationFor7_7_4(String collection, MigrationResourcesProvider migrationResourcesProvider,
				AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		public String getVersion() {
			return "7.7.4";
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaBuilder task = typesBuilder.getSchemaType(Task.SCHEMA_TYPE)
					.getDefaultSchema();
			if (!typesBuilder.getSchema(Task.DEFAULT_SCHEMA).hasMetadata(Task.REASON)) {
				typesBuilder.getSchema(Task.DEFAULT_SCHEMA).createUndeletable(Task.REASON).setType(MetadataValueType.TEXT);
			}
		}
	}
}
