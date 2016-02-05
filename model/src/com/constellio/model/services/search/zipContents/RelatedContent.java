package com.constellio.model.services.search.zipContents;

import com.constellio.model.entities.records.Content;

public class RelatedContent {
	Content content;
	String containerPrincipalPath;
	String containerId;

	public RelatedContent(Content content, String containerPrincipalPath, String containerId) {
		this.content = content;
		this.containerPrincipalPath = containerPrincipalPath;
		this.containerId = containerId;
	}

	public Content getContent() {
		return content;
	}

	public String getContainerPrincipalPath() {
		return containerPrincipalPath;
	}

	public String getContainerId() {
		return containerId;
	}
}
