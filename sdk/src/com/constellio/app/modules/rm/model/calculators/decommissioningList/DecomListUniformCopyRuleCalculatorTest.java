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
package com.constellio.app.modules.rm.model.calculators.decommissioningList;

import static com.constellio.app.modules.rm.model.CopyRetentionRule.newPrincipal;
import static com.constellio.app.modules.rm.model.CopyRetentionRule.newSecondary;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.CalculatorParametersValidatingDependencies;
import com.constellio.sdk.tests.ConstellioTest;

public class DecomListUniformCopyRuleCalculatorTest extends ConstellioTest {

	CopyRetentionRule principal, secondPrincipal, thirdPrincipal, secondary;
	List<CopyRetentionRule> equalsValues;
	List<CopyRetentionRule> differentsValues;
	List<CopyRetentionRule> emptyList;
	List<CopyRetentionRule> copyRetentionRules;
	DecomListUniformCopyRuleCalculator calculator;
	@Mock CalculatorParameters parameters;
	CopyRetentionRule copyRetentionRule;

	@Before
	public void setUp()
			throws Exception {
		calculator = spy(new DecomListUniformCopyRuleCalculator());

		secondary = newSecondary(asList("PA", "FI"), "888-0-C");
		principal = newPrincipal(asList("PA"), "888-0-D");
		secondPrincipal = newPrincipal(asList("FI"), "888-0-D");
		thirdPrincipal = newPrincipal(asList("PA", "FI"), "888-0-D");

		equalsValues = Arrays.asList(principal, principal, principal, principal);
		differentsValues = Arrays.asList(principal, secondPrincipal, thirdPrincipal, secondary);
		emptyList = new ArrayList<>();
	}

	@Test
	public void givenDifferentsValuesInListParamsWhenCalculateThenReturnNull()
			throws Exception {

		copyRetentionRules = differentsValues;

		copyRetentionRule = calculatedValue();

		assertThat(copyRetentionRule).isNull();
	}

	@Test
	public void givenEqualsValuesInListParamsWhenCalculateThenReturnNull()
			throws Exception {

		copyRetentionRules = equalsValues;

		copyRetentionRule = calculatedValue();

		assertThat(copyRetentionRule).isEqualTo(principal);
	}

	@Test
	public void givenEmptyListParamsWhenCalculateThenReturnNull()
			throws Exception {

		copyRetentionRules = emptyList;

		copyRetentionRule = calculatedValue();

		assertThat(copyRetentionRule).isNull();
	}

	// --------------------

	private CopyRetentionRule calculatedValue() {

		when(parameters.get(calculator.copyRulesParam)).thenReturn(copyRetentionRules);

		return calculator.calculate(new CalculatorParametersValidatingDependencies(parameters, calculator));
	}
}
