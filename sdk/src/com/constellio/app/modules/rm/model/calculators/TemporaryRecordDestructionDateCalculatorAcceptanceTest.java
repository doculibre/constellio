package com.constellio.app.modules.rm.model.calculators;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.sdk.tests.ConstellioTest;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class TemporaryRecordDestructionDateCalculatorAcceptanceTest extends ConstellioTest {
	RMTestRecords records = new RMTestRecords(zeCollection);

	TemporaryRecordDestructionDateCalculator calculator;

	RMSchemasRecordsServices rm;

	RecordServices recordServices;

	SearchServices searchServices;

	@Mock
	CalculatorParameters parameters;

	@Before
	public void setUp() {
		givenBackgroundThreadsEnabled();
		calculator = spy(new TemporaryRecordDestructionDateCalculator());
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
		when(parameters.get(calculator.numberOfDaysParams)).thenReturn(5D);
		when(parameters.get(calculator.creationDate)).thenReturn(new LocalDateTime());
		assertThat(calculator.calculate(parameters).toString("dd/MM/yyyy")).isEqualTo(new LocalDate().plusDays(5).toString("dd/MM/yyyy"));
	}
}
