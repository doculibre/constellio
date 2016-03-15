package com.constellio.app.modules.rm.model.calculators;

import org.joda.time.LocalDate;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.sdk.tests.ConstellioTest;

/**
 * Created by Patrick on 2016-03-15.
 */
public class FolderFinancialYearCalculatorTest extends ConstellioTest {

	@Mock CalculatorParameters parameters;

	FolderFinancialYearCalculator calculator = new FolderFinancialYearCalculator();

	LocalDate november3_2012 = new LocalDate(2012, 11, 3);
	LocalDate november4_2012 = new LocalDate(2012, 11, 4);
	LocalDate november5_2012 = new LocalDate(2012, 11, 5);

	LocalDate december13_2013 = new LocalDate(2013, 11, 13);
	LocalDate december14_2013 = new LocalDate(2013, 11, 14);

	@Test
	public void given()
			throws Exception {

		//		givenTimeIs(november4_2012);
		//
		//		when(parameters.get(calculator.transferDateParam)).thenReturn(null);
		//		when(parameters.get(calculator.depositDateParam)).thenReturn(null);
		//		when(parameters.get(calculator.destructionDateParam)).thenReturn(null);
		//
		//		assertThat(calculate()).isEqualTo(FolderStatus.ACTIVE);
	}

}