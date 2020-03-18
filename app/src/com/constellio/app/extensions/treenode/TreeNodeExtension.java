package com.constellio.app.extensions.treenode;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.framework.data.trees.RecordTreeNodesDataProvider;
import com.constellio.app.ui.framework.data.trees.TreeNodesProvider;

import java.util.List;

public interface TreeNodeExtension {
	default RecordTreeNodesDataProvider getTreeNodeFor(String codeToxonomie, AppLayerFactory appLayerFactory) {
		return null;
	}

	default List<TreeNodesProvider<?>> getExtraTreeNodeProvider(String taxonomyCode) {
		return null;
	}

}
