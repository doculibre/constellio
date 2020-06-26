package com.constellio.data.services.tenant;

import com.constellio.data.utils.TenantUtils;

import java.util.HashMap;
import java.util.Map;

public class TenantLocal<V> {

	private Map<String, V> values = new HashMap<>();

	public synchronized V get() {
		String currentTenantId = TenantUtils.getTenantId();
		if (currentTenantId == null) {
			currentTenantId = "default";
		}
		return values.get(currentTenantId);
	}

	public synchronized void set(V value) {
		String currentTenantId = TenantUtils.getTenantId();
		if (currentTenantId == null) {
			currentTenantId = "default";
		}
		values.put(currentTenantId, value);
	}

}
