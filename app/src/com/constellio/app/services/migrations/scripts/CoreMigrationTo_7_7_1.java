package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.wrappers.ThesaurusConfig;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class CoreMigrationTo_7_7_1 implements MigrationScript {


    @Override
    public String getVersion() {
        return "7.7.1";
    }

    @Override
    public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory) throws Exception {
        new CoreSchemaAlterationFor_7_7_1(collection ,migrationResourcesProvider, appLayerFactory);
    }

    class CoreSchemaAlterationFor_7_7_1 extends MetadataSchemasAlterationHelper {

        protected CoreSchemaAlterationFor_7_7_1(String collection,
                                                MigrationResourcesProvider migrationResourcesProvider,
                                                AppLayerFactory appLayerFactory) {
            super(collection, migrationResourcesProvider, appLayerFactory);
        }

        @Override
        protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
            MetadataSchemaBuilder thesaurusConfig = typesBuilder.createNewSchemaType(ThesaurusConfig.SCHEMA_TYPE).addLabel(Language.French,"configuration du thesaurus")
                    .addLabel(Language.English, "thesaurus configuration").getDefaultSchema();
            thesaurusConfig.createUndeletable(ThesaurusConfig.CONTENT).setSystemReserved(true);
        }
    }
}
