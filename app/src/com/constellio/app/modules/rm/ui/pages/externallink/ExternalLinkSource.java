package com.constellio.app.modules.rm.ui.pages.externallink;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class ExternalLinkSource {
	private String source;
	private String caption;
	private Map<String, List<String>> tabs;

	public ExternalLinkSource(String source, String caption) {
		this.source = source;
		this.caption = caption;

		tabs = new HashMap<>();
	}

	public void addTab(String caption, List<String> types) {
		tabs.put(caption, types);
	}

	public Set<Entry<String, List<String>>> getTabs() {
		return tabs.entrySet();
	}

	public String getSource() {
		return source;
	}

	public String getCaption() {
		return caption;
	}
}
