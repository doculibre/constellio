package com.constellio.model.services.schemas.calculators;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.calculators.dependencies.SpecialDependencies;
import com.constellio.model.entities.calculators.dependencies.SpecialDependency;
import com.constellio.sdk.tests.ConstellioTest;

public class PathCalculatorTest extends ConstellioTest {

	@Mock CalculatorParameters parameters;

	PathCalculator calculator;

	String path1 = "first/path";
	String path2 = "second/path";

	LocalDependency<List<String>> parentPathDependency = LocalDependency.toAStringList("parentpath");
	SpecialDependency<String> idDependency = SpecialDependencies.IDENTIFIER;

	@Before
	public void setUp()
			throws Exception {
		calculator = new PathCalculator();

		when(parameters.get(idDependency)).thenReturn("theId");
	}

	@Test
	public void givenPathsWhenCalculatingValueThenRightValueReturned()
			throws Exception {
		when(parameters.get(parentPathDependency)).thenReturn(asList(path1, path2));

		List<String> calculatedValue = calculator.calculate(parameters);

		assertThat(calculatedValue).containsOnly(path1 + "/theId", path2 + "/theId");
	}

	@Test
	public void givenNoPathsWhenCalculatingValueThenPAthWithIdIsReturned()
			throws Exception {
		List<String> calculatedValue = calculator.calculate(parameters);

		assertThat(calculatedValue).isEqualTo(asList("/theId"));
	}

	@Test
	public void whenGettingReturnTypeThenText()
			throws Exception {
		assertThat(calculator.getReturnType()).isEqualTo(STRING);
	}

	@Test
	public void whenGettingDependenciesThenRightValueReturned()
			throws Exception {
		assertThat((List) calculator.getDependencies()).containsOnly(idDependency, parentPathDependency);
	}

	@Test
	public void whenCheckingIfMultivalueThenTrue()
			throws Exception {
		assertThat(calculator.isMultiValue()).isTrue();
	}
}
