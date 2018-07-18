package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.records.wrappers.TemporaryRecord;
import com.constellio.model.entities.records.wrappers.VaultScanReport;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class CoreMigrationTo_8_0_1 implements MigrationScript {
    @Override
    public String getVersion() {
        return "8.0.1";
    }

    @Override
    public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory) throws Exception {
        new CoreSchemaAlterationFor_8_0_1(collection, migrationResourcesProvider, appLayerFactory).migrate();
        SchemasDisplayManager displayManager = appLayerFactory.getMetadataSchemasDisplayManager();
        displayManager.saveSchema(displayManager.getSchema(collection, VaultScanReport.FULL_SCHEMA).withNewTableMetadatas(
                VaultScanReport.FULL_SCHEMA + "_" + VaultScanReport.NUMBER_OF_DELETED_CONTENTS,
                VaultScanReport.FULL_SCHEMA + "_" + VaultScanReport.MESSAGE));
    }

    class CoreSchemaAlterationFor_8_0_1 extends MetadataSchemasAlterationHelper {

        protected CoreSchemaAlterationFor_8_0_1(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory) {
            super(collection, migrationResourcesProvider, appLayerFactory);
        }

        @Override
        protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
            MetadataSchemaBuilder schema = typesBuilder.getSchemaType(TemporaryRecord.SCHEMA_TYPE).createCustomSchema(VaultScanReport.SCHEMA);
            schema.createUndeletable(VaultScanReport.NUMBER_OF_DELETED_CONTENTS).setType(MetadataValueType.NUMBER).setSystemReserved(true);
            schema.createUndeletable(VaultScanReport.MESSAGE).setType(MetadataValueType.TEXT).setSystemReserved(true);
        }
    }
}
