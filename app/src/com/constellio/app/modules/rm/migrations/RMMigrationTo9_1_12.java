package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.RMEmailTemplateConstants;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.managers.config.ConfigManagerException.OptimisticLockingConfiguration;
import com.constellio.model.services.emails.EmailTemplatesManager;

import java.io.IOException;
import java.io.InputStream;

public class RMMigrationTo9_1_12 extends MigrationHelper implements MigrationScript {

	private String collection;

	private MigrationResourcesProvider migrationResourcesProvider;

	private AppLayerFactory appLayerFactory;

	@Override
	public String getVersion() {
		return "9.1.12";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory) throws Exception {
		this.collection = collection;
		this.migrationResourcesProvider = migrationResourcesProvider;
		this.appLayerFactory = appLayerFactory;

		addEmailTemplates();
	}

	private void addEmailTemplates() {
		addEmailTemplates("externalSignatureRequestTemplate.html",
				RMEmailTemplateConstants.SIGNATURE_REQUEST);
	}

	private void addEmailTemplates(String templateFileName, String templateId) {
		EmailTemplatesManager emailTemplateManager = appLayerFactory.getModelLayerFactory()
				.getEmailTemplatesManager();
		try (InputStream templateInputStream = migrationResourcesProvider.getStream(templateFileName)) {
			emailTemplateManager.addCollectionTemplateIfInexistent(templateId, collection, templateInputStream);
		} catch (IOException | OptimisticLockingConfiguration e) {
			throw new RuntimeException(e);
		}
	}
}
