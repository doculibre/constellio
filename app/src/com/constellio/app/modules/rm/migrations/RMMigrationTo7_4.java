package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Printable;
import com.constellio.app.modules.rm.wrappers.PrintableReport;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

/**
 * Created by constellios on 2017-07-13.
 */
public class RMMigrationTo7_4 implements MigrationScript {
    @Override
    public String getVersion() {
        return "7.4";
    }

    @Override
    public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory) throws Exception {
        new RMMigrationTo7_4.SchemaAlterationFor7_4(collection, migrationResourcesProvider, appLayerFactory).migrate();
    }

    class SchemaAlterationFor7_4 extends MetadataSchemasAlterationHelper {

        protected SchemaAlterationFor7_4(String collection, MigrationResourcesProvider migrationResourcesProvider,
                                         AppLayerFactory appLayerFactory) {
            super(collection, migrationResourcesProvider, appLayerFactory);
        }

        @Override
        protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
            MetadataSchemaBuilder metadataSchemaBuilder = typesBuilder.getSchemaType(Printable.SCHEMA_TYPE).createCustomSchema(PrintableReport.SCHEMA_TYPE);

            metadataSchemaBuilder.create(PrintableReport.RECORD_TYPE).setType(MetadataValueType.STRING).setEssential(true);
            metadataSchemaBuilder.create(PrintableReport.RECORD_SCHEMA).setType(MetadataValueType.STRING).setEssential(true);

            MetadataSchemaBuilder containerRecord = typesBuilder.getSchema(ContainerRecord.DEFAULT_SCHEMA);
            containerRecord.getMetadata(ContainerRecord.DECOMMISSIONING_TYPE).setDefaultRequirement(true);
            containerRecord.getMetadata(ContainerRecord.ADMINISTRATIVE_UNITS).setDefaultRequirement(true);

        }
    }
}
