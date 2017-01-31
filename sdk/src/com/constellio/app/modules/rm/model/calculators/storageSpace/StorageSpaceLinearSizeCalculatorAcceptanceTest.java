package com.constellio.app.modules.rm.model.calculators.storageSpace;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.StorageSpace;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Schemas;
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

public class StorageSpaceLinearSizeCalculatorAcceptanceTest extends ConstellioTest {

    RMTestRecords records = new RMTestRecords(zeCollection);

    StorageSpaceLinearSizeCalculator calculator;

    RMSchemasRecordsServices rm;

    RecordServices recordServices;

    SearchServices searchServices;

    @Mock CalculatorParameters parameters;

    @Before
    public void setUp() {
        givenBackgroundThreadsEnabled();
        calculator = spy(new StorageSpaceLinearSizeCalculator());
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
        when(parameters.get(calculator.enteredLinearSizeParam)).thenReturn(new Double(5));

        assertThat(calculator.calculate(parameters)).isEqualTo(5);

        when(parameters.get(calculator.linearSizeSumParam)).thenReturn(new Double(9001));

        assertThat(calculator.calculate(parameters)).isEqualTo(5);

        when(parameters.get(calculator.enteredLinearSizeParam)).thenReturn(null);

        assertThat(calculator.calculate(parameters)).isEqualTo(9001);

        when(parameters.get(calculator.linearSizeSumParam)).thenReturn(null);

        assertThat(calculator.calculate(parameters)).isNull();
    }

    @Test
    public void givenContainerWithLinearSizeLinkedToStorageSpaceWithoutLinearSizeEnteredThenLinearSizeIsEqualToSum()
            throws RecordServicesException {

        StorageSpace storageRecord = buildDefaultStorageSpace();
        recordServices.add(storageRecord);
        addContainersLinkedToStorageSpace(storageRecord.getId());

        getModelLayerFactory().getBatchProcessesManager().waitUntilAllFinished();
        Record record = searchServices.searchSingleResult(from(rm.storageSpace.schemaType()).where(Schemas.IDENTIFIER).isEqualTo("storageTest"));
        assertThat(rm.wrapStorageSpace(record).getLinearSizeEntered()).isNull();
        assertThat(rm.wrapStorageSpace(record).getLinearSizeSum()).isEqualTo(new Double(6));
        assertThat(rm.wrapStorageSpace(record).getLinearSize()).isEqualTo(new Double(6));
    }

    @Test
    public void givenContainerWithLinearSizeLinkedToStorageSpaceWithLinearSizeEnteredThenLinearSizeIsEqualToEnteredValue()
            throws RecordServicesException {

        StorageSpace storageRecord = buildDefaultStorageSpace().setLinearSizeEntered(2);
        recordServices.add(storageRecord);
        addContainersLinkedToStorageSpace(storageRecord.getId());

        getModelLayerFactory().getBatchProcessesManager().waitUntilAllFinished();
        Record record = searchServices.searchSingleResult(from(rm.storageSpace.schemaType()).where(Schemas.IDENTIFIER).isEqualTo("storageTest"));
        assertThat(rm.wrapStorageSpace(record).getLinearSizeEntered()).isEqualTo(new Double(2));
        assertThat(rm.wrapStorageSpace(record).getLinearSizeSum()).isEqualTo(new Double(6));
        assertThat(rm.wrapStorageSpace(record).getLinearSize()).isEqualTo(new Double(2));
    }

    @Test
    public void givenStorageSpaceWithLinearSizeEnteredWithoutLinkedContainerThenLinearSizeIsEqualToEnteredValue()
            throws RecordServicesException {

        StorageSpace storageRecord = buildDefaultStorageSpace().setLinearSizeEntered(2);
        recordServices.add(storageRecord);

        getModelLayerFactory().getBatchProcessesManager().waitUntilAllFinished();
        Record record = searchServices.searchSingleResult(from(rm.storageSpace.schemaType()).where(Schemas.IDENTIFIER).isEqualTo("storageTest"));
        assertThat(rm.wrapStorageSpace(record).getLinearSizeEntered()).isEqualTo(new Double(2));
        assertThat(rm.wrapStorageSpace(record).getLinearSizeSum()).isEqualTo(new Double(0));
        assertThat(rm.wrapStorageSpace(record).getLinearSize()).isEqualTo(new Double(2));
    }

    @Test
    public void givenStorageSpaceWithoutLinearSizeEnteredAndWithoutLinkedContainerThenLinearSizeIsEqualToZero()
            throws RecordServicesException {

        StorageSpace storageRecord = buildDefaultStorageSpace();
        recordServices.add(storageRecord);

        getModelLayerFactory().getBatchProcessesManager().waitUntilAllFinished();
        Record record = searchServices.searchSingleResult(from(rm.storageSpace.schemaType()).where(Schemas.IDENTIFIER).isEqualTo("storageTest"));
        assertThat(rm.wrapStorageSpace(record).getLinearSizeEntered()).isNull();
        assertThat(rm.wrapStorageSpace(record).getLinearSizeSum()).isEqualTo(new Double(0));
        assertThat(rm.wrapStorageSpace(record).getLinearSize()).isEqualTo(new Double(0));
    }

    public StorageSpace buildDefaultStorageSpace() {
        return rm.newStorageSpaceWithId("storageTest").setCode("TEST").setTitle("storageTest");
    }

    public void addContainersLinkedToStorageSpace(String storageID)
            throws RecordServicesException {

        recordServices.add(rm.newContainerRecord().setTitle("title").setLinearSizeEntered(new Double(2))
                .setStorageSpace(storageID).setType(records.containerTypeId_boite22x22).setTemporaryIdentifier("containerTestTemporary1")
        );
        recordServices.add(rm.newContainerRecord().setTitle("title").setLinearSizeEntered(new Double(2))
                .setStorageSpace(storageID).setType(records.containerTypeId_boite22x22).setTemporaryIdentifier("containerTestTemporary2")
        );
        recordServices.add(rm.newContainerRecord().setTitle("title").setLinearSizeEntered(new Double(2))
                .setStorageSpace(storageID).setType(records.containerTypeId_boite22x22).setTemporaryIdentifier("containerTestTemporary3")
        );
    }
}
