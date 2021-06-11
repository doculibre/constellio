package com.constellio.model.entities.schemas;

import com.constellio.model.entities.schemas.AbstractMapBasedSeparatedStructureFactory.MapBasedStructure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractMapBasedSeparatedStructureFactory<T extends MapBasedStructure> implements SeparatedStructureFactory {

	public static abstract class MapBasedStructure implements ModifiableStructure {

		protected boolean dirty;
		private Map<String, Object> map = new HashMap<>();

		protected void set(String key, Object value) {
			map.put(key, value);
			dirty = true;
		}

		protected <T> T get(String key) {
			return (T) map.get(key);
		}

		protected <V> List<V> getList(String key) {
			List<V> values = get(key);
			return values == null ? new ArrayList<V>() : values;
		}

		@Override
		public boolean isDirty() {
			return dirty;
		}
	}

	public ModifiableStructure build(Map<String, Object> fields, StructureInstanciationParams params) {
		MapBasedStructure mapStructure = newEmptyStructure(params);
		mapStructure.map.putAll(fields);
		return mapStructure;
	}

	public Map<String, Object> toFields(ModifiableStructure structure) {
		return ((MapBasedStructure) structure).map;
	}

	protected abstract T newEmptyStructure(StructureInstanciationParams params);
}
