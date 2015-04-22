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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.joda.time.LocalDate;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.modules.rm.model.enums.FolderStatus;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.CalculatorParametersValidatingDependencies;
import com.constellio.sdk.tests.ConstellioTest;

public class FolderStatusCalculatorTest extends ConstellioTest {

	@Mock CalculatorParameters parameters;

	FolderStatusCalculator calculator = new FolderStatusCalculator();

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

		assertThat(calculate()).isEqualTo(FolderStatus.INACTIVATE_DEPOSITED);
	}

	@Test
	public void givenPastOrEqualTransferAndDepositDateThenInactiveDeposited()
			throws Exception {

		givenTimeIs(december14_2013);

		when(parameters.get(calculator.transferDateParam)).thenReturn(november3_2012);
		when(parameters.get(calculator.depositDateParam)).thenReturn(december14_2013);
		when(parameters.get(calculator.destructionDateParam)).thenReturn(null);

		assertThat(calculate()).isEqualTo(FolderStatus.INACTIVATE_DEPOSITED);
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
