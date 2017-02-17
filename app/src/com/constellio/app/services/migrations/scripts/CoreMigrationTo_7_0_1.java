package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.records.wrappers.UserDocument;
import com.constellio.model.entities.records.wrappers.UserFolder;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import sun.plugin.javascript.navig4.Document;

/**
 * Created by Nicolas D'Amours on 2017-
 */
public class CoreMigrationTo_7_0_1 implements MigrationScript {
    @Override
    public String getVersion() {
        return "7.0.1";
    }

    @Override
    public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory) throws Exception {
        new CoreAlternationFor7_0_1(collection, migrationResourcesProvider, appLayerFactory).migrate();
    }

    class CoreAlternationFor7_0_1 extends MetadataSchemasAlterationHelper {

        public CoreAlternationFor7_0_1(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory) {
            super(collection, migrationResourcesProvider, appLayerFactory);

        }

        @Override
        protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
            typesBuilder.getSchema(User.DEFAULT_SCHEMA).create(User.AGENT_ENABLED).setType(MetadataValueType.BOOLEAN).setDefaultValue(true);
            MetadataSchemaTypeBuilder type = typesBuilder.createNewSchemaType(UserFolder.SCHEMA_TYPE);
            MetadataSchemaBuilder defaultSchema = type.getDefaultSchema();
            type.setSecurity(false);
            defaultSchema.create(UserFolder.PARENT).setType(MetadataValueType.REFERENCE).setEssential(false).defineReferencesTo(typesBuilder.getSchemaType(UserFolder.SCHEMA_TYPE));

            typesBuilder.getDefaultSchema(UserDocument.SCHEMA_TYPE).create(UserDocument.USER_FOLDER).setEssential(false).setType(MetadataValueType.REFERENCE).defineReferencesTo(typesBuilder.getSchemaType(UserFolder.SCHEMA_TYPE));
        }
    }
}
