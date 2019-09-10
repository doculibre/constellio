package com.constellio.app.modules.rm.ui.pages.personalspace;

import com.constellio.app.entities.navigation.NavigationItem;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.entities.records.wrappers.User;

import java.util.List;

public class PersonnalSpacePresenter extends BasePresenter<PersonnalSpaceView> {
	public PersonnalSpacePresenter(PersonnalSpaceView view) {
		super(view);
	}


	public List<NavigationItem> getPersonnalSpaceItems() {
		return navigationConfig().getNavigation(PersonnalSpaceView.PERSONAL_SPACE);
	}

	public boolean isThereAtleastOneButtonVisible() {
		return isAnyItemEnabled(getPersonnalSpaceItems());
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return isThereAtleastOneButtonVisible();
	}

	private boolean isAnyItemEnabled(List<NavigationItem> items) {
		for (NavigationItem item : items) {
			if (getStateFor(item).isEnabled()) {
				return true;
			}
		}
		return false;
	}

}
