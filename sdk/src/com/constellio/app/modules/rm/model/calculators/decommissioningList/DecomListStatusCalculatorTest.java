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
package com.constellio.app.modules.rm.model.calculators.decommissioningList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.modules.rm.model.enums.DecomListStatus;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.CalculatorParametersValidatingDependencies;
import com.constellio.sdk.tests.ConstellioTest;

public class DecomListStatusCalculatorTest extends ConstellioTest {

	LocalDate processedDateParam;
	DecomListStatusCalculator calculator;
	@Mock CalculatorParameters parameters;
	DecomListStatus decomListStatus;

	@Before
	public void setUp()
			throws Exception {
		calculator = spy(new DecomListStatusCalculator());
	}

	@Test
	public void givenNullDateWhenCalculateThenReturnGeneratedStatus()
			throws Exception {

		processedDateParam = null;

		decomListStatus = calculatedValue();

		assertThat(decomListStatus).isEqualTo(DecomListStatus.GENERATED);
	}

	@Test
	public void givenADateWhenCalculateThenReturnProcessedStatus()
			throws Exception {

		processedDateParam = TimeProvider.getLocalDate();

		decomListStatus = calculatedValue();

		assertThat(decomListStatus).isEqualTo(DecomListStatus.PROCESSED);
	}

	// --------------------

	private DecomListStatus calculatedValue() {

		when(parameters.get(calculator.processedDateParam)).thenReturn(processedDateParam);

		return calculator.calculate(new CalculatorParametersValidatingDependencies(parameters, calculator));
	}
}
