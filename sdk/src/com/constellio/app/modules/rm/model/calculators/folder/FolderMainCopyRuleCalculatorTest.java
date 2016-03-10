package com.constellio.app.modules.rm.model.calculators.folder;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.joda.time.LocalDate;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.CopyRetentionRuleBuilder;
import com.constellio.app.modules.rm.model.CopyRetentionRuleBuilderWithDefinedIds;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.CalculatorParametersValidatingDependencies;
import com.constellio.sdk.tests.ConstellioTest;

public class FolderMainCopyRuleCalculatorTest extends ConstellioTest {

	@Mock CalculatorParameters parameters;

	List<CopyRetentionRule> copyRules;
	List<LocalDate> expectedDepositDates;
	List<LocalDate> expectedDestructionDates;

	CopyRetentionRuleBuilder copyBuilder = new CopyRetentionRuleBuilderWithDefinedIds();

	@Test
	public void givenOnlyOneApplicableCopyRuleAndNoInactiveDatesThenReturnThatCopyRule()
			throws Exception {

		copyRules = asList(copy("2-2-D"));
		expectedDepositDates = asList(new LocalDate[] { null });
		expectedDestructionDates = asList(new LocalDate[] { null });

		assertThat(calculate()).isEqualTo(copy("2-2-D"));
	}

	@Test
	public void givenOnlyMultipleApplicableCopyRulesThenReturnRuleWithSmallestDate()
			throws Exception {

		copyRules = asList(copy("1-2-D"), copy("2-888-D"), copy("2-3-D"), copy("2-4-D"));
		expectedDepositDates = asList(new LocalDate(2015, 1, 1), new LocalDate(2013, 1, 1), null, null);
		expectedDestructionDates = asList(new LocalDate(2015, 1, 1), null, new LocalDate(2014, 1, 1), null);

		assertThat(calculate()).isEqualTo(copy("2-888-D"));
	}

	@Test
	public void givenOnlyMultipleApplicableCopyRulesThenReturnRuleWithSmallestDate2()
			throws Exception {

		copyRules = asList(copy("1-2-D"), copy("2-888-D"), copy("2-3-D"), copy("2-4-D"));
		expectedDepositDates = asList(new LocalDate(2015, 1, 1), new LocalDate(2014, 1, 1), null, null);
		expectedDestructionDates = asList(new LocalDate(2015, 1, 1), null, new LocalDate(2013, 1, 1), null);

		assertThat(calculate()).isEqualTo(copy("2-3-D"));
	}

	@Test
	public void givenOnlyMultipleApplicableCopyRulesThenReturnRuleWithSmallestDate3()
			throws Exception {

		copyRules = asList(copy("1-2-D"), copy("2-888-D"), copy("2-3-D"), copy("2-4-D"));
		expectedDepositDates = asList(new LocalDate(2012, 1, 1), new LocalDate(2014, 1, 1), null, null);
		expectedDestructionDates = asList(new LocalDate(2012, 1, 1), null, new LocalDate(2013, 1, 1), null);

		assertThat(calculate()).isEqualTo(copy("1-2-D"));
	}

	//--------------------------------------------

	private CopyRetentionRule copy(String delays) {
		return copyBuilder.newPrincipal(asList("PA", "MD"), delays);
	}

	private CopyRetentionRule calculate() {
		FolderMainCopyRuleCalculator2 calculator = new FolderMainCopyRuleCalculator2();

		when(parameters.get(calculator.copyRulesParam)).thenReturn(copyRules);
		when(parameters.get(calculator.expectedDepositDatesParam)).thenReturn(expectedDepositDates);
		when(parameters.get(calculator.expectedDestructionDatesParam)).thenReturn(expectedDestructionDates);

		calculator.calculate(parameters);
		return calculator.calculate(new CalculatorParametersValidatingDependencies(parameters, calculator));
	}

}
