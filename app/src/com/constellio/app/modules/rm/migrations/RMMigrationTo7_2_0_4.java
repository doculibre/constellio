package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

/**
 * Created by Constellio on 2017-05-11.
 */
public class RMMigrationTo7_2_0_4 implements MigrationScript {
    @Override
    public String getVersion() {
        return "7.2.0.4";
    }

    @Override
    public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
                        AppLayerFactory appLayerFactory) {
        new SchemaAlterationFor7_2_0_4(collection, migrationResourcesProvider, appLayerFactory).migrate();
    }

    class SchemaAlterationFor7_2_0_4 extends MetadataSchemasAlterationHelper {

        protected SchemaAlterationFor7_2_0_4(String collection, MigrationResourcesProvider migrationResourcesProvider,
                                             AppLayerFactory appLayerFactory) {
            super(collection, migrationResourcesProvider, appLayerFactory);
        }

        public String getVersion() {
            return "7.2.0.4";
        }

        @Override
        protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
            typesBuilder.getDefaultSchema(ContainerRecord.SCHEMA_TYPE).createUndeletable(ContainerRecord.DOCUMENT_RESPONSIBLE)
                    .setType(MetadataValueType.REFERENCE).setSystemReserved(true).defineReferencesTo(typesBuilder.getSchemaType(User.SCHEMA_TYPE));
        }
    }
}
