package com.constellio.sdk.load.script.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LinkableRecordsList<T> {

	private int current = 0;
	private List<T> records = new ArrayList<>();

	public LinkableRecordsList() {
	}

	public LinkableRecordsList(List<T> records) {
		this.records.addAll(records);
		Collections.shuffle(this.records);
	}

	public T attach(T wrapper) {
		records.add(wrapper);
		return wrapper;
	}

	public synchronized T next() {
		if (current + 1 >= records.size()) {
			current = 0;
		} else {
			current++;
		}
		return records.get(current);
	}
}




