package com.constellio.app.modules.rm.model.evaluators;

import com.constellio.model.entities.calculators.evaluators.CalculatorEvaluatorParameters;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class FolderHasParentCalculatorEvaluatorAcceptanceTest extends ConstellioTest {

	private FolderHasParentCalculatorEvaluator calculatorEvaluator;

	@Mock CalculatorEvaluatorParameters parameters;

	@Before
	public void setUp() {
		calculatorEvaluator = spy(new FolderHasParentCalculatorEvaluator());
	}

	@Test
	public void givenFolderWithParentThenAutomaticallyFiledIsTrue() {
		when(parameters.get(calculatorEvaluator.parentFolderParam)).thenReturn(anyString());

		assertThat(calculatorEvaluator.isAutomaticallyFilled(parameters)).isTrue();
	}

	@Test
	public void givenFolderWithoutParentThenAutomaticallyFiledIsFalse() {
		when(parameters.get(calculatorEvaluator.parentFolderParam)).thenReturn(null);

		assertThat(calculatorEvaluator.isAutomaticallyFilled(parameters)).isFalse();
	}

}
