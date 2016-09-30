package com.constellio.data.utils;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class InfiniteMapIterator<K, V> extends LazyIterator<Map.Entry<K, V>> {

	Map<K, V> map;
	Iterator<Entry<K, V>> currentIterator;

	public InfiniteMapIterator(Map<K, V> map) {
		this.map = map;
	}

	@Override
	protected synchronized Entry<K, V> getNextOrNull() {
		if (currentIterator == null || !currentIterator.hasNext()) {
			currentIterator = map.entrySet().iterator();
		}
		return currentIterator.next();
	}

	public static <K, V> Iterator<Map.Entry<K, V>> infinitelyIteratingOverMap(Map<K, V> values) {
		return new InfiniteMapIterator<>(values);
	}
}