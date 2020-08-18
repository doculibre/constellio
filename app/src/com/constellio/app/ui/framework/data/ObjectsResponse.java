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

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		ObjectsResponse<?> that = (ObjectsResponse<?>) o;

		if (count != that.count) {
			return false;
		}
		return objects != null ? objects.equals(that.objects) : that.objects == null;
	}

	@Override
	public int hashCode() {
		int result = objects != null ? objects.hashCode() : 0;
		result = 31 * result + count;
		return result;
	}

	@Override
	public String toString() {
		return "ObjectsResponse{" +
			   "objects=" + objects +
			   ", count=" + count +
			   '}';
	}
}
