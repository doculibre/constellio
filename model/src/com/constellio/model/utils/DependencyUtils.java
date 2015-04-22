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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DependencyUtils<V> {

	public void validateNoCyclicDependencies(Map<V, Set<V>> dependenciesMap) {
		sortByDependency(dependenciesMap, new Comparator<V>() {
			@Override
			public int compare(V o1, V o2) {
				return 0;
			}
		});
	}

	public List<V> sortByDependency(Map<V, Set<V>> dependenciesMap, Comparator<V> tieComparator) {
		return sortByDependency(dependenciesMap, tieComparator, true);

	}

	public List<V> sortByDependencyWithoutTieSort(Map<V, Set<V>> dependenciesMap) {
		return sortByDependency(dependenciesMap, null, false);
	}

	public List<V> sortByDependency(Map<V, Set<V>> dependenciesMap, Comparator<V> tieComparator, boolean sortTie) {
		Map<V, Set<V>> dependenciesMapCopy = copyInModifiableMap(dependenciesMap);
		removeSelfDependencies(dependenciesMapCopy);
		removeDependenciesToOtherElements(dependenciesMapCopy);

		List<V> sortedElements = new ArrayList<>();

		while (!dependenciesMapCopy.isEmpty()) {
			IterationResults<V> iterationResults = getMetadatasWithoutDependencies(dependenciesMapCopy);

			if (iterationResults.valuesWithoutDependencies.isEmpty()) {
				List<V> cycleElements = getCyclicElements(dependenciesMapCopy.keySet());
				throw new DependencyUtilsRuntimeException.CyclicDependency(cycleElements);
			} else {
				if (sortTie) {
					Collections.sort(iterationResults.valuesWithoutDependencies, tieComparator);
				}

				sortedElements.addAll(iterationResults.valuesWithoutDependencies);
			}
			dependenciesMapCopy = iterationResults.valuesWithDependencies;
		}
		return sortedElements;
	}

	private void removeSelfDependencies(Map<V, Set<V>> dependenciesMapCopy) {
		for (V key : dependenciesMapCopy.keySet()) {
			dependenciesMapCopy.get(key).remove(key);
		}
	}

	private void removeDependenciesToOtherElements(Map<V, Set<V>> dependenciesMapCopy) {
		for (V key : dependenciesMapCopy.keySet()) {
			Set<V> values = new HashSet<>(dependenciesMapCopy.get(key));
			Iterator<V> valuesIterator = values.iterator();
			while (valuesIterator.hasNext()) {
				if (!dependenciesMapCopy.keySet().contains(valuesIterator.next())) {
					valuesIterator.remove();
				}
			}
			dependenciesMapCopy.put(key, values);
		}
	}

	private List<V> getCyclicElements(Set<V> keySet) {
		List<V> elements = new ArrayList<V>();

		elements.addAll(keySet);

		return elements;
	}

	private Map<V, Set<V>> copyInModifiableMap(Map<V, Set<V>> metadatasWithLocalCodeDependencies) {
		Map<V, Set<V>> metadatas = new HashMap<>();
		for (Map.Entry<V, Set<V>> entry : metadatasWithLocalCodeDependencies.entrySet()) {
			metadatas.put(entry.getKey(), new HashSet<V>(entry.getValue()));
		}
		return metadatas;
	}

	private IterationResults<V> getMetadatasWithoutDependencies(Map<V, Set<V>> metadatas) {
		List<V> valuesWithoutDependencies = new ArrayList<>();
		Map<V, Set<V>> valuesWithDependencies = new HashMap<>();
		for (Map.Entry<V, Set<V>> entry : metadatas.entrySet()) {
			if (entry.getValue().isEmpty()) {
				valuesWithoutDependencies.add(entry.getKey());
			} else {
				valuesWithDependencies.put(entry.getKey(), entry.getValue());
			}
		}

		for (V nextElement : valuesWithoutDependencies) {
			for (Map.Entry<V, Set<V>> otherMetadataEntry : valuesWithDependencies.entrySet()) {
				otherMetadataEntry.getValue().remove(nextElement);
			}
		}
		return new IterationResults<>(valuesWithoutDependencies, valuesWithDependencies);
	}

	private static class IterationResults<V> {

		private List<V> valuesWithoutDependencies;

		private Map<V, Set<V>> valuesWithDependencies;

		private IterationResults(List<V> valuesWithoutDependencies, Map<V, Set<V>> valuesWithDependencies) {
			this.valuesWithoutDependencies = valuesWithoutDependencies;
			this.valuesWithDependencies = valuesWithDependencies;
		}
	}

}
