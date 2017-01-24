package com.constellio.app.ui.framework.components.breadcrumb;

public class DefaultBreadcrumbItem implements BreadcrumbItem {
	
	private String label;
	
	private boolean enabled;

	public DefaultBreadcrumbItem(String label, boolean enabled) {
		this.label = label;
		this.enabled = enabled;
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

}
