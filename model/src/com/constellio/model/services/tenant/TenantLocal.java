package com.constellio.model.services.tenant;

import com.constellio.model.utils.TenantUtils;

import java.util.HashMap;
import java.util.Map;

public class TenantLocal<V> {

	private Map<String, V> values = new HashMap<>();

	public V get() {
		String currentTenantId = TenantUtils.getTenantId();
		return currentTenantId == null ? null : values.get(currentTenantId);
	}

	public void set(V value) {
		String currentTenantId = TenantUtils.getTenantId();
		if (currentTenantId != null) {
			values.put(currentTenantId, value);
		}
	}

}
