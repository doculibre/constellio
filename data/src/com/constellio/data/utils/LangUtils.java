package com.constellio.data.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.constellio.data.utils.AccentApostropheCleaner.removeAccents;

public class LangUtils {

	public static <T, V extends T, D extends T> T valueOrDefault(V value, D defaultValue) {
		return value != null ? value : defaultValue;
	}


	public static String toNullableString(Object o) {
		return o == null ? null : o.toString();
	}


	public static Comparator<Entry<String, String>> mapStringStringEntryValueComparator() {
		return new Comparator<Entry<String, String>>() {

			@Override
			public int compare(Entry<String, String> o1, Entry<String, String> o2) {
				String s1 = removeAccents(o1.getValue());
				String s2 = removeAccents(o2.getValue());
				return s1.compareTo(s2);
			}
		};
	}

	public static LocalDate max(LocalDate date1, LocalDate date2) {
		if (date1 == null) {
			return date2;
		}
		if (date2 == null) {
			return date1;
		}
		return date1.isBefore(date2) ? date2 : date1;
	}

	public static LocalDate min(LocalDate date1, LocalDate date2) {
		if (date1 == null) {
			return date2;
		}
		if (date2 == null) {
			return date1;
		}
		return date1.isAfter(date2) ? date2 : date1;
	}

	public static LocalDateTime max(LocalDateTime date1, LocalDateTime date2) {
		if (date1 == null) {
			return date2;
		}
		if (date2 == null) {
			return date1;
		}
		return date1.isBefore(date2) ? date2 : date1;
	}

	public static LocalDateTime min(LocalDateTime date1, LocalDateTime date2) {
		if (date1 == null) {
			return date2;
		}
		if (date2 == null) {
			return date1;
		}
		return date1.isAfter(date2) ? date2 : date1;
	}

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
		if (after == null) {
			removedItems = new ArrayList<>(before);

		} else if (before == null) {
			newItems = new ArrayList<>(after);

		} else if (after != null && after != null) {

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

	public static <T> List<T> withoutDuplicates(List<T> value) {
		return new ArrayList<>(new HashSet<>(value));
	}

	public static List<String> withoutDuplicatesAndNulls(List<String> value) {
		List<String> values = new ArrayList<>(new HashSet<>(value));
		values.remove(null);
		return values;
	}

	public static <T> List<T> withoutNulls(List<T> items) {
		return (List<T>) withoutNulls((Collection<T>) items);
	}

	public static <T> Collection<T> withoutNulls(Collection<T> userPermissionsOnRecord) {

		List<T> withoutNulls = new ArrayList<>();

		Iterator<T> valuesIterator = userPermissionsOnRecord.iterator();
		while (valuesIterator.hasNext()) {
			T value = valuesIterator.next();
			if (value != null) {
				withoutNulls.add(value);
			}
		}

		return withoutNulls;
	}

	public static int compareStrings(String value1, String value2) {
		String normalizedValue1 = removeAccents(value1).toLowerCase();
		String normalizedValue2 = removeAccents(value2).toLowerCase();
		return normalizedValue1.compareTo(normalizedValue2);
	}

	public static <T> boolean hasSameElementsNoMatterTheOrder(List<T> list1, List<T> list2) {
		Set<T> set1 = new HashSet<>(list1);
		Set<T> set2 = new HashSet<>(list2);
		return set1.equals(set2);
	}

	public static int countIteratorValues(Iterator<?> iterator) {
		int count = 0;
		while (iterator.hasNext()) {
			iterator.next();
			count++;
		}
		return count;
	}

	public static int nullableNaturalCompare(Comparable v1, Comparable v2) {
		if (v1 == null) {

			if (v2 == null) {
				return 0;
			} else {
				return -1;
			}

		} else {

			if (v2 == null) {
				return 1;
			} else {
				return v1.compareTo(v2);
			}

		}

	}

	public static StringReplacer replacingLiteral(String target, String replacement) {
		return new StringReplacer().replacingLiteral(target, replacement);
	}

	public static StringReplacer replacingRegex(String regex, String replacement) {
		return new StringReplacer().replacingRegex(regex, replacement);
	}

	public static boolean isEmptyList(Object modifiedValue) {
		return (modifiedValue instanceof List) && (((List) modifiedValue).isEmpty());
	}

	public static List<File> listFiles(File tempFolder) {
		File[] files = tempFolder.listFiles();
		return files == null ? Collections.<File>emptyList() : Arrays.<File>asList(files);

	}

	public static List<String> listFilenames(File tempFolder) {
		String[] files = tempFolder.list();
		return files == null ? Collections.<String>emptyList() : Arrays.<String>asList(files);

	}

	public static class StringReplacer {

		List<StringReplacement> stringReplacements = new ArrayList<>();

		public StringReplacer replacingRegex(String regex, String replacement) {
			Pattern pattern = Pattern.compile(regex);
			stringReplacements.add(new StringReplacement(pattern, replacement));
			return this;
		}

		public StringReplacer replacingLiteral(String target, String replacement) {
			if (!target.equals(replacement)) {
				Pattern pattern = Pattern.compile(target.toString(), Pattern.LITERAL);
				stringReplacements.add(new StringReplacement(pattern, replacement));
			}
			return this;
		}

		public String replaceFirst(String value) {

			String output = value;
			for (StringReplacement stringReplacement : stringReplacements) {
				output = stringReplacement.replaceFirst(output);
			}

			return output;
		}

		public String replaceOn(String value) {

			String output = value;
			for (StringReplacement stringReplacement : stringReplacements) {
				output = stringReplacement.replace(output);
			}

			return output;
		}
	}

	public static class StringReplacement {

		Pattern pattern;
		CharSequence replacement;

		public StringReplacement(Pattern pattern, CharSequence replacement) {
			this.pattern = pattern;
			this.replacement = replacement;
		}

		String replaceFirst(String value) {
			return pattern.matcher(value).replaceFirst(Matcher.quoteReplacement(replacement.toString()));
		}

		String replace(String value) {
			return pattern.matcher(value).replaceAll(Matcher.quoteReplacement(replacement.toString()));
		}
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

	public static <K, V> Map<K, V> asMap(K key1, V value1) {
		Map<K, V> parameters = new HashMap<>();
		parameters.put(key1, value1);
		return parameters;
	}

	public static <K, V> Map<K, V> asMap(K key1, V value1, K key2, V value2) {
		Map<K, V> parameters = new HashMap<>();
		parameters.put(key1, value1);
		parameters.put(key2, value2);
		return parameters;
	}

	public static <K, V> Map<K, V> asMap(K key1, V value1, K key2, V value2, K key3, V value3) {
		Map<K, V> parameters = new HashMap<>();
		parameters.put(key1, value1);
		parameters.put(key2, value2);
		parameters.put(key3, value3);
		return parameters;
	}

	public static <V> Iterator<V> synchronizedIterator(final Iterator<V> nestedIterator) {
		return new Iterator<V>() {
			@Override
			public synchronized boolean hasNext() {
				return nestedIterator.hasNext();
			}

			@Override
			public synchronized V next() {
				return nestedIterator.next();
			}

			@Override
			public synchronized void remove() {
				nestedIterator.remove();
			}
		};
	}

	public static boolean isNullOrEmptyCollection(Object value) {
		return value == null || ((value instanceof Collection) && ((Collection) value).isEmpty());
	}

	public static boolean isNotEmptyValue(Object value) {
		boolean notEmptyValue = value != null;
		if (value instanceof String) {
			notEmptyValue = StringUtils.isNotEmpty((String) value);
		}

		if (value instanceof Collection) {
			Iterator<Object> iterator = ((Collection) value).iterator();
			notEmptyValue = false;
			while (!notEmptyValue && iterator.hasNext()) {
				notEmptyValue = isNotEmptyValue(iterator.next());
			}

		}
		return notEmptyValue;
	}
}
