package com.constellio.app.modules.rm.ui.entities;

import java.io.Serializable;

public class ContainerVO implements Serializable {
	private final String id;
	private final String caption;
	private final Double availableSize;

	public ContainerVO(String id, String caption, Double availableSize) {
		this.id = id;
		this.caption = caption;
		this.availableSize = availableSize;
	}

	public String getId() {
		return id;
	}

	public String getCaption() {
		return caption;
	}

	public Double getAvailableSize() {
		return availableSize;
	}
}
