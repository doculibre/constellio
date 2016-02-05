package com.constellio.model.utils;

import static com.constellio.sdk.tests.TestUtils.asList;
import static com.constellio.sdk.tests.TestUtils.asSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class DependencyUtilsTest {

	DependencyUtils utils = new DependencyUtils();

	@Test
	public void givenMapWithoutCyclicDependenciesWhenSortingByDependencyThenCorrectOrder()
			throws Exception {

		Map<String, Set<String>> dependenciesMap = new HashMap<>();
		dependenciesMap.put("a", asSet("b", "c"));
		dependenciesMap.put("b", asSet("c"));

		List<String> dependencies = utils.sortByDependency(dependenciesMap);

		assertThat(dependencies).containsExactly("b", "a");

	}

	@Test
	public void givenMapWithoutCyclicDependenciesWhenValidatingNoCyclicDependenciesThenNothingHappens()
			throws Exception {

		Map<String, Set<String>> dependenciesMap = new HashMap<>();
		dependenciesMap.put("a", asSet("b", "c"));
		dependenciesMap.put("b", asSet("c"));

		utils.validateNoCyclicDependencies(dependenciesMap);

	}


	@Test()
	public void givenMapWithCyclicDependenciesWhenSortingWithCyclicTolerationThenCorrectlySorted()
			throws Exception {

		Map<String, Set<String>> dependenciesMap = new HashMap<>();
		dependenciesMap.put("a", asSet("b", "c"));
		dependenciesMap.put("b", asSet("c"));
		dependenciesMap.put("c", asSet("b", "d"));
		dependenciesMap.put("d", asSet("h"));
		dependenciesMap.put("e", asSet("d"));
		dependenciesMap.put("f", asSet("a"));
		dependenciesMap.put("g", asSet("a"));
		// d -> e -> b/c -> a

		DependencyUtilsParams params = new DependencyUtilsParams().sortUsingDefaultComparator()
				.withToleratedCyclicDepencies();

		List<String> sortedValues = utils.sortByDependency(dependenciesMap, params);
		assertThat(sortedValues).isEqualTo(asList("d", "e", "b", "c", "a", "f", "g"));


	}

	@Test()
	public void givenMapWithCyclicDependenciesWhenValidatingNoCyclicDependenciesThenException()
			throws Exception {

		Map<String, Set<String>> dependenciesMap = new HashMap<>();
		dependenciesMap.put("a", asSet("b", "c"));
		dependenciesMap.put("b", asSet("c"));
		dependenciesMap.put("c", asSet("a"));
		dependenciesMap.put("d", asSet("e"));

		try {
			utils.validateNoCyclicDependencies(dependenciesMap);
			fail("Cyclic dependency expected");
		} catch (DependencyUtilsRuntimeException.CyclicDependency e) {
			assertThat(e.getCyclicDependencies()).containsOnly("a", "b", "c");
		}

	}

	@Test
	public void givenElementWithDependentToItselfThenOK()
			throws Exception {

		Map<String, Set<String>> dependenciesMap = new HashMap<>();
		dependenciesMap.put("a", asSet("b", "a"));
		dependenciesMap.put("b", new HashSet<String>());

		utils.validateNoCyclicDependencies(dependenciesMap);

	}

	@Test
	public void givenElementWithDependentWhenSortingByDependencyThenSortedByDependencyAndTiesUsingComparator()
			throws Exception {

		Map<String, Set<String>> dependenciesMap = new HashMap<>();
		dependenciesMap.put("a", asSet("b", "a"));
		dependenciesMap.put("b", new HashSet<String>());
		dependenciesMap.put("b2", new HashSet<String>());
		dependenciesMap.put("c", new HashSet<String>());
		dependenciesMap.put("a2", asSet("b", "a2"));
		dependenciesMap.put("d1", asSet("a"));
		dependenciesMap.put("d2", asSet("a2"));

		List<String> dependencies = utils.sortByDependency(dependenciesMap, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return o1.compareTo(o2);
			}
		});

		assertThat(dependencies).containsExactly("b", "b2", "c", "a", "a2", "d1", "d2");

	}

}
