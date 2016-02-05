package com.constellio.app.ui.pages.user;

import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.viewGroups.AdminViewGroup;

public interface DisplayUserCredentialView extends BaseView, AdminViewGroup {

	void refreshTable();
}
