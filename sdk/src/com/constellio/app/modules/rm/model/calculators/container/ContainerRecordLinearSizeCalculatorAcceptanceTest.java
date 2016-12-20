package com.constellio.app.modules.rm.model.calculators.container;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.sdk.tests.ConstellioTest;
import org.joda.time.LocalDate;
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

public class ContainerRecordLinearSizeCalculatorAcceptanceTest extends ConstellioTest {

    RMTestRecords records = new RMTestRecords(zeCollection);

    ContainerRecordLinearSizeCalculator calculator;

    @Mock CalculatorParameters parameters;

    @Before
    public void setUp()
            throws Exception {
        givenBackgroundThreadsEnabled();
        calculator = spy(new ContainerRecordLinearSizeCalculator());
        prepareSystem(
                withZeCollection().withConstellioRMModule().withAllTestUsers()
                        .withRMTest(records).withFoldersAndContainersOfEveryStatus().withDocumentsDecommissioningList()
        );
    }

    @Test
    public void givenParametersThenCalculatorReturnsSameNumber() throws RecordServicesException {
        when(parameters.get(calculator.enteredLinearSizeParam)).thenReturn(new Double(5));

        assertThat(calculator.calculate(parameters)).isEqualTo(5);

        when(parameters.get(calculator.enteredLinearSizeSumParam)).thenReturn(new Double(9001));

        assertThat(calculator.calculate(parameters)).isEqualTo(5);

        when(parameters.get(calculator.enteredLinearSizeParam)).thenReturn(null);

        assertThat(calculator.calculate(parameters)).isEqualTo(9001);
    }

    @Test
    public void givenContainerWithLinearSizeEnteredThenLinearSizeIsCalculated() throws RecordServicesException {

        RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
        ContainerRecord containerRecord = rm.newContainerRecordWithId("containerTest").setType(records.containerTypeId_boite22x22).setTemporaryIdentifier("containerTestTemporary");
        RecordServices recordServices = getModelLayerFactory().newRecordServices();
        recordServices.add(containerRecord);
        recordServices.add(rm.newFolderWithId("parentFolder").setTitle("title").setLinearSize(new Double(2)).setContainer("containerTest")
                .setAdministrativeUnitEntered(records.unitId_10).setCategoryEntered(records.categoryId_X).setRetentionRuleEntered(records.ruleId_1).setMediumTypes(records.PA).setOpenDate(new LocalDate())
        );
        recordServices.add(rm.newFolder().setTitle("title").setLinearSize(new Double(2)).setContainer("containerTest")
                .setAdministrativeUnitEntered(records.unitId_10).setCategoryEntered(records.categoryId_X).setRetentionRuleEntered(records.ruleId_1).setMediumTypes(records.PA).setOpenDate(new LocalDate())
        );
        recordServices.add(rm.newFolder().setTitle("title").setLinearSize(new Double(2)).setContainer("containerTest").setParentFolder("parentFolder")
                .setAdministrativeUnitEntered(records.unitId_10).setCategoryEntered(records.categoryId_X).setRetentionRuleEntered(records.ruleId_1).setMediumTypes(records.PA).setOpenDate(new LocalDate())
        );

        SearchServices searchServices = getModelLayerFactory().newSearchServices();
        getModelLayerFactory().getBatchProcessesManager().waitUntilAllFinished();
        Record record = searchServices.searchSingleResult(from(rm.containerRecord.schemaType()).where(Schemas.IDENTIFIER).isEqualTo("containerTest"));
        assertThat(rm.wrapContainerRecord(record).getLinearSizeEntered()).isNull();
        assertThat(rm.wrapContainerRecord(record).get(ContainerRecord.LINEAR_SIZE_SUM)).isEqualTo(new Double(6));
        rm.wrapContainerRecord(record).get(ContainerRecord.LINEAR_SIZE);
        assertThat(rm.wrapContainerRecord(record).get(ContainerRecord.LINEAR_SIZE)).isEqualTo(new Double(6));
    }
}
