package com.constellio.model.utils;

import org.junit.Test;

import java.util.*;

import static com.constellio.sdk.tests.TestUtils.asList;
import static com.constellio.sdk.tests.TestUtils.asSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@SuppressWarnings({"rawtypes", "unchecked"})
public class DependencyUtilsTest {

	DependencyUtils utils = new DependencyUtils();


	//	@Test
	//	public void whenSortingByDependencyUsingMultipleLevelsThenUseNextLevelsToHandleEqualitiesInFirstLevels()
	//			throws Exception {
	//
	//		Map<String, Set<String>> firstLevelMap = new HashMap<>();
	//		firstLevelMap.put("a", asSet("b", "c"));
	//		firstLevelMap.put("b", asSet("c"));
	//		firstLevelMap.put("c", new HashSet<String>());
	//		firstLevelMap.put("d", new HashSet<String>());
	//		firstLevelMap.put("e", new HashSet<String>());
	//
	//		Map<String, Set<String>> secondLevelMap = new HashMap<>();
	//		firstLevelMap.put("c", asSet("d", "e"));
	//		firstLevelMap.put("e", asSet("b"));
	//
	//		Map<String, Set<String>> thirdLevelMap = new HashMap<>();
	//		firstLevelMap.put("a", asSet("b", "c"));
	//		firstLevelMap.put("b", asSet("c"));
	//
	//		List<String> dependencies = utils.sortByDependency(asMap(firstLevelMap));
	//
	//		assertThat(dependencies).containsExactly("b", "a");
	//
	//	}

	@Test
	public void testfrancis()
			throws Exception {

		Map<String, Set<String>> dependenciesMap = new HashMap<>();
		dependenciesMap.put("a", asSet("a@2"));
		dependenciesMap.put("b", asSet("b@2", "a"));
		dependenciesMap.put("c", asSet("c@2", "d"));
		dependenciesMap.put("d", asSet("d@2"));
		dependenciesMap.put("e", asSet("e@2"));
		dependenciesMap.put("f", asSet("f@2"));

		dependenciesMap.put("a@2", asSet("a@3"));
		dependenciesMap.put("b@2", asSet("b@3"));
		dependenciesMap.put("c@2", asSet("c@3", "e@2"));
		dependenciesMap.put("d@2", asSet("d@3"));
		dependenciesMap.put("e@2", asSet("e@3"));
		dependenciesMap.put("f@2", asSet("f@3", "b@2"));


		dependenciesMap.put("a@3", asSet("e@3"));
		dependenciesMap.put("b@3", new HashSet<String>());
		dependenciesMap.put("c@3", asSet("f@3", "d@3"));
		dependenciesMap.put("d@3", asSet("e@3", "c@3"));
		dependenciesMap.put("e@3", new HashSet<String>());
		dependenciesMap.put("f@3", new HashSet<String>());


		//utils.validateNoCyclicDependencies(dependenciesMap);

		List<String> dependencies = utils.sortByDependency(dependenciesMap);

		System.out.println(dependencies);

	}

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
