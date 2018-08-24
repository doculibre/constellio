package com.constellio.model.utils;

import com.constellio.model.utils.DependencyUtils.MultiMapDependencyResults;
import com.constellio.model.utils.DependencyUtilsRuntimeException.CyclicDependency;
import org.junit.Test;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.constellio.sdk.tests.TestUtils.asSet;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.data.MapEntry.entry;

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
	public void whenSortingByDependencyUsingTwoMapsThenReturnSortedElementsWithFirstMapRequirementAndMostOfTheSecondOnes() {

		Map<String, Set<String>> dependenciesMap = new HashMap<>();
		dependenciesMap.put("a", new HashSet<String>());
		dependenciesMap.put("b", asSet("a"));
		dependenciesMap.put("c", asSet("d"));
		dependenciesMap.put("d", new HashSet<String>());
		dependenciesMap.put("e", new HashSet<String>());
		dependenciesMap.put("f", asSet("b"));

		//'a' must be before 'b', 'c' must be before 'd', 'f' must be before 'b'

		Map<String, Set<String>> secondDependenciesMap = new HashMap<>();
		secondDependenciesMap.put("a", asSet("f", "b"));
		secondDependenciesMap.put("b", new HashSet<String>());
		secondDependenciesMap.put("c", asSet("f", "e"));
		secondDependenciesMap.put("d", asSet("e", "c"));
		secondDependenciesMap.put("e", new HashSet<String>());
		secondDependenciesMap.put("f", new HashSet<String>());

		//'a' should be before 'f', but it is not possible
		//'b' should be before 'a', but it is not possible
		//'c' should be before 'f',
		//'c' should be before 'e',
		//'d' should be before 'c', but it is not possible
		//'d' should be before 'e',

		MultiMapDependencyResults<String> results = utils.sortTwoLevelOfDependencies(dependenciesMap, secondDependenciesMap, new DependencyUtilsParams());
		assertThat(results.getSortedElements()).isEqualTo(asList("a", "e", "b", "d", "f", "c"));
		assertThat(results.getRemovedDependencies()).containsOnly(
				entry("a", asSet("b", "f")),
				entry("d", asSet("c"))
		);

		results = utils.sortTwoLevelOfDependencies(dependenciesMap, secondDependenciesMap, new DependencyUtilsParams().withToleratedCyclicDepencies());
		assertThat(results.getSortedElements()).isEqualTo(asList("a", "e", "b", "d", "f", "c"));
		assertThat(results.getRemovedDependencies()).containsOnly(
				entry("a", asSet("b", "f")),
				entry("d", asSet("c"))
		);
	}

	@Test
	public void whenSortingByDependencyUsingTwoMapsWithTheFirstHavingCyclicDependencyThenException() {

		Map<String, Set<String>> dependenciesMap = new HashMap<>();
		dependenciesMap.put("a", asSet("f"));
		dependenciesMap.put("b", asSet("a"));
		dependenciesMap.put("c", asSet("d"));
		dependenciesMap.put("d", new HashSet<String>());
		dependenciesMap.put("e", new HashSet<String>());
		dependenciesMap.put("f", asSet("b"));

		//'a' must be before 'b', 'c' must be before 'd', 'f' must be before 'b'

		Map<String, Set<String>> secondDependenciesMap = new HashMap<>();
		secondDependenciesMap.put("a", asSet("f", "b"));
		secondDependenciesMap.put("b", new HashSet<String>());
		secondDependenciesMap.put("c", asSet("f", "e"));
		secondDependenciesMap.put("d", asSet("e", "c"));
		secondDependenciesMap.put("e", new HashSet<String>());
		secondDependenciesMap.put("f", new HashSet<String>());

		//'a' should be before 'f', but it is not possible
		//'b' should be before 'a', but it is not possible
		//'c' should be before 'f',
		//'c' should be before 'e',
		//'d' should be before 'c', but it is not possible
		//'d' should be before 'e',

		try {
			utils.sortTwoLevelOfDependencies(dependenciesMap, secondDependenciesMap, new DependencyUtilsParams());
			fail("Exception expected");
		} catch (CyclicDependency e) {
			assertThat(e.getCyclicDependencies()).containsOnly(
					entry("a", asSet("f")),
					entry("b", asSet("a")),
					entry("f", asSet("b"))
			);
		}

		try {
			MultiMapDependencyResults<String> results = utils.sortTwoLevelOfDependencies(
					dependenciesMap, secondDependenciesMap, new DependencyUtilsParams().withToleratedCyclicDepencies());
			fail("Exception expected");
		} catch (CyclicDependency e) {
			assertThat(e.getCyclicDependencies()).containsOnly(
					entry("a", asSet("f")),
					entry("b", asSet("a")),
					entry("f", asSet("b"))
			);
		}
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
			assertThat(e.getCyclicDependencies().keySet()).containsOnly("a", "b", "c");
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
