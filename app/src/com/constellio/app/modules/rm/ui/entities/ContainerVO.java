package com.constellio.app.modules.rm.ui.entities;

import java.io.Serializable;

public class ContainerVO implements Serializable {
	private final String id;
	private final String caption;

	public ContainerVO(String id, String caption) {
		this.id = id;
		this.caption = caption;
	}

	public String getId() {
		return id;
	}

	public String getCaption() {
		return caption;
	}
}
