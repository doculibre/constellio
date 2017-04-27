package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.RMUser;
import com.constellio.app.modules.rm.wrappers.UniformSubdivision;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.utils.BatchBuilderIterator;
import com.constellio.model.entities.records.RecordUpdateOptions;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

/**
 * Created by Charles Blanchette on 2017-03-22.
 */
public class RMMigrationTo7_2_0_1 implements MigrationScript {
    @Override
    public String getVersion() {
        return "7.2.0.1";
    }

    @Override
    public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
                        AppLayerFactory appLayerFactory) {
        RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
        RecordServices recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
        List<UniformSubdivision> uniformSubdivisions = rm.wrapUniformSubdivisions(appLayerFactory.getModelLayerFactory().newSearchServices().search(
                new LogicalSearchQuery().setCondition(from(rm.uniformSubdivision.schemaType()).returnAll())));
        Map<String, String> descriptions = new HashMap<>();
        for(UniformSubdivision uniformSubdivision: uniformSubdivisions) {
            descriptions.put(uniformSubdivision.getId(), uniformSubdivision.getDescription());
            uniformSubdivision.setDescription(null);
        }

        BatchBuilderIterator<UniformSubdivision> batchIterator = new BatchBuilderIterator<>(uniformSubdivisions.iterator(), 100);

        while (batchIterator.hasNext()) {
            Transaction transaction = new Transaction();
            transaction.addAll(batchIterator.next());
            transaction.setOptions(RecordUpdateOptions.validationExceptionSafeOptions());
            try {
                recordServices.executeWithoutImpactHandling(transaction);
            } catch (RecordServicesException e) {
                e.printStackTrace();
                throw new RuntimeException("Failed to set uniformSubdivision descriptions to null in RMMigration7_2_0_1");
            }
        }

        new SchemaAlterationFor7_2_0_1(collection, migrationResourcesProvider, appLayerFactory).migrate();
        new SchemaAlterationFor7_2_0_1_step2(collection, migrationResourcesProvider, appLayerFactory).migrate();

        uniformSubdivisions = rm.wrapUniformSubdivisions(appLayerFactory.getModelLayerFactory().newSearchServices().search(
                new LogicalSearchQuery().setCondition(from(rm.uniformSubdivision.schemaType()).returnAll())));
        for(UniformSubdivision uniformSubdivision: uniformSubdivisions) {
            uniformSubdivision.setDescription(descriptions.get(uniformSubdivision.getId()));
        }

        batchIterator = new BatchBuilderIterator<>(uniformSubdivisions.iterator(), 100);

        while (batchIterator.hasNext()) {
            Transaction transaction = new Transaction();
            transaction.addAll(batchIterator.next());
            transaction.setOptions(RecordUpdateOptions.validationExceptionSafeOptions());
            try {
                recordServices.executeWithoutImpactHandling(transaction);
            } catch (RecordServicesException e) {
                e.printStackTrace();
                throw new RuntimeException("Failed to set uniformSubdivision descriptions to null in RMMigration7_2_0_1");
            }
        }

    }

    class SchemaAlterationFor7_2_0_1 extends MetadataSchemasAlterationHelper {

        protected SchemaAlterationFor7_2_0_1(String collection, MigrationResourcesProvider migrationResourcesProvider,
                                               AppLayerFactory appLayerFactory) {
            super(collection, migrationResourcesProvider, appLayerFactory);
        }

        public String getVersion() {
            return "7.2.0.1";
        }

        @Override
        protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
            typesBuilder.getDefaultSchema(RMUser.SCHEMA_TYPE).createUndeletable(RMUser.DEFAULT_ADMINISTRATIVE_UNIT)
                    .setType(MetadataValueType.STRING).setSystemReserved(true);
            typesBuilder.getDefaultSchema(UniformSubdivision.SCHEMA_TYPE)
                    .deleteMetadataWithoutValidation(typesBuilder.getMetadata(UniformSubdivision.DEFAULT_SCHEMA + "_" + UniformSubdivision.DESCRIPTION));
        }
    }

    class SchemaAlterationFor7_2_0_1_step2 extends MetadataSchemasAlterationHelper {

        protected SchemaAlterationFor7_2_0_1_step2(String collection, MigrationResourcesProvider migrationResourcesProvider,
                                             AppLayerFactory appLayerFactory) {
            super(collection, migrationResourcesProvider, appLayerFactory);
        }

        public String getVersion() {
            return "7.2.0.1";
        }

        @Override
        protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
            typesBuilder.getDefaultSchema(UniformSubdivision.SCHEMA_TYPE).create(UniformSubdivision.DESCRIPTION).setType(MetadataValueType.TEXT);
        }
    }
}
