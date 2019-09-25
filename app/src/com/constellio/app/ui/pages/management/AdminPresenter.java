package com.constellio.app.ui.pages.management;

import com.constellio.app.entities.navigation.NavigationItem;
import com.constellio.app.entities.navigation.NavigationItem.BaseNavigationItem;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.entities.records.wrappers.User;

import java.util.List;

public class AdminPresenter extends BasePresenter<AdminView> {

	public AdminPresenter(AdminView view) {
		super(view);
	}

	public List<NavigationItem> getSystemItems() {
		return navigationConfig().getNavigation(AdminView.SYSTEM_SECTION);
	}

	public List<NavigationItem> getCollectionItems() {
		return navigationConfig().getNavigation(AdminView.COLLECTION_SECTION);
	}

	public boolean isSystemSectionVisible() {
		return isAnyItemEnabled(getSystemItems());
	}

	public boolean isCollectionSectionVisible() {
		return isAnyItemEnabled(getCollectionItems());
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return isSystemSectionVisible() || isCollectionSectionVisible();
	}

	private boolean isAnyItemEnabled(List<NavigationItem> items) {
		for (NavigationItem item : items) {
			if (item instanceof BaseNavigationItem && ((BaseNavigationItem) item).urlNeedToEndWith() != null) {
				continue;
			}

			if (getStateFor(item).isEnabled()) {
				return true;
			}
		}
		return false;
	}
}
