package com.constellio.app.ui.pages.management.facet;

import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.management.facet.fields.FacetConfigurationForm;

import java.util.List;

public interface AddEditFacetConfigurationView extends BaseView {
	void displayInvalidQuery(List<Integer> invalids);

	FacetConfigurationForm getForm();
}
