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
