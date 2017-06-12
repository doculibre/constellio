package com.constellio.app.modules.rm.model.calculators.folder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.CalculatorParametersValidatingDependencies;
import com.constellio.sdk.tests.ConstellioTest;

public class FolderEssentialCalculatorTest extends ConstellioTest {

	@Mock CalculatorParameters parameters;
	Boolean ruleConfidentialStatus;
	CopyRetentionRule copyRetentionRule = new CopyRetentionRule();

	@Test
	public void givenRuleHasNoEssentialStatusAndCopyRuleEssentialStatusSetToTrueThenFolderEssential()
			throws Exception {
		ruleConfidentialStatus = null;
		copyRetentionRule.setEssential(true);
		assertThat(calculate()).isEqualTo(true);

	}

	@Test
	public void givenRuleHasNoEssentialStatusAndCopyRuleEssentialStatusSetToFalseThenFolderNotEssential()
			throws Exception {
		ruleConfidentialStatus = null;
		copyRetentionRule.setEssential(false);
		assertThat(calculate()).isEqualTo(false);

	}

	@Test
	public void givenRuleHasEssentialStatusToTrueAndCopyRuleEssentialStatusToFalseThenFolderEssential()
			throws Exception {
		ruleConfidentialStatus = true;
		copyRetentionRule.setEssential(false);
		assertThat(calculate()).isEqualTo(true);

	}

	@Test
	public void givenRuleHasEssentialStatusToTrueAndCopyRuleEssentialStatusToTrueThenFolderEssential()
			throws Exception {
		ruleConfidentialStatus = true;
		copyRetentionRule.setEssential(true);
		assertThat(calculate()).isEqualTo(true);

	}

	@Test
	public void givenRuleHasEssentialStatusToFalseAndCopyRuleEssentialStatusToFalseThenFolderNotEssential()
			throws Exception {
		ruleConfidentialStatus = false;
		copyRetentionRule.setEssential(false);
		assertThat(calculate()).isEqualTo(false);

	}

	@Test
	public void givenRuleHasEssentialStatusToFalseAndCopyRuleEssentialStatusToTrueThenFolderEssential()
			throws Exception {
		ruleConfidentialStatus = false;
		copyRetentionRule.setEssential(true);
		assertThat(calculate()).isEqualTo(true);

	}

	//--------------------------------------------

	private boolean calculate() {
		FolderEssentialCalculator calculator = new FolderEssentialCalculator();

		when(parameters.get(calculator.retentionRuleEssentialParam)).thenReturn(ruleConfidentialStatus);
		when(parameters.get(calculator.mainCopyRuleParam)).thenReturn(copyRetentionRule);

		calculator.calculate(parameters);
		return calculator.calculate(new CalculatorParametersValidatingDependencies(parameters, calculator));
	}

}
