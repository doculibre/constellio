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
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.sdk.tests.ConstellioTest;

public class AllAuthorizationsCalculatorTest extends ConstellioTest {

	String anAuth = "anAuth";
	String anotherAuth = "anotherAuth";
	String anInheritedAuth = "anInheritedAuth";
	String anotherInheritedAuth = "anotherInheritedAuth";
	@Mock CalculatorParameters parameters;

	List<String> auths;
	List<String> inheritedAuths;
	List<String> removedAuths;

	AllAuthorizationsCalculator calculator;

	LocalDependency<List<String>> authorizationsParam = LocalDependency.toAStringList("authorizations");
	LocalDependency<List<String>> inheritedAuthorizationsParam = LocalDependency.toAStringList("inheritedauthorizations");
	LocalDependency<List<String>> removedAuthorizationsParam = LocalDependency.toAStringList("removedauthorizations");
	LocalDependency<Boolean> detachedAuthorizationsParam = LocalDependency.toABoolean("detachedauthorizations");

	@Before
	public void setUp()
			throws Exception {
		calculator = new AllAuthorizationsCalculator();

		auths = new ArrayList<>();
		inheritedAuths = new ArrayList<>();
		removedAuths = new ArrayList<>();

		auths.add(anAuth);
		auths.add(anotherAuth);
		inheritedAuths.add(anInheritedAuth);
		inheritedAuths.add(anotherInheritedAuth);
		removedAuths.add(anotherInheritedAuth);

		when(parameters.get(authorizationsParam)).thenReturn(auths);
		when(parameters.get(inheritedAuthorizationsParam)).thenReturn(inheritedAuths);
		when(parameters.get(removedAuthorizationsParam)).thenReturn(removedAuths);
	}

	@Test
	public void givenDetachedThenOnlyAuthsReturned()
			throws Exception {
		when(parameters.get(detachedAuthorizationsParam)).thenReturn(false);

		List<String> calculatedAuths = calculator.calculate(parameters);

		assertThat(calculatedAuths).containsOnly(anAuth, anotherAuth, anInheritedAuth);
	}

	@Test
	public void givenNotDetachedThenInheritedThenAllAuthsExceptRemovedReturned()
			throws Exception {
		when(parameters.get(detachedAuthorizationsParam)).thenReturn(true);

		List<String> calculatedAuths = calculator.calculate(parameters);

		assertThat(calculatedAuths).containsOnly(anAuth, anotherAuth);
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
				.containsOnly(authorizationsParam, inheritedAuthorizationsParam, removedAuthorizationsParam,
						detachedAuthorizationsParam);
	}

	@Test
	public void whenCheckingIfMultivalueThenTrue()
			throws Exception {
		assertThat(calculator.isMultiValue()).isTrue();
	}
}
