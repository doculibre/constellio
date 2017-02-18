package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RMUserFolder;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.wrappers.UserFolder;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

/**
 * Created by Marco on 2017-02-13.
 */
public class RMMigrationTo_7_0_1 implements MigrationScript {
    @Override
    public String getVersion() {
        return "7.0.1";
    }

    @Override
    public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory) throws Exception {
        new RMAlternationFor7_0_1(collection, migrationResourcesProvider, appLayerFactory).migrate();
    }

    class RMAlternationFor7_0_1 extends MetadataSchemasAlterationHelper {

        protected RMAlternationFor7_0_1(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory) {
            super(collection, migrationResourcesProvider, appLayerFactory);
        }

        @Override
        protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
            MetadataSchemaBuilder defaultSchema = typesBuilder.getSchemaType(UserFolder.SCHEMA_TYPE).getDefaultSchema();

            defaultSchema.create(RMUserFolder.ADMINISTRATIVE_UNIT).setType(MetadataValueType.REFERENCE).setEssential(false).defineReferencesTo(typesBuilder.getSchemaType(AdministrativeUnit.SCHEMA_TYPE));
            defaultSchema.create(RMUserFolder.CATEGORY).setType(MetadataValueType.REFERENCE).setEssential(false).defineReferencesTo(typesBuilder.getSchemaType(Category.SCHEMA_TYPE));
            defaultSchema.create(RMUserFolder.RETENTION_RULE).setType(MetadataValueType.REFERENCE).setEssential(false).defineReferencesTo(typesBuilder.getSchemaType(RetentionRule.SCHEMA_TYPE));
            defaultSchema.create(RMUserFolder.PARENT_FOLDER).setType(MetadataValueType.REFERENCE).setEssential(false).defineReferencesTo(typesBuilder.getSchemaType(Folder.SCHEMA_TYPE));
        }
    }
}
