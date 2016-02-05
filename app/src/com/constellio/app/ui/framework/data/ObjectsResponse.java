package com.constellio.app.ui.framework.data;

import java.util.List;

public class ObjectsResponse<T> {

	List<T> objects;

	int count;

	public ObjectsResponse(List<T> objects, Long count) {
		this.objects = objects;
		this.count = count.intValue();
	}

	public List<T> getObjects() {
		return objects;
	}

	public int getCount() {
		return count;
	}
}
