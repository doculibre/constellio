package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.RMEmailTemplateConstants;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.managers.config.ConfigManagerException;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

public class RMMigrationTo9_0_3_20 implements MigrationScript {
	@Override
	public String getVersion() {
		return "9.0.3.20";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory) throws Exception {
		new SchemaAlterationFor9_0_3_13(collection, migrationResourcesProvider, appLayerFactory).migrate();
		addEmailTemplates(collection, migrationResourcesProvider, appLayerFactory);
	}

	private class SchemaAlterationFor9_0_3_13 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationFor9_0_3_13(String collection,
											  MigrationResourcesProvider migrationResourcesProvider,
											  AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {

		}

	}


	public static void addEmailTemplates(String collection,
										 MigrationResourcesProvider migrationResourcesProvider,
										 AppLayerFactory appLayerFactory) {
		addEmailTemplate(appLayerFactory, migrationResourcesProvider, collection, "alertBorrowingPeriodEndedTemplate2.html",
				RMEmailTemplateConstants.ALERT_BORROWING_PERIOD_ENDED_V2);

		addEmailTemplate(appLayerFactory, migrationResourcesProvider, collection, "alertBorrowingPeriodEndedTemplate2En.html",
				RMEmailTemplateConstants.ALERT_BORROWING_PERIOD_ENDED_V2_EN);
	}

	private static void addEmailTemplate(AppLayerFactory appLayerFactory,
										 MigrationResourcesProvider migrationResourcesProvider, String collection,
										 String templateFileName, String templateId) {
		InputStream remindReturnBorrowedFolderTemplate = migrationResourcesProvider.getStream(templateFileName);
		try {
			appLayerFactory.getModelLayerFactory().getEmailTemplatesManager()
					.addCollectionTemplateIfInexistent(templateId, collection, remindReturnBorrowedFolderTemplate);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (ConfigManagerException.OptimisticLockingConfiguration optimisticLockingConfiguration) {
			throw new RuntimeException(optimisticLockingConfiguration);
		} finally {
			IOUtils.closeQuietly(remindReturnBorrowedFolderTemplate);
		}
	}
}
