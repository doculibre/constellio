package com.constellio.app.ui.framework.components.breadcrumb;

import com.constellio.app.ui.application.Navigation;

public abstract class IntermediateBreadCrumbTailItem implements BreadcrumbItem {

	public abstract String getTitle();

	@Override
	public String getLabel() {
		return getTitle();
	}

	public abstract void activate(Navigation navigate);

}
