package com.constellio.model.services.records;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.model.enums.DecommissioningType;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.RecordUpdateOptions;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.schemas.entries.AggregatedCalculator;
import com.constellio.model.entities.schemas.entries.AggregatedValuesParams;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static com.constellio.model.entities.schemas.MetadataValueType.DATE;
import static com.constellio.model.entities.schemas.MetadataValueType.DATE_TIME;
import static com.constellio.sdk.tests.TestUtils.asList;
import static com.constellio.sdk.tests.TestUtils.assertThatRecord;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

public class AggregatedCalculatorAcceptanceTest extends ConstellioTest{
    RMTestRecords records = new RMTestRecords(zeCollection);

    String AGGREGATED_METADATA = "aggregatedMetadata";

    @Before
    public void setUp()
            throws Exception {

        givenBackgroundThreadsEnabled();
        givenSystemLanguageIs("fr");
        givenTransactionLogIsEnabled();

        prepareSystem(
                withZeCollection().withConstellioRMModule().withAllTestUsers().withRobotsModule()
                        .withRMTest(records).withFoldersAndContainersOfEveryStatus()
        );
    }

    @Test
    public void givenADependencyForAggregatedCalculatorIsModifiedThenRecordMarkedForReindexing() throws Exception {
        getAppLayerFactory().getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
            @Override
            public void alter(MetadataSchemaTypesBuilder types) {
                types.getDefaultSchema(ContainerRecord.SCHEMA_TYPE).create(AGGREGATED_METADATA)
                        .setType(MetadataValueType.STRING).defineDataEntry()
                        .asCalculatedAggregation(types.getDefaultSchema(Folder.SCHEMA_TYPE).getMetadata(Folder.CONTAINER),
                                TestCalculatorThatConcatenatesTitles.class);
            }
        });

        RecordServices recordServices = getModelLayerFactory().newRecordServices();
        ContainerRecord containerBac13 = records.getContainerBac13();
        recordServices.recalculate(containerBac13);
        recordServices.update(containerBac13.getWrappedRecord());

        assertThatRecord(records.getContainerBac13()).extracting(AGGREGATED_METADATA).isEqualTo(asList("Crocodile - Dauphin - Dindon"));

        recordServices.update(records.getFolder_A42().setTitle("new Title"));
        assertThat(fetchBac13FromSolr().get(Schemas.MARKED_FOR_REINDEXING)).isEqualTo(Boolean.TRUE);
        waitForBatchProcess();
        assertThatRecord(records.getContainerBac13()).extracting(AGGREGATED_METADATA).isEqualTo(asList("Dauphin - Dindon - new Title"));
    }

    @Test
    public void givenAggregatedMinimumMetadataThenReturnsMinimum() throws Exception {
        getAppLayerFactory().getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
            @Override
            public void alter(MetadataSchemaTypesBuilder types) {
                types.getDefaultSchema(ContainerRecord.SCHEMA_TYPE).create(AGGREGATED_METADATA)
                        .setType(MetadataValueType.NUMBER).defineDataEntry()
                        .asMin(types.getDefaultSchema(Folder.SCHEMA_TYPE).getMetadata(Folder.CONTAINER),
                                types.getDefaultSchema(Folder.SCHEMA_TYPE).getMetadata(Folder.LINEAR_SIZE));
            }
        });

        RecordServices recordServices = getModelLayerFactory().newRecordServices();
        ContainerRecord containerBac13 = records.getContainerBac13();
        recordServices.recalculate(containerBac13);
        recordServices.update(containerBac13.getWrappedRecord());

        assertThatRecord(records.getContainerBac13()).extracting(AGGREGATED_METADATA).isEqualTo(asList(0D));

        recordServices.update(records.getFolder_A42().setLinearSize(3D));
        recordServices.update(records.getFolder_A43().setLinearSize(2D));
        recordServices.update(records.getFolder_A44().setLinearSize(1D));
        assertThat(fetchBac13FromSolr().get(Schemas.MARKED_FOR_REINDEXING)).isEqualTo(Boolean.TRUE);
        waitForBatchProcess();
        assertThatRecord(records.getContainerBac13()).extracting(AGGREGATED_METADATA).isEqualTo(asList(1D));
    }

    @Test
    public void givenAggregatedMaximumMetadataThenReturnsMaximum() throws Exception {
        getAppLayerFactory().getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
            @Override
            public void alter(MetadataSchemaTypesBuilder types) {
                types.getDefaultSchema(ContainerRecord.SCHEMA_TYPE).create(AGGREGATED_METADATA)
                        .setType(MetadataValueType.NUMBER).defineDataEntry()
                        .asMax(types.getDefaultSchema(Folder.SCHEMA_TYPE).getMetadata(Folder.CONTAINER),
                                types.getDefaultSchema(Folder.SCHEMA_TYPE).getMetadata(Folder.LINEAR_SIZE));
            }
        });

        RecordServices recordServices = getModelLayerFactory().newRecordServices();
        ContainerRecord containerBac13 = records.getContainerBac13();
        recordServices.recalculate(containerBac13);
        recordServices.update(containerBac13.getWrappedRecord());

        assertThatRecord(records.getContainerBac13()).extracting(AGGREGATED_METADATA).isEqualTo(asList(0D));

        recordServices.update(records.getFolder_A42().setLinearSize(3D));
        recordServices.update(records.getFolder_A43().setLinearSize(2D));
        recordServices.update(records.getFolder_A44().setLinearSize(1D));
        assertThat(fetchBac13FromSolr().get(Schemas.MARKED_FOR_REINDEXING)).isEqualTo(Boolean.TRUE);
        waitForBatchProcess();
        assertThatRecord(records.getContainerBac13()).extracting(AGGREGATED_METADATA).isEqualTo(asList(3D));
    }

    public Record fetchBac13FromSolr() {
        return getModelLayerFactory().newSearchServices().search(
                new LogicalSearchQuery().setCondition(LogicalSearchQueryOperators.fromAllSchemasIn("zeCollection")
                        .where(Schemas.IDENTIFIER).isEqualTo("bac13"))).get(0);
    }

    static public class TestCalculatorThatConcatenatesTitles implements AggregatedCalculator<String> {

        @Override
        public String calculate(AggregatedValuesParams params) {
            SearchServices searchServices = params.getSearchServices();
            LogicalSearchQuery query = params.getQuery();
            List<Record> referenceRecords = searchServices.search(query);
            StringBuilder stringBuilder = new StringBuilder();
            String prefix = "";
            for(Record record: referenceRecords) {
                stringBuilder.append(prefix);
                stringBuilder.append(record.getTitle());
                prefix = " - ";
            }
            return stringBuilder.toString();
        }

        @Override
        public List<String> getMetadataDependencies() {
            return asList(Folder.DEFAULT_SCHEMA + "_" + Folder.TITLE);
        }
    }
}
