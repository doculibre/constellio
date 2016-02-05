package com.constellio.app.ui.framework.data;

import java.io.Serializable;

public interface LazyTreeDataProvider<T extends Serializable> extends DataProvider {

	ObjectsResponse<T> getRootObjects(int start, int maxSize);

	T getParent(T child);

	ObjectsResponse<T> getChildren(T parent, int start, int maxSize);

	boolean hasChildren(T parent);

	boolean isLeaf(T object);

	String getTaxonomyCode();

	String getCaption(T id);
	
	String getDescription(T id);
	
}
