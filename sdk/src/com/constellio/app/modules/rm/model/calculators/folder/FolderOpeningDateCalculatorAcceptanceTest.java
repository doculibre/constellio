package com.constellio.app.modules.rm.model.calculators.folder;

import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.sdk.tests.ConstellioTest;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class FolderOpeningDateCalculatorAcceptanceTest extends ConstellioTest {

	private FolderOpeningDateCalculator calculator;

	@Mock CalculatorParameters parameters;
	private LocalDate parentDate;
	private LocalDate date;

	@Before
	public void setUp() {
		calculator = spy(new FolderOpeningDateCalculator());

		parentDate = new LocalDate(2018, 1, 1);
		date = new LocalDate(2019, 1, 1);
	}

	@Test
	public void givenFolderWithParentThenOpeningDateIsInheritedFromParent() {
		when(parameters.get(calculator.openingDateParam)).thenReturn(date);
		when(parameters.get(calculator.parentOpeningDateParam)).thenReturn(parentDate);
		when(parameters.get(calculator.parentFolderParam)).thenReturn(anyString());

		assertThat(calculator.calculate(parameters)).isEqualTo(parentDate);
	}

	@Test
	public void givenFolderWithoutParentThenOpeningDateIsDate() {
		when(parameters.get(calculator.openingDateParam)).thenReturn(date);
		when(parameters.get(calculator.parentOpeningDateParam)).thenReturn(parentDate);
		when(parameters.get(calculator.parentFolderParam)).thenReturn(null);

		assertThat(calculator.calculate(parameters)).isEqualTo(date);
	}

}
