package com.constellio.app.ui.pages.management.shares;

import com.constellio.app.ui.entities.AuthorizationVO;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.viewGroups.AdminViewGroup;

public interface ShareManagementView extends BaseView, AdminViewGroup {

	void setSaveButton(boolean enabled);

	void addShare(AuthorizationVO authorization);

	void refreshView();
}