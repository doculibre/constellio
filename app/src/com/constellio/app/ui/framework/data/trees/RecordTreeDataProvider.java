package com.constellio.app.ui.framework.data.trees;

import com.constellio.app.ui.framework.components.tree.LazyTree;
import com.constellio.app.ui.framework.data.LazyTreeDataProvider;
import com.constellio.app.ui.framework.data.ObjectsResponse;
import com.vaadin.server.Resource;

public class RecordTreeDataProvider implements LazyTreeDataProvider<String> {

	RecordTreeNodesDataProvider recordTreeNodesDataProvider;

	public RecordTreeDataProvider(
			RecordTreeNodesDataProvider recordTreeNodesDataProvider) {
		this.recordTreeNodesDataProvider = recordTreeNodesDataProvider;
	}

	@Override
	public ObjectsResponse<String> getRootObjects(int start, int maxSize) {
		return null;
	}

	@Override
	public String getParent(String child) {
		return null;
	}

	@Override
	public ObjectsResponse<String> getChildren(String parent, int start, int maxSize) {
		return null;
	}

	@Override
	public boolean hasChildren(String parent) {
		return false;
	}

	@Override
	public boolean isLeaf(String object) {
		return false;
	}

	@Override
	public String getTaxonomyCode() {
		return null;
	}

	@Override
	public String getCaption(String id) {
		return null;
	}

	@Override
	public String getDescription(String id) {
		return null;
	}

	@Override
	public Resource getIcon(String id, boolean expanded) {
		return null;
	}

	@Override
	public int getEstimatedRootNodesCount() {
		return 0;
	}

	@Override
	public int getEstimatedChildrenNodesCount(String parent) {
		return 0;
	}

}
