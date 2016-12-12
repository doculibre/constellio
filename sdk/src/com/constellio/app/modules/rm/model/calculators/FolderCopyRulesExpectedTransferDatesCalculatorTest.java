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
import com.constellio.app.modules.rm.model.CopyRetentionRuleBuilder;
import com.constellio.app.modules.rm.model.enums.FolderStatus;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.CalculatorParametersValidatingDependencies;
import com.constellio.model.entities.calculators.DynamicDependencyValues;
import com.constellio.sdk.tests.ConstellioTest;

public class FolderCopyRulesExpectedTransferDatesCalculatorTest extends ConstellioTest {

	@Mock DynamicDependencyValues dynamicDependencyValues;
	@Spy FolderCopyRulesExpectedTransferDatesCalculator calculator;
	@Mock CalculatorParameters params;

	LocalDate actualTransferDate, decommissioningDate;
	int configNumberOfYearWhenVariableDelay = 666;

	List<CopyRetentionRule> applicableCopyRules;
	int confiRequiredDaysBeforeYearEnd;
	String configYearEnd;
	FolderStatus status = null;
	boolean calculatedMetadatasBasedOnFirstTimerangePartParam = true;

	CopyRetentionRuleBuilder copyBuilder = CopyRetentionRuleBuilder.UUID();

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
	public void givenInactiveFolderWithoutActualTransferDateThenNoTransferDateCalculated()
			throws Exception {

		status = FolderStatus.INACTIVE_DEPOSITED;
		configNumberOfYearWhenVariableDelay = -1;
		decommissioningDate = new LocalDate(2012, 1, 15);

		assertThat(calculateFor(4, copy("4-6-C"))).isNull();
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

	@Test
	public void givenFixedValueOfZeroForSemiActivePeriodWhenCalculatingExpectedTransferDateThenReturnNull()
			throws Exception {

		decommissioningDate = new LocalDate(2012, 1, 15);

		assertThat(calculateFor(4, copy("888-0-C"))).isNull();
	}

	private CopyRetentionRule copy(String delays) {
		return copyBuilder.newPrincipal(asList("PA", "MD"), delays);
	}

	private LocalDate calculateFor(int index, CopyRetentionRule copy) {

		if (status == null) {
			status = actualTransferDate == null ? FolderStatus.ACTIVE : FolderStatus.SEMI_ACTIVE;
		}
		when(params.get(calculator.statusParam)).thenReturn(status);
		when(params.get(calculator.actualTransferDateParam)).thenReturn(actualTransferDate);
		when(params.get(calculator.configNumberOfYearWhenVariableDelayPeriodParam))
				.thenReturn(configNumberOfYearWhenVariableDelay);
		when(params.get(calculator.decommissioningDateParam)).thenReturn(decommissioningDate);
		when(params.get(calculator.datesAndDateTimesParam)).thenReturn(dynamicDependencyValues);
		when(params.get(calculator.calculatedMetadatasBasedOnFirstTimerangePartParam))
				.thenReturn(calculatedMetadatasBasedOnFirstTimerangePartParam);

		return calculator.calculateForCopyRule(index, copy, new CalculatorParametersValidatingDependencies(params, calculator));
	}

	private List<LocalDate> calculate() {
		FolderStatus status = actualTransferDate == null ? FolderStatus.ACTIVE : FolderStatus.SEMI_ACTIVE;
		when(params.get(calculator.statusParam)).thenReturn(status);
		when(params.get(calculator.actualTransferDateParam)).thenReturn(actualTransferDate);
		when(params.get(calculator.applicableCopyRulesParam)).thenReturn(applicableCopyRules);
		when(params.get(calculator.configNumberOfYearWhenVariableDelayPeriodParam))
				.thenReturn(configNumberOfYearWhenVariableDelay);
		when(params.get(calculator.decommissioningDateParam)).thenReturn(decommissioningDate);
		when(params.get(calculator.configYearEndParam)).thenReturn(configYearEnd);
		when(params.get(calculator.configRequiredDaysBeforeYearEndParam)).thenReturn(confiRequiredDaysBeforeYearEnd);
		when(params.get(calculator.datesAndDateTimesParam)).thenReturn(dynamicDependencyValues);
		when(params.get(calculator.calculatedMetadatasBasedOnFirstTimerangePartParam))
				.thenReturn(calculatedMetadatasBasedOnFirstTimerangePartParam);

		return calculator.calculate(new CalculatorParametersValidatingDependencies(params, calculator));
	}
}
