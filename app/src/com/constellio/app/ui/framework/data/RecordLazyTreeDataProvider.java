package com.constellio.app.ui.framework.data;

import com.constellio.app.extensions.treenode.TreeNodeExtension;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.framework.data.trees.RecordTreeNodesDataProvider;
import com.constellio.app.ui.framework.data.trees.VisibleRecordTreeNodesDataProvider;

import static com.constellio.app.services.factories.ConstellioFactories.getInstance;

public class RecordLazyTreeDataProvider extends BaseRecordTreeDataProvider implements LazyTreeDataProvider<String> {

	public RecordLazyTreeDataProvider(RecordTreeNodesDataProvider nodesDataProvider) {
		super(nodesDataProvider);
	}


	public static RecordTreeNodesDataProvider getTreeDataProvider(String taxnomieCode, String collection) {
		AppLayerFactory appLayerFactory = getInstance().getAppLayerFactory();

		RecordTreeNodesDataProvider recordTreeNodesDataProvider = null;

		for (TreeNodeExtension treeNodeAppExtension : appLayerFactory.getExtensions()
				.forCollection(collection).treeNodeAppExtension) {
			recordTreeNodesDataProvider = treeNodeAppExtension.getTreeNodeFor(taxnomieCode, appLayerFactory);
			if (recordTreeNodesDataProvider != null) {
				break;
			}
		}

		if (recordTreeNodesDataProvider == null) {
			recordTreeNodesDataProvider = new VisibleRecordTreeNodesDataProvider(taxnomieCode);
		}

		return recordTreeNodesDataProvider;
	}
}
