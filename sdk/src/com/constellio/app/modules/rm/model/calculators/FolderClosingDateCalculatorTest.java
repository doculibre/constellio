package com.constellio.app.modules.rm.model.calculators;

import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.CopyRetentionRuleBuilder;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.CalculatorParametersValidatingDependencies;
import com.constellio.sdk.tests.ConstellioTest;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FolderClosingDateCalculatorTest extends ConstellioTest {

	LocalDate aDay = LocalDate.now().minusWeeks(42);
	LocalDate openingDate;
	LocalDate enteredClosingDate;
	List<CopyRetentionRule> copies;
	Boolean configCalculatedClosingDate = true;

	@Mock CalculatorParameters parameters;
	FolderClosingDateCalculator2 calculator;

	int configRequiredDaysBeforeYearEnd = 0;
	int configNumberOfYearWhenFixedDelay = 0;
	int configNumberOfYearWhenVariableDelay = 0;
	boolean addAYearIfYEarEnd = true;
	String configYearEnd;

	CopyRetentionRuleBuilder copyBuilder = CopyRetentionRuleBuilder.UUID();

	@Before
	public void setUp()
			throws Exception {

		calculator = spy(new FolderClosingDateCalculator2());

	}

	@Test
	public void givenClosingDateCalculationDisabledWhenCalculatingOnFolderWithoutEnteredClosingDateThenReturnNull()
			throws Exception {
		configCalculatedClosingDate = false;

		assertThat(calculate()).isNull();

		verify(calculator, never()).calculateForCopy(any(CopyRetentionRule.class), any(CalculatorParameters.class));
	}

	@Test
	public void givenClosingDateCalculationDisabledWhenCalculatingOnFolderWithEnteredClosingDateThenReturnTheEnteredClosingDate()
			throws Exception {
		configCalculatedClosingDate = false;
		enteredClosingDate = aDay;

		assertThat(calculate()).isEqualTo(aDay);
		verify(calculator, never()).calculateForCopy(any(CopyRetentionRule.class), any(CalculatorParameters.class));
	}

	@Test
	public void givenClosingDateCalculationEnabledWhenCalculatingOnFolderWithEnteredClosingDateThenReturnTheEnteredClosingDate()
			throws Exception {
		enteredClosingDate = aDay;

		assertThat(calculate()).isEqualTo(aDay);
		verify(calculator, never()).calculateForCopy(any(CopyRetentionRule.class), any(CalculatorParameters.class));
	}

	@Test
	public void whenCalculatingFolderWithNoEnteredClosingDateThenReturnSmallestCalculatedClosingDate()
			throws Exception {

		openingDate = new LocalDate(1900, 1, 1);
		configRequiredDaysBeforeYearEnd = 30;
		configYearEnd = "03/31";

		LocalDate nov1_2015 = new LocalDate(2015, 11, 1);
		LocalDate nov1_2016 = new LocalDate(2016, 11, 2);
		LocalDate nov1_2017 = new LocalDate(2017, 11, 3);
		LocalDate nov1_2018 = new LocalDate(2018, 11, 4);

		CopyRetentionRule copy1 = copy("888-2-D");
		CopyRetentionRule copy2 = copy("10-888-C");
		CopyRetentionRule copy3 = copy("2-2-D");
		CopyRetentionRule copy4 = copy("999-2-C");
		copies = asList(copy1, copy2, copy3, copy4);

		doReturn(nov1_2018).when(calculator).calculateForCopy(eq(copy1), any(CalculatorParameters.class));
		doReturn(nov1_2016).when(calculator).calculateForCopy(eq(copy2), any(CalculatorParameters.class));
		doReturn(nov1_2015).when(calculator).calculateForCopy(eq(copy3), any(CalculatorParameters.class));
		doReturn(nov1_2017).when(calculator).calculateForCopy(eq(copy4), any(CalculatorParameters.class));

		assertThat(calculate()).isEqualTo(new LocalDate(2016, 3, 31));

	}

	@Test
	public void whenCalculatingForFixedCopyBeforeYearEndThenReturnYearDateIncrementedByActivePeriod()
			throws Exception {

		configNumberOfYearWhenFixedDelay = -1;
		configRequiredDaysBeforeYearEnd = 30;
		configYearEnd = "03/31";

		openingDate = new LocalDate(2013, 1, 1);
		assertThat(calculateCopy(copy("3-2-C"))).isEqualTo(new LocalDate(2016, 3, 31));

		openingDate = new LocalDate(2013, 1, 1);
		assertThat(calculateCopy(copy("8-2-C"))).isEqualTo(new LocalDate(2021, 3, 31));

		openingDate = new LocalDate(2015, 2, 3);
		assertThat(calculateCopy(copy("3-2-C"))).isEqualTo(new LocalDate(2018, 3, 31));

		openingDate = new LocalDate(2014, 10, 11);
		assertThat(calculateCopy(copy("3-2-C"))).isEqualTo(new LocalDate(2018, 3, 31));

	}

	@Test
	public void givenConfiguredStaticPeriodForFixedCopieswhenCalculatingForFixedCopyThenUseStaticPeriod()
			throws Exception {

		configNumberOfYearWhenVariableDelay = 666;
		configNumberOfYearWhenFixedDelay = 2;
		configRequiredDaysBeforeYearEnd = 30;
		configYearEnd = "03/31";

		openingDate = new LocalDate(2013, 1, 1);
		assertThat(calculateCopy(copy("3-2-C"))).isEqualTo(new LocalDate(2015, 3, 31));

		openingDate = new LocalDate(2013, 1, 1);
		assertThat(calculateCopy(copy("8-2-C"))).isEqualTo(new LocalDate(2015, 3, 31));

		openingDate = new LocalDate(2015, 2, 3);
		assertThat(calculateCopy(copy("3-2-C"))).isEqualTo(new LocalDate(2017, 3, 31));

		openingDate = new LocalDate(2014, 10, 11);
		assertThat(calculateCopy(copy("3-2-C"))).isEqualTo(new LocalDate(2017, 3, 31));

		configNumberOfYearWhenFixedDelay = 0;
		openingDate = new LocalDate(2014, 10, 11);
		assertThat(calculateCopy(copy("3-2-C"))).isEqualTo(new LocalDate(2015, 3, 31));

	}

	@Test
	public void givenConfiguredStaticPeriodForVariableCopieswhenCalculatingForVariableCopyThenUseStaticPeriod()
			throws Exception {

		configNumberOfYearWhenVariableDelay = 2;
		configNumberOfYearWhenFixedDelay = 666;
		configRequiredDaysBeforeYearEnd = 30;
		configYearEnd = "03/31";

		openingDate = new LocalDate(2013, 1, 1);
		assertThat(calculateCopy(copy("888-2-C"))).isEqualTo(new LocalDate(2015, 3, 31));

		openingDate = new LocalDate(2013, 1, 1);
		assertThat(calculateCopy(copy("999-2-C"))).isEqualTo(new LocalDate(2015, 3, 31));

		openingDate = new LocalDate(2015, 2, 3);
		assertThat(calculateCopy(copy("888-2-C"))).isEqualTo(new LocalDate(2017, 3, 31));

		openingDate = new LocalDate(2014, 10, 11);
		assertThat(calculateCopy(copy("999-2-C"))).isEqualTo(new LocalDate(2017, 3, 31));

		configNumberOfYearWhenVariableDelay = 0;
		openingDate = new LocalDate(2014, 10, 11);
		assertThat(calculateCopy(copy("888-2-C"))).isEqualTo(new LocalDate(2015, 3, 31));

		configNumberOfYearWhenVariableDelay = 0;
		openingDate = new LocalDate(2014, 3, 31);
		assertThat(calculateCopy(copy("888-2-C"))).isEqualTo(new LocalDate(2015, 3, 31));

	}

	@Test
	public void whenNotAddingAYearIfDateIsYearEndThenOK()
			throws Exception {

		configNumberOfYearWhenVariableDelay = 2;
		configNumberOfYearWhenFixedDelay = 666;
		configRequiredDaysBeforeYearEnd = 30;
		addAYearIfYEarEnd = false;
		configYearEnd = "03/31";

		openingDate = new LocalDate(2013, 1, 1);
		assertThat(calculateCopy(copy("888-2-C"))).isEqualTo(new LocalDate(2015, 3, 31));

		openingDate = new LocalDate(2013, 1, 1);
		assertThat(calculateCopy(copy("999-2-C"))).isEqualTo(new LocalDate(2015, 3, 31));

		openingDate = new LocalDate(2015, 2, 3);
		assertThat(calculateCopy(copy("888-2-C"))).isEqualTo(new LocalDate(2017, 3, 31));

		openingDate = new LocalDate(2014, 10, 11);
		assertThat(calculateCopy(copy("999-2-C"))).isEqualTo(new LocalDate(2017, 3, 31));

		configNumberOfYearWhenVariableDelay = 0;
		openingDate = new LocalDate(2014, 3, 31);
		assertThat(calculateCopy(copy("888-2-C"))).isEqualTo(new LocalDate(2014, 3, 31));

	}

	@Test
	public void givenNoConfiguredStaticPeriodForVariableCopieswhenCalculatingForVariableCopyThenReturnNull()
			throws Exception {

		configNumberOfYearWhenVariableDelay = -1;
		configNumberOfYearWhenFixedDelay = 666;
		configRequiredDaysBeforeYearEnd = 30;
		configYearEnd = "03/31";

		openingDate = new LocalDate(2013, 1, 1);
		assertThat(calculateCopy(copy("888-2-C"))).isNull();

	}

	//todo when null date

	// -----------------

	private CopyRetentionRule copy(String delays) {
		return copyBuilder.newPrincipal(asList("PA", "MD"), delays);
	}

	private LocalDate calculateCopy(CopyRetentionRule copy) {
		copies = asList(copy);
		return calculate();
	}

	private LocalDate calculate() {

		when(parameters.get(calculator.openingDateParam)).thenReturn(openingDate);
		when(parameters.get(calculator.enteredClosingDateParam)).thenReturn(enteredClosingDate);
		when(parameters.get(calculator.copiesParam)).thenReturn(copies);
		when(parameters.get(calculator.configCalculatedClosingDateParam)).thenReturn(configCalculatedClosingDate);
		when(parameters.get(calculator.configRequiredDaysBeforeYearEndParam))
				.thenReturn(configRequiredDaysBeforeYearEnd);
		when(parameters.get(calculator.configNumberOfYearWhenFixedDelayParam)).thenReturn(configNumberOfYearWhenFixedDelay);
		when(parameters.get(calculator.configNumberOfYearWhenVariableDelayParam)).thenReturn(configNumberOfYearWhenVariableDelay);
		when(parameters.get(calculator.configYearEndParam)).thenReturn(configYearEnd);
		when(parameters.get(calculator.configAddYEarIfDateIsEndOfYearParam)).thenReturn(addAYearIfYEarEnd);

		return calculator.calculate(new CalculatorParametersValidatingDependencies(parameters, calculator));
	}
}
