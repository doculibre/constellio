/*
 * Copyright (c) 2017. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.wrappers.ExportAudit;
import com.constellio.model.entities.records.wrappers.ImportAudit;
import com.constellio.model.entities.records.wrappers.TemporaryRecord;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

import java.util.HashMap;

import static java.util.Arrays.asList;

public class CoreMigrationTo_7_6_2 implements MigrationScript {
    @Override
    public String getVersion() {
        return "7.6.2";
    }

    @Override
    public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory) throws Exception {
        new CoreSchemaAlterationFor_7_6_2(collection, migrationResourcesProvider, appLayerFactory).migrate();
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

    class CoreSchemaAlterationFor_7_6_2 extends MetadataSchemasAlterationHelper {

        protected CoreSchemaAlterationFor_7_6_2(String collection, MigrationResourcesProvider migrationResourcesProvider,
                                                AppLayerFactory appLayerFactory) {
            super(collection, migrationResourcesProvider, appLayerFactory);
        }

        @Override
        protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
            HashMap<Language, String> labels = new HashMap<>();
            labels.put(Language.French, "Jetons manuels");
            labels.put(Language.English, "Manual tokens");

            for(MetadataSchemaTypeBuilder type: typesBuilder.getTypes()) {
                MetadataSchemaBuilder defaultSchema = type.getDefaultSchema();
                defaultSchema.getMetadata(Schemas.MANUAL_TOKENS.getLocalCode())
                        .setLabels(labels);
                if(defaultSchema.hasMetadata("errorStackTrace")) {
                    defaultSchema.getMetadata(Schemas.MANUAL_TOKENS.getLocalCode())
                            .addLabel(Language.French, "Fil d'exécution");
                }
            }
        }
    }
}
