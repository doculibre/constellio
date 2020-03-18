package com.constellio.app.ui.framework.data.trees;

import com.constellio.app.ui.framework.data.TreeNode;
import com.constellio.app.ui.framework.data.trees.CompositeTreeNodeDataProvider.CompositeTreeNodeDataProviderFastContinuationInfos;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class CompositeTreeNodeDataProvider implements TreeNodesProvider<CompositeTreeNodeDataProviderFastContinuationInfos> {

	private List<TreeNodesProvider> childTreeNodesProviders;

	public CompositeTreeNodeDataProvider(
			List<TreeNodesProvider<?>> childTreeNodesProviders) {
		this.childTreeNodesProviders = (List) childTreeNodesProviders;
	}

	@Override
	public TreeNodesProviderResponse<CompositeTreeNodeDataProviderFastContinuationInfos> getNodes(
			String optionalParentId, int start, int maxSize,
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
			TreeNodesProviderResponse<Object> response = childTreeNodesProviders.get(continueAtProvider)
					.getNodes(optionalParentId, continueAtProviderIndex, missingNodes, currentFastContinuationInfos);
			nodes.addAll(response.getNodes());
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
