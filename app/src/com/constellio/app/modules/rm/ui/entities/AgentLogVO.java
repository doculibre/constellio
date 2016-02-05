package com.constellio.app.modules.rm.ui.entities;

import java.io.Serializable;

public class AgentLogVO implements Serializable {
	
	private String username;
	
	private String filename;
	
	public AgentLogVO(String username, String filename) {
		this.username = username;
		this.filename = filename;
	}

	public final String getUsername() {
		return username;
	}

	public final String getFilename() {
		return filename;
	}
	
}
