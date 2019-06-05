package com.constellio.app.ui.framework.buttons;

import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.shared.ui.BorderStyle;
import com.vaadin.ui.Link;

public class BaseLink extends Link {

	public BaseLink() {
	}

	public BaseLink(String caption, Resource resource) {
		super(caption, resource);
	}

	public BaseLink(String caption, Resource resource, String targetName, int width, int height, BorderStyle border) {
		super(caption, resource, targetName, width, height, border);
	}

	@SuppressWarnings("deprecation")
	public void click() {
		Resource resource = getResource();
		if (resource != null) {
			Page.getCurrent().open(getResource(), null, false);
		}
	}

}
