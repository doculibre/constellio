package com.constellio.app.modules.rm.services.menu.behaviors.util;

import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.search.AdvancedSearchViewImpl;
import com.constellio.app.ui.pages.search.SimpleSearchViewImpl;
import com.vaadin.navigator.View;
import com.vaadin.server.Page;

public class BehaviorsUtil {
	public static boolean reloadIfSearchView(BaseView baseView) {
		if (baseView instanceof SimpleSearchViewImpl || baseView instanceof AdvancedSearchViewImpl) {
			Page.getCurrent().reload();
			return true;
		} else {
			return false;
		}
	}

	public static boolean reloadIfWasSearchView(BaseView baseView) {
		View oldView = ConstellioUI.getCurrent().getViewChangeEvent().getOldView();
		if (oldView instanceof SimpleSearchViewImpl) {
			baseView.navigate().to().simpleSearchReplay(((SimpleSearchViewImpl) oldView).getSavedSearchId());
			return true;
		} else if (oldView instanceof AdvancedSearchViewImpl) {
			baseView.navigate().to().advancedSearchReplay(((AdvancedSearchViewImpl) oldView).getSavedSearchId());
			return true;
		} else {
			return false;
		}
	}
}
