package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.SIParchive;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.wrappers.TemporaryRecord;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import org.joda.time.LocalDateTime;

public class RMMigrationTo7_5 extends MigrationHelper implements MigrationScript {
    @Override
    public String getVersion() {
        return "7.5";
    }

    @Override
    public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory)
            throws Exception {
        new SchemaAlterationFor7_5(collection,
                migrationResourcesProvider, appLayerFactory).migrate();
    }

    class SchemaAlterationFor7_5 extends MetadataSchemasAlterationHelper {

        protected SchemaAlterationFor7_5(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory) {
            super(collection, migrationResourcesProvider, appLayerFactory);
        }

        @Override
        protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
            MetadataSchemaBuilder metadataSchemaBuilder = typesBuilder.getSchemaType(Folder.SCHEMA_TYPE).getDefaultSchema();
            metadataSchemaBuilder.createSystemReserved(Folder.IS_RESTRICTED_ACCESS).setType(MetadataValueType.BOOLEAN).setUndeletable(true);

            MetadataSchemaBuilder builder = typesBuilder.getSchemaType(TemporaryRecord.SCHEMA_TYPE).createCustomSchema(SIParchive.SCHEMA_NAME);
            builder.createUndeletable(SIParchive.NAME).setType(MetadataValueType.STRING).defineDataEntry().asManual();
            builder.createUndeletable(SIParchive.CREATION_DATE).setType(MetadataValueType.DATE_TIME).setDefaultValue(new LocalDateTime()).setEssential(true);
            builder.createUndeletable(SIParchive.USER).setType(MetadataValueType.REFERENCE).defineReferencesTo(typesBuilder.getSchemaType(User.SCHEMA_TYPE)).defineDataEntry().asManual();
        }

    }
}

