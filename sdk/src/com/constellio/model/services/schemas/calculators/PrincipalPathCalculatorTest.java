package com.constellio.model.services.schemas.calculators;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.calculators.dependencies.SpecialDependencies;
import com.constellio.model.entities.calculators.dependencies.SpecialDependency;
import com.constellio.sdk.tests.ConstellioTest;

public class PrincipalPathCalculatorTest extends ConstellioTest {

	@Mock CalculatorParameters parameters;

	PrincipalPathCalculator calculator;

	String path1 = "first/path";
	String path2 = "second/path2";
	String path3 = "second/path3";

	LocalDependency<List<String>> pathDependency = LocalDependency.toARequiredStringList("path");
	SpecialDependency<String> principalTaxonomyDependency = SpecialDependencies.PRINCIPAL_TAXONOMY_CODE;

	@Before
	public void setUp()
			throws Exception {
		calculator = new PrincipalPathCalculator();

		when(parameters.get(principalTaxonomyDependency)).thenReturn("first");
	}

	@Test
	public void givenPathsWhenCalculatingValueThenRightValueReturned()
			throws Exception {
		when(parameters.get(pathDependency)).thenReturn(Arrays.asList(path1, path2, path3));

		String calculatedValue = calculator.calculate(parameters);

		assertThat(calculatedValue).isEqualTo(path1);
	}

	@Test
	public void givenNoPathsWhenCalculatingValueThenNullReturned()
			throws Exception {
		String calculatedValue = calculator.calculate(parameters);

		assertThat(calculatedValue).isNull();
	}

	@Test
	public void whenGettingReturnTypeThenText()
			throws Exception {
		assertThat(calculator.getReturnType()).isEqualTo(STRING);
	}

	@Test
	public void whenGettingDependenciesThenRightValueReturned()
			throws Exception {
		assertThat((List) calculator.getDependencies()).containsOnly(principalTaxonomyDependency, pathDependency);
	}

	@Test
	public void whenCheckingIfMultivalueThenTrue()
			throws Exception {
		assertThat(calculator.isMultiValue()).isFalse();
	}
}
