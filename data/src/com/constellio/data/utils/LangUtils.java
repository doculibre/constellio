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
package com.constellio.data.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class LangUtils {

	public static <V> boolean containsAny(Collection<V> firstCollection, Collection<V> secondCollection) {

		for (V firstCollectionValue : firstCollection) {
			for (V secondCollectionValue : secondCollection) {
				if (firstCollectionValue != null && firstCollectionValue.equals(secondCollectionValue)) {
					return true;
				}
			}
		}

		return false;
	}

	public static <K, V> Map<K, V> newMapWithEntry(K key, V value) {
		Map<K, V> values = new HashMap<>();
		values.put(key, value);
		return values;
	}

	public static boolean areNullableEqual(Object o1, Object o2) {
		if (o1 == null && o2 == null) {
			return true;

		} else if (o1 == null && o2 != null) {
			return false;

		} else if (o1 != null && o2 == null) {
			return false;

		} else {
			return o1.equals(o2);
		}
	}

	public static <K, V> MapComparisonResults<K, V> compare(Map<K, V> before, Map<K, V> after) {

		Set<K> keysBefore = before.keySet();

		ListComparisonResults<K> results = compare(keysBefore, after.keySet());
		List<ModifiedEntry<K, V>> modifiedEntries = new ArrayList<>();

		for (K key : keysBefore) {
			if (after.containsKey(key)) {
				V valueBefore = before.get(key);
				V valueAfter = after.get(key);

				if (!LangUtils.areNullableEqual(valueBefore, valueAfter)) {
					modifiedEntries.add(new ModifiedEntry<>(key, valueBefore, valueAfter));
				}
			}
		}

		return new MapComparisonResults<>(results.getNewItems(), results.getRemovedItems(), modifiedEntries);
	}

	public static <T> ListComparisonResults<T> compare(Set<T> before, Set<T> after) {
		return compare(new ArrayList<>(before), new ArrayList<>(after));
	}

	public static <T> ListComparisonResults<T> compare(List<T> before, List<T> after) {
		List<T> newItems = new ArrayList<>();
		List<T> removedItems = new ArrayList<>();

		if (before != null) {
			for (T item : before) {
				if (!after.contains(item) && !removedItems.contains(item)) {
					removedItems.add(item);
				}
			}
		}

		if (after != null) {
			for (T item : after) {
				if (!before.contains(item) && !newItems.contains(item)) {
					newItems.add(item);
				}
			}
		}

		return new ListComparisonResults<>(newItems, removedItems);
	}

	public static boolean isEqual(Object value1, Object value2) {
		if (value1 == null) {
			return value2 == null;
		} else {
			return value1.equals(value2);
		}
	}

	public static void ensureNoNullItems(List<?> items) {
		for (Object item : items) {
			if (item == null) {
				throw new IllegalArgumentException("Null values are not allowed in list");
			}
		}
	}

	public static boolean isTrueOrNull(Object value) {
		return !Boolean.FALSE.equals(value);
	}

	public static boolean isFalseOrNull(Object value) {
		return !Boolean.TRUE.equals(value);
	}

	public static List<String> withoutDuplicates(List<String> value) {
		return new ArrayList<>(new HashSet<>(value));
	}

	public static class ListComparisonResults<T> {

		private List<T> newItems;

		private List<T> removedItems;

		public ListComparisonResults(List<T> newItems, List<T> removedItems) {
			this.newItems = Collections.unmodifiableList(newItems);
			this.removedItems = Collections.unmodifiableList(removedItems);
		}

		public List<T> getNewItems() {
			return newItems;
		}

		public List<T> getRemovedItems() {
			return removedItems;
		}
	}

	public static class MapComparisonResults<K, V> {

		private List<K> newEntries;

		private List<K> removedEntries;

		private List<ModifiedEntry<K, V>> modifiedEntries;

		public MapComparisonResults(List<K> newEntries, List<K> removedEntries,
				List<ModifiedEntry<K, V>> modifiedEntries) {
			this.newEntries = Collections.unmodifiableList(newEntries);
			this.removedEntries = Collections.unmodifiableList(removedEntries);
			this.modifiedEntries = Collections.unmodifiableList(modifiedEntries);
		}

		public List<K> getNewEntries() {
			return newEntries;
		}

		public List<K> getRemovedEntries() {
			return removedEntries;
		}

		public List<ModifiedEntry<K, V>> getModifiedEntries() {
			return modifiedEntries;
		}
	}

	public static class ModifiedEntry<K, V> {

		private K key;

		private V valueBefore;

		private V valueAfter;

		public ModifiedEntry(K key, V valueBefore, V valueAfter) {
			this.key = key;
			this.valueBefore = valueBefore;
			this.valueAfter = valueAfter;
		}

		public K getKey() {
			return key;
		}

		public V getValueBefore() {
			return valueBefore;
		}

		public V getValueAfter() {
			return valueAfter;
		}

		@Override
		public int hashCode() {
			return HashCodeBuilder.reflectionHashCode(this);
		}

		@Override
		public boolean equals(Object obj) {
			return EqualsBuilder.reflectionEquals(this, obj);
		}
	}

	public static String tabs(int n) {
		return repeat("\t", n);
	}

	public static String repeat(String string, int n) {
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < n; i++) {
			sb.append(string);
		}

		return sb.toString();
	}

	public static Map<String, String> asMap(String key1, String value1) {
		Map<String, String> parameters = new HashMap<>();
		parameters.put(key1, value1);
		return parameters;
	}

	public static Map<String, String> asMap(String key1, String value1, String key2, String value2) {
		Map<String, String> parameters = new HashMap<>();
		parameters.put(key1, value1);
		parameters.put(key2, value2);
		return parameters;
	}

	public static Map<String, String> asMap(String key1, String value1, String key2, String value2, String key3, String value3) {
		Map<String, String> parameters = new HashMap<>();
		parameters.put(key1, value1);
		parameters.put(key2, value2);
		parameters.put(key3, value3);
		return parameters;
	}
}
