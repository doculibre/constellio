package com.constellio.app.modules.rm.model.calculators;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.sdk.tests.ConstellioTest;

public class CategoryIsLinkableCalculatorTest extends ConstellioTest {

	@Mock CalculatorParameters parameters;
	CategoryIsLinkableCalculator calculator = new CategoryIsLinkableCalculator();

	@Before
	public void setUp()
			throws Exception {

	}

	@Test
	public void givenTwoActiveRulesWithNoOptionalConditionsWhenCalculatingThenTrue()
			throws Exception {
		when(parameters.get(calculator.rulesDependency)).thenReturn(Arrays.asList("rule1", "rule2"));
		when(parameters.get(calculator.rulesInactiveDependency)).thenReturn(Arrays.asList(false, false));
		when(parameters.get(calculator.parentDependency)).thenReturn(null);
		when(parameters.get(calculator.configNotRoot)).thenReturn(false);
		when(parameters.get(calculator.configRulesApproved)).thenReturn(false);

		assertThat(calculator.calculate(parameters)).isTrue();
	}

	@Test
	public void givenTwoApprovedActiveRulesWithAllOptionalConditionsWhenCalculatingThenTrue()
			throws Exception {
		when(parameters.get(calculator.rulesDependency)).thenReturn(Arrays.asList("rule1", "rule2"));
		when(parameters.get(calculator.rulesInactiveDependency)).thenReturn(Arrays.asList(false, false));
		when(parameters.get(calculator.parentDependency)).thenReturn("aParent");
		when(parameters.get(calculator.configNotRoot)).thenReturn(true);
		when(parameters.get(calculator.configRulesApproved)).thenReturn(true);
		when(parameters.get(calculator.rulesApprovedDependency)).thenReturn(Arrays.asList(true, true));

		assertThat(calculator.calculate(parameters)).isTrue();
	}

	@Test
	public void givenRootCategoryWithTwoApprovedActiveRulesWithAllOptionalConditionsWhenCalculatingThenFalse()
			throws Exception {
		when(parameters.get(calculator.rulesDependency)).thenReturn(Arrays.asList("rule1", "rule2"));
		when(parameters.get(calculator.rulesInactiveDependency)).thenReturn(Arrays.asList(false, false));
		when(parameters.get(calculator.parentDependency)).thenReturn(null);
		when(parameters.get(calculator.configNotRoot)).thenReturn(true);
		when(parameters.get(calculator.configRulesApproved)).thenReturn(true);
		when(parameters.get(calculator.rulesApprovedDependency)).thenReturn(Arrays.asList(true, true));

		assertThat(calculator.calculate(parameters)).isFalse();
	}

	@Test
	public void givenTwoUnapprovedActiveRulesWithAllOptionalConditionsWhenCalculatingThenFalse()
			throws Exception {
		when(parameters.get(calculator.rulesDependency)).thenReturn(Arrays.asList("rule1", "rule2"));
		when(parameters.get(calculator.rulesInactiveDependency)).thenReturn(Arrays.asList(false, false));
		when(parameters.get(calculator.parentDependency)).thenReturn("aParent");
		when(parameters.get(calculator.configNotRoot)).thenReturn(true);
		when(parameters.get(calculator.configRulesApproved)).thenReturn(true);
		when(parameters.get(calculator.rulesApprovedDependency)).thenReturn(Arrays.asList(false, false));

		assertThat(calculator.calculate(parameters)).isFalse();
	}

	@Test
	public void givenTwoInactiveRulesWithAllOptionalConditionsWhenCalculatingThenFalse()
			throws Exception {
		when(parameters.get(calculator.rulesDependency)).thenReturn(Arrays.asList("rule1", "rule2"));
		when(parameters.get(calculator.rulesInactiveDependency)).thenReturn(Arrays.asList(true, true));
		when(parameters.get(calculator.parentDependency)).thenReturn("aParent");
		when(parameters.get(calculator.configNotRoot)).thenReturn(true);
		when(parameters.get(calculator.configRulesApproved)).thenReturn(false);

		assertThat(calculator.calculate(parameters)).isFalse();
	}
}
