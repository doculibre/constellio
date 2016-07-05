package com.constellio.app.ui.pages.management.ldap;

import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.viewGroups.AdminViewGroup;
import com.constellio.model.conf.ldap.LDAPDirectoryType;

public interface LDAPConfigManagementView extends BaseView, AdminViewGroup {
	void updateComponents();
}
