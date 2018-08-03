package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.RMEmailTemplateConstants;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.managers.config.ConfigManagerException;
import org.apache.chemistry.opencmis.commons.impl.IOUtils;

import java.io.IOException;
import java.io.InputStream;

public class RMMigrationTo7_6_10 extends MigrationHelper implements MigrationScript {
	@Override
	public String getVersion() {
		return "7.6.10";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory) throws Exception {
		if (appLayerFactory.getModelLayerFactory().getCollectionsListManager().getCollectionLanguages(collection).get(0)
				.equals("fr")) {
			reloadEmailTemplate("alertReactivatedTemplate.html", RMEmailTemplateConstants.ALERT_REACTIVATED_ACCEPTED,
					appLayerFactory, migrationResourcesProvider, collection);
			reloadEmailTemplate("alertReactivatedTemplateDenied.html", RMEmailTemplateConstants.ALERT_REACTIVATED_DENIED,
					appLayerFactory, migrationResourcesProvider, collection);
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
}
