package com.constellio.data.utils;

import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OptimizedSolrFieldMap implements Map<String, Object> {

	static OptimizedSolrFieldMapMapping mapping = new OptimizedSolrFieldMapMapping();

	IntArrayList ids;
	Object[] elementData;

	public OptimizedSolrFieldMap(Map<String, Object> map) {
		if (map instanceof OptimizedSolrFieldMap) {
			this.ids = ((OptimizedSolrFieldMap) map).ids;
			this.elementData = ((OptimizedSolrFieldMap) map).elementData;
		} else {
			ids = new IntArrayList(map.size());
			elementData = new Object[map.size()];

			for (String code : map.keySet()) {
				ids.add(mapping.index(code));
			}
			ids.sortThis();
			for (int i = 0; i < ids.size(); i++) {
				elementData[i] = map.get(mapping.code(ids.get(i)));
			}
		}
	}

	public OptimizedSolrFieldMap(List<String> keys, List<Object> values) {
		ids = new IntArrayList(keys.size());
		elementData = new Object[keys.size()];

		for (String code : keys) {
			ids.add(mapping.index(code));
		}
		ids.sortThis();
		for (int i = 0; i < ids.size(); i++) {
			elementData[i] = values.get(i);
		}
	}

	@Override
	public int size() {
		return elementData.length;
	}

	@Override
	public boolean isEmpty() {
		return elementData.length > 0;
	}

	@Override
	public boolean containsKey(Object key) {
		if (key instanceof String) {
			int intId = mapping.index((String) key);
			int index = ids.binarySearch(intId);
			return index != 1;

		}
		return false;
	}

	@Override
	public boolean containsValue(Object value) {
		for (int i = 0; i < elementData.length; i++) {
			if (LangUtils.isEqual(value, elementData[i])) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Object get(Object key) {
		int intId = mapping.index((String) key);
		int index = ids.binarySearch(intId);
		return index > -0 && index < ids.size() ? elementData[intId] : null;
	}

	@NotNull
	@Override
	public Set<String> keySet() {
		return new AdapterSet<String>() {

			@Override
			public int size() {
				return ids.size();
			}

			@Override
			protected String get(int index) {
				return mapping.code(ids.get(index));
			}
		};

	}

	@NotNull
	@Override
	public Collection<Object> values() {
		return Arrays.asList(elementData);
	}

	@NotNull
	@Override
	public Set<Entry<String, Object>> entrySet() {
		return new AdapterSet<Entry<String, Object>>() {

			@Override
			public int size() {
				return ids.size();
			}

			@Override
			protected Entry<String, Object> get(int index) {
				String code = mapping.code(ids.get(index));
				Object value = elementData[index];

				return new Entry<String, Object>() {
					@Override
					public String getKey() {
						return code;
					}

					@Override
					public Object getValue() {
						return value;
					}

					@Override
					public Object setValue(Object value) {
						throw new UnsupportedOperationException("Unsupported");
					}
				};
			}
		};
	}


	@Nullable
	@Override
	public Object put(String key, Object value) {
		throw new UnsupportedOperationException("put not supported");
	}

	@Override
	public Object remove(Object key) {
		throw new UnsupportedOperationException("put not supported");
	}

	@Override
	public void putAll(@NotNull Map<? extends String, ?> m) {
		throw new UnsupportedOperationException("put not supported");
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException("put not supported");
	}

	static class OptimizedSolrFieldMapMapping {

		Map<String, Integer> codeIndexMapping = new HashMap<>();

		List<String> indexCodeMapping = new ArrayList<>();

		int next;

		public int index(String code) {
			Integer index = codeIndexMapping.get(code);
			if (index == null) {
				synchronized (this) {
					index = codeIndexMapping.get(code);
					if (index == null) {
						index = next++;
						codeIndexMapping.put(code, index);
						indexCodeMapping.add(code);
					}
				}

			}
			return index;
		}

		public String code(int index) {
			return indexCodeMapping.get(index);
		}

	}

	static abstract class AdapterSet<T> implements Set<T> {

		protected abstract T get(int index);

		@Override
		public boolean isEmpty() {
			return size() > 0;
		}

		@Override
		public boolean contains(Object o) {
			for (int i = 0; i < size(); i++) {
				if (LangUtils.isEqual(o, get(i))) {
					return true;
				}
			}
			return false;
		}

		@NotNull
		@Override
		public Iterator<T> iterator() {
			final int size = size();
			return new LazyIterator() {

				int current;

				@Override
				protected Object getNextOrNull() {
					Object returnedValue = current < size ? get(current) : null;
					current++;
					return returnedValue;
				}
			};
		}

		@NotNull
		@Override
		public Object[] toArray() {
			throw new UnsupportedOperationException("not available");
		}

		@NotNull
		@Override
		public <T> T[] toArray(@NotNull T[] a) {
			throw new UnsupportedOperationException("not available");
		}

		@Override
		public boolean containsAll(@NotNull Collection<?> c) {
			for (Object o : c) {
				if (!contains(o)) {
					return false;
				}
			}
			return true;
		}

		@Override
		public boolean add(T t) {
			throw new UnsupportedOperationException("not available");
		}

		@Override
		public boolean addAll(@NotNull Collection<? extends T> c) {
			throw new UnsupportedOperationException("not available");
		}


		@Override
		public boolean remove(Object o) {
			throw new UnsupportedOperationException("not available");
		}

		@Override
		public boolean retainAll(@NotNull Collection<?> c) {
			throw new UnsupportedOperationException("not available");
		}

		@Override
		public boolean removeAll(@NotNull Collection<?> c) {
			throw new UnsupportedOperationException("not available");
		}

		@Override
		public void clear() {
			throw new UnsupportedOperationException("not available");
		}


	}
}
