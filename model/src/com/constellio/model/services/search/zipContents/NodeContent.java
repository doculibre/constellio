package com.constellio.model.services.search.zipContents;

import org.apache.commons.lang.StringUtils;

import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.ContentVersion;

public class NodeContent {
	RecordToZipNode parent;
	String containerId;
	String uniqueName;
	Content content;

	public NodeContent(RecordToZipNode parent, String containerId, Content content) {
		this.parent = parent;
		this.containerId = containerId;
		this.content = content;
		this.uniqueName = getContentName();
	}

	public String getContentName() {
		return content.getCurrentVersion().getFilename();
	}

	public void rename(String countOrRecordId) {
		String currentUniqueName = this.uniqueName;
		String titleWithoutExtension = currentUniqueName;
		String extension = "";
		if (currentUniqueName.contains(".")) {
			titleWithoutExtension = StringUtils.substringBeforeLast(currentUniqueName, ".");
			extension = "." + StringUtils.substringAfterLast(currentUniqueName, ".");
		}
		this.uniqueName = titleWithoutExtension + "(" + countOrRecordId + ")" + extension;
	}

	public String getUniqueName() {
		return uniqueName;
	}

	public ContentVersion getContentCurrentVersion() {
		return this.content.getCurrentVersion();
	}
}
