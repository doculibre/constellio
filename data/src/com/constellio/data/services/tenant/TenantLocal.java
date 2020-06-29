package com.constellio.data.services.tenant;

import com.constellio.data.utils.TenantUtils;

public class TenantLocal<V> {

	private Object[] values = new Object[257];

	public V get() {
		return (V) values[getIdx()];
	}

	public void set(V value) {
		values[getIdx()] = value;
	}

	private int getIdx() {
		Byte currentTenantId = TenantUtils.getByteTenantId();
		int idx;
		if (currentTenantId == null) {
			idx = 256;
		} else {
			idx = currentTenantId - Byte.MIN_VALUE;
		}
		return idx;
	}

}
