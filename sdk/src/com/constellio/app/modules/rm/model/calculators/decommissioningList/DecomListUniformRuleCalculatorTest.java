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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.CalculatorParametersValidatingDependencies;
import com.constellio.sdk.tests.ConstellioTest;

public class DecomListUniformRuleCalculatorTest extends ConstellioTest {

	String uniformRule;
	List<String> equalsValues;
	List<String> differentsValues;
	List<String> emptyList;
	List<String> uniformRules;
	DecomListUniformRuleCalculator calculator;
	@Mock CalculatorParameters parameters;

	@Before
	public void setUp()
			throws Exception {
		calculator = spy(new DecomListUniformRuleCalculator());

		equalsValues = Arrays.asList("value1", "value1", "value1");
		differentsValues = Arrays.asList("value1", "value2", "value1");
		emptyList = new ArrayList<>();
	}

	@Test
	public void givenDifferentsValuesInListParamsWhenCalculateThenReturnNull()
			throws Exception {

		uniformRules = differentsValues;

		uniformRule = calculatedValue();

		assertThat(uniformRule).isNull();
	}

	@Test
	public void givenEqualsValuesInListParamsWhenCalculateThenReturnNull()
			throws Exception {

		uniformRules = equalsValues;

		uniformRule = calculatedValue();

		assertThat(uniformRule).isEqualTo("value1");
	}

	@Test
	public void givenEmptyListParamsWhenCalculateThenReturnNull()
			throws Exception {

		uniformRules = emptyList;

		uniformRule = calculatedValue();

		assertThat(uniformRule).isNull();
	}

	// --------------------

	private String calculatedValue() {

		when(parameters.get(calculator.foldersRulesParam)).thenReturn(uniformRules);

		return calculator.calculate(new CalculatorParametersValidatingDependencies(parameters, calculator));
	}
}
