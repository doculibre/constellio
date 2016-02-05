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
