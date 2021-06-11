package com.constellio.app.ui.util;

import com.constellio.app.modules.rm.ui.pages.document.DisplayDocumentView;
import com.constellio.app.modules.rm.ui.pages.folder.DisplayFolderView;
import com.constellio.app.services.menu.behavior.MenuItemActionBehaviorParams;
import com.constellio.app.ui.pages.base.BaseView;

public class ViewUtils {

	public static void baseViewRefresh(MenuItemActionBehaviorParams params) {
		BaseView view = params.getView();
		baseViewRefresh(view);
	}

	public static void baseViewRefresh(BaseView view) {
		if (view instanceof DisplayDocumentView || view instanceof DisplayFolderView) {
			view.refreshActionMenu();
			view.partialRefresh();
		} else {
			view.updateUI();
		}
	}
}
