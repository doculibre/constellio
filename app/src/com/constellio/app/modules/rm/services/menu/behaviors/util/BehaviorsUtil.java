package com.constellio.app.modules.rm.services.menu.behaviors.util;

import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.search.AdvancedSearchViewImpl;
import com.constellio.app.ui.pages.search.SimpleSearchViewImpl;
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
}
