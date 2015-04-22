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
package com.constellio.model.services.schemas.calculators;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations.Mock;

import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.calculators.dependencies.ReferenceDependency;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.sdk.tests.ConstellioTest;

public class RoleCalculationTest extends ConstellioTest {

	@Mock CalculatorParameters parameters;
	RolesCalculator calculator;

	String roleUser1 = "zeRoleCode";
	String roleUser2 = "zeRoleCode1";
	String roleGroup1 = "zeRoleCode2";
	String roleGroup2 = "zeRoleCode3";

	LocalDependency<List<String>> userRolesParam;
	ReferenceDependency<List<String>> groupsParam;

	@Before
	public void setUp()
			throws Exception {
		calculator = new RolesCalculator();
		userRolesParam = LocalDependency.toARequiredStringList("userroles");
		groupsParam = ReferenceDependency.toAString("groups", "roles").whichIsRequired().whichIsMultivalue();
	}

	@Test
	public void givenRoleAndGroupRoleWhenCalculatingValueThenRightValueReturned()
			throws Exception {
		when(parameters.get(userRolesParam)).thenReturn(Arrays.asList(roleUser1, roleUser2));
		when(parameters.get(groupsParam)).thenReturn(Arrays.asList(roleGroup1, roleGroup2));

		List<String> calculatedValue = calculator.calculate(parameters);

		assertThat(calculatedValue).containsOnly(roleGroup1, roleGroup2, roleUser1, roleUser2);
	}

	@Test
	public void givenNoRoleWhenCalculatingValueThenEmptyListReturned()
			throws Exception {
		List<String> calculatedValue = calculator.calculate(parameters);

		assertThat(calculatedValue).isEmpty();
	}

	@Test
	public void whenGettingReturnTypeThenText()
			throws Exception {
		assertThat(calculator.getReturnType()).isEqualTo(MetadataValueType.STRING);
	}

	@Test
	public void whenGettingDependenciesThenRightValueReturned()
			throws Exception {
		assertThat((List) calculator.getDependencies()).containsOnly(userRolesParam, groupsParam);
	}

	@Test
	public void whenCheckingIfMultivalueThenTrue()
			throws Exception {
		assertThat(calculator.isMultiValue()).isTrue();
	}
}
