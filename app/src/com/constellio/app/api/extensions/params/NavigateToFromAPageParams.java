package com.constellio.app.api.extensions.params;

import java.util.Map;

public class NavigateToFromAPageParams {
	private Map<String, String> params;
	private boolean isStructure;
	private String id = null;

	public NavigateToFromAPageParams(Map<String, String> params, String id) {
		this.id = id;
		this.params = params;
	}

	public NavigateToFromAPageParams(Map<String, String> params) {
		this.params = params;
		this.isStructure = false;
	}

	public NavigateToFromAPageParams(Map<String, String> params, boolean isStructure, String id) {
		this.params = params;
		this.isStructure = isStructure;
		this.id = id;
	}

	public NavigateToFromAPageParams(Map<String, String> params, boolean isStructure) {
		this.params = params;
		this.isStructure = isStructure;
	}

	public Map<String, String> getParams() {
		return params;
	}

	public boolean isStructure() {
		return isStructure;
	}

	public String getId() {
		return id;
	}
}

