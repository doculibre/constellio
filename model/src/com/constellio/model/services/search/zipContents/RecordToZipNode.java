package com.constellio.model.services.search.zipContents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RecordToZipNode {
	String recordId;
	String recordName;
	String uniqueNameInHierarchy;
	boolean canHaveChildren;
	RecordToZipNode parent;
	List<RecordToZipNode> children = new ArrayList<>();
	List<NodeContent> contents = new ArrayList<>();
	private Set<String> allChildrenNames = new HashSet<>();
	Set<String> redundantChildrenNames = new HashSet<>();
	private Set<String> allContentsNames = new HashSet<>();
	Set<String> redundantContentsNames = new HashSet<>();

	public RecordToZipNode(String recordId, String recordAbbreviation, String recordName, boolean canHaveChildren) {
		this.recordId = recordId;
		this.recordName = recordName;
		this.canHaveChildren = canHaveChildren;
		this.uniqueNameInHierarchy = recordName;

		if (recordAbbreviation == null) {

			if (recordName.length() > 60) {
				uniqueNameInHierarchy = recordName.substring(0, 37) + "..." + recordName.substring(recordName.length() - 20);
			}
		} else {
			uniqueNameInHierarchy = recordAbbreviation;
		}
	}

	public String getRecordId() {
		return recordId;
	}

	public String getRecordName() {
		return recordName;
	}

	public String getUniqueNameInHierarchy() {
		return uniqueNameInHierarchy;
	}

	public boolean isCanHaveChildren() {
		return canHaveChildren;
	}

	public RecordToZipNode getParent() {
		return parent;
	}

	public List<RecordToZipNode> getChildren() {
		return new ArrayList<>(Collections.unmodifiableCollection(children));
	}

	public List<NodeContent> getContents() {
		return new ArrayList<>(Collections.unmodifiableCollection(contents));
	}

	public void addContent(RelatedContent relatedContent) {
		NodeContent content = new NodeContent(this, relatedContent.getContainerId(), relatedContent.getContent());
		this.contents.add(content);
		String contentName = content.getContentName();
		if (this.allContentsNames.contains(contentName)) {
			this.redundantContentsNames.add(contentName);
		} else {
			this.allContentsNames.add(contentName);
		}
	}

	public void setParent(RecordToZipNode parent) {
		this.parent = parent;
	}

	public void addChild(RecordToZipNode childNode) {
		String childName = childNode.getRecordName();
		this.children.add(childNode);
		if (this.allChildrenNames.contains(childName)) {
			this.redundantChildrenNames.add(childName);
		} else {
			this.allChildrenNames.add(childName);
		}
	}

	public void setUniqueNameInHierarchy(Boolean withId) {
		if (withId) {
			this.uniqueNameInHierarchy = recordName + "(" + recordId + ")";
		} else {
			this.uniqueNameInHierarchy = recordName;
		}
	}

	public Set<String> getRedundantChildrenNames() {
		return redundantChildrenNames;
	}

	public Set<String> getRedundantContentsNames() {
		return redundantContentsNames;
	}
}
