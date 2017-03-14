package com.constellio.app.modules.rm.migrations;

import static com.constellio.model.entities.schemas.MetadataValueType.DATE_TIME;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.wrappers.Borrowing;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

/**
 * Created by Charles Blanchette on 2017-03-10.
 */
public class RMMigrationTo7_1_1 extends MigrationHelper implements MigrationScript {

    @Override
    public String getVersion() {
        return "7.1.1";
    }

    @Override
    public void migrate(String collection, MigrationResourcesProvider provider, AppLayerFactory factory)
            throws Exception {
        new SchemaAlterationsFor7_1_1(collection, provider, factory).migrate();
    }

    public static class SchemaAlterationsFor7_1_1 extends MetadataSchemasAlterationHelper {

        protected SchemaAlterationsFor7_1_1(String collection, MigrationResourcesProvider provider, AppLayerFactory factory) {
            super(collection, provider, factory);
        }

        @Override
        protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
            MetadataSchemaBuilder builder = typesBuilder.getSchemaType(Event.SCHEMA_TYPE)
                    .createCustomSchema(Borrowing.SCHEMA_BORROWING);

            builder.create(Borrowing.REQUEST_DATE).setType(DATE_TIME).setEssential(true).defineDataEntry().asManual();

            builder.create(Borrowing.BORROWING_DATE).setType(DATE_TIME).setEssential(true).defineDataEntry().asManual();

            builder.create(Borrowing.RETURN_DATE).setType(DATE_TIME).setEssential(true).defineDataEntry().asManual();

            builder.create(Borrowing.RETURN_USERNAME).setType(STRING).setEssential(true).defineDataEntry().asManual();

            builder.create(Borrowing.RETURN_USER_ID).setType(STRING).setEssential(true).defineDataEntry().asManual();
        }

    }

}