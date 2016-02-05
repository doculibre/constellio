package com.constellio.app.ui.pages.management.taxonomy;

import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.viewGroups.AdminViewGroup;

public interface ListTaxonomyView extends BaseView, AdminViewGroup {
	void refreshTable();
}
