package com.constellio.app.ui.framework.data;

import com.constellio.app.ui.framework.data.trees.LegacyTreeNodesDataProviderAdapter;
import com.constellio.data.utils.LangUtils;
import com.vaadin.server.Resource;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

public class TreeNode implements Serializable {
	@Getter
	private String id;

	@Getter
	private String providerType;

	@Getter
	private String nodeType;

	@Getter @Setter
	private String caption;

	@Getter @Setter
	private String description;

	@Getter
	private Resource collapsedIconFilename;

	@Getter
	private Resource expandedIconFilename;

	@Getter
	private boolean expandable;

	public TreeNode(String id, String providerType, String nodeType, String caption, String description,
					Resource collapsedIconFilename, Resource expandedIconFilename, boolean expandable) {

		if (id.contains("|") || id.equals("root")) {
			throw new IllegalArgumentException("Invalid tree node id : " + id);
		}
		this.id = id;
		this.providerType = providerType;
		this.nodeType = nodeType;
		this.caption = caption;
		this.description = description;
		this.expandable = expandable;
		this.collapsedIconFilename = collapsedIconFilename;
		this.expandedIconFilename = expandedIconFilename;
	}

	public Resource getIcon(boolean expanded) {
		return expanded ? expandedIconFilename : collapsedIconFilename;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		TreeNode that = (TreeNode) o;

		if (id != null ? !id.equals(that.id) : that.id != null) {
			return false;
		}

		return true;
	}

	public boolean isType(String providerType, String providerNodeType) {
		return LangUtils.isEqual(this.providerType, providerType) && LangUtils.isEqual(this.nodeType, providerNodeType);
	}

	public boolean isRecord() {
		return LegacyTreeNodesDataProviderAdapter.PROVIDER_ID.endsWith(providerType);
	}

	public boolean isProviderType(String providerType) {
		return LangUtils.isEqual(this.providerType, providerType);
	}

	@Override
	public int hashCode() {
		return id != null ? id.hashCode() : 0;
	}

	public static TreeNode namelessNode(String localId, String providerType, String nodeType) {
		return new TreeNode(localId, providerType, nodeType, null, null, null, null, false);
	}
}
