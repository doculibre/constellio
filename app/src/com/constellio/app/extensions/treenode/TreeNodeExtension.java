package com.constellio.app.extensions.treenode;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.framework.data.trees.RecordTreeNodesDataProvider;

public interface TreeNodeExtension {
	RecordTreeNodesDataProvider getTreeNodeFor(String codeToxonomie, AppLayerFactory appLayerFactory);
}
