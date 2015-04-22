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
package com.constellio.model.utils;

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

		List<String> dependencies = utils.sortByDependency(dependenciesMap, null);

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
