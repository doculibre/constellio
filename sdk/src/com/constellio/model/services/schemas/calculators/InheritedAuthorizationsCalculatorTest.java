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
