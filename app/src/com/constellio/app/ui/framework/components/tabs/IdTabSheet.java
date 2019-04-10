package com.constellio.app.ui.framework.components.tabs;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.vaadin.server.Resource;
import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;

public class IdTabSheet extends TabSheet {
	
	private Map<String, String> captions = new HashMap<>();
	
	private Map<String, String> badges = new HashMap<>();

	public IdTabSheet() {
		super();
		init();
	}

	public IdTabSheet(Component... components) {
		super(components);
		init();
	}
	
	private void init() {
		setTabCaptionsAsHtml(true);
	}

	@Override
	public Tab addTab(Component tabComponent, String caption, Resource icon, int position) {
		String tabId = tabComponent.getId();
		if (tabId == null) {
			throw new RuntimeException("Id is required for tab component at position " + position);
		}
		captions.put(tabId, caption);
		return super.addTab(tabComponent, caption, icon, position);
	}
	
	public Component getTabComponent(String tabId) {
		Component match = null;
		for (Iterator<Component> it = iterator(); it.hasNext();) {
			Component component = it.next();
			if (tabId.equals(component.getId())) {
				match = component;
				break;
			}
		}	
		return match;
	}
	
	public void setSelectedTab(String tabId) {
		Component tabComponent = getTabComponent(tabId);
		setSelectedTab(tabComponent);
	}
	
	public String getBadge(String tabId) {
		return badges.get(tabId);
	}
	
	public void setBadge(String tabId, String badge) {
		badges.put(tabId, badge);
		Component tabComponent = getTabComponent(tabId);
		Tab tab = getTab(tabComponent);
		String captionAndBadge = captions.get(tabId);
		if (StringUtils.isNotBlank(badge)) {
			captionAndBadge += " <span class=\"tab-badge\">" + badge + "</span>";
		}
		tab.setCaption(captionAndBadge);
	}

}
