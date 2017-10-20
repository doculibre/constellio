/*
 * Copyright (c) 2017. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.records.wrappers.ExportAudit;
import com.constellio.model.entities.records.wrappers.ImportAudit;
import com.constellio.model.entities.records.wrappers.TemporaryRecord;
import com.constellio.model.entities.schemas.Schemas;

import static java.util.Arrays.asList;

public class CoreMigrationTo_7_6_2 implements MigrationScript {
    @Override
    public String getVersion() {
        return "7.6.2";
    }

    @Override
    public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory) throws Exception {
        editTableMetadataOrder(collection, appLayerFactory.getMetadataSchemasDisplayManager());
    }

    private void editTableMetadataOrder(String collection, SchemasDisplayManager manager) {
        manager.saveSchema(manager.getSchema(collection, ExportAudit.FULL_SCHEMA).withTableMetadataCodes(asList(
                ExportAudit.FULL_SCHEMA + "_" + Schemas.CREATED_BY.getLocalCode(),
                ExportAudit.FULL_SCHEMA + "_" + Schemas.CREATED_ON.getLocalCode(),
                ExportAudit.FULL_SCHEMA + "_" + TemporaryRecord.DESTRUCTION_DATE,
                ExportAudit.FULL_SCHEMA + "_" + TemporaryRecord.CONTENT)));

        manager.saveSchema(manager.getSchema(collection, ImportAudit.FULL_SCHEMA).withTableMetadataCodes(asList(
                ImportAudit.FULL_SCHEMA + "_" + Schemas.CREATED_BY.getLocalCode(),
                ImportAudit.FULL_SCHEMA + "_" + Schemas.CREATED_ON.getLocalCode(),
                ImportAudit.FULL_SCHEMA + "_" + TemporaryRecord.DESTRUCTION_DATE,
                ImportAudit.FULL_SCHEMA + "_" + TemporaryRecord.CONTENT)));
    }
}
