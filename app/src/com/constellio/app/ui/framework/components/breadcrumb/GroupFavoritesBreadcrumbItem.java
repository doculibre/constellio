package com.constellio.app.ui.framework.components.breadcrumb;

public class GroupFavoritesBreadcrumbItem implements BreadcrumbItem {
	private String favoriteGroupId;
	private String favoriteGroupTitle;

	public GroupFavoritesBreadcrumbItem(String favoriteGroupId, String favoriteGroupTitle) {
		this.favoriteGroupId = favoriteGroupId;
		this.favoriteGroupTitle = favoriteGroupTitle;
	}

	public String getFavoriteGroupId() {
		return favoriteGroupId;
	}

	@Override
	public String getLabel() {
		return favoriteGroupTitle;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
}
