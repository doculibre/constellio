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

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.CalculatorParametersValidatingDependencies;
import com.constellio.sdk.tests.ConstellioTest;

public class FolderMediumTypesCalculatorTest extends ConstellioTest {

	List<String> PA_DM_MV_PA = asList("PA", "DM", "MV", "PA");
	List<String> PA_DM_MV = asList("PA", "DM", "MV");
	FoldersMediumTypesCalculator calculator;
	@Mock CalculatorParameters parameters;
	List<String> mediumTypes;

	@Before
	public void setUp()
			throws Exception {
		calculator = spy(new FoldersMediumTypesCalculator());
	}

	@Test
	public void givenListWithRepeatedValuesWhenCalculateThenReturnListWithoutRepeatedValues()
			throws Exception {

		mediumTypes = PA_DM_MV_PA;
		List<String> newMediumTypesList = calculatedValue();

		assertThat(newMediumTypesList).hasSize(3);
		assertThat(newMediumTypesList).containsOnly("PA", "DM", "MV");
	}

	@Test
	public void givenListWithoutRepeatedValuesWhenCalculateThenReturnTheList()
			throws Exception {

		mediumTypes = PA_DM_MV;

		List<String> newMediumTypesList = calculatedValue();

		assertThat(newMediumTypesList).hasSize(3);
		assertThat(newMediumTypesList).containsOnly("PA", "DM", "MV");
	}

	@Test
	public void givenEmptyListWhenCalculateThenReturnEmptyList()
			throws Exception {

		mediumTypes = new ArrayList<>();

		List<String> newMediumTypesList = calculatedValue();

		assertThat(newMediumTypesList).isEmpty();
	}

	// --------------------

	private List<String> calculatedValue() {

		when(parameters.get(calculator.mediumTypesParam)).thenReturn(mediumTypes);

		return calculator.calculate(new CalculatorParametersValidatingDependencies(parameters, calculator));
	}
}
