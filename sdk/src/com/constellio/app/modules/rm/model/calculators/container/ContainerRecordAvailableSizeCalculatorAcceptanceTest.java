package com.constellio.app.modules.rm.model.calculators.container;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordPhysicalDeleteOptions;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.sdk.tests.ConstellioTest;

/**
 * Created by Constellio on 2016-12-19.
 */

public class ContainerRecordAvailableSizeCalculatorAcceptanceTest extends ConstellioTest {

	RMTestRecords records = new RMTestRecords(zeCollection);

	ContainerRecordAvailableSizeCalculator calculator;

	RMSchemasRecordsServices rm;

	RecordServices recordServices;

	SearchServices searchServices;

	@Mock CalculatorParameters parameters;

	@Before
	public void setUp() {
		givenBackgroundThreadsEnabled();
		calculator = spy(new ContainerRecordAvailableSizeCalculator());
		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers()
						.withRMTest(records).withFoldersAndContainersOfEveryStatus().withDocumentsDecommissioningList()
		);

		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();
		searchServices = getModelLayerFactory().newSearchServices();
	}

	@Test
	public void givenParametersThenCalculatorReturnsGoodValue() {
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
	public void givenContainerWithCapacityAndLinearSizeThenAvailableSizeIsEqualToDifference()
			throws RecordServicesException {

		ContainerRecord containerRecord = buildDefaultContainer().setCapacity(new Double(10)).setLinearSizeEntered(6.0);
		recordServices.add(containerRecord);

		getModelLayerFactory().getBatchProcessesManager().waitUntilAllFinished();
		Record record = searchServices
				.searchSingleResult(from(rm.containerRecord.schemaType()).where(Schemas.IDENTIFIER).isEqualTo("containerTest"));
		assertThat(rm.wrapContainerRecord(record).getLinearSizeEntered()).isEqualTo(6);
		assertThat(rm.wrapContainerRecord(record).getLinearSize()).isEqualTo(new Double(6));
		assertThat(rm.wrapContainerRecord(record).getCapacity()).isEqualTo(new Double(10));
		assertThat(rm.wrapContainerRecord(record).getAvailableSize()).isEqualTo(new Double(4));
	}

	@Test
	public void givenContainerWithoutCapacityThenAvailableSizeIsNull()
			throws RecordServicesException {

		ContainerRecord containerRecord = buildDefaultContainer().setLinearSizeEntered(6.0);
		recordServices.add(containerRecord);

		getModelLayerFactory().getBatchProcessesManager().waitUntilAllFinished();
		Record record = searchServices
				.searchSingleResult(from(rm.containerRecord.schemaType()).where(Schemas.IDENTIFIER).isEqualTo("containerTest"));
		assertThat(rm.wrapContainerRecord(record).getLinearSizeEntered()).isEqualTo(6);
		assertThat(rm.wrapContainerRecord(record).getLinearSize()).isEqualTo(new Double(6));
		assertThat(rm.wrapContainerRecord(record).getCapacity()).isNull();
		assertThat(rm.wrapContainerRecord(record).getAvailableSize()).isNull();
		recordServices.physicallyDeleteNoMatterTheStatus(record, User.GOD, new RecordPhysicalDeleteOptions());

		containerRecord = buildDefaultContainer();
		recordServices.add(containerRecord);

		getModelLayerFactory().getBatchProcessesManager().waitUntilAllFinished();
		record = searchServices
				.searchSingleResult(from(rm.containerRecord.schemaType()).where(Schemas.IDENTIFIER).isEqualTo("containerTest"));
		assertThat(rm.wrapContainerRecord(record).getLinearSizeEntered()).isNull();
		assertThat(rm.wrapContainerRecord(record).getLinearSize()).isEqualTo(new Double(0));
		assertThat(rm.wrapContainerRecord(record).getCapacity()).isNull();
		assertThat(rm.wrapContainerRecord(record).getAvailableSize()).isNull();
	}

	@Test
	public void givenContainerWithFullMetadataSetToTrueThenAvailableSizeIsZeroOrNull()
			throws RecordServicesException {

		ContainerRecord containerRecord = buildDefaultContainer();
		containerRecord.setFull(Boolean.TRUE);
		containerRecord.setCapacity(42);
		recordServices.add(containerRecord);

		getModelLayerFactory().getBatchProcessesManager().waitUntilAllFinished();
		Record record = searchServices
				.searchSingleResult(from(rm.containerRecord.schemaType()).where(Schemas.IDENTIFIER).isEqualTo("containerTest"));
		assertThat(rm.wrapContainerRecord(record).getLinearSizeEntered()).isNull();
		assertThat(rm.wrapContainerRecord(record).getLinearSizeSum()).isEqualTo(new Double(0));
		assertThat(rm.wrapContainerRecord(record).getLinearSize()).isEqualTo(new Double(42));
		assertThat(rm.wrapContainerRecord(record).getAvailableSize()).isEqualTo(new Double(0));

		containerRecord.setCapacity(null);
		recordServices.add(containerRecord);

		getModelLayerFactory().getBatchProcessesManager().waitUntilAllFinished();
		record = searchServices
				.searchSingleResult(from(rm.containerRecord.schemaType()).where(Schemas.IDENTIFIER).isEqualTo("containerTest"));
		assertThat(rm.wrapContainerRecord(record).getLinearSizeEntered()).isNull();
		assertThat(rm.wrapContainerRecord(record).getLinearSizeSum()).isEqualTo(new Double(0));
		assertThat(rm.wrapContainerRecord(record).getLinearSize()).isNull();
		assertThat(rm.wrapContainerRecord(record).getAvailableSize()).isNull();
	}

	public ContainerRecord buildDefaultContainer() {
		return rm.newContainerRecordWithId("containerTest").setType(records.containerTypeId_boite22x22)
				.setTemporaryIdentifier("containerTestTemporary");
	}
}
