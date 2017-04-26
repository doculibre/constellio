package com.constellio.app.modules.rm.model.calculators.container;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.model.calculators.ContainerTitleCalculator;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Created by Constellio on 2016-12-19.
 */

public class ContainerRecordTitleCalculatorAcceptanceTest extends ConstellioTest {

    RMTestRecords records = new RMTestRecords(zeCollection);

    ContainerTitleCalculator calculator;

    RMSchemasRecordsServices rm;

    RecordServices recordServices;

    SearchServices searchServices;

    @Mock
    CalculatorParameters parameters;

    @Before
    public void setUp() {
        givenBackgroundThreadsEnabled();
        calculator = spy(new ContainerTitleCalculator());
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
        when(parameters.get(calculator.identifierParam)).thenReturn("1");
        when(parameters.get(calculator.temporaryIdentifierParam)).thenReturn("temporary");
        assertThat(calculator.calculate(parameters)).isEqualTo("1");

        when(parameters.get(calculator.identifierParam)).thenReturn("testId");
        when(parameters.get(calculator.temporaryIdentifierParam)).thenReturn("temporary");
        assertThat(calculator.calculate(parameters)).isEqualTo("testId");

        when(parameters.get(calculator.identifierParam)).thenReturn("testId");
        when(parameters.get(calculator.temporaryIdentifierParam)).thenReturn(null);
        assertThat(calculator.calculate(parameters)).isEqualTo("testId");

        when(parameters.get(calculator.identifierParam)).thenReturn(null);
        when(parameters.get(calculator.temporaryIdentifierParam)).thenReturn("temporary");
        assertThat(calculator.calculate(parameters)).isEqualTo("temporary");

        when(parameters.get(calculator.identifierParam)).thenReturn(null);
        when(parameters.get(calculator.temporaryIdentifierParam)).thenReturn(null);
        assertThat(calculator.calculate(parameters)).isEqualTo(null);
    }

    @Test
    public void givenNewContainerThenTitleIsCalculatedCorrectly()
            throws RecordServicesException {

        ContainerRecord containerRecord = buildDefaultContainer().setIdentifier("testId").setTemporaryIdentifier("temporary");
        recordServices.add(containerRecord);
        assertThat(containerRecord.getTitle()).isEqualTo("testId");

        containerRecord = buildDefaultContainer().setIdentifier("testId").setTemporaryIdentifier(null);
        recordServices.add(containerRecord);
        assertThat(containerRecord.getTitle()).isEqualTo("testId");

        containerRecord = buildDefaultContainer().setIdentifier(null).setTemporaryIdentifier("temporary");
        recordServices.add(containerRecord);
        assertThat(containerRecord.getTitle()).isEqualTo("temporary");

        try {
            containerRecord = buildDefaultContainer().setIdentifier(null).setTemporaryIdentifier(null);
            recordServices.add(containerRecord);
            assertThat(containerRecord.getTitle()).isEqualTo("1");
            fail("No exception thrown");
        } catch (Exception e) {

        }
    }

    public ContainerRecord buildDefaultContainer() {
        return rm.newContainerRecord().setType(records.containerTypeId_boite22x22);
    }
}
