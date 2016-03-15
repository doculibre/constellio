package com.constellio.app.modules.rm.model.calculators;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.joda.time.LocalDate;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.CalculatorParametersValidatingDependencies;
import com.constellio.sdk.tests.ConstellioTest;

/**
 * Created by Patrick on 2016-03-15.
 */
public class FolderCalendarYearCalculatorTest extends ConstellioTest {

	@Mock CalculatorParameters parameters;

	FolderCalendarYearCalculator calculator = new FolderCalendarYearCalculator();

	LocalDate january1_2012 = new LocalDate(2012, 1, 1);

	@Test
	public void givenAYearWhenCalculateThenFirstJanuaryOfYear()
			throws Exception {

		when(parameters.get(calculator.calendarYearParam)).thenReturn("2012");

		assertThat(calculate()).isEqualTo(january1_2012);
	}

	@Test
	public void givenAInvalidStringWhenCalculateThenNull()
			throws Exception {

		when(parameters.get(calculator.calendarYearParam)).thenReturn("asd");

		assertThat(calculate()).isNull();
	}

	private LocalDate calculate() {
		return calculator.calculate(new CalculatorParametersValidatingDependencies(parameters, calculator));
	}

}