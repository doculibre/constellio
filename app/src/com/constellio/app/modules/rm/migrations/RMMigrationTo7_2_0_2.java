package com.constellio.app.modules.rm.migrations;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.PrintableLabel;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.model.entities.records.Content;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;

public class RMMigrationTo7_2_0_2 implements MigrationScript {
	@Override
	public String getVersion() {
		return "7.2.0.2";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
			AppLayerFactory appLayerFactory) {

		fixTemplates(collection, migrationResourcesProvider, appLayerFactory);
		appLayerFactory.getSystemGlobalConfigsManager().setReindexingRequired(true);
	}

	private void fixTemplates(String collection, MigrationResourcesProvider migrationResourcesProvider,
			AppLayerFactory appLayerFactory) {

		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);

		for (PrintableLabel printableLabel : rm.searchPrintableLabels(LogicalSearchQueryOperators.ALL)) {
			switch (printableLabel.getTitle()) {
			case "Code de plan justifié de dossier à droite (Avery 5161)":
				installLabel(printableLabel, "Etiquettes Dossiers/5161/Avery_5161_D_Dossier.jasper", appLayerFactory,
						migrationResourcesProvider);
				break;

			case "Code de plan justifié de dossier à droite (Avery 5163)":
				installLabel(printableLabel, "Etiquettes Dossiers/5163/Avery_5163_D_Dossier.jasper", appLayerFactory,
						migrationResourcesProvider);
				break;

			case "Code de plan justifié de dossier à gauche (Avery 5163)":
				installLabel(printableLabel, "Etiquettes Dossiers/5163/Avery_5163_G_Dossier.jasper", appLayerFactory,
						migrationResourcesProvider);
				break;

			case "Code de plan justifié de dossier à droite (Avery 5159)":
				installLabel(printableLabel, "Etiquettes Dossiers/5159/Avery_5159_D_Dossier.jasper", appLayerFactory,
						migrationResourcesProvider);
				break;

			case "Code de plan justifié de dossier à droite (Avery 5162)":
				installLabel(printableLabel, "Etiquettes Dossiers/5162/Avery_5162_D_Dossier.jasper", appLayerFactory,
						migrationResourcesProvider);
				break;

			case "Code de plan justifié de dossier à gauche (Avery 5162)":
				installLabel(printableLabel, "Etiquettes Dossiers/5162/Avery_5162_G_Dossier.jasper", appLayerFactory,
						migrationResourcesProvider);
				break;

			case "Code de plan justifié de dossier à gauche (Avery 5161)":
				installLabel(printableLabel, "Etiquettes Dossiers/5161/Avery_5161_G_Dossier.jasper", appLayerFactory,
						migrationResourcesProvider);
				break;

			case "Code de plan justifié de dossier à gauche (Avery 5159)":
				installLabel(printableLabel, "Etiquettes Dossiers/5159/Avery_5159_G_Dossier.jasper", appLayerFactory,
						migrationResourcesProvider);
				break;

			case "Code de plan justifié de conteneur (Avery 5161)":
				installLabel(printableLabel, "Etiquettes Contenants/5161/Avery_5161_Container.jasper", appLayerFactory,
						migrationResourcesProvider);
				break;

			case "Code de plan justifié de conteneur (Avery 5159)":
				installLabel(printableLabel, "Etiquettes Contenants/5159/Avery_5159_Container.jasper", appLayerFactory,
						migrationResourcesProvider);
				break;

			case "Code de plan justifié de conteneur (Avery 5163)":
				installLabel(printableLabel, "Etiquettes Contenants/5163/Avery_5163_Container.jasper", appLayerFactory,
						migrationResourcesProvider);
				break;

			case "Code de plan justifié de conteneur (Avery 5162)":
				installLabel(printableLabel, "Etiquettes Contenants/5162/Avery_5162_Container.jasper", appLayerFactory,
						migrationResourcesProvider);
				break;

			}
		}

	}

	private void installLabel(PrintableLabel printableLabel, String subPath, AppLayerFactory appLayerFactory,
			MigrationResourcesProvider migrationResourcesProvider) {
		IOServices ioServices = appLayerFactory.getModelLayerFactory().getIOServicesFactory().newIOServices();
		RecordServices recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
		ContentManager contentManager = appLayerFactory.getModelLayerFactory().getContentManager();
		Content content = printableLabel.getJasperfile();
		File jasperFile = migrationResourcesProvider.getFile(("defaultJasperFiles/" + subPath).replace("/", File.separator));

		InputStream inputStream = null;
		try {
			inputStream = ioServices.newFileInputStream(jasperFile, "installLabel");
			ContentVersionDataSummary summary = contentManager.upload(inputStream, false, false, "jasperFile.jasper");
			Content newContent = contentManager.createFileSystem(content.getCurrentVersion().getFilename(), summary);
			printableLabel.setJasperFile(newContent);
			try {
				recordServices.update(printableLabel);
			} catch (RecordServicesException e) {
				throw new RuntimeException(e);
			}
		} catch (FileNotFoundException e) {
			ioServices.closeQuietly(inputStream);
		}

	}

}
