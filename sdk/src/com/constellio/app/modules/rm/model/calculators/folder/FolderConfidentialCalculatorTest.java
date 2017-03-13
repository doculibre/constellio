package com.constellio.app.modules.rm.model.calculators.folder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.Mock;

import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.CalculatorParametersValidatingDependencies;
import com.constellio.sdk.tests.ConstellioTest;

public class FolderConfidentialCalculatorTest extends ConstellioTest {

	@Mock CalculatorParameters parameters;
	Boolean ruleConfidentialStatus;

	@Test
	public void givenRuleHasNoConfidentialStatusThenFolderNotConfidential()
			throws Exception {
		ruleConfidentialStatus = null;
		assertThat(calculate()).isEqualTo(false);

	}

	@Test
	public void givenRuleHasConfidentialStatusToTrueThenFolderNotConfidential()
			throws Exception {
		ruleConfidentialStatus = true;
		assertThat(calculate()).isEqualTo(true);

	}

	@Test
	public void givenRuleHasConfidentialStatusToFalseThenFolderNotConfidential()
			throws Exception {
		ruleConfidentialStatus = false;
		assertThat(calculate()).isEqualTo(false);

	}

	//--------------------------------------------

	private boolean calculate() {
		FolderConfidentialCalculator calculator = new FolderConfidentialCalculator();

		when(parameters.get(calculator.retentionRuleConfidentialParam)).thenReturn(ruleConfidentialStatus);

		calculator.calculate(parameters);
		return calculator.calculate(new CalculatorParametersValidatingDependencies(parameters, calculator));
	}

}
