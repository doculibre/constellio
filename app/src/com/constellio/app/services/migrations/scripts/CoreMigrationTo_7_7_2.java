package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.model.calculators.TemporaryRecordDestructionDateCalculator;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.records.wrappers.ExportAudit;
import com.constellio.model.entities.records.wrappers.ImportAudit;
import com.constellio.model.entities.records.wrappers.TemporaryRecord;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.schemas.validators.TemporaryRecordValidator;

import static java.util.Arrays.asList;

public class CoreMigrationTo_7_7_2 implements MigrationScript {
    @Override
    public String getVersion() {
        return "7.7.2";
    }

    @Override
    public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory) throws Exception {
        new CoreSchemaAlterationFor_7_7_2(collection, migrationResourcesProvider, appLayerFactory).migrate();
    }

    class CoreSchemaAlterationFor_7_7_2 extends MetadataSchemasAlterationHelper {

        protected CoreSchemaAlterationFor_7_7_2(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory) {
            super(collection, migrationResourcesProvider, appLayerFactory);
        }

        @Override
        protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
            MetadataSchemaBuilder defaultSchema = typesBuilder.getDefaultSchema(TemporaryRecord.SCHEMA_TYPE);
            defaultSchema.get(TemporaryRecord.DAY_BEFORE_DESTRUCTION).setDefaultValue(7.0);
        }
    }
}
