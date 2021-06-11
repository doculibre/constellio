package com.constellio.app.ui.pages.management.taxonomy;

import com.constellio.app.ui.framework.components.RecordForm;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.viewGroups.AdminViewGroup;

public interface AddEditTaxonomyConceptView extends BaseView, AdminViewGroup {
	RecordForm getForm();
}
