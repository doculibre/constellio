package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RMUser;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaDisplayManagerTransaction;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

import static com.constellio.model.entities.schemas.MetadataValueType.BOOLEAN;

public class RMMigrationTo7_7_4_33 extends MigrationHelper implements MigrationScript {
    @Override
    public String getVersion() {
        return "7.7.4.33";
    }

    @Override
    public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory) throws Exception {
        new SchemaAlterationFor7_7_4_33(collection, migrationResourcesProvider, appLayerFactory).migrate();
    }

    class SchemaAlterationFor7_7_4_33 extends MetadataSchemasAlterationHelper {

        protected SchemaAlterationFor7_7_4_33(String collection, MigrationResourcesProvider migrationResourcesProvider,
                                           AppLayerFactory appLayerFactory) {
            super(collection, migrationResourcesProvider, appLayerFactory);
        }

        @Override
        protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
            MetadataSchemaTypeBuilder userSchemaType = typesBuilder.getSchemaType(RMUser.SCHEMA_TYPE);
            MetadataSchemaBuilder userSchema = userSchemaType.getDefaultSchema();
            userSchema.createUndeletable(RMUser.HIDE_NOT_ACTIVE).setType(BOOLEAN).setDefaultValue(false);

            SchemasDisplayManager displayManager = appLayerFactory.getMetadataSchemasDisplayManager();
            SchemaDisplayManagerTransaction transaction = new SchemaDisplayManagerTransaction();
            transaction.add(displayManager.getSchema(collection, User.DEFAULT_SCHEMA)
                    .withNewDisplayMetadataQueued(RMUser.HIDE_NOT_ACTIVE));
        }
    }
}
