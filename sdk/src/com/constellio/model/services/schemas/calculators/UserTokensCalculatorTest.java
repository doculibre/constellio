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

public class UserTokensCalculatorTest extends ConstellioTest {

	String auth1 = "r_zeRole_auth1";
	String auth2 = "rw__auth2";
	String auth3 = "rwd_role1,role2_auth3";
	@Mock CalculatorParameters parameters;

	List<String> auths;

	UserTokensCalculator calculator;

	LocalDependency<List<String>> allAuthorizationsParam = LocalDependency.toARequiredStringList(User.ALL_USER_AUTHORIZATIONS);

	@Before
	public void setUp()
			throws Exception {
		calculator = new UserTokensCalculator();

		auths = new ArrayList<>();

		auths.add(auth1);
		auths.add(auth2);
		auths.add(auth3);

		when(parameters.get(allAuthorizationsParam)).thenReturn(auths);
	}

	@Test
	public void whenCalculatingThenAllTokensOk()
			throws Exception {
		List<String> calculatedAuths = calculator.calculate(parameters);

		assertThat(calculatedAuths)
				.containsOnly("r_zeRole_auth1", "r__auth2", "w__auth2", "r_role1,role2_auth3", "w_role1,role2_auth3",
						"d_role1,role2_auth3");
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
				.containsOnly(allAuthorizationsParam);
	}

	@Test
	public void whenCheckingIfMultivalueThenTrue()
			throws Exception {
		assertThat(calculator.isMultiValue()).isTrue();
	}
}
