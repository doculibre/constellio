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

public class RMMigrationTo9_1_0_13 extends MigrationHelper implements MigrationScript {
	private String collection;

	private MigrationResourcesProvider migrationResourcesProvider;

	private AppLayerFactory appLayerFactory;

	@Override
	public String getVersion() {
		return "9.1.0.13";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {
		this.collection = collection;
		this.migrationResourcesProvider = migrationResourcesProvider;
		this.appLayerFactory = appLayerFactory;

		reloadEmailTemplates(collection, migrationResourcesProvider, appLayerFactory);
	}

	public static void reloadEmailTemplates(String collection, MigrationResourcesProvider migrationResourcesProvider,
											AppLayerFactory appLayerFactory) {
		// Template is changed for a new template because the new one now include document and box instead of folder only.
		// The previous template was remindReturnBorrowedFolderTemplate.html/remindReturnBorrowedFolderTemplate_en.html.
		if (appLayerFactory.getModelLayerFactory().getCollectionsListManager()
				.getCollectionLanguages(collection).get(0).equals("en")) {
			reloadEmailTemplate(collection, migrationResourcesProvider, appLayerFactory, "remindReturnBorrowedRecordTemplate_en.html", RMEmailTemplateConstants.REMIND_BORROW_TEMPLATE_ID);
		} else {
			reloadEmailTemplate(collection, migrationResourcesProvider, appLayerFactory, "remindReturnBorrowedRecordTemplate.html", RMEmailTemplateConstants.REMIND_BORROW_TEMPLATE_ID);
		}
	}

	private static void reloadEmailTemplate(String collection, MigrationResourcesProvider migrationResourcesProvider,
									 AppLayerFactory appLayerFactory, final String templateFileName, final String templateId) {
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
