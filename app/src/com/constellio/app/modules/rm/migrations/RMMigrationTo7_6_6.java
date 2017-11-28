package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.wrappers.BagInfo;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import org.apache.commons.collections.Bag;

public class RMMigrationTo7_6_6 extends MigrationHelper implements MigrationScript {

    @Override
    public String getVersion() {
        return "7.6.6";
    }

    @Override
    public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory) throws Exception {
        new SchemaAlterationFor7_6_5(collection, migrationResourcesProvider, appLayerFactory).migrate();
    }

    class SchemaAlterationFor7_6_5 extends MetadataSchemasAlterationHelper {

        protected SchemaAlterationFor7_6_5(String collection, MigrationResourcesProvider migrationResourcesProvider,
                                         AppLayerFactory appLayerFactory) {
            super(collection, migrationResourcesProvider, appLayerFactory);
        }

        @Override
        protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
            MetadataSchemaTypeBuilder builder = typesBuilder.createNewSchemaType(BagInfo.SCHEMA_TYPE);
            MetadataSchemaBuilder defaultBagInfoSchema = builder.getDefaultSchema();

            defaultBagInfoSchema.create(BagInfo.ARCHIVE_TITLE).setType(MetadataValueType.STRING);
            defaultBagInfoSchema.create(BagInfo.NOTE).setType(MetadataValueType.TEXT);
            defaultBagInfoSchema.create(BagInfo.IDENTIFICATION_ORGANISME_VERSEUR_OU_DONATEUR).setType(MetadataValueType.STRING);
            defaultBagInfoSchema.create(BagInfo.ID_ORGANISME_VERSEUR_OU_DONATEUR).setType(MetadataValueType.STRING);
            defaultBagInfoSchema.create(BagInfo.ADRESSE).setType(MetadataValueType.STRING);
            defaultBagInfoSchema.create(BagInfo.REGION_ADMINISTRATIVE).setType(MetadataValueType.STRING);
            defaultBagInfoSchema.create(BagInfo.ENTITE_RESPONSABLE).setType(MetadataValueType.STRING);
            defaultBagInfoSchema.create(BagInfo.IDENTIFICATION_RESPONSABLE).setType(MetadataValueType.STRING);
            defaultBagInfoSchema.create(BagInfo.COURRIEL_RESPONSABLE).setType(MetadataValueType.STRING);
            defaultBagInfoSchema.create(BagInfo.NUMERO_TELEPHONE_RESPONSABLE).setType(MetadataValueType.STRING);
            defaultBagInfoSchema.create(BagInfo.DESCRIPTION_SOMMAIRE).setType(MetadataValueType.TEXT);
            defaultBagInfoSchema.create(BagInfo.CATEGORIE_DOCUMENT).setType(MetadataValueType.STRING);
            defaultBagInfoSchema.create(BagInfo.METHODE_TRANSFERE).setType(MetadataValueType.STRING);
            defaultBagInfoSchema.create(BagInfo.RESTRICTION_ACCESSIBILITE).setType(MetadataValueType.STRING);
            defaultBagInfoSchema.create(BagInfo.ENCODAGE).setType(MetadataValueType.STRING);
        }

    }
}
