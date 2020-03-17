package com.constellio.app.ui.framework.data.trees;

import com.constellio.app.ui.framework.data.TreeNode;
import lombok.Getter;

import java.io.Serializable;
import java.util.List;


public interface TreeNodesProvider<T extends Serializable> {

	TreeNodesProviderResponse<T> getNodes(String optionalParentId, int start, int maxSize, T fastContinuationInfos);

	class TreeNodesProviderResponse<T> {

		@Getter
		private long numFound;

		@Getter
		private List<TreeNode> nodes;

		@Getter
		private T fastContinuationInfos;

		public TreeNodesProviderResponse(long numFound, List<TreeNode> nodes, T fastContinuationInfos) {
			this.numFound = numFound;
			this.nodes = nodes;
			this.fastContinuationInfos = fastContinuationInfos;
		}

		public TreeNodesProviderResponse(long numFound, List<TreeNode> nodes) {
			this.numFound = numFound;
			this.nodes = nodes;
		}
	}

}
