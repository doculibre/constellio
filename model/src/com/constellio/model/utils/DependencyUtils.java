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
import java.util.concurrent.atomic.AtomicInteger;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;

import org.apache.commons.collections.map.HashedMap;

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

		while (!dependenciesMapCopy.isEmpty()) {
			IterationResults<V> iterationResults = getMetadatasWithoutDependencies(dependenciesMapCopy, params);

			if (iterationResults.valuesWithoutDependencies.isEmpty()) {
				List<V> cycleElements = getCyclicElements(dependenciesMapCopy.keySet());
				throw new DependencyUtilsRuntimeException.CyclicDependency(cycleElements);
			} else {
				if (params.isSortTie()) {
					Collections.sort(iterationResults.valuesWithoutDependencies, params.<V>getTieComparator());
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

	private IterationResults<V> getMetadatasWithoutDependencies(Map<V, Set<V>> metadatas, DependencyUtilsParams params) {
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

}
