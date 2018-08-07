package com.constellio.model.utils;

import java.util.*;
import com.constellio.data.utils.KeySetMap;

import java.util.*;
import java.util.Map.Entry;

public class DependencyUtils<V> {


	public boolean hasCyclicDependencies(Map<V, Set<V>> dependenciesMap) {

		try {
			validateNoCyclicDependencies(dependenciesMap);
			return false;
		} catch (DependencyUtilsRuntimeException.CyclicDependency e) {
			return true;
		}
	}

	public void validateNoCyclicDependencies(Map<V, Set<V>> dependenciesMap) {
		sortByDependency(dependenciesMap, new Comparator<V>() {
			@Override
			public int compare(V o1, V o2) {
				return 0;
			}
		});
	}

	public MultiMapDependencyResults<V> sortTwoLevelOfDependencies(Map<V, Set<V>> primaryDependenciesMap,
																   Map<V, Set<V>> secondaryDependenciesMap,
																   DependencyUtilsParams params) {

		Map<V, Set<V>> allDependencies = merge(primaryDependenciesMap, secondaryDependenciesMap);
		KeySetMap<Object, Object> removed = new KeySetMap<>();
		List<V> sorted = null;

		while (sorted == null) {
			try {
				sorted = sortByDependency(allDependencies, new DependencyUtilsParams(params).withoutToleratedCyclicDepencies());

			} catch (DependencyUtilsRuntimeException.CyclicDependency e) {
				boolean removedSomething = false;
				removingASecondaryDepedendency:
				for (Map.Entry<Object, Set<Object>> entry : e.getCyclicDependencies().entrySet()) {

					for (Object object : entry.getValue()) {
						Set<V> principalDependenciesValues = primaryDependenciesMap.get(entry.getKey());
						if (principalDependenciesValues == null || !principalDependenciesValues.contains(object)) {
							HashSet<V> newValues = new HashSet<>(allDependencies.get(entry.getKey()));
							newValues.remove(object);
							allDependencies.put((V) entry.getKey(), newValues);
							removed.add(entry.getKey(), object);
							removedSomething = true;
							break removingASecondaryDepedendency;
						}

					}

				}

				if (!removedSomething) {
					throw new DependencyUtilsRuntimeException.CyclicDependency(e.getCyclicDependencies());
				}
			}


		}

		return new MultiMapDependencyResults(sorted, removed.getNestedMap());
	}

	private Map<V, Set<V>> merge(Map<V, Set<V>> map1, Map<V, Set<V>> map2) {
		Map<V, Set<V>> merged = new HashMap<>();

		for (Map<V, Set<V>> aMap : Arrays.asList(map1, map2)) {
			for (Map.Entry<V, Set<V>> entry : aMap.entrySet()) {
				Set<V> values = merged.get(entry.getKey());
				if (values == null) {
					values = new HashSet<>();
					merged.put(entry.getKey(), values);
				}
				values.addAll(entry.getValue());
			}


		}
		return merged;
	}

	public List<V> sortByDependency(Map<V, Set<V>> dependenciesMap, Comparator<V> tieComparator) {
		return sortByDependency(dependenciesMap, new DependencyUtilsParams().sortTieUsing(tieComparator));

	}

	public List<V> sortByDependency(Map<V, Set<V>> dependenciesMap) {
		return sortByDependency(dependenciesMap, new DependencyUtilsParams().sortUsingDefaultComparator());

	}

	public List<V> sortByDependencyWithoutTieSort(Map<V, Set<V>> dependenciesMap) {
		return sortByDependency(dependenciesMap, new DependencyUtilsParams());
	}

	public List<V> sortByDependency(Map<V, Set<V>> dependenciesMap, DependencyUtilsParams params) {
		Map<V, Set<V>> dependenciesMapCopy = copyInModifiableMap(dependenciesMap);
		removeSelfDependencies(dependenciesMapCopy);
		removeDependenciesToOtherElements(dependenciesMapCopy);

		List<V> sortedElements = new ArrayList<>();

		KeySetMap<V, V> removedDependencyLinks = new KeySetMap<>();

		while (!dependenciesMapCopy.isEmpty()) {
			IterationResults<V> iterationResults = getMetadatasWithoutDependencies(dependenciesMapCopy, params);

			if (iterationResults.valuesWithoutDependencies.isEmpty()) {
				Map<V, Set<V>> cyclicDependencyCauses = toCyclicDependencyCauses(dependenciesMapCopy);
				List<V> cycleElements = getCyclicElements(cyclicDependencyCauses.keySet());

				StringBuilder sb = new StringBuilder();

				for (Entry<V, Set<V>> entry : cyclicDependencyCauses.entrySet()) {
					sb.append(entry.getKey() + " => " + entry.getValue() + "\n");
				}

				throw new DependencyUtilsRuntimeException.CyclicDependency(cyclicDependencyCauses, sb.toString());
			}

			if (params.isSortTie()) {
				Collections.sort(iterationResults.valuesWithoutDependencies, params.<V>getTieComparator());
			}

			sortedElements.addAll(iterationResults.valuesWithoutDependencies);
			dependenciesMapCopy = iterationResults.valuesWithDependencies;
		}
		return sortedElements;
	}

	private Map<V, Set<V>> toCyclicDependencyCauses(Map<V, Set<V>> dependenciesMapCopy) {
		Map<V, Set<V>> causes = new HashMap<>(dependenciesMapCopy);

		Set<V> keysNotCausingCyclicDependencies = null;
		while (keysNotCausingCyclicDependencies == null || !keysNotCausingCyclicDependencies.isEmpty()) {

			if (keysNotCausingCyclicDependencies != null) {
				for (V key : keysNotCausingCyclicDependencies) {
					causes.remove(key);
				}
			}
			keysNotCausingCyclicDependencies = new HashSet<>();

			for (V key : causes.keySet()) {

				int countOtherElementDependenciesToCurrentKey = 0;

				for (Map.Entry<V, Set<V>> entry : causes.entrySet()) {
					if (!key.equals(entry.getKey())) {
						for (V dependency : entry.getValue()) {
							if (dependency.equals(key)) {
								countOtherElementDependenciesToCurrentKey++;
							}
						}
					}
				}

				if (countOtherElementDependenciesToCurrentKey == 0) {
					keysNotCausingCyclicDependencies.add(key);
				}
			}
		}


		return causes;
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

	private IterationResults<V> getMetadatasWithoutDependencies(Map<V, Set<V>> metadatas,
																DependencyUtilsParams params) {

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

		if (valuesWithoutDependencies.isEmpty() && !valuesWithDependencies.isEmpty() && params.isTolerateCyclicDependencies()) {
			return getMetadatasWithoutLesserDependencies(metadatas);
		} else {
			return new IterationResults<>(valuesWithoutDependencies, valuesWithDependencies);
		}
	}

	private IterationResults<V> getMetadatasWithoutLesserDependencies(Map<V, Set<V>> metadatas) {

		boolean moreDepenciesToAdd = true;
		while (moreDepenciesToAdd) {
			moreDepenciesToAdd = false;
			for (Map.Entry<V, Set<V>> entry : metadatas.entrySet()) {
				Set<V> valuesToAdd = new HashSet<>();
				for (V value : entry.getValue()) {
					valuesToAdd.addAll(metadatas.get(value));
				}
				valuesToAdd.add(entry.getKey());
				int sizeBefore = entry.getValue().size();
				entry.getValue().addAll(valuesToAdd);
				int sizeAfter = entry.getValue().size();

				moreDepenciesToAdd |= sizeBefore != sizeAfter;
			}
		}

		int minCounterOfDependencies = 10000000;
		for (Map.Entry<V, Set<V>> entry : metadatas.entrySet()) {
			int currentValue = entry.getValue().size();
			if (currentValue < minCounterOfDependencies) {
				minCounterOfDependencies = currentValue;
			}
		}

		List<V> valuesWithLesserDependencies = new ArrayList<>();
		Map<V, Set<V>> valuesWithDependencies = new HashMap<>();

		for (Map.Entry<V, Set<V>> entry : metadatas.entrySet()) {
			if (entry.getValue().size() == minCounterOfDependencies) {
				valuesWithLesserDependencies.add(entry.getKey());
			} else {
				valuesWithDependencies.put(entry.getKey(), metadatas.get(entry.getKey()));
			}
		}

		for (V nextElement : valuesWithLesserDependencies) {
			for (Map.Entry<V, Set<V>> otherMetadataEntry : valuesWithDependencies.entrySet()) {
				otherMetadataEntry.getValue().remove(nextElement);
			}
		}

		return new IterationResults<>(valuesWithLesserDependencies, valuesWithDependencies);
	}

	private static class IterationResults<V> {

		private List<V> valuesWithoutDependencies;

		private Map<V, Set<V>> valuesWithDependencies;

		private IterationResults(List<V> valuesWithoutDependencies, Map<V, Set<V>> valuesWithDependencies) {
			this.valuesWithoutDependencies = valuesWithoutDependencies;
			this.valuesWithDependencies = valuesWithDependencies;
		}
	}

	public static class MultiMapDependencyResults<V> {

		private List<V> sortedElements;

		private Map<V, Set<V>> removedDependencies;

		private MultiMapDependencyResults(List<V> sortedElements, Map<V, Set<V>> removedDependencies) {
			this.sortedElements = sortedElements;
			this.removedDependencies = removedDependencies;
		}

		public List<V> getSortedElements() {
			return sortedElements;
		}

		public Map<V, Set<V>> getRemovedDependencies() {
			return removedDependencies;
		}
	}

}
