package com.constellio.sdk.load.script.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.constellio.model.entities.records.wrappers.RecordWrapper;

public class LinkableIdsList {

	private int current = 0;
	private List<String> ids = new ArrayList<>();

	public LinkableIdsList() {
	}

	public LinkableIdsList(List<String> ids) {
		this.ids.addAll(ids);
		Collections.shuffle(this.ids);
	}

	public static LinkableIdsList forRecords(List<RecordWrapper> recordWrappers) {
		List<String> ids = new ArrayList<>();
		for (RecordWrapper recordWrapper : recordWrappers) {
			ids.add(recordWrapper.getId());
		}
		return new LinkableIdsList(ids);
	}

	public <T extends RecordWrapper> T attach(T wrapper) {
		ids.add(wrapper.getId());
		return wrapper;
	}

	public synchronized String next() {
		if (current + 1 >= ids.size()) {
			current = 0;
		} else {
			current++;
		}
		return ids.get(current);
	}
}




