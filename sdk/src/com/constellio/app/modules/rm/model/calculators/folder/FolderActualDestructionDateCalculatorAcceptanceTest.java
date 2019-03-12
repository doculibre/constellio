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

public class FolderActualDestructionDateCalculatorAcceptanceTest extends ConstellioTest {

	private FolderActualDestructionDateCalculator calculator;

	@Mock CalculatorParameters parameters;
	private LocalDate parentDate;
	private LocalDate date;

	@Before
	public void setUp() {
		calculator = spy(new FolderActualDestructionDateCalculator());

		parentDate = new LocalDate(2018, 1, 1);
		date = new LocalDate(2019, 1, 1);
	}

	@Test
	public void givenFolderWithParentThenActualDestructionDateIsInheritedFromParent() {
		when(parameters.get(calculator.actualDestructionDateParam)).thenReturn(date);
		when(parameters.get(calculator.parentActualDestructionDateParam)).thenReturn(parentDate);
		when(parameters.get(calculator.parentFolderParam)).thenReturn(anyString());

		assertThat(calculator.calculate(parameters)).isEqualTo(parentDate);
	}

	@Test
	public void givenFolderWithoutParentThenActualDestructionDateIsDate() {
		when(parameters.get(calculator.actualDestructionDateParam)).thenReturn(date);
		when(parameters.get(calculator.parentActualDestructionDateParam)).thenReturn(parentDate);
		when(parameters.get(calculator.parentFolderParam)).thenReturn(null);

		assertThat(calculator.calculate(parameters)).isEqualTo(date);
	}

}
