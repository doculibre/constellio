package com.constellio.app.modules.rm.model.calculators;

import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.CopyRetentionRuleBuilder;
import com.constellio.app.modules.rm.model.calculators.AbstractFolderCopyRulesExpectedDatesCalculator.AbstractFolderCopyRulesExpectedDatesCalculator_CalculatorInput;
import com.constellio.app.modules.rm.model.enums.FolderStatus;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.CalculatorParametersValidatingDependencies;
import com.constellio.model.entities.calculators.DynamicDependencyValues;
import com.constellio.model.entities.calculators.dependencies.DynamicLocalDependency;
import com.constellio.sdk.tests.ConstellioTest;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Spy;
import org.restlet.engine.util.StringUtils;

import java.util.Arrays;
import java.util.List;

import static com.constellio.app.modules.rm.model.enums.DecommissioningDateBasedOn.CLOSE_DATE;
import static com.constellio.app.modules.rm.model.enums.DecommissioningDateBasedOn.OPEN_DATE;
import static com.constellio.app.modules.rm.model.enums.FolderStatus.ACTIVE;
import static com.constellio.app.modules.rm.model.enums.FolderStatus.INACTIVE_DEPOSITED;
import static com.constellio.app.modules.rm.model.enums.FolderStatus.INACTIVE_DESTROYED;
import static com.constellio.app.modules.rm.model.enums.FolderStatus.SEMI_ACTIVE;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

public class FolderCopyRulesExpectedDepositDatesCalculatorTest extends ConstellioTest {

	@Mock DynamicDependencyValues dynamicDependencyValues;
	@Spy FolderCopyRulesExpectedDepositDatesCalculator2 calculator;
	@Mock CalculatorParameters params;

	FolderStatus archivisticStatus;
	LocalDate decommissioningDate;
	List<LocalDate> copyRulesExpectedTransferDate;
	int configSemiActiveNumberOfYearWhenVariableDelay = 0;
	int configInactiveNumberOfYearWhenVariableDelay = 0;

	List<CopyRetentionRule> applicableCopyRules;
	int confiRequiredDaysBeforeYearEnd = 0;
	boolean calculatedMetadatasBasedOnFirstTimerangePartParam = true;
	String configYearEnd;

	CopyRetentionRuleBuilder copyBuilder = CopyRetentionRuleBuilder.UUID();

	LocalDate november3_2012 = new LocalDate(2012, 11, 3);
	LocalDate november4_2012 = new LocalDate(2012, 11, 4);
	LocalDate november5_2012 = new LocalDate(2012, 11, 5);
	LocalDate november3_2013 = new LocalDate(2013, 11, 3);

	LocalDate december13_2013 = new LocalDate(2013, 11, 13);
	LocalDate december13_2014 = new LocalDate(2014, 11, 13);
	LocalDate december13_2015 = new LocalDate(2014, 11, 13);

	LocalDate may30_2013 = new LocalDate(2013, 5, 30);
	LocalDate may31_2013 = new LocalDate(2013, 5, 31);
	LocalDate may31_2014 = new LocalDate(2014, 5, 31);
	LocalDate may31_2015 = new LocalDate(2015, 5, 31);
	LocalDate april16_2014 = new LocalDate(2014, 4, 16);
	LocalDate april16_2015 = new LocalDate(2015, 4, 16);

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
		configInactiveNumberOfYearWhenVariableDelay = 666;
		archivisticStatus = FolderStatus.ACTIVE;
		decommissioningDate = new LocalDate(1995, 4, 5);
		copyRulesExpectedTransferDate = asList(new LocalDate(1995, 4, 5), new LocalDate(1997, 4, 5));

		assertThat(calculateFor(1, principal("3-5-C"))).isEqualTo(new LocalDate(2002, 4, 5));
	}

	@Test
	public void whenCalculatingOnActiveFolderWithFixedSemiActivePeriodAndDestructionDisposalThenReturnNull()
			throws Exception {
		configInactiveNumberOfYearWhenVariableDelay = 666;
		archivisticStatus = FolderStatus.ACTIVE;
		decommissioningDate = new LocalDate(1995, 4, 5);
		copyRulesExpectedTransferDate = asList(new LocalDate(1995, 4, 5), new LocalDate(1997, 4, 5));

		assertThat(calculateFor(1, principal("3-5-D"))).isNull();
	}

	@Test
	public void whenCalculatingOnActiveFolderWithFixedSemiActivePeriodAndSortDisposalThenReturnExpectedTransferDatePlusFixedPeriod()
			throws Exception {
		configInactiveNumberOfYearWhenVariableDelay = 666;
		archivisticStatus = FolderStatus.ACTIVE;
		decommissioningDate = new LocalDate(1995, 4, 5);
		copyRulesExpectedTransferDate = asList(new LocalDate(1995, 4, 5), new LocalDate(1997, 4, 5));

		assertThat(calculateFor(1, principal("3-5-T"))).isEqualTo(new LocalDate(2002, 4, 5));
	}

	@Test
	public void whenCalculatingOnSemiActiveFolderWithFixedSemiActivePeriodThenReturnDecommissioningDatePlusFixedPeriod()
			throws Exception {
		configInactiveNumberOfYearWhenVariableDelay = 666;
		archivisticStatus = FolderStatus.SEMI_ACTIVE;
		decommissioningDate = new LocalDate(1998, 4, 5);
		copyRulesExpectedTransferDate = asList(new LocalDate(1995, 4, 5), new LocalDate(1997, 4, 5));

		assertThat(calculateFor(1, principal("3-5-T"))).isEqualTo(new LocalDate(2003, 4, 5));
	}

	@Test
	public void givenNotCalculatedWhenVariablePeriodwhenCalculatingOnActiveFolderWithVariableSemiActivePeriodThenReturnNull()
			throws Exception {
		configInactiveNumberOfYearWhenVariableDelay = -1;
		archivisticStatus = FolderStatus.ACTIVE;
		decommissioningDate = new LocalDate(1994, 4, 5);
		copyRulesExpectedTransferDate = asList(new LocalDate(1995, 4, 5), new LocalDate(1997, 4, 5));

		assertThat(calculateFor(1, principal("3-888-T"))).isNull();
	}

	@Test
	public void givenNotCalculatedWhenVariablePeriodwhenCalculatingOnSemiActiveFolderWithVariableSemiActivePeriodThenReturnNull()
			throws Exception {
		configInactiveNumberOfYearWhenVariableDelay = -1;
		archivisticStatus = FolderStatus.SEMI_ACTIVE;
		decommissioningDate = new LocalDate(1998, 4, 5);
		copyRulesExpectedTransferDate = asList(new LocalDate(1995, 4, 5), new LocalDate(1997, 4, 5));

		assertThat(calculateFor(1, principal("3-888-T"))).isNull();
	}

	@Test
	public void givenCalculatedWhenVariablePeriodwhenCalculatingOnActiveFolderWithVariableSemiActivePeriodThenReturnExpectedPeriodPlusConfigValue()
			throws Exception {
		configInactiveNumberOfYearWhenVariableDelay = 7;
		archivisticStatus = FolderStatus.ACTIVE;
		decommissioningDate = new LocalDate(1994, 4, 5);
		copyRulesExpectedTransferDate = asList(new LocalDate(1995, 4, 5), new LocalDate(1997, 4, 5));

		assertThat(calculateFor(1, principal("3-888-T"))).isEqualTo(new LocalDate(2004, 4, 5));
	}

	@Test
	public void givenCalculatedWhenVariablePeriodwhenCalculatingOnSemiActiveFolderWithVariableSemiActivePeriodThenReturnActualTransferPlusConfigValue()
			throws Exception {
		configInactiveNumberOfYearWhenVariableDelay = 7;
		archivisticStatus = FolderStatus.SEMI_ACTIVE;
		decommissioningDate = new LocalDate(1998, 4, 5);
		copyRulesExpectedTransferDate = asList(new LocalDate(1995, 4, 5), new LocalDate(1997, 4, 5));

		assertThat(calculateFor(1, principal("3-888-T"))).isEqualTo(new LocalDate(2005, 4, 5));
	}

	// calculateDecommissioningDate

	@Test
	public void givenDecommissioningDateBasedOnOpenDateWhenCalculatingOnActiveFolderThenReturnOpenDateAtEndOfYear()
			throws Exception {

		when(params.get(calculator.decommissioningDateBasedOnParam)).thenReturn(OPEN_DATE);
		when(params.get(calculator.folderStatusParam)).thenReturn(ACTIVE);
		when(params.get(calculator.openingDateParam)).thenReturn(november3_2012);
		when(params.get(calculator.closingDateParam)).thenReturn(december13_2013);

		assertThat(calculateDecommissioningDate()).isEqualTo(may31_2013);

	}

	@Test
	public void givenDecommissioningDateBasedOnOpenDateWhenCalculatingOnActiveFolderWithOpenDateAtEndOfYearThenReturnSameDate()
			throws Exception {

		when(params.get(calculator.decommissioningDateBasedOnParam)).thenReturn(OPEN_DATE);
		when(params.get(calculator.folderStatusParam)).thenReturn(ACTIVE);
		when(params.get(calculator.openingDateParam)).thenReturn(may31_2013);
		when(params.get(calculator.closingDateParam)).thenReturn(december13_2013);

		assertThat(calculateDecommissioningDate()).isEqualTo(may31_2013);

	}

	@Test
	public void givenDecommissioningDateBasedOnOpenDateWithInsufficientRequiredPeriodBeforeEndOfYearWhenCalculatingOnActiveFolderThenReturnOpenDateAtEndOfNextYear()
			throws Exception {

		confiRequiredDaysBeforeYearEnd = 180;
		configYearEnd = "04/16";
		when(params.get(calculator.decommissioningDateBasedOnParam)).thenReturn(OPEN_DATE);
		when(params.get(calculator.folderStatusParam)).thenReturn(ACTIVE);
		when(params.get(calculator.openingDateParam)).thenReturn(november3_2012);
		when(params.get(calculator.closingDateParam)).thenReturn(december13_2013);

		assertThat(calculateDecommissioningDate()).isEqualTo(april16_2014);

	}

	@Test
	public void givenDecommissioningDateBasedOnCloseDateWhenCalculatingOnActiveFolderThenReturnCloseDate()
			throws Exception {

		when(params.get(calculator.decommissioningDateBasedOnParam)).thenReturn(CLOSE_DATE);
		when(params.get(calculator.folderStatusParam)).thenReturn(ACTIVE);
		when(params.get(calculator.openingDateParam)).thenReturn(november3_2012);
		when(params.get(calculator.closingDateParam)).thenReturn(may31_2013);

		assertThat(calculateDecommissioningDate()).isEqualTo(may31_2013);

	}

	@Test
	public void givenDecommissioningDateBasedOnCloseDateAndNullDateWhenCalculatingOnActiveFolderThenReturnNull()
			throws Exception {

		when(params.get(calculator.decommissioningDateBasedOnParam)).thenReturn(CLOSE_DATE);
		when(params.get(calculator.folderStatusParam)).thenReturn(ACTIVE);
		when(params.get(calculator.openingDateParam)).thenReturn(november3_2012);
		when(params.get(calculator.closingDateParam)).thenReturn(null);
		when(params.get(calculator.actualTransferDateParam)).thenReturn(null);

		assertThat(calculateDecommissioningDate()).isNull();

	}

	@Test
	public void givenDecommissioningDateBasedOnCloseDateWhenCalculatingOnActiveFolderWithCloseDateNotAtEndOfYEarWithSufficientPeriod()
			throws Exception {

		when(params.get(calculator.decommissioningDateBasedOnParam)).thenReturn(CLOSE_DATE);
		when(params.get(calculator.folderStatusParam)).thenReturn(ACTIVE);
		when(params.get(calculator.openingDateParam)).thenReturn(november3_2012);
		when(params.get(calculator.closingDateParam)).thenReturn(november3_2013);

		assertThat(calculateDecommissioningDate()).isEqualTo(may31_2014);

	}

	@Test
	public void givenDecommissioningDateBasedOnCloseDateWhenCalculatingOnActiveFolderWithCloseDateNotAtEndOfYEarWithInufficientPeriod()
			throws Exception {

		when(params.get(calculator.decommissioningDateBasedOnParam)).thenReturn(CLOSE_DATE);
		when(params.get(calculator.folderStatusParam)).thenReturn(ACTIVE);
		when(params.get(calculator.openingDateParam)).thenReturn(november3_2012);
		when(params.get(calculator.closingDateParam)).thenReturn(may30_2013);

		assertThat(calculateDecommissioningDate()).isEqualTo(may31_2014);

	}

	@Test
	public void givenDecommissioningDateBasedOnCloseDateWhenCalculatingOnSemiActiveFolderThenReturnTransferDateAtYearEnd()
			throws Exception {

		when(params.get(calculator.decommissioningDateBasedOnParam)).thenReturn(CLOSE_DATE);
		when(params.get(calculator.folderStatusParam)).thenReturn(SEMI_ACTIVE);
		when(params.get(calculator.openingDateParam)).thenReturn(november3_2012);
		when(params.get(calculator.closingDateParam)).thenReturn(november4_2012);
		when(params.get(calculator.actualTransferDateParam)).thenReturn(december13_2013);

		assertThat(calculateDecommissioningDate()).isEqualTo(may31_2014);

	}

	@Test
	public void givenDecommissioningDateBasedOnCloseDateWhenCalculatingOnSemiActiveFolderWithTransferDateAtYearThenReturnTransferDate()
			throws Exception {

		when(params.get(calculator.decommissioningDateBasedOnParam)).thenReturn(CLOSE_DATE);
		when(params.get(calculator.folderStatusParam)).thenReturn(SEMI_ACTIVE);
		when(params.get(calculator.openingDateParam)).thenReturn(november3_2012);
		when(params.get(calculator.closingDateParam)).thenReturn(november4_2012);
		when(params.get(calculator.actualTransferDateParam)).thenReturn(may31_2014);

		assertThat(calculateDecommissioningDate()).isEqualTo(may31_2014);

	}

	@Test
	public void givenDecommissioningDateBasedOnCloseDateWhenCalculatingOnActiveFolderWithTransferDateThenReturnTransferDateAtEndOfNextYear()
			throws Exception {

		when(params.get(calculator.decommissioningDateBasedOnParam)).thenReturn(CLOSE_DATE);
		when(params.get(calculator.folderStatusParam)).thenReturn(ACTIVE);
		when(params.get(calculator.openingDateParam)).thenReturn(november3_2012);
		when(params.get(calculator.closingDateParam)).thenReturn(november4_2012);
		when(params.get(calculator.actualTransferDateParam)).thenReturn(december13_2013);

		assertThat(calculateDecommissioningDate()).isEqualTo(may31_2014);

	}

	@Test
	public void givenDecommissioningDateBasedOnCloseDateWhenCalculatingOnActiveFolderWithTransferDateWithInsufficientPeriodBeforeEnfOfYEarThenReturnTransferDateAAtEndOfNextYear()
			throws Exception {

		confiRequiredDaysBeforeYearEnd = 180;
		configYearEnd = "04/16";
		when(params.get(calculator.decommissioningDateBasedOnParam)).thenReturn(CLOSE_DATE);
		when(params.get(calculator.folderStatusParam)).thenReturn(ACTIVE);
		when(params.get(calculator.openingDateParam)).thenReturn(november3_2012);
		when(params.get(calculator.closingDateParam)).thenReturn(november4_2012);
		when(params.get(calculator.actualTransferDateParam)).thenReturn(december13_2013);

		assertThat(calculateDecommissioningDate()).isEqualTo(april16_2015);

	}

	@Test
	public void givenDecommissioningDateBasedOnCloseDateWhenCalculatingOnDepositedFolderThenReturnCloseDate()
			throws Exception {

		when(params.get(calculator.decommissioningDateBasedOnParam)).thenReturn(CLOSE_DATE);
		when(params.get(calculator.folderStatusParam)).thenReturn(INACTIVE_DEPOSITED);
		when(params.get(calculator.openingDateParam)).thenReturn(november3_2012);
		when(params.get(calculator.closingDateParam)).thenReturn(november4_2012);
		when(params.get(calculator.actualTransferDateParam)).thenReturn(december13_2013);

		assertThat(calculateDecommissioningDate()).isEqualTo(may31_2013);

	}

	@Test
	public void givenDecommissioningDateBasedOnOpenDateWhenCalculatingOnDepositedFolderThenReturnOpenDate()
			throws Exception {

		when(params.get(calculator.decommissioningDateBasedOnParam)).thenReturn(OPEN_DATE);
		when(params.get(calculator.folderStatusParam)).thenReturn(INACTIVE_DEPOSITED);
		when(params.get(calculator.openingDateParam)).thenReturn(november3_2012);
		when(params.get(calculator.closingDateParam)).thenReturn(may31_2014);
		when(params.get(calculator.actualTransferDateParam)).thenReturn(december13_2014);

		assertThat(calculateDecommissioningDate()).isEqualTo(may31_2013);

	}

	@Test
	public void givenDecommissioningDateBasedOnCloseDateWhenCalculatingOnDestroyedFolderThenReturnCloseDate()
			throws Exception {

		when(params.get(calculator.decommissioningDateBasedOnParam)).thenReturn(CLOSE_DATE);
		when(params.get(calculator.folderStatusParam)).thenReturn(INACTIVE_DESTROYED);
		when(params.get(calculator.openingDateParam)).thenReturn(november3_2012);
		when(params.get(calculator.closingDateParam)).thenReturn(may31_2013);
		when(params.get(calculator.actualTransferDateParam)).thenReturn(december13_2014);

		assertThat(calculateDecommissioningDate()).isEqualTo(may31_2013);

	}

	@Test
	public void givenDecommissioningDateBasedOnOpenDateWhenCalculatingOnDestroyedFolderThenReturnOpenDate()
			throws Exception {

		when(params.get(calculator.decommissioningDateBasedOnParam)).thenReturn(OPEN_DATE);
		when(params.get(calculator.folderStatusParam)).thenReturn(INACTIVE_DESTROYED);
		when(params.get(calculator.openingDateParam)).thenReturn(november3_2012);
		when(params.get(calculator.closingDateParam)).thenReturn(may31_2014);
		when(params.get(calculator.actualTransferDateParam)).thenReturn(december13_2015);

		assertThat(calculateDecommissioningDate()).isEqualTo(may31_2013);

	}

	//--------------------

	private LocalDate calculateFor(int index, CopyRetentionRule copy) {

		when(params.get(calculator.archivisticStatusParam)).thenReturn(archivisticStatus);
		when(params.get(calculator.configInactiveNumberOfYearWhenVariableDelayPeriodParam))
				.thenReturn(configInactiveNumberOfYearWhenVariableDelay);
		when(params.get(calculator.configSemiActiveNumberOfYearWhenVariableDelayPeriodParam))
				.thenReturn(configSemiActiveNumberOfYearWhenVariableDelay);
		when(params.get(calculator.copyRulesExpectedTransferDateParam)).thenReturn(copyRulesExpectedTransferDate);
		when(params.get(calculator.calculatedMetadatasBasedOnFirstTimerangePartParam))
				.thenReturn(calculatedMetadatasBasedOnFirstTimerangePartParam);
		//when(params.get(any(DynamicLocalDependency.class))).thenReturn(dynamicDependencyValues);
		doReturn(dynamicDependencyValues).when(params).get(any(DynamicLocalDependency.class));

		doReturn(decommissioningDate).when(calculator).calculateDecommissioningDate(any(CopyRetentionRule.class),
				any(AbstractFolderCopyRulesExpectedDatesCalculator_CalculatorInput.class));

		return calculator.calculateForCopyRule(index, copy, new CalculatorParametersValidatingDependencies(params, calculator));
	}

	private CopyRetentionRule principal(String delays) {
		return copyBuilder.newPrincipal(asList("PA", "MD"), delays);
	}

	private CopyRetentionRule secondary(String delays) {
		return copyBuilder.newSecondary(asList("PA", "MD"), delays);
	}

	private List<LocalDate> calculate() {

		when(params.get(calculator.archivisticStatusParam)).thenReturn(archivisticStatus);
		when(params.get(calculator.applicableCopyRulesParam)).thenReturn(applicableCopyRules);
		when(params.get(calculator.configInactiveNumberOfYearWhenVariableDelayPeriodParam))
				.thenReturn(configInactiveNumberOfYearWhenVariableDelay);
		when(params.get(calculator.configSemiActiveNumberOfYearWhenVariableDelayPeriodParam))
				.thenReturn(configSemiActiveNumberOfYearWhenVariableDelay);
		when(params.get(calculator.copyRulesExpectedTransferDateParam)).thenReturn(copyRulesExpectedTransferDate);
		when(params.get(calculator.configYearEndParam)).thenReturn(configYearEnd);
		when(params.get(calculator.configRequiredDaysBeforeYearEndParam)).thenReturn(confiRequiredDaysBeforeYearEnd);
		when(params.get(calculator.calculatedMetadatasBasedOnFirstTimerangePartParam))
				.thenReturn(calculatedMetadatasBasedOnFirstTimerangePartParam);
		when(params.get(calculator.configAddYearIfCalculationDateIsEndOfYearParam)).thenReturn(true);
		//when(params.get(any(DynamicLocalDependency.class))).thenReturn(dynamicDependencyValues);
		doReturn(dynamicDependencyValues).when(params).get(any(DynamicLocalDependency.class));

		doReturn(decommissioningDate).when(calculator)
				.calculateDecommissioningDate(any(CopyRetentionRule.class),
						any(AbstractFolderCopyRulesExpectedDatesCalculator_CalculatorInput.class));

		return calculator.calculate(new CalculatorParametersValidatingDependencies(params, calculator));
	}

	private LocalDate calculateDecommissioningDate() {
		confiRequiredDaysBeforeYearEnd = confiRequiredDaysBeforeYearEnd != 0 ? confiRequiredDaysBeforeYearEnd : 90;
		configYearEnd = !StringUtils.isNullOrEmpty(configYearEnd) ? configYearEnd : "05/31";

		when(params.get(calculator.configRequiredDaysBeforeYearEndParam)).thenReturn(confiRequiredDaysBeforeYearEnd);
		when(params.get(calculator.configYearEndParam)).thenReturn(configYearEnd);

		when(params.get(calculator.calculatedMetadatasBasedOnFirstTimerangePartParam))
				.thenReturn(calculatedMetadatasBasedOnFirstTimerangePartParam);

		AbstractFolderCopyRulesExpectedDatesCalculator_CalculatorInput input =
				calculator.new AbstractFolderCopyRulesExpectedDatesCalculator_CalculatorInput(params);
		return calculator.calculateDecommissioningDate(principal("3-888-T"), input);
	}
}
