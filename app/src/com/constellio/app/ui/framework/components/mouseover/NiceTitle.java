package com.constellio.app.ui.framework.components.mouseover;

import org.apache.commons.lang3.StringUtils;

import com.vaadin.server.AbstractExtension;
import com.vaadin.server.ClientConnector;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Component;

public class NiceTitle extends AbstractExtension {

	private Component component;

	private String title;

	private boolean visibleWhenDisabled;

	public NiceTitle(String title) {
		this(title, true);
	}

	public NiceTitle(String title, boolean visibleWhenDisabled) {
		this.title = title;
		this.visibleWhenDisabled = visibleWhenDisabled;
	}

	@Override
	public void beforeClientResponse(boolean initial) {
		super.beforeClientResponse(initial);
	}

	@Override
	public void setParent(ClientConnector parent) {
		if (this.component != null && parent == null) {
			// Removing extension
			((AbstractComponent) component).setDescription(null);
		}
		
		super.setParent(parent);
		this.component = (Component) parent;
		
		if (component != null) {
			boolean emptyTitle = StringUtils.isBlank(title) || "null".equals(title);
			boolean alreadyApplied = component.getDescription() != null && component.getDescription().equals(title);
			boolean enabledOrApplyIfDisabled = visibleWhenDisabled || component.isEnabled();
			if (component.isVisible() && !alreadyApplied && !emptyTitle && enabledOrApplyIfDisabled) {
				((AbstractComponent) component).setDescription(title);
			} 
		}
	}

	@Override
	public void detach() {
		try {
			if (visibleWhenDisabled && !component.isEnabled()) {
				((AbstractComponent) component).setDescription(null);
			}
		} finally {
			super.detach();
		}
	}

}
