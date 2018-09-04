package com.constellio.app.modules.rm.model.calculators.document;

import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Created by Patrick on 2016-01-06.
 */
public class DocumentArchvisticStatusCalculatorTest extends ConstellioTest {

	@Mock CalculatorParameters parameters;
	DocumentRetentionRuleCalculator calculator;
	String retentionRule = "retentionRuleId";

	@Before
	public void setUp()
			throws Exception {
		calculator = new DocumentRetentionRuleCalculator();


	}

	@Test
	public void givenParamRetentionRuleIdWhenCalculateThenReturnParam()
			throws Exception {

		when(parameters.get(calculator.documentRetentionRulesEnabledParam)).thenReturn(false);
		when(parameters.get(calculator.folderRetentionRuleParam)).thenReturn(retentionRule);

		assertThat(calculator.calculate(parameters)).isEqualTo("retentionRuleId");
	}
}