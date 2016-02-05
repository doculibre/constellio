package com.constellio.data.utils;

import com.constellio.data.utils.DelayedRuntimeException.DelayedRuntimeException_AlreadyDefined;
import com.constellio.data.utils.DelayedRuntimeException.DelayedRuntimeException_NotYetDefined;

public class Delayed<T> {

	boolean defined;

	T value;

	public Delayed(T value) {
		this.defined = true;
		this.value = value;
	}

	public Delayed() {
	}

	public T get() {
		if (!defined) {
			throw new DelayedRuntimeException_NotYetDefined();
		}
		return value;
	}

	public void set(T value) {
		if (defined) {
			throw new DelayedRuntimeException_AlreadyDefined();
		}
		defined = true;
		this.value = value;
	}

}
