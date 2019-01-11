package com.constellio.app.modules.rm.model.calculators.folder;

import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.CalculatorParametersValidatingDependencies;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

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
	public void givenRuleHasNoEssentialStatusAndCopyRuleEssentialStatusSetToFalseThenFolderEssentialIsNull()
			throws Exception {
		ruleConfidentialStatus = null;
		copyRetentionRule.setEssential(false);
		assertThat(calculate()).isNull();

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
	public void givenRuleHasEssentialStatusToFalseAndCopyRuleEssentialStatusToFalseThenFolderEssentialIsNull()
			throws Exception {
		ruleConfidentialStatus = false;
		copyRetentionRule.setEssential(false);
		assertThat(calculate()).isNull();

	}

	@Test
	public void givenRuleHasEssentialStatusToFalseAndCopyRuleEssentialStatusToTrueThenFolderEssential()
			throws Exception {
		ruleConfidentialStatus = false;
		copyRetentionRule.setEssential(true);
		assertThat(calculate()).isEqualTo(true);

	}

	//--------------------------------------------

	private Boolean calculate() {
		FolderEssentialCalculator calculator = new FolderEssentialCalculator();

		when(parameters.get(calculator.retentionRuleEssentialParam)).thenReturn(ruleConfidentialStatus);
		when(parameters.get(calculator.mainCopyRuleParam)).thenReturn(copyRetentionRule);

		calculator.calculate(parameters);
		return calculator.calculate(new CalculatorParametersValidatingDependencies(parameters, calculator));
	}

}
