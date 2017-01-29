package com.constellio.app.ui.framework.data;

import java.io.Serializable;

import com.vaadin.server.Resource;

public class RecordDataTreeNode implements Serializable {

	String id;

	String caption;

	String description;

	String schemaType;

	Resource collapsedIconFilename;
	Resource expandedIconFilename;

	boolean hasChildren;

	public RecordDataTreeNode(String id, String caption, String description, String schemaType,
			Resource collapsedIconFilename, Resource expandedIconFilename, boolean hasChildren) {
		this.id = id;
		this.caption = caption;
		this.description = description;
		this.hasChildren = hasChildren;
		this.schemaType = schemaType;
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

	public boolean hasChildren() {
		return hasChildren;
	}

	public String getSchemaType() {
		return schemaType;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		RecordDataTreeNode that = (RecordDataTreeNode) o;

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
