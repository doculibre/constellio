package com.constellio.app.modules.rm.model.calculators.folder;

import com.constellio.app.modules.rm.model.evaluators.FolderCanInheritFromParentCalculatorEvaluator;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.evaluators.CalculatorEvaluatorParameters;
import com.constellio.sdk.tests.ConstellioTest;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class FolderOpeningDateCalculatorAcceptanceTest extends ConstellioTest {

	@Mock private FolderCanInheritFromParentCalculatorEvaluator calculatorEvaluator;

	@InjectMocks @Spy private FolderOpeningDateCalculator calculator;

	@Mock CalculatorParameters parameters;
	private LocalDate parentDate;
	private LocalDate date;

	@Before
	public void setUp() {
		initMocks(this);

		parentDate = new LocalDate(2018, 1, 1);
		date = new LocalDate(2019, 1, 1);
	}

	@Test
	public void givenFolderWithParentThenOpeningDateIsInheritedFromParent() {
		when(parameters.get(calculator.openingDateParam)).thenReturn(date);
		when(parameters.get(calculator.parentOpeningDateParam)).thenReturn(parentDate);

		when(calculatorEvaluator.isAutomaticallyFilled(any(CalculatorEvaluatorParameters.class))).thenReturn(true);

		assertThat(calculator.calculate(parameters)).isEqualTo(parentDate);
	}

	@Test
	public void givenFolderWithoutParentThenOpeningDateIsDate() {
		when(parameters.get(calculator.openingDateParam)).thenReturn(date);
		when(parameters.get(calculator.parentOpeningDateParam)).thenReturn(parentDate);

		when(calculatorEvaluator.isAutomaticallyFilled(any(CalculatorEvaluatorParameters.class))).thenReturn(false);

		assertThat(calculator.calculate(parameters)).isEqualTo(date);
	}

}
