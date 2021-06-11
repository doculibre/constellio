package com.constellio.data.services.tenant;

import com.constellio.data.utils.TenantUtils;

import java.util.function.Supplier;

public class TenantLocal<V> {

	private Supplier<V> defaultValueSupplier;

	public TenantLocal() {
		this.defaultValueSupplier = () -> null;
	}


	public TenantLocal(Supplier<V> defaultValueSupplier) {
		this.defaultValueSupplier = defaultValueSupplier;
	}

	private Object[] values = new Object[257];

	public V get() {
		V value = (V) values[getIdx()];
		if (value == null) {
			value = defaultValueSupplier.get();
		}
		return value;
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
