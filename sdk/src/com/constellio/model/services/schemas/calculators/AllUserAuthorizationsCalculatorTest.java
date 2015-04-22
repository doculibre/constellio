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
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.sdk.tests.ConstellioTest;

public class AllUserAuthorizationsCalculatorTest extends ConstellioTest {

	String anAuth = "anAuth";
	String anotherAuth = "anotherAuth";
	String aGroupAuth = "aGroupAuth";
	String anotherGroupAuth = "anotherGroupAuth";
	@Mock CalculatorParameters parameters;

	List<String> auths;
	List<String> groupsAuths;

	AllUserAuthorizationsCalculator calculator;

	LocalDependency<List<String>> authorizationsParam = LocalDependency.toARequiredStringList("authorizations");
	LocalDependency<List<String>> groupsAuthorizationsParam = LocalDependency.toARequiredStringList(User.GROUPS_AUTHORIZATIONS);

	@Before
	public void setUp()
			throws Exception {
		calculator = new AllUserAuthorizationsCalculator();

		auths = new ArrayList<>();
		groupsAuths = new ArrayList<>();

		auths.add(anAuth);
		auths.add(anotherAuth);
		groupsAuths.add(aGroupAuth);
		groupsAuths.add(anotherGroupAuth);

		when(parameters.get(authorizationsParam)).thenReturn(auths);
		when(parameters.get(groupsAuthorizationsParam)).thenReturn(groupsAuths);
	}

	@Test
	public void whenCalculatingThenAllAuthsReturned()
			throws Exception {
		List<String> calculatedAuths = calculator.calculate(parameters);

		assertThat(calculatedAuths).containsOnly(anAuth, anotherAuth, aGroupAuth, anotherGroupAuth);
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
				.containsOnly(authorizationsParam, groupsAuthorizationsParam);
	}

	@Test
	public void whenCheckingIfMultivalueThenTrue()
			throws Exception {
		assertThat(calculator.isMultiValue()).isTrue();
	}
}
