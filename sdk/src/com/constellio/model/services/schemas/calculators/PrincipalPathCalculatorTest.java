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
