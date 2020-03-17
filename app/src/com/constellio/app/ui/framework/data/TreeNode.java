package com.constellio.app.ui.framework.data;

import com.vaadin.server.Resource;

import java.io.Serializable;

public class TreeNode implements Serializable {
	private String id;

	private String type;

	private String caption;

	private String description;

	private Resource collapsedIconFilename;

	private Resource expandedIconFilename;

	private boolean expandable;

	public TreeNode(String id, String type, String caption, String description,
					Resource collapsedIconFilename, Resource expandedIconFilename, boolean expandable) {

		if (id.contains("|") || id.equals("root")) {
			throw new IllegalArgumentException("Invalid tree node id : " + id);
		}
		this.id = id;
		this.type = type;
		this.caption = caption;
		this.description = description;
		this.expandable = expandable;
		this.collapsedIconFilename = collapsedIconFilename;
		this.expandedIconFilename = expandedIconFilename;
	}

	public String getId() {
		return id;
	}

	public Resource getIcon(boolean expanded) {
		return expanded ? expandedIconFilename : collapsedIconFilename;
	}

	public String getCaption() {
		return caption;
	}

	public String getDescription() {
		return description;
	}

	public boolean expandable() {
		return expandable;
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

	@Override
	public int hashCode() {
		return id != null ? id.hashCode() : 0;
	}


}
