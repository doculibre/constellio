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
public class DocumentActualTransferDateCalculatorTest extends ConstellioTest {

	@Mock CalculatorParameters parameters;
	DocumentActualTransferDateCalculator calculator;
	LocalDate localDate = TimeProvider.getLocalDate();

	@Before
	public void setUp()
			throws Exception {
		calculator = new DocumentActualTransferDateCalculator();


	}

	@Test
	public void givenParamRetentionRuleIdWhenCalculateThenReturnParam()
			throws Exception {

		when(parameters.get(calculator.documentRetentionRulesEnabledParam)).thenReturn(false);
		when(parameters.get(calculator.folderActualTransferDateParam)).thenReturn(localDate);

		assertThat(calculator.calculate(parameters)).isEqualTo(localDate);
	}
}