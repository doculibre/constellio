package com.constellio.app.ui.framework.data.trees;

import com.constellio.app.ui.framework.data.TreeNode;
import com.constellio.app.ui.framework.data.trees.CompositeTreeNodeDataProvider.CompositeTreeNodeDataProviderFastContinuationInfos;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class CompositeTreeNodeDataProvider implements TreeNodesProvider<CompositeTreeNodeDataProviderFastContinuationInfos> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CompositeTreeNodeDataProvider.class);

	private List<TreeNodesProvider> childTreeNodesProviders;

	private static boolean DEBUG = true;

	public CompositeTreeNodeDataProvider(
			List<TreeNodesProvider<?>> childTreeNodesProviders) {
		this.childTreeNodesProviders = (List) childTreeNodesProviders;
	}

	public static TreeNodesProvider forNodesProvider(List<TreeNodesProvider<?>> nodesProvider) {
		if (nodesProvider.size() == 1) {
			return nodesProvider.get(0);
		} else {
			return new CompositeTreeNodeDataProvider(nodesProvider);
		}
	}

	@Override
	public boolean areNodesPossibleIn(TreeNode optionalParentTreeNode) {
		return childTreeNodesProviders.stream().anyMatch((p) -> p.areNodesPossibleIn(optionalParentTreeNode));
	}

	@Override
	public TreeNodesProviderResponse<CompositeTreeNodeDataProviderFastContinuationInfos> getNodes(
			TreeNode optionalParent, int start, int maxSize,
			CompositeTreeNodeDataProviderFastContinuationInfos fastContinuationInfos) {

		int continueAtProvider = 0;
		int continueAtProviderIndex = 0;
		Serializable currentFastContinuationInfos = null;

		if (fastContinuationInfos != null) {
			continueAtProvider = fastContinuationInfos.continueAtProvider;
			continueAtProviderIndex = fastContinuationInfos.continueAtProviderIndex;
			currentFastContinuationInfos = (Serializable) fastContinuationInfos.currentFastContinuationInfos;
		}

		List<TreeNode> nodes = new ArrayList<>();
		boolean hasMoreNodes = false;

		while (continueAtProvider < childTreeNodesProviders.size() && (nodes.size() < maxSize || !hasMoreNodes)) {
			int missingNodes = maxSize - nodes.size();
			TreeNodesProviderResponse<Serializable> response = TreeNodesProviderResponse.EMPTY();

			TreeNodesProvider childProvider = childTreeNodesProviders.get(continueAtProvider);
			try {
				if (childProvider.areNodesPossibleIn(optionalParent)) {
					response = childProvider.getNodes(optionalParent, continueAtProviderIndex, missingNodes,
							currentFastContinuationInfos);
					if (DEBUG) {
						StringBuilder sb = new StringBuilder();
						sb.append(childProvider.getClass().getName());
						sb.append("(").append(optionalParent == null ? "null" : optionalParent.getId());
						sb.append(",").append(continueAtProviderIndex);
						sb.append(",").append(missingNodes);
						sb.append(",").append(currentFastContinuationInfos).append(")");
						for (TreeNode treeNode : response.getNodes()) {
							sb.append("\n\t" + treeNode.getId() + "\t" + treeNode.getCaption());
						}
						LOGGER.info(sb.toString());
					}
					nodes.addAll(response.getNodes());
				}
			} catch (Throwable t) {
				LOGGER.warn("Child provider '" + childProvider.getClass() + "' has thrown an exception, continuing with next provider", t);
			}


			if (response.isMoreNodes()) {
				hasMoreNodes = true;
				if (missingNodes > 0) {
					continueAtProviderIndex += missingNodes;
					currentFastContinuationInfos = (Serializable) response.getFastContinuationInfos();
				}
			} else {
				continueAtProvider++;
				continueAtProviderIndex = 0;
				currentFastContinuationInfos = null;
			}
		}

		return new TreeNodesProviderResponse(hasMoreNodes, nodes, new CompositeTreeNodeDataProviderFastContinuationInfos(
				continueAtProvider, continueAtProviderIndex, currentFastContinuationInfos));
	}

	@AllArgsConstructor
	public static class CompositeTreeNodeDataProviderFastContinuationInfos implements Serializable {


		@Getter
		int continueAtProvider;

		@Getter
		int continueAtProviderIndex;

		@Getter
		Object currentFastContinuationInfos;

		@Override
		public boolean equals(Object o) {
			return EqualsBuilder.reflectionEquals(this, o);
		}

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this);
		}
	}

	public TreeNodesProvider<?> compositeOf(List<TreeNodesProvider<?>> dataProviders) {
		if (dataProviders.size() == 1) {
			return dataProviders.get(0);
		} else {
			return new CompositeTreeNodeDataProvider(dataProviders);
		}
	}

}
