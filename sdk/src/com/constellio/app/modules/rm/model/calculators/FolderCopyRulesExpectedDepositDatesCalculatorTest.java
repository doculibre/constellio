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
import com.constellio.app.modules.rm.model.enums.FolderStatus;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.CalculatorParametersValidatingDependencies;
import com.constellio.sdk.tests.ConstellioTest;

public class FolderCopyRulesExpectedDepositDatesCalculatorTest extends ConstellioTest {

	@Spy FolderCopyRulesExpectedDepositDatesCalculator calculator;
	@Mock CalculatorParameters params;

	FolderStatus archivisticStatus;
	LocalDate decommissioningDate;
	List<LocalDate> copyRulesExpectedTransferDate;
	int configNumberOfYearWhenVariableDelay = 0;

	List<CopyRetentionRule> applicableCopyRules;
	int confiRequiredDaysBeforeYearEnd = 0;
	String configYearEnd;

	@Test
	public void givenMultipleApplicableCopyRulesThenCalculateDateForEachAndReturnEndOFYearDates()
			throws Exception {

		CopyRetentionRule copyRule1 = principal("3-2-D");
		CopyRetentionRule copyRule2 = principal("5-2-T");
		CopyRetentionRule copyRule3 = principal("1-0-D");
		CopyRetentionRule copyRule4 = principal("3-2-C");

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
	public void whenCalculatingOnActiveFolderWithFixedSemiActivePeriodThenReturnExpectedTransferDatePlusFixedPeriod()
			throws Exception {
		configNumberOfYearWhenVariableDelay = 666;
		archivisticStatus = FolderStatus.ACTIVE;
		decommissioningDate = new LocalDate(1995, 4, 5);
		copyRulesExpectedTransferDate = asList(new LocalDate(1995, 4, 5), new LocalDate(1997, 4, 5));

		assertThat(calculateFor(1, principal("3-5-C"))).isEqualTo(new LocalDate(2002, 4, 5));
	}

	@Test
	public void whenCalculatingOnActiveFolderWithFixedSemiActivePeriodAndDestructionDisposalThenReturnNull()
			throws Exception {
		configNumberOfYearWhenVariableDelay = 666;
		archivisticStatus = FolderStatus.ACTIVE;
		decommissioningDate = new LocalDate(1995, 4, 5);
		copyRulesExpectedTransferDate = asList(new LocalDate(1995, 4, 5), new LocalDate(1997, 4, 5));

		assertThat(calculateFor(1, principal("3-5-D"))).isNull();
	}

	@Test
	public void whenCalculatingOnActiveFolderWithFixedSemiActivePeriodAndSortDisposalThenReturnExpectedTransferDatePlusFixedPeriod()
			throws Exception {
		configNumberOfYearWhenVariableDelay = 666;
		archivisticStatus = FolderStatus.ACTIVE;
		decommissioningDate = new LocalDate(1995, 4, 5);
		copyRulesExpectedTransferDate = asList(new LocalDate(1995, 4, 5), new LocalDate(1997, 4, 5));

		assertThat(calculateFor(1, principal("3-5-T"))).isEqualTo(new LocalDate(2002, 4, 5));
	}

	@Test
	public void whenCalculatingOnSemiActiveFolderWithFixedSemiActivePeriodThenReturnDecommissioningDatePlusFixedPeriod()
			throws Exception {
		configNumberOfYearWhenVariableDelay = 666;
		archivisticStatus = FolderStatus.SEMI_ACTIVE;
		decommissioningDate = new LocalDate(1998, 4, 5);
		copyRulesExpectedTransferDate = asList(new LocalDate(1995, 4, 5), new LocalDate(1997, 4, 5));

		assertThat(calculateFor(1, principal("3-5-T"))).isEqualTo(new LocalDate(2003, 4, 5));
	}

	@Test
	public void givenNotCalculatedWhenVariablePeriodwhenCalculatingOnActiveFolderWithVariableSemiActivePeriodThenReturnNull()
			throws Exception {
		configNumberOfYearWhenVariableDelay = -1;
		archivisticStatus = FolderStatus.ACTIVE;
		decommissioningDate = new LocalDate(1994, 4, 5);
		copyRulesExpectedTransferDate = asList(new LocalDate(1995, 4, 5), new LocalDate(1997, 4, 5));

		assertThat(calculateFor(1, principal("3-888-T"))).isNull();
	}

	@Test
	public void givenNotCalculatedWhenVariablePeriodwhenCalculatingOnSemiActiveFolderWithVariableSemiActivePeriodThenReturnNull()
			throws Exception {
		configNumberOfYearWhenVariableDelay = -1;
		archivisticStatus = FolderStatus.SEMI_ACTIVE;
		decommissioningDate = new LocalDate(1998, 4, 5);
		copyRulesExpectedTransferDate = asList(new LocalDate(1995, 4, 5), new LocalDate(1997, 4, 5));

		assertThat(calculateFor(1, principal("3-888-T"))).isNull();
	}

	@Test
	public void givenCalculatedWhenVariablePeriodwhenCalculatingOnActiveFolderWithVariableSemiActivePeriodThenReturnExpectedPeriodPlusConfigValue()
			throws Exception {
		configNumberOfYearWhenVariableDelay = 7;
		archivisticStatus = FolderStatus.ACTIVE;
		decommissioningDate = new LocalDate(1994, 4, 5);
		copyRulesExpectedTransferDate = asList(new LocalDate(1995, 4, 5), new LocalDate(1997, 4, 5));

		assertThat(calculateFor(1, principal("3-888-T"))).isEqualTo(new LocalDate(2004, 4, 5));
	}

	@Test
	public void givenCalculatedWhenVariablePeriodwhenCalculatingOnSemiActiveFolderWithVariableSemiActivePeriodThenReturnActualTransferPlusConfigValue()
			throws Exception {
		configNumberOfYearWhenVariableDelay = 7;
		archivisticStatus = FolderStatus.SEMI_ACTIVE;
		decommissioningDate = new LocalDate(1998, 4, 5);
		copyRulesExpectedTransferDate = asList(new LocalDate(1995, 4, 5), new LocalDate(1997, 4, 5));

		assertThat(calculateFor(1, principal("3-888-T"))).isEqualTo(new LocalDate(2005, 4, 5));
	}

	//--------------------

	private LocalDate calculateFor(int index, CopyRetentionRule copy) {

		when(params.get(calculator.archivisticStatusParam)).thenReturn(archivisticStatus);
		when(params.get(calculator.configNumberOfYearWhenVariableDelayPeriod)).thenReturn(configNumberOfYearWhenVariableDelay);
		when(params.get(calculator.copyRulesExpectedTransferDateParam)).thenReturn(copyRulesExpectedTransferDate);
		when(params.get(calculator.decommissioningDateParam)).thenReturn(decommissioningDate);

		return calculator.calculateForCopyRule(index, copy, new CalculatorParametersValidatingDependencies(params, calculator));
	}

	private static CopyRetentionRule principal(String delays) {
		return CopyRetentionRule.newPrincipal(asList("PA", "MD"), delays);
	}

	private static CopyRetentionRule secondary(String delays) {
		return CopyRetentionRule.newSecondary(asList("PA", "MD"), delays);
	}

	private List<LocalDate> calculate() {

		when(params.get(calculator.archivisticStatusParam)).thenReturn(archivisticStatus);
		when(params.get(calculator.applicableCopyRulesParam)).thenReturn(applicableCopyRules);
		when(params.get(calculator.configNumberOfYearWhenVariableDelayPeriod)).thenReturn(configNumberOfYearWhenVariableDelay);
		when(params.get(calculator.copyRulesExpectedTransferDateParam)).thenReturn(copyRulesExpectedTransferDate);
		when(params.get(calculator.decommissioningDateParam)).thenReturn(decommissioningDate);
		when(params.get(calculator.configYearEndParam)).thenReturn(configYearEnd);
		when(params.get(calculator.configRequiredDaysBeforeYearEndParam)).thenReturn(confiRequiredDaysBeforeYearEnd);

		return calculator.calculate(new CalculatorParametersValidatingDependencies(params, calculator));
	}
}
