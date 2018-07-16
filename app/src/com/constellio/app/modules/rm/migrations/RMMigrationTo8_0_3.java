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

public class RMMigrationTo8_0_3 extends MigrationHelper implements MigrationScript {
	@Override
	public String getVersion() {
		return "8.0.3";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory)
			throws Exception {
		reloadEmailTemplates(appLayerFactory, migrationResourcesProvider, collection);
	}

	public static void reloadEmailTemplates(AppLayerFactory appLayerFactory,
											MigrationResourcesProvider migrationResourcesProvider,
											String collection) {
		if (appLayerFactory.getModelLayerFactory().getCollectionsListManager().getCollectionLanguages(collection).get(0)
				.equals("fr")) {
			reloadEmailTemplate("approvalRequestDeniedForDecomListTemplate.html",
					RMEmailTemplateConstants.APPROVAL_REQUEST_DENIED_TEMPLATE_ID, appLayerFactory, migrationResourcesProvider,
					collection);
		} else {
			reloadEmailTemplate("approvalRequestDeniedForDecomListTemplate_en.html",
					RMEmailTemplateConstants.APPROVAL_REQUEST_DENIED_TEMPLATE_ID, appLayerFactory, migrationResourcesProvider,
					collection);
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
