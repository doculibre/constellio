package com.constellio.app.ui.framework.buttons;

import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.ui.themes.ValoTheme;

public class BaseLink extends BaseButton implements Clickable {

	private Resource resource;

	private String targetName;

	public BaseLink() {
		init();
	}

	public BaseLink(String caption, Resource resource) {
		super(caption);
		setResource(resource);
		init();
	}

	private void init() {
		addStyleName(ValoTheme.BUTTON_LINK);
	}

	public Resource getResource() {
		return resource;
	}

	public void setResource(Resource resource) {
		this.resource = resource;
	}

	public String getTargetName() {
		return targetName;
	}

	public void setTargetName(String targetName) {
		this.targetName = targetName;
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void buttonClick(ClickEvent event) {
		Resource resource = getResource();
		if (resource != null) {
			Page.getCurrent().open(resource, getTargetName(), false);
		}
	}

}
