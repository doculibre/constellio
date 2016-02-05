package com.constellio.app.ui.pages.management.permissions;

import com.constellio.app.ui.entities.RoleVO;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.viewGroups.AdminViewGroup;

public interface PermissionsManagementView extends BaseView, AdminViewGroup {

	void setSaveAndRevertButtonStatus(boolean enabled);

	void addRole(RoleVO role);

	void refreshView();
}
