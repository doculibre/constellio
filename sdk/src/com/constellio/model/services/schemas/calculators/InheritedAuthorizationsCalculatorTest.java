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

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.dependencies.HierarchyDependencyValue;
import com.constellio.model.entities.calculators.dependencies.SpecialDependencies;
import com.constellio.model.entities.calculators.dependencies.SpecialDependency;
import com.constellio.sdk.tests.ConstellioTest;

public class InheritedAuthorizationsCalculatorTest extends ConstellioTest {

	String anInheritedAuth = "anInheritedAuth";
	String anotherInheritedAuth = "anotherInheritedAuth";
	@Mock CalculatorParameters parameters;
	@Mock HierarchyDependencyValue dependencyValue;

	List<String> inheritedAuths;

	InheritedAuthorizationsCalculator calculator;

	SpecialDependency<HierarchyDependencyValue> inheritedAuthorizationsParam = SpecialDependencies.HIERARCHY;

	@Before
	public void setUp()
			throws Exception {
		calculator = new InheritedAuthorizationsCalculator();

		inheritedAuths = new ArrayList<>();

		inheritedAuths.add(anInheritedAuth);
		inheritedAuths.add(anotherInheritedAuth);

		when(dependencyValue.getParentAuthorizations()).thenReturn(inheritedAuths);

		when(parameters.get(inheritedAuthorizationsParam)).thenReturn(dependencyValue);
	}

	@Test
	public void whenCalculatingThenAllAuthsReturned()
			throws Exception {
		List<String> calculatedAuths = calculator.calculate(parameters);

		assertThat(calculatedAuths).containsOnly(anInheritedAuth, anotherInheritedAuth);
	}

	@Test
	public void whenGettingReturnTypeThenText()
			throws Exception {
		assertThat(calculator.getReturnType()).isEqualTo(STRING);
	}

	@Test
	public void whenGettingDependenciesThenRightValueReturned()
			throws Exception {
		assertThat((List) calculator.getDependencies())
				.containsOnly(inheritedAuthorizationsParam);
	}

	@Test
	public void whenCheckingIfMultivalueThenTrue()
			throws Exception {
		assertThat(calculator.isMultiValue()).isTrue();
	}
}
