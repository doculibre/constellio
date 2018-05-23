package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class RMMigrationTo8_0_1 extends MigrationHelper implements MigrationScript {

    @Override
    public String getVersion() {
        return "8.0.1";
    }

    @Override
    public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory) throws Exception {
        new RMMigrationTo8_0_1.RMSchemaAlterationFor_8_0_1(collection, migrationResourcesProvider, appLayerFactory).migrate();
    }

    class RMSchemaAlterationFor_8_0_1 extends MetadataSchemasAlterationHelper {

        protected RMSchemaAlterationFor_8_0_1(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory) {
            super(collection, migrationResourcesProvider, appLayerFactory);
        }

        @Override
        protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
            MetadataSchemaBuilder defaultSchema = typesBuilder.getDefaultSchema(Folder.SCHEMA_TYPE);
            defaultSchema.createUndeletable(Folder.SUMMARY).setType(MetadataValueType.STRING);
            // summary
        }
    }
}
