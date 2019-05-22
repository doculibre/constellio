package com.constellio.app.ui.framework.components.breadcrumb;

import com.constellio.app.ui.i18n.i18n;

public class FavoritesBreadcrumbItem implements BreadcrumbItem {
	@Override
	public String getLabel() {
		return i18n.$("ViewGroup.CartViewGroup");
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
}
