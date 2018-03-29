package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.wrappers.TemporaryRecord;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class CoreMigrationTo_7_7_3 implements MigrationScript {
    @Override
    public String getVersion() {
        return "7.7.3";
    }

    @Override
    public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory) throws Exception {
        new CoreMigrationTo_7_7_3.CoreSchemaAlterationFor_7_7_3(collection, migrationResourcesProvider, appLayerFactory).migrate();
    }

    class CoreSchemaAlterationFor_7_7_3 extends MetadataSchemasAlterationHelper {

        protected CoreSchemaAlterationFor_7_7_3(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory) {
            super(collection, migrationResourcesProvider, appLayerFactory);
        }

        @Override
        protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
            MetadataSchemaBuilder categoryDefaultSchema = typesBuilder.getDefaultSchema(Category.SCHEMA_TYPE);
            categoryDefaultSchema.getMetadata(Category.TITLE).setMultiLingual(true);
            categoryDefaultSchema.getMetadata(Category.DESCRIPTION).setMultiLingual(true);
            categoryDefaultSchema.getMetadata(Category.KEYWORDS).setMultiLingual(true);

            MetadataSchemaBuilder administrativeUnitDefaultSchema = typesBuilder.getDefaultSchema(AdministrativeUnit.DEFAULT_SCHEMA);
            administrativeUnitDefaultSchema.getMetadata(AdministrativeUnit.TITLE).setMultiLingual(true);
            administrativeUnitDefaultSchema.getMetadata(AdministrativeUnit.DESCRIPTION).setMultiLingual(true);

            MetadataSchemaBuilder retentionRuleDefaultSchema = typesBuilder.getDefaultSchema(RetentionRule.DEFAULT_SCHEMA);

            retentionRuleDefaultSchema.getMetadata(RetentionRule.TITLE).setMultiLingual(true);
        }
    }
}
