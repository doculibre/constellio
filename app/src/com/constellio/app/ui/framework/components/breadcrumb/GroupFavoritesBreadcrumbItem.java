package com.constellio.app.ui.framework.components.breadcrumb;

import com.constellio.app.ui.i18n.i18n;

public class GroupFavoritesBreadcrumbItem implements BreadcrumbItem {
	private String favoriteGroupId;

	public GroupFavoritesBreadcrumbItem(String favoriteGroupId) {
		this.favoriteGroupId = favoriteGroupId;
	}

	public String getFavoriteGroupId() {
		return favoriteGroupId;
	}

	@Override
	public String getLabel() {
		return i18n.$("GroupFavoritesBreadcrumbItem.groupfavorites");
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
}
