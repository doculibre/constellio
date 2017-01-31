package com.constellio.app.modules.rm.model.calculators.storageSpace;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.StorageSpace;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordPhysicalDeleteOptions;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Created by Constellio on 2016-12-19.
 */

public class StorageSpaceAvailableSizeCalculatorAcceptanceTest extends ConstellioTest {

    RMTestRecords records = new RMTestRecords(zeCollection);

    StorageSpaceAvailableSizeCalculator calculator;

    RMSchemasRecordsServices rm;

    RecordServices recordServices;

    SearchServices searchServices;

    @Mock CalculatorParameters parameters;

    @Before
    public void setUp() {
        givenBackgroundThreadsEnabled();
        calculator = spy(new StorageSpaceAvailableSizeCalculator());
        prepareSystem(
                withZeCollection().withConstellioRMModule().withAllTestUsers()
                        .withRMTest(records).withFoldersAndContainersOfEveryStatus().withDocumentsDecommissioningList()
        );

        rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
        recordServices = getModelLayerFactory().newRecordServices();
        searchServices = getModelLayerFactory().newSearchServices();
    }

    @Test
    public void givenParametersThenCalculatorReturnsGoodValue()  {
        when(parameters.get(calculator.linearSizeParam)).thenReturn(new Double(6));
        when(parameters.get(calculator.capacityParam)).thenReturn(new Double(10));
        assertThat(calculator.calculate(parameters)).isEqualTo(4);

        when(parameters.get(calculator.linearSizeParam)).thenReturn(new Double(6));
        when(parameters.get(calculator.capacityParam)).thenReturn(null);
        assertThat(calculator.calculate(parameters)).isEqualTo(null);

        when(parameters.get(calculator.linearSizeParam)).thenReturn(null);
        when(parameters.get(calculator.capacityParam)).thenReturn(new Double(10));
        assertThat(calculator.calculate(parameters)).isEqualTo(10);

        when(parameters.get(calculator.linearSizeParam)).thenReturn(null);
        when(parameters.get(calculator.capacityParam)).thenReturn(null);
        assertThat(calculator.calculate(parameters)).isEqualTo(null);
    }

    @Test
    public void givenStorageSpaceWithCapacityAndLinearSizeThenAvailableSizeIsEqualToDifference()
            throws RecordServicesException {

        StorageSpace storageSpace = buildDefaultStorageSpace().setCapacity(new Long(10)).setLinearSizeEntered(6);
        recordServices.add(storageSpace);

        getModelLayerFactory().getBatchProcessesManager().waitUntilAllFinished();
        Record record = searchServices.searchSingleResult(from(rm.storageSpace.schemaType()).where(Schemas.IDENTIFIER).isEqualTo("storageTest"));
        assertThat(rm.wrapStorageSpace(record).getLinearSizeEntered()).isEqualTo(6);
        assertThat(rm.wrapStorageSpace(record).getLinearSize()).isEqualTo(new Double(6));
        assertThat(rm.wrapStorageSpace(record).getCapacity()).isEqualTo(new Long(10));
        assertThat(rm.wrapStorageSpace(record).getAvailableSize()).isEqualTo(new Double(4));
    }

    @Test
    public void givenStorageSpaceWithoutCapacityThenAvailableSizeIsNull()
            throws RecordServicesException {

        StorageSpace storageSpace = buildDefaultStorageSpace().setLinearSizeEntered(6);
        recordServices.add(storageSpace);

        getModelLayerFactory().getBatchProcessesManager().waitUntilAllFinished();
        Record record = searchServices.searchSingleResult(from(rm.storageSpace.schemaType()).where(Schemas.IDENTIFIER).isEqualTo("storageTest"));
        assertThat(rm.wrapStorageSpace(record).getLinearSizeEntered()).isEqualTo(6);
        assertThat(rm.wrapStorageSpace(record).getLinearSize()).isEqualTo(new Double(6));
        assertThat(rm.wrapStorageSpace(record).getCapacity()).isNull();
        assertThat(rm.wrapStorageSpace(record).getAvailableSize()).isNull();
        recordServices.physicallyDeleteNoMatterTheStatus(record, User.GOD, new RecordPhysicalDeleteOptions());

        storageSpace = buildDefaultStorageSpace();
        recordServices.add(storageSpace);

        getModelLayerFactory().getBatchProcessesManager().waitUntilAllFinished();
        record = searchServices.searchSingleResult(from(rm.storageSpace.schemaType()).where(Schemas.IDENTIFIER).isEqualTo("storageTest"));
        assertThat(rm.wrapStorageSpace(record).getLinearSizeEntered()).isNull();
        assertThat(rm.wrapStorageSpace(record).getLinearSize()).isEqualTo(new Double(0));
        assertThat(rm.wrapStorageSpace(record).getCapacity()).isNull();
        assertThat(rm.wrapStorageSpace(record).getAvailableSize()).isNull();
    }

    public StorageSpace buildDefaultStorageSpace() {
        return rm.newStorageSpaceWithId("storageTest").setCode("TEST").setTitle("storageTest");
    }
}
