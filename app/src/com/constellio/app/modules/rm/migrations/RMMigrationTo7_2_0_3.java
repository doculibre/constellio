package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.PrintableLabel;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.RecordUpdateOptions;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;

/**
 * Created by Constellio on 2017-05-11.
 */
public class RMMigrationTo7_2_0_3 implements MigrationScript {
    @Override
    public String getVersion() {
        return "7.2.0.3";
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

        Transaction transaction = new Transaction();
        transaction.setOptions(RecordUpdateOptions.validationExceptionSafeOptions());

        for (PrintableLabel printableLabel : rm.searchPrintableLabels(LogicalSearchQueryOperators.ALL)) {
            switch (printableLabel.getTitle()) {
                case "Code de plan justifié de dossier à droite (Avery 5161)":
                case "Code de plan justifié de dossier à droite (Avery 5163)":
                case "Code de plan justifié de dossier à gauche (Avery 5163)":
                case "Code de plan justifié de dossier à droite (Avery 5159)":
                case "Code de plan justifié de dossier à droite (Avery 5162)":
                case "Code de plan justifié de dossier à gauche (Avery 5162)":
                case "Code de plan justifié de dossier à gauche (Avery 5161)":
                case "Code de plan justifié de dossier à gauche (Avery 5159)":
                case "Code de plan justifié de conteneur (Avery 5161)":
                case "Code de plan justifié de conteneur (Avery 5159)":
                case "Code de plan justifié de conteneur (Avery 5163)":
                case "Code de plan justifié de conteneur (Avery 5162)":
                    transaction.add(printableLabel.setIsDeletable(true));
                    break;
            }
        }

        try {
            appLayerFactory.getModelLayerFactory().newRecordServices().executeWithoutImpactHandling(transaction);
        } catch (RecordServicesException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to set default printable labels to deletable");
        }
    }
}
