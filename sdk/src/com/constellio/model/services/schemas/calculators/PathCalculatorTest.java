package com.constellio.model.services.schemas.calculators;

import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.dependencies.HierarchyDependencyValue;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.calculators.dependencies.SpecialDependencies;
import com.constellio.model.entities.calculators.dependencies.SpecialDependency;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class PathCalculatorTest extends ConstellioTest {

	@Mock Taxonomy taxonomy;
	@Mock CalculatorParameters parameters;

	PathCalculator calculator;

	String zeSubFolder = "zeSubFolder";

	String path1 = "first/path";
	String path2 = "second/path2";
	String path3 = "second/path3";

	HierarchyDependencyValue hierarchyDependencyValue;
	SpecialDependency<HierarchyDependencyValue> taxonomiesParam = SpecialDependencies.HIERARCHY;
	LocalDependency<List<String>> pathDependency = LocalDependency.toARequiredStringList("path");
	SpecialDependency<String> idDependency = SpecialDependencies.IDENTIFIER;

	@Before
	public void setUp()
			throws Exception {
		calculator = new PathCalculator();

		when(parameters.getId()).thenReturn(zeSubFolder);
		when(parameters.get(idDependency)).thenReturn(zeSubFolder);
	}

	@Test
	public void givenNullPathsWhenCalculatingValueThenNotNullReturned()
			throws Exception {
		hierarchyDependencyValue = new HierarchyDependencyValue(
				taxonomy,
				Arrays.asList(path1, path2, path3),
				Collections.emptyList(),
				Collections.emptyList());
		when(parameters.get(taxonomiesParam)).thenReturn(hierarchyDependencyValue);

		List<String> calculatedValue = calculator.calculate(parameters);

		assertThat(calculatedValue).isEqualTo(Arrays.asList("first/path/zeSubFolder", "second/path2/zeSubFolder", "second/path3/zeSubFolder"));
	}

	@Test
	public void givenNoNullPathsWhenCalculatingValueThenAllReturned()
			throws Exception {
		hierarchyDependencyValue = new HierarchyDependencyValue(
				taxonomy,
				Arrays.asList(path1, null, path3),
				Collections.emptyList(),
				Collections.emptyList());
		when(parameters.get(taxonomiesParam)).thenReturn(hierarchyDependencyValue);

		List<String> calculatedValue = calculator.calculate(parameters);

		assertThat(calculatedValue).isEqualTo(Arrays.asList("first/path/zeSubFolder", "second/path3/zeSubFolder"));
	}
}
