package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.type.DocumentType;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.managers.config.ConfigManagerException;
import com.constellio.model.entities.modules.EmailTemplateConstants;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import org.apache.chemistry.opencmis.commons.impl.IOUtils;

import java.io.IOException;
import java.io.InputStream;

public class RMMigrationTo9_0_0_47 implements MigrationScript {
	@Override
	public String getVersion() {
		return "9.0.0.47";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory) throws Exception {
		reloadEmailTemplates(appLayerFactory, migrationResourcesProvider, collection);
		new SchemaAlterationFor9_0_0_47(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	public static void reloadEmailTemplates(AppLayerFactory appLayerFactory,
											MigrationResourcesProvider migrationResourcesProvider,
											String collection) {
		if (appLayerFactory.getModelLayerFactory().getCollectionsListManager().getCollectionLanguages(collection).get(0)
				.equals("en")) {
			reloadEmailTemplate("alertShareTemplate_en.html", EmailTemplateConstants.ALERT_SHARE,
					appLayerFactory, migrationResourcesProvider, collection);

		} else {
			reloadEmailTemplate("alertShareTemplate.html", EmailTemplateConstants.ALERT_SHARE, appLayerFactory,
					migrationResourcesProvider, collection);
		}
	}

	private static void reloadEmailTemplate(final String templateFileName, final String templateId,
											AppLayerFactory appLayerFactory,
											MigrationResourcesProvider migrationResourcesProvider, String collection) {
		final InputStream templateInputStream = migrationResourcesProvider.getStream(templateFileName);

		try {
			appLayerFactory.getModelLayerFactory().getEmailTemplatesManager()
					.replaceCollectionTemplate(templateId, collection, templateInputStream);
		} catch (IOException | ConfigManagerException.OptimisticLockingConfiguration e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(templateInputStream);
		}
	}

	private class SchemaAlterationFor9_0_0_47 extends MetadataSchemasAlterationHelper {
		SchemaAlterationFor9_0_0_47(String collection, MigrationResourcesProvider migrationResourcesProvider,
									AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {

			MetadataSchemaBuilder folderSchema = typesBuilder.getSchemaType(Document.SCHEMA_TYPE).getDefaultSchema();
			MetadataSchemaBuilder documentTypeSchema = typesBuilder.getSchemaType(DocumentType.SCHEMA_TYPE).getDefaultSchema();
			folderSchema.create(Document.PUBLISHED_START_DATE)
					.setType(MetadataValueType.DATE);
			folderSchema.create(Document.PUBLISHED_EXPIRATION_DATE)
					.setType(MetadataValueType.DATE);
			MetadataSchemaBuilder documentSchema = typesBuilder.getSchemaType(Document.SCHEMA_TYPE).getDefaultSchema();
		}
	}
}
