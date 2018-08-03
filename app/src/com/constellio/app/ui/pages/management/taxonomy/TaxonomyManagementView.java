package com.constellio.app.ui.pages.management.taxonomy;

import com.constellio.app.api.extensions.taxonomies.TaxonomyManagementClassifiedType;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.viewGroups.AdminViewGroup;

import java.util.List;

public interface TaxonomyManagementView extends BaseView, AdminViewGroup {

	void setTabs(List<TaxonomyManagementClassifiedType> types);

	void refreshTable();
}
