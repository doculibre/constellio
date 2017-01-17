package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.reports.wrapper.ReportConfig;
import com.constellio.app.modules.rm.constants.RMTaxonomies;
import com.constellio.app.modules.rm.wrappers.RMReport;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class RMMigrationTo6_7 implements MigrationScript {
    @Override
    public String getVersion() {
        return "6.7";
    }

    @Override
    public void migrate(String collection, MigrationResourcesProvider provider, AppLayerFactory factory)
            throws Exception {
        new SchemaAlterationsFor6_7(collection, provider, factory).migrate();

    }

    public static class SchemaAlterationsFor6_7 extends MetadataSchemasAlterationHelper {

        protected SchemaAlterationsFor6_7(String collection, MigrationResourcesProvider provider, AppLayerFactory factory) {
            super(collection, provider, factory);
        }

        @Override
        protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
            MetadataSchemaBuilder builder = typesBuilder.getSchemaType(RMReport.SCHEMA_TYPE).getDefaultSchema();
            builder.create(RMReport.WIDTH).setType(MetadataValueType.STRING).setUndeletable(true).setEssential(true).defineDataEntry().asManual();
            builder.create(RMReport.HEIGHT).setType(MetadataValueType.STRING).setUndeletable(true).setEssential(true).defineDataEntry().asManual();

        }
    }

}