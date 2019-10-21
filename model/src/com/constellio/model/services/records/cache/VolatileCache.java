package com.constellio.model.services.records.cache;

import com.constellio.data.dao.dto.records.RecordDTO;
import org.mapdb.HTreeMap;

import java.util.Map.Entry;
import java.util.Set;

public class VolatileCache {

	HTreeMap<String, RecordDTO> map;

	boolean enabled = true;

	public VolatileCache(HTreeMap<String, RecordDTO> map) {
		this.map = map;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public VolatileCache setEnabled(boolean enabled) {
		this.enabled = enabled;
		map.clear();
		return this;
	}

	public void remove(String id) {
		if (enabled) {
			map.remove(id);
		}
	}

	public RecordDTO get(String id) {
		return enabled ? map.get(id) : null;
	}

	public void put(String id, RecordDTO recordDTO) {
		if (enabled) {
			map.put(id, recordDTO);
		}
	}

	public void clear() {
		map.clear();
	}

	public Set<Entry<String, RecordDTO>> getEntries() {
		return map.getEntries();
	}
}
