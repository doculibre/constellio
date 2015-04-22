/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
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
