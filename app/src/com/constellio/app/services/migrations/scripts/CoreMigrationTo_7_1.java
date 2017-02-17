package com.constellio.app.services.migrations.scripts;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import com.constellio.app.modules.reports.wrapper.Printable;
import com.constellio.model.entities.schemas.MetadataValueType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class CoreMigrationTo_7_1 implements MigrationScript {

    private static final Logger LOGGER = LoggerFactory.getLogger(CoreMigrationTo_7_1.class);

    @Override
    public String getVersion() {
        return "7.1";
    }

    @Override
    public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory)
            throws Exception {

        new CoreSchemaAlterationFor7_1(collection, migrationResourcesProvider, appLayerFactory).migrate();


    }

    private class CoreSchemaAlterationFor7_1 extends MetadataSchemasAlterationHelper {
        public CoreSchemaAlterationFor7_1(String collection, MigrationResourcesProvider migrationResourcesProvider,
                                          AppLayerFactory appLayerFactory) {
            super(collection, migrationResourcesProvider, appLayerFactory);
        }

        @Override
        protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
            MetadataSchemaBuilder builder = typesBuilder.createNewSchemaType(Printable.SCHEMA_TYPE).getDefaultSchema();
            builder.create(Printable.JASPERFILE).setType(MetadataValueType.CONTENT).setUndeletable(true).setEssential(true).defineDataEntry().asManual();
            builder.create(Printable.ISDELETABLE).setType(MetadataValueType.BOOLEAN).setUndeletable(true).setDefaultValue(true).defineDataEntry().asManual();
        }
    }
}
