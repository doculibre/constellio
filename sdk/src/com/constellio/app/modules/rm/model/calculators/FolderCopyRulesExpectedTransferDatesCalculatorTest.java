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

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.joda.time.LocalDate;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Spy;

import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.CalculatorParametersValidatingDependencies;
import com.constellio.sdk.tests.ConstellioTest;

public class FolderCopyRulesExpectedTransferDatesCalculatorTest extends ConstellioTest {

	@Spy FolderCopyRulesExpectedTransferDatesCalculator calculator;
	@Mock CalculatorParameters params;

	LocalDate actualTransferDate, decommissioningDate;
	int configNumberOfYearWhenVariableDelay = 666;

	List<CopyRetentionRule> applicableCopyRules;
	int confiRequiredDaysBeforeYearEnd;
	String configYearEnd;

	@Test
	public void givenMultipleApplicableCopyRulesThenCalculateDateForEachAndReturnEndOFYearDates()
			throws Exception {

		CopyRetentionRule copyRule1 = copy("3-2-D");
		CopyRetentionRule copyRule2 = copy("5-2-T");
		CopyRetentionRule copyRule3 = copy("1-0-D");
		CopyRetentionRule copyRule4 = copy("3-2-C");

		LocalDate copyRule1Result = new LocalDate(2013, 1, 1);
		LocalDate copyRule2Result = new LocalDate(2013, 3, 5);
		LocalDate copyRule3Result = new LocalDate(2011, 3, 31);
		LocalDate copyRule4Result = null;

		confiRequiredDaysBeforeYearEnd = 30;
		configYearEnd = "03/31";
		applicableCopyRules = Arrays.asList(copyRule1, copyRule2, copyRule3, copyRule4);

		doReturn(copyRule1Result).when(calculator).calculateForCopyRule(eq(0), eq(copyRule1), any(CalculatorParameters.class));
		doReturn(copyRule2Result).when(calculator).calculateForCopyRule(eq(1), eq(copyRule2), any(CalculatorParameters.class));
		doReturn(copyRule3Result).when(calculator).calculateForCopyRule(eq(2), eq(copyRule3), any(CalculatorParameters.class));
		doReturn(copyRule4Result).when(calculator).calculateForCopyRule(eq(3), eq(copyRule4), any(CalculatorParameters.class));

		assertThat(calculate())
				.containsExactly(new LocalDate(2013, 3, 31), new LocalDate(2014, 3, 31), new LocalDate(2011, 3, 31), null);
	}

	@Test
	public void whenCalculatingFolderWithNoDecommissioningDateThenReturnNull()
			throws Exception {

		assertThat(calculateFor(4, copy("5-5-C"))).isNull();
	}

	@Test
	public void whenCalculatingFolderWithActualTransferDateThenReturnNull()
			throws Exception {
		actualTransferDate = new LocalDate(2013, 1, 1);

		assertThat(calculateFor(4, copy("5-5-C"))).isNull();
	}

	@Test
	public void whenCalculatingFolderWithFixedPeriodThenIncrementByActivePeriod()
			throws Exception {

		configNumberOfYearWhenVariableDelay = 666;
		decommissioningDate = new LocalDate(2012, 1, 15);

		assertThat(calculateFor(4, copy("3-5-C"))).isEqualTo(new LocalDate(2015, 1, 15));
	}

	@Test
	public void givenNoCalculatioOfVariablePeriodWhenCalculatingFolderWithFixedPeriodThenIncrementByActivePeriod()
			throws Exception {

		configNumberOfYearWhenVariableDelay = -1;
		decommissioningDate = new LocalDate(2012, 1, 15);

		assertThat(calculateFor(4, copy("4-6-C"))).isEqualTo(new LocalDate(2016, 1, 15));
	}

	@Test
	public void givenNoCalculatioOfVariablePeriodWhenCalculatingFolderWithVariablePeriodThenReturnNull()
			throws Exception {

		configNumberOfYearWhenVariableDelay = -1;
		decommissioningDate = new LocalDate(2012, 1, 15);

		assertThat(calculateFor(4, copy("888-5-C"))).isNull();
	}

	@Test
	public void whenCalculatingFolderWithVariablePeriodThenReturnNull()
			throws Exception {

		configNumberOfYearWhenVariableDelay = 2;
		decommissioningDate = new LocalDate(2012, 1, 15);

		assertThat(calculateFor(4, copy("999-5-C"))).isEqualTo(new LocalDate(2014, 1, 15));
	}

	@Test
	public void givenFixedYearOfZeroOnVariablePeriodsWhenCalculatingFolderWithVariablePeriodThenReturnNull()
			throws Exception {

		configNumberOfYearWhenVariableDelay = 0;
		decommissioningDate = new LocalDate(2012, 1, 15);

		assertThat(calculateFor(4, copy("888-5-C"))).isEqualTo(new LocalDate(2012, 1, 15));
	}

	private static CopyRetentionRule copy(String delays) {
		return CopyRetentionRule.newPrincipal(asList("PA", "MD"), delays);
	}

	private LocalDate calculateFor(int index, CopyRetentionRule copy) {

		when(params.get(calculator.actualTransferDateParam)).thenReturn(actualTransferDate);
		when(params.get(calculator.configNumberOfYearWhenVariableDelayPeriod)).thenReturn(configNumberOfYearWhenVariableDelay);
		when(params.get(calculator.decommissioningDateParam)).thenReturn(decommissioningDate);

		return calculator.calculateForCopyRule(index, copy, new CalculatorParametersValidatingDependencies(params, calculator));
	}

	private List<LocalDate> calculate() {

		when(params.get(calculator.actualTransferDateParam)).thenReturn(actualTransferDate);
		when(params.get(calculator.applicableCopyRulesParam)).thenReturn(applicableCopyRules);
		when(params.get(calculator.configNumberOfYearWhenVariableDelayPeriod)).thenReturn(configNumberOfYearWhenVariableDelay);
		when(params.get(calculator.decommissioningDateParam)).thenReturn(decommissioningDate);
		when(params.get(calculator.configYearEndParam)).thenReturn(configYearEnd);
		when(params.get(calculator.configRequiredDaysBeforeYearEndParam)).thenReturn(confiRequiredDaysBeforeYearEnd);

		return calculator.calculate(new CalculatorParametersValidatingDependencies(params, calculator));
	}
}
