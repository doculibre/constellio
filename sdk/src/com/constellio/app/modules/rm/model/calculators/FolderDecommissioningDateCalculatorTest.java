package com.constellio.app.modules.rm.model.calculators;

import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.CalculatorParametersValidatingDependencies;
import com.constellio.sdk.tests.ConstellioTest;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static com.constellio.app.modules.rm.model.enums.DecommissioningDateBasedOn.CLOSE_DATE;
import static com.constellio.app.modules.rm.model.enums.DecommissioningDateBasedOn.OPEN_DATE;
import static com.constellio.app.modules.rm.model.enums.FolderStatus.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class FolderDecommissioningDateCalculatorTest extends ConstellioTest {

	@Mock CalculatorParameters parameters;

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

	FolderDecommissioningDateCalculator2 calculator = new FolderDecommissioningDateCalculator2();

	int confiRequiredDaysBeforeYearEnd = 90;
	String configYearEnd = "05/31";

	@Before
	public void setUp()
			throws Exception {

		when(parameters.get(calculator.configRequiredDaysBeforeYearEndParam)).thenReturn(confiRequiredDaysBeforeYearEnd);
		when(parameters.get(calculator.configYearEndParam)).thenReturn(configYearEnd);
	}

	@Test
	public void givenDecommissioningDateBasedOnOpenDateWhenCalculatingOnActiveFolderThenReturnOpenDateAtEndOfYear()
			throws Exception {

		when(parameters.get(calculator.decommissioningDateBasedOnParam)).thenReturn(OPEN_DATE);
		when(parameters.get(calculator.folderStatusParam)).thenReturn(ACTIVE);
		when(parameters.get(calculator.openingDateParam)).thenReturn(november3_2012);
		when(parameters.get(calculator.closingDateParam)).thenReturn(december13_2013);

		assertThat(calculate()).isEqualTo(may31_2013);

	}

	@Test
	public void givenDecommissioningDateBasedOnOpenDateWhenCalculatingOnActiveFolderWithOpenDateAtEndOfYearThenReturnSameDate()
			throws Exception {

		when(parameters.get(calculator.decommissioningDateBasedOnParam)).thenReturn(OPEN_DATE);
		when(parameters.get(calculator.folderStatusParam)).thenReturn(ACTIVE);
		when(parameters.get(calculator.openingDateParam)).thenReturn(may31_2013);
		when(parameters.get(calculator.closingDateParam)).thenReturn(december13_2013);

		assertThat(calculate()).isEqualTo(may31_2013);

	}

	@Test
	public void givenDecommissioningDateBasedOnOpenDateWithInsufficientRequiredPeriodBeforeEndOfYearWhenCalculatingOnActiveFolderThenReturnOpenDateAtEndOfNextYear()
			throws Exception {

		when(parameters.get(calculator.configRequiredDaysBeforeYearEndParam)).thenReturn(180);
		when(parameters.get(calculator.configYearEndParam)).thenReturn("04/16");
		when(parameters.get(calculator.decommissioningDateBasedOnParam)).thenReturn(OPEN_DATE);
		when(parameters.get(calculator.folderStatusParam)).thenReturn(ACTIVE);
		when(parameters.get(calculator.openingDateParam)).thenReturn(november3_2012);
		when(parameters.get(calculator.closingDateParam)).thenReturn(december13_2013);

		assertThat(calculate()).isEqualTo(april16_2014);

	}

	@Test
	public void givenDecommissioningDateBasedOnCloseDateWhenCalculatingOnActiveFolderThenReturnCloseDate()
			throws Exception {

		when(parameters.get(calculator.decommissioningDateBasedOnParam)).thenReturn(CLOSE_DATE);
		when(parameters.get(calculator.folderStatusParam)).thenReturn(ACTIVE);
		when(parameters.get(calculator.openingDateParam)).thenReturn(november3_2012);
		when(parameters.get(calculator.closingDateParam)).thenReturn(may31_2013);

		assertThat(calculate()).isEqualTo(may31_2013);

	}

	@Test
	public void givenDecommissioningDateBasedOnCloseDateAndNullDateWhenCalculatingOnActiveFolderThenReturnNull()
			throws Exception {

		when(parameters.get(calculator.decommissioningDateBasedOnParam)).thenReturn(CLOSE_DATE);
		when(parameters.get(calculator.folderStatusParam)).thenReturn(ACTIVE);
		when(parameters.get(calculator.openingDateParam)).thenReturn(november3_2012);
		when(parameters.get(calculator.closingDateParam)).thenReturn(null);
		when(parameters.get(calculator.actualTransferDateParam)).thenReturn(null);

		assertThat(calculate()).isNull();

	}

	@Test
	public void givenDecommissioningDateBasedOnCloseDateWhenCalculatingOnActiveFolderWithCloseDateNotAtEndOfYEarWithSufficientPeriod()
			throws Exception {

		when(parameters.get(calculator.decommissioningDateBasedOnParam)).thenReturn(CLOSE_DATE);
		when(parameters.get(calculator.folderStatusParam)).thenReturn(ACTIVE);
		when(parameters.get(calculator.openingDateParam)).thenReturn(november3_2012);
		when(parameters.get(calculator.closingDateParam)).thenReturn(november3_2013);

		assertThat(calculate()).isEqualTo(may31_2014);

	}

	@Test
	public void givenDecommissioningDateBasedOnCloseDateWhenCalculatingOnActiveFolderWithCloseDateNotAtEndOfYEarWithInufficientPeriod()
			throws Exception {

		when(parameters.get(calculator.decommissioningDateBasedOnParam)).thenReturn(CLOSE_DATE);
		when(parameters.get(calculator.folderStatusParam)).thenReturn(ACTIVE);
		when(parameters.get(calculator.openingDateParam)).thenReturn(november3_2012);
		when(parameters.get(calculator.closingDateParam)).thenReturn(may30_2013);

		assertThat(calculate()).isEqualTo(may31_2014);

	}

	@Test
	public void givenDecommissioningDateBasedOnCloseDateWhenCalculatingOnSemiActiveFolderThenReturnTransferDateAtYearEnd()
			throws Exception {

		when(parameters.get(calculator.decommissioningDateBasedOnParam)).thenReturn(CLOSE_DATE);
		when(parameters.get(calculator.folderStatusParam)).thenReturn(SEMI_ACTIVE);
		when(parameters.get(calculator.openingDateParam)).thenReturn(november3_2012);
		when(parameters.get(calculator.closingDateParam)).thenReturn(november4_2012);
		when(parameters.get(calculator.actualTransferDateParam)).thenReturn(december13_2013);

		assertThat(calculate()).isEqualTo(may31_2014);

	}

	@Test
	public void givenDecommissioningDateBasedOnCloseDateWhenCalculatingOnSemiActiveFolderWithTransferDateAtYearThenReturnTransferDate()
			throws Exception {

		when(parameters.get(calculator.decommissioningDateBasedOnParam)).thenReturn(CLOSE_DATE);
		when(parameters.get(calculator.folderStatusParam)).thenReturn(SEMI_ACTIVE);
		when(parameters.get(calculator.openingDateParam)).thenReturn(november3_2012);
		when(parameters.get(calculator.closingDateParam)).thenReturn(november4_2012);
		when(parameters.get(calculator.actualTransferDateParam)).thenReturn(may31_2014);

		assertThat(calculate()).isEqualTo(may31_2014);

	}

	@Test
	public void givenDecommissioningDateBasedOnCloseDateWhenCalculatingOnActiveFolderWithTransferDateThenReturnTransferDateAtEndOfNextYear()
			throws Exception {

		when(parameters.get(calculator.decommissioningDateBasedOnParam)).thenReturn(CLOSE_DATE);
		when(parameters.get(calculator.folderStatusParam)).thenReturn(ACTIVE);
		when(parameters.get(calculator.openingDateParam)).thenReturn(november3_2012);
		when(parameters.get(calculator.closingDateParam)).thenReturn(november4_2012);
		when(parameters.get(calculator.actualTransferDateParam)).thenReturn(december13_2013);

		assertThat(calculate()).isEqualTo(may31_2014);

	}

	@Test
	public void givenDecommissioningDateBasedOnCloseDateWhenCalculatingOnActiveFolderWithTransferDateWithInsufficientPeriodBeforeEnfOfYEarThenReturnTransferDateAAtEndOfNextYear()
			throws Exception {

		when(parameters.get(calculator.configRequiredDaysBeforeYearEndParam)).thenReturn(180);
		when(parameters.get(calculator.configYearEndParam)).thenReturn("04/16");
		when(parameters.get(calculator.decommissioningDateBasedOnParam)).thenReturn(CLOSE_DATE);
		when(parameters.get(calculator.folderStatusParam)).thenReturn(ACTIVE);
		when(parameters.get(calculator.openingDateParam)).thenReturn(november3_2012);
		when(parameters.get(calculator.closingDateParam)).thenReturn(november4_2012);
		when(parameters.get(calculator.actualTransferDateParam)).thenReturn(december13_2013);

		assertThat(calculate()).isEqualTo(april16_2015);

	}

	@Test
	public void givenDecommissioningDateBasedOnCloseDateWhenCalculatingOnDepositedFolderThenReturnCloseDate()
			throws Exception {

		when(parameters.get(calculator.decommissioningDateBasedOnParam)).thenReturn(CLOSE_DATE);
		when(parameters.get(calculator.folderStatusParam)).thenReturn(INACTIVE_DEPOSITED);
		when(parameters.get(calculator.openingDateParam)).thenReturn(november3_2012);
		when(parameters.get(calculator.closingDateParam)).thenReturn(november4_2012);
		when(parameters.get(calculator.actualTransferDateParam)).thenReturn(december13_2013);

		assertThat(calculate()).isEqualTo(may31_2013);

	}

	@Test
	public void givenDecommissioningDateBasedOnOpenDateWhenCalculatingOnDepositedFolderThenReturnOpenDate()
			throws Exception {

		when(parameters.get(calculator.decommissioningDateBasedOnParam)).thenReturn(OPEN_DATE);
		when(parameters.get(calculator.folderStatusParam)).thenReturn(INACTIVE_DEPOSITED);
		when(parameters.get(calculator.openingDateParam)).thenReturn(november3_2012);
		when(parameters.get(calculator.closingDateParam)).thenReturn(may31_2014);
		when(parameters.get(calculator.actualTransferDateParam)).thenReturn(december13_2014);

		assertThat(calculate()).isEqualTo(may31_2013);

	}

	@Test
	public void givenDecommissioningDateBasedOnCloseDateWhenCalculatingOnDestroyedFolderThenReturnCloseDate()
			throws Exception {

		when(parameters.get(calculator.decommissioningDateBasedOnParam)).thenReturn(CLOSE_DATE);
		when(parameters.get(calculator.folderStatusParam)).thenReturn(INACTIVE_DESTROYED);
		when(parameters.get(calculator.openingDateParam)).thenReturn(november3_2012);
		when(parameters.get(calculator.closingDateParam)).thenReturn(may31_2013);
		when(parameters.get(calculator.actualTransferDateParam)).thenReturn(december13_2014);

		assertThat(calculate()).isEqualTo(may31_2013);

	}

	@Test
	public void givenDecommissioningDateBasedOnOpenDateWhenCalculatingOnDestroyedFolderThenReturnOpenDate()
			throws Exception {

		when(parameters.get(calculator.decommissioningDateBasedOnParam)).thenReturn(OPEN_DATE);
		when(parameters.get(calculator.folderStatusParam)).thenReturn(INACTIVE_DESTROYED);
		when(parameters.get(calculator.openingDateParam)).thenReturn(november3_2012);
		when(parameters.get(calculator.closingDateParam)).thenReturn(may31_2014);
		when(parameters.get(calculator.actualTransferDateParam)).thenReturn(december13_2015);

		assertThat(calculate()).isEqualTo(may31_2013);

	}

	private LocalDate calculate() {
		return calculator.calculate(new CalculatorParametersValidatingDependencies(parameters, calculator));
	}
}
