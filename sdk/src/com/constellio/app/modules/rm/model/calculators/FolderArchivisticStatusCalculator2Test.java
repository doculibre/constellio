package com.constellio.app.modules.rm.model.calculators;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.joda.time.LocalDate;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.modules.rm.model.enums.FolderStatus;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.CalculatorParametersValidatingDependencies;
import com.constellio.sdk.tests.ConstellioTest;

public class FolderArchivisticStatusCalculator2Test extends ConstellioTest {

	@Mock CalculatorParameters parameters;

	FolderArchivisticStatusCalculator2 calculator = new FolderArchivisticStatusCalculator2();

	LocalDate november3_2012 = new LocalDate(2012, 11, 3);
	LocalDate november4_2012 = new LocalDate(2012, 11, 4);
	LocalDate november5_2012 = new LocalDate(2012, 11, 5);

	LocalDate december13_2013 = new LocalDate(2013, 11, 13);
	LocalDate december14_2013 = new LocalDate(2013, 11, 14);

	@Test
	public void givenNullTransferDestructionAndDepositDateThenActive()
			throws Exception {

		givenTimeIs(november4_2012);

		when(parameters.get(calculator.transferDateParam)).thenReturn(null);
		when(parameters.get(calculator.depositDateParam)).thenReturn(null);
		when(parameters.get(calculator.destructionDateParam)).thenReturn(null);

		assertThat(calculate()).isEqualTo(FolderStatus.ACTIVE);
	}

	//TODO Remove this test if the current date has no impact on the status @Test
	public void givenFutureTransferDateThenActive()
			throws Exception {

		givenTimeIs(november4_2012);

		when(parameters.get(calculator.transferDateParam)).thenReturn(november5_2012);
		when(parameters.get(calculator.depositDateParam)).thenReturn(null);
		when(parameters.get(calculator.destructionDateParam)).thenReturn(null);

		assertThat(calculate()).isEqualTo(FolderStatus.ACTIVE);
	}

	//TODO Remove this test if the current date has no impact on the status @Test
	public void givenFutureTransferAndDepositDateThenActive()
			throws Exception {

		givenTimeIs(november4_2012);

		when(parameters.get(calculator.transferDateParam)).thenReturn(november5_2012);
		when(parameters.get(calculator.depositDateParam)).thenReturn(december14_2013);
		when(parameters.get(calculator.destructionDateParam)).thenReturn(null);

		assertThat(calculate()).isEqualTo(FolderStatus.ACTIVE);
	}

	//TODO Remove this test if the current date has no impact on the status @Test
	public void givenFutureTransferAndDestructionDateThenActive()
			throws Exception {

		givenTimeIs(november4_2012);

		when(parameters.get(calculator.transferDateParam)).thenReturn(november5_2012);
		when(parameters.get(calculator.depositDateParam)).thenReturn(null);
		when(parameters.get(calculator.destructionDateParam)).thenReturn(december14_2013);

		assertThat(calculate()).isEqualTo(FolderStatus.ACTIVE);
	}

	@Test
	public void givenEqualTransferDateThenSemiActive()
			throws Exception {

		givenTimeIs(november4_2012);

		when(parameters.get(calculator.transferDateParam)).thenReturn(november4_2012);
		when(parameters.get(calculator.depositDateParam)).thenReturn(null);
		when(parameters.get(calculator.destructionDateParam)).thenReturn(null);

		assertThat(calculate()).isEqualTo(FolderStatus.SEMI_ACTIVE);
	}

	@Test
	public void givenPastTransferDateThenSemiActive()
			throws Exception {

		givenTimeIs(november4_2012);

		when(parameters.get(calculator.transferDateParam)).thenReturn(november3_2012);
		when(parameters.get(calculator.depositDateParam)).thenReturn(null);
		when(parameters.get(calculator.destructionDateParam)).thenReturn(null);

		assertThat(calculate()).isEqualTo(FolderStatus.SEMI_ACTIVE);
	}

	//TODO Remove this test if the current date has no impact on the status @Test
	public void givenEqualTransferDateAndFutureDepositDateThenSemiActive()
			throws Exception {

		givenTimeIs(november4_2012);

		when(parameters.get(calculator.transferDateParam)).thenReturn(november4_2012);
		when(parameters.get(calculator.depositDateParam)).thenReturn(november5_2012);
		when(parameters.get(calculator.destructionDateParam)).thenReturn(null);

		assertThat(calculate()).isEqualTo(FolderStatus.SEMI_ACTIVE);
	}

	//TODO Remove this test if the current date has no impact on the status @Test
	public void givenPastTransferDateAndFutureDepositDateThenSemiActive()
			throws Exception {

		givenTimeIs(november4_2012);

		when(parameters.get(calculator.transferDateParam)).thenReturn(november3_2012);
		when(parameters.get(calculator.depositDateParam)).thenReturn(november5_2012);
		when(parameters.get(calculator.destructionDateParam)).thenReturn(null);

		assertThat(calculate()).isEqualTo(FolderStatus.SEMI_ACTIVE);
	}

	//TODO Remove this test if the current date has no impact on the status @Test
	public void givenPastTransferDateAndFutureDestructionDateThenSemiActive()
			throws Exception {

		givenTimeIs(november4_2012);

		when(parameters.get(calculator.transferDateParam)).thenReturn(november3_2012);
		when(parameters.get(calculator.depositDateParam)).thenReturn(null);
		when(parameters.get(calculator.destructionDateParam)).thenReturn(november5_2012);

		assertThat(calculate()).isEqualTo(FolderStatus.SEMI_ACTIVE);
	}

	@Test
	public void givenPastTransferAndDepositDateThenInactiveDeposited()
			throws Exception {

		givenTimeIs(december14_2013);

		when(parameters.get(calculator.transferDateParam)).thenReturn(november3_2012);
		when(parameters.get(calculator.depositDateParam)).thenReturn(december13_2013);
		when(parameters.get(calculator.destructionDateParam)).thenReturn(null);

		assertThat(calculate()).isEqualTo(FolderStatus.INACTIVE_DEPOSITED);
	}

	@Test
	public void givenPastOrEqualTransferAndDepositDateThenInactiveDeposited()
			throws Exception {

		givenTimeIs(december14_2013);

		when(parameters.get(calculator.transferDateParam)).thenReturn(november3_2012);
		when(parameters.get(calculator.depositDateParam)).thenReturn(december14_2013);
		when(parameters.get(calculator.destructionDateParam)).thenReturn(null);

		assertThat(calculate()).isEqualTo(FolderStatus.INACTIVE_DEPOSITED);
	}

	@Test
	public void givenPastTransferAndDestroyedDateThenInactiveDeposited()
			throws Exception {

		givenTimeIs(december14_2013);

		when(parameters.get(calculator.transferDateParam)).thenReturn(november3_2012);
		when(parameters.get(calculator.depositDateParam)).thenReturn(null);
		when(parameters.get(calculator.destructionDateParam)).thenReturn(december13_2013);

		assertThat(calculate()).isEqualTo(FolderStatus.INACTIVE_DESTROYED);
	}

	@Test
	public void givenPastOrEqualTransferAndDestroyedDateThenInactiveDeposited()
			throws Exception {

		givenTimeIs(december14_2013);

		when(parameters.get(calculator.transferDateParam)).thenReturn(november3_2012);
		when(parameters.get(calculator.depositDateParam)).thenReturn(null);
		when(parameters.get(calculator.destructionDateParam)).thenReturn(december14_2013);

		assertThat(calculate()).isEqualTo(FolderStatus.INACTIVE_DESTROYED);
	}

	private FolderStatus calculate() {
		return calculator.calculate(new CalculatorParametersValidatingDependencies(parameters, calculator));
	}
}
