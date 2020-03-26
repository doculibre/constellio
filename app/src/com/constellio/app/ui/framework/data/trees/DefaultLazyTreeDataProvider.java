package com.constellio.app.ui.framework.data.trees;

import com.constellio.app.ui.framework.data.AbstractDataProvider;
import com.constellio.app.ui.framework.data.LazyTreeDataProvider;
import com.constellio.app.ui.framework.data.ObjectsResponse;
import com.constellio.app.ui.framework.data.TreeNode;
import com.constellio.app.ui.framework.data.trees.TreeNodesProvider.TreeNodesProviderResponse;
import com.vaadin.server.Resource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultLazyTreeDataProvider extends AbstractDataProvider implements LazyTreeDataProvider<String> {

	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultLazyTreeDataProvider.class);

	private Map<String, TreeNode> nodesCache = new HashMap<>();
	private Map<String, Integer> estimatedChildrenCountMap = new HashMap<>();
	private Map<String, Serializable> fastContinuationInfosMap = new HashMap<>();

	private TreeNodesProvider<Serializable> nodesProvider;
	private String taxonomyCode;

	public DefaultLazyTreeDataProvider(TreeNodesProvider<? extends Serializable> nodesProvider, String taxonomyCode) {
		this.nodesProvider = (TreeNodesProvider<Serializable>) nodesProvider;
		this.taxonomyCode = taxonomyCode;
	}

	@Override
	public final String getTaxonomyCode() {
		return taxonomyCode;
	}

	TreeNode getNode(String id) {
		return nodesCache.get(id);
	}

	@Override
	public final String getCaption(String id) {
		TreeNode treeNode = getNode(id);
		return treeNode == null ? "" : treeNode.getCaption();
	}

	@Override
	public final String getDescription(String id) {
		TreeNode treeNode = getNode(id);
		return treeNode == null ? null : treeNode.getDescription();
	}

	@Override
	public final Resource getIcon(String id, boolean expanded) {
		TreeNode treeNode = getNode(id);
		return treeNode == null ? null : treeNode.getIcon(expanded);
	}

	@Override
	public final ObjectsResponse<String> getRootObjects(int start, int maxSize) {
		Object previousFastContinueInfos = fastContinuationInfosMap.get("root@" + start);
		TreeNodesProviderResponse<Serializable> response =
				nodesProvider.getNodes(null, start, maxSize, (Serializable) previousFastContinueInfos);

		List<String> recordIds = new ArrayList<>();
		for (TreeNode treeNode : response.getNodes()) {
			String uniqueId = nodeUniqueId(null, treeNode);
			this.nodesCache.put(uniqueId, treeNode);
			recordIds.add(uniqueId);
		}
		int numFound = start + response.getNodes().size();
		if (response.isMoreNodes()) {
			numFound++;
		}
		updateParentEstimatedChildrenCount("root", numFound);

		int end = start + maxSize;
		Serializable nextFastContinueInfos = response.getFastContinuationInfos();
		if (nextFastContinueInfos != null) {
			fastContinuationInfosMap.put("root@" + end, nextFastContinueInfos);
		}

		return new ObjectsResponse<>(recordIds, (long) numFound);
	}

	@Override
	public final String getParent(String child) {
		if (child.contains("|")) {
			return StringUtils.substringBeforeLast(child, "|");
		} else {
			return null;
		}
	}

	/**
	 * Create a unique id, storing the node's local id, provider type and node type and it's parent unique id
	 */
	String nodeUniqueId(String parentUniqueId, TreeNode node) {
		//We could use a map to have shorter names
		if (parentUniqueId == null) {
			return node.getProviderType() + ":" + node.getNodeType() + ":" + node.getId();
		} else {
			return parentUniqueId + "|" + node.getProviderType() + ":" + node.getNodeType() + ":" + node.getId();
		}
	}

	public static TreeNode toTreeNodeSupportingLegacyProviders(String nodePath) {
		if (nodePath.contains(":")) {
			return toTreeNode(nodePath);
		} else {
			return TreeNode.namelessNode(nodePath, LegacyTreeNodesDataProviderAdapter.PROVIDER_ID, null);
		}
	}

	public static TreeNode toTreeNode(String nodePath) {
		String[] nodeParts;
		if (nodePath.contains("|")) {
			String nodePart = StringUtils.substringAfterLast(nodePath, "|");
			nodeParts = nodePart.split(":");
		} else {
			nodeParts = nodePath.split(":");
		}

		return TreeNode.namelessNode(nodeParts[2], nodeParts[0], nodeParts[1]);
	}

	@Override
	public ObjectsResponse<String> getChildren(String parent, int start, int maxSize) {

		Serializable previousFastContinueInfos = fastContinuationInfosMap.get(parent + "@" + start);

		//This parent tree node has no label, description or icon
		TreeNode parentTreeNode = toTreeNode(parent);
		TreeNodesProviderResponse<Serializable> response = this.nodesProvider.getNodes(parentTreeNode, start, maxSize, previousFastContinueInfos);

		List<String> recordIds = new ArrayList<>();
		for (TreeNode treeNode : response.getNodes()) {
			String uniqueId = nodeUniqueId(parent, treeNode);
			nodesCache.put(uniqueId, treeNode);
			recordIds.add(uniqueId);
		}
		//LOGGER.info("getChildren(" + parent + ", " + start + ", " + maxSize + ") => " + recordIds);

		int numFound = start + response.getNodes().size();
		if (response.isMoreNodes()) {
			numFound++;
		}
		updateParentEstimatedChildrenCount(parent, numFound);

		int end = start + maxSize;
		Serializable nextFastContinueInfos = response.getFastContinuationInfos();
		if (nextFastContinueInfos != null) {
			fastContinuationInfosMap.put(parent + "@" + end, nextFastContinueInfos);
		}


		return new ObjectsResponse<>(recordIds, (long) numFound);
	}

	private void updateParentEstimatedChildrenCount(String parent, long numfound) {
		Integer estimatedChildrenCount = estimatedChildrenCountMap.get(parent);
		if (estimatedChildrenCount == null) {
			estimatedChildrenCount = 0;
		}
		estimatedChildrenCount = Math.max(estimatedChildrenCount, (int) numfound);
		estimatedChildrenCountMap.put(parent, estimatedChildrenCount);
	}

	@Override
	public boolean hasChildren(String parent) {
		return getNode(parent).isExpandable();
	}

	@Override
	public boolean isLeaf(String parent) {
		return !getNode(parent).isExpandable();
	}

	@Override
	public final int getEstimatedRootNodesCount() {
		return getEstimatedChildrenNodesCount("root");
	}

	@Override
	public final int getEstimatedChildrenNodesCount(String id) {
		Integer value = estimatedChildrenCountMap.get(id);
		return value == null ? -1 : value;
	}

	//	protected RecordDataTreeNode toTreeNode(TaxonomySearchRecord searchRecord) {
	//		Record record = searchRecord.getRecord();
	//		String schemaType = new SchemaUtils().getSchemaTypeCode(record.getSchemaCode());
	//		String caption = SchemaCaptionUtils.getCaptionForRecord(record, i18n.getLocale());
	//		String description = record.get(Schemas.DESCRIPTION_STRING, NO_SUMMARY_METADATA_VALIDATION);
	//		if (description == null) {
	//			description = record.get(Schemas.DESCRIPTION_TEXT, NO_SUMMARY_METADATA_VALIDATION);
	//		}
	//
	//		Resource collapsedIcon = getCollapsedIconOf(record);
	//		Resource expandedIcon = getExpandedIconOf(record);
	//
	//		return new RecordDataTreeNode(searchRecord.getId(), caption, description, schemaType,
	//				collapsedIcon, expandedIcon, searchRecord.hasChildren());
	//	}

}
