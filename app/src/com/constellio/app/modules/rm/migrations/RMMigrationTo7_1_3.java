package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.type.MediumType;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Charles Blanchette on 2017-03-22.
 */
public class RMMigrationTo7_1_3 implements MigrationScript {
    @Override
    public String getVersion() {
        return "7.1.3";
    }

    @Override
    public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
                        AppLayerFactory appLayerFactory) {
        new SchemaAlterationFor7_1_3(collection, migrationResourcesProvider, appLayerFactory).migrate();
        createMediumTypes(collection, appLayerFactory.getModelLayerFactory(), migrationResourcesProvider);
    }

    private void createMediumTypes(String collection, ModelLayerFactory modelLayerFactory,
                                   MigrationResourcesProvider migrationResourcesProvider) {
        Transaction transaction = new Transaction();

        RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, modelLayerFactory);
        SearchServices searchServices = modelLayerFactory.newSearchServices();

        String driveCode = migrationResourcesProvider.getDefaultLanguageString("MediumType.driveCode");
        String driveTitle = migrationResourcesProvider.getDefaultLanguageString("MediumType.driveTitle");

        LogicalSearchQuery query = new LogicalSearchQuery(from(rm.mediumTypeSchemaType()).where(ALL));

        List<MediumType> mediumTypes = rm.wrapMediumTypes(searchServices.search(query));
        List<MediumType> mediumTypesAnalogiques = new ArrayList<>();
        List<MediumType> mediumTypesNonAnalogiques = new ArrayList<>();

        if (rm.getMediumTypeByCode(driveCode) == null) {

            for (MediumType mediumType : mediumTypes) {
                if (mediumType.isAnalogical()) {
                    mediumTypesAnalogiques.add(mediumType);
                } else {
                    mediumTypesNonAnalogiques.add(mediumType);
                }
            }

            if (mediumTypesNonAnalogiques.size() == 0) {
                transaction.add(rm.newMediumType().setCode(driveCode)
                        .setTitle(driveTitle)
                        .setAnalogical(false));
            } else {
                MediumType mediumType = mediumTypesNonAnalogiques.get(0);
                transaction.add(mediumType.setCode(driveCode)
                        .setAnalogical(false));
            }
        }

//        transaction.add(mediumType.setCode(driveCode)
//                            .setTitle(driveTitle));

//        transaction.add(rm.newMediumType().setCode(driveCode)
//                .setTitle("test")
//                .setAnalogical(false));

        try {
            modelLayerFactory.newRecordServices().execute(transaction);
        } catch (RecordServicesException e) {
            throw new RuntimeException(e);
        }

    }

    class SchemaAlterationFor7_1_3 extends MetadataSchemasAlterationHelper {

        protected SchemaAlterationFor7_1_3(String collection, MigrationResourcesProvider migrationResourcesProvider,
                                           AppLayerFactory appLayerFactory) {
            super(collection, migrationResourcesProvider, appLayerFactory);
        }

        public String getVersion() {
            return "7.1.3";
        }

        @Override
        protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {

        }
    }
}
