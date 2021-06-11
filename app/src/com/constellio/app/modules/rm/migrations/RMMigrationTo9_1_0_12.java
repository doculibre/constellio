package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.RMEmailTemplateConstants;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.managers.config.ConfigManagerException;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

public class RMMigrationTo9_1_0_12 extends MigrationHelper implements MigrationScript {

	@Override
	public String getVersion() {
		return "9.1.0.12";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {
		addEmailTemplates(collection, migrationResourcesProvider, appLayerFactory);
	}

	public static void addEmailTemplates(String collection, MigrationResourcesProvider migrationResourcesProvider,
										 AppLayerFactory appLayerFactory) {
		addEmailTemplate(appLayerFactory, migrationResourcesProvider, collection, "alertBorrowingPeriodEndedTemplate.html",
				RMEmailTemplateConstants.ALERT_BORROWING_PERIOD_ENDED);
	}

	public static void addEmailTemplate(AppLayerFactory appLayerFactory,
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
