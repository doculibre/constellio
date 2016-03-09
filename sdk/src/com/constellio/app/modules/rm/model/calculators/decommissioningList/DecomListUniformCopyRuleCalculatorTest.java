package com.constellio.app.modules.rm.model.calculators.decommissioningList;

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
import com.constellio.app.modules.rm.model.CopyRetentionRuleBuilder;
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

	CopyRetentionRuleBuilder copyBuilder = CopyRetentionRuleBuilder.UUID();

	@Before
	public void setUp()
			throws Exception {
		calculator = spy(new DecomListUniformCopyRuleCalculator());

		secondary = copyBuilder.newSecondary(asList("PA", "FI"), "888-0-C");
		principal = copyBuilder.newPrincipal(asList("PA"), "888-0-D");
		secondPrincipal = copyBuilder.newPrincipal(asList("FI"), "888-0-D");
		thirdPrincipal = copyBuilder.newPrincipal(asList("PA", "FI"), "888-0-D");

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
