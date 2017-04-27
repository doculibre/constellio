package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.wrappers.RMUser;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

/**
 * Created by Charles Blanchette on 2017-03-22.
 */
public class RMMigrationTo7_2_0_1 implements MigrationScript {
    @Override
    public String getVersion() {
        return "7.2.0.1";
    }

    @Override
    public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
                        AppLayerFactory appLayerFactory) {
        new SchemaAlterationFor7_2_0_1(collection, migrationResourcesProvider, appLayerFactory).migrate();
    }

    class SchemaAlterationFor7_2_0_1 extends MetadataSchemasAlterationHelper {

        protected SchemaAlterationFor7_2_0_1(String collection, MigrationResourcesProvider migrationResourcesProvider,
                                               AppLayerFactory appLayerFactory) {
            super(collection, migrationResourcesProvider, appLayerFactory);
        }

        public String getVersion() {
            return "7.2.0.1";
        }

        @Override
        protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
            typesBuilder.getDefaultSchema(RMUser.SCHEMA_TYPE).createUndeletable(RMUser.DEFAULT_ADMINISTRATIVE_UNIT)
                    .setType(MetadataValueType.STRING).setSystemReserved(true);
        }
    }
}
