package com.constellio.app.modules.rm.model.calculators.document;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.sdk.tests.ConstellioTest;

/**
 * Created by Patrick on 2016-01-06.
 */
public class DocumentExpectedTransferDateCalculatorTest extends ConstellioTest {

	@Mock CalculatorParameters parameters;
	DocumentExpectedTransferDateCalculator calculator;
	LocalDate localDate = TimeProvider.getLocalDate();

	@Before
	public void setUp()
			throws Exception {
		calculator = new DocumentExpectedTransferDateCalculator();

	}

	@Test
	public void givenParamRetentionRuleIdWhenCalculateThenReturnParam()
			throws Exception {

		when(parameters.get(calculator.documentRetentionRulesEnabledParam)).thenReturn(false);
		when(parameters.get(calculator.expectedTransferDateParam)).thenReturn(localDate);
		when(parameters.get(calculator.numberOfYearWhenSemiActiveVariableDelayParam)).thenReturn(1);
		when(parameters.get(calculator.requiredDaysBeforeYearEndParam)).thenReturn(90);

		assertThat(calculator.calculate(parameters)).isEqualTo(localDate);
	}
}