package com.constellio.app.ui.pages.trash;

import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.viewGroups.AdminViewGroup;

public interface TrashView extends BaseView, AdminViewGroup {
	String getSelectedType();
	void enableOrDisableActionButtons();
}
