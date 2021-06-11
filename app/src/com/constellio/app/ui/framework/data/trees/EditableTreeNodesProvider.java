package com.constellio.app.ui.framework.data.trees;

import com.constellio.app.ui.framework.data.TreeNode;

import java.io.Serializable;
import java.util.List;

public interface EditableTreeNodesProvider<T extends Serializable> extends TreeNodesProvider<T> {

	void addNode(TreeNode optionalParentTreeNode, TreeNode treeNodeToAdd, int position) throws Exception;

	void updateNode(TreeNode treeNodeToUpdate) throws Exception;

	void removeNode(TreeNode optionalParentTreeNode, TreeNode treeNodeToRemove) throws Exception;

	void removeNode(TreeNode optionalParentTreeNode, String treeNodeId) throws Exception;

	List<String> getSupportedNodeTypes(TreeNode parentTreeNode);

}