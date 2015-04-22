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

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.CalculatorParametersValidatingDependencies;
import com.constellio.sdk.tests.ConstellioTest;

public class DecomListIsUniformTest extends ConstellioTest {

	CopyRetentionRule copyRetentionRule;
	CopyType copyType;
	String uniformRuleParam;
	String uniformCategoryParam;
	DecomListIsUniform calculator;
	@Mock CalculatorParameters parameters;
	boolean isUniform;

	@Before
	public void setUp()
			throws Exception {
		calculator = spy(new DecomListIsUniform());
	}

	@Test
	public void givenNonNullsParamsWhenCalculateThenReturnTrue()
			throws Exception {

		copyRetentionRule = new CopyRetentionRule();
		copyType = CopyType.PRINCIPAL;
		uniformRuleParam = "uniformRule";
		uniformCategoryParam = "uniformCategory";

		isUniform = calculatedValue();

		assertThat(isUniform).isTrue();
	}

	@Test
	public void givenOneNullParamWhenCalculateThenReturnFalse()
			throws Exception {

		copyRetentionRule = new CopyRetentionRule();
		copyType = CopyType.PRINCIPAL;
		uniformRuleParam = "uniformRule";
		uniformCategoryParam = null;

		isUniform = calculatedValue();

		assertThat(isUniform).isFalse();
	}

	// --------------------

	private Boolean calculatedValue() {

		when(parameters.get(calculator.uniformCopyRuleParam)).thenReturn(copyRetentionRule);
		when(parameters.get(calculator.uniformCopyTypeParam)).thenReturn(copyType);
		when(parameters.get(calculator.uniformRuleParam)).thenReturn(uniformRuleParam);
		when(parameters.get(calculator.uniformCategoryParam)).thenReturn(uniformCategoryParam);

		return calculator.calculate(new CalculatorParametersValidatingDependencies(parameters, calculator));
	}
}
