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

import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.dependencies.HierarchyDependencyValue;
import com.constellio.model.entities.calculators.dependencies.SpecialDependencies;
import com.constellio.sdk.tests.ConstellioTest;

public class ParentPathCalculatorTest extends ConstellioTest {

	@Mock CalculatorParameters parameters;

	ParentPathCalculator calculator;
	@Mock HierarchyDependencyValue dependencyValue;
	@Mock Taxonomy aTaxonomy;

	String path1 = "first/path";
	String path2 = "second/path";

	@Before
	public void setUp()
			throws Exception {
		calculator = new ParentPathCalculator();

		when(aTaxonomy.getCode()).thenReturn("taxoCode");
		when(parameters.get(SpecialDependencies.HIERARCHY)).thenReturn(dependencyValue);

	}

	@Test
	public void givenPathsWhenCalculatingValueThenRightValueReturned()
			throws Exception {
		when(dependencyValue.getPaths()).thenReturn(Arrays.asList(path1, path2));

		List<String> calculatedValue = calculator.calculate(parameters);

		assertThat(calculatedValue).containsOnly(path1, path2);
	}

	@Test
	public void givenNoPathsWhenCalculatingValueThenRightValueReturned()
			throws Exception {
		when(dependencyValue.getTaxonomy()).thenReturn(aTaxonomy);

		List<String> calculatedValue = calculator.calculate(parameters);

		assertThat(calculatedValue).containsOnly("/taxoCode");
	}

	@Test
	public void givenNoPathsAndNoTaxonomyWhenCalculatingValueThenEmptyListReturned()
			throws Exception {
		List<String> calculatedValue = calculator.calculate(parameters);

		assertThat(calculatedValue).isEmpty();
	}

	@Test
	public void whenGettingReturnTypeThenText()
			throws Exception {
		assertThat(calculator.getReturnType()).isEqualTo(STRING);
	}

	@Test
	public void whenGettingDependenciesThenRightValueReturned()
			throws Exception {
		assertThat((List) calculator.getDependencies()).containsOnly(SpecialDependencies.HIERARCHY);
	}

	@Test
	public void whenCheckingIfMultivalueThenTrue()
			throws Exception {
		assertThat(calculator.isMultiValue()).isTrue();
	}
}
