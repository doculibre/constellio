package com.constellio.app.modules.rm.model.calculators.decommissioningList;

import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.CopyRetentionRuleBuilder;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.CalculatorParametersValidatingDependencies;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class DecomListIsUniformTest extends ConstellioTest {

	CopyRetentionRule copyRetentionRule;
	CopyType copyType;
	String uniformRuleParam;
	String uniformCategoryParam;
	DecomListIsUniform calculator;
	@Mock CalculatorParameters parameters;
	boolean isUniform;

	CopyRetentionRuleBuilder copyBuilder = CopyRetentionRuleBuilder.UUID();

	@Before
	public void setUp()
			throws Exception {
		calculator = spy(new DecomListIsUniform());
	}

	@Test
	public void givenNonNullsParamsWhenCalculateThenReturnTrue()
			throws Exception {

		copyRetentionRule = copyBuilder.newCopyRetentionRule();
		copyType = CopyType.PRINCIPAL;
		uniformRuleParam = "uniformRule";
		uniformCategoryParam = "uniformCategory";

		isUniform = calculatedValue();

		assertThat(isUniform).isTrue();
	}

	@Test
	public void givenOneNullParamWhenCalculateThenReturnFalse()
			throws Exception {

		copyRetentionRule = copyBuilder.newCopyRetentionRule();
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
