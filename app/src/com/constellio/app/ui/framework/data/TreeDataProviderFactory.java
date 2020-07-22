package com.constellio.app.ui.framework.data;

import com.constellio.app.extensions.treenode.TreeNodeExtension;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.framework.data.trees.CompositeTreeNodeDataProvider;
import com.constellio.app.ui.framework.data.trees.DefaultLazyTreeDataProvider;
import com.constellio.app.ui.framework.data.trees.LegacyTreeNodesDataProviderAdapter;
import com.constellio.app.ui.framework.data.trees.RecordTreeNodesDataProvider;
import com.constellio.app.ui.framework.data.trees.TreeNodesProvider;
import com.constellio.app.ui.framework.data.trees.VisibleRecordTreeNodesDataProvider;
import com.constellio.model.entities.Taxonomy;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.services.factories.ConstellioFactories.getInstance;

public class TreeDataProviderFactory {

	public static LazyTreeDataProvider<String> forTaxonomy(String taxonomyCode, String collection) {
		return getTreeDataProvider(taxonomyCode, collection);
	}

	public static LazyTreeDataProvider<String> forTaxonomy(Taxonomy taxonomy) {
		return forTaxonomy(taxonomy.getCode(), taxonomy.getCollection());
	}

	public static LazyTreeDataProvider<String> getTreeDataProvider(String taxonomyCode, String collection) {
		AppLayerFactory appLayerFactory = getInstance().getAppLayerFactory();

		RecordTreeNodesDataProvider recordTreeNodesDataProvider = null;

		for (TreeNodeExtension treeNodeAppExtension : appLayerFactory.getExtensions()
				.forCollection(collection).treeNodeAppExtension) {
			recordTreeNodesDataProvider = treeNodeAppExtension.getTreeNodeFor(taxonomyCode, appLayerFactory);
			if (recordTreeNodesDataProvider != null) {
				break;
			}
		}

		if (recordTreeNodesDataProvider != null) {
			return new RecordLazyTreeDataProvider(recordTreeNodesDataProvider);

		} else {

			List<TreeNodesProvider<?>> nodesProvider = new ArrayList<>();

			nodesProvider.add(new LegacyTreeNodesDataProviderAdapter(
					new VisibleRecordTreeNodesDataProvider(taxonomyCode)));

			for (TreeNodeExtension treeNodeAppExtension : appLayerFactory.getExtensions()
					.forCollection(collection).treeNodeAppExtension) {
				List<TreeNodesProvider<?>> extraTreeNodeProviders = treeNodeAppExtension.getExtraTreeNodeProvider(taxonomyCode);
				if (extraTreeNodeProviders != null) {
					nodesProvider.addAll(extraTreeNodeProviders);
				}
			}

			TreeNodesProvider compositeNodesProvider = CompositeTreeNodeDataProvider.forNodesProvider(nodesProvider);
			return new DefaultLazyTreeDataProvider(compositeNodesProvider, taxonomyCode);

			//return new RecordLazyTreeDataProvider(new VisibleRecordTreeNodesDataProvider(taxonomyCode));
		}


	}
}
