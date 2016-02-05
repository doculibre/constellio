package com.constellio.app.ui.pages.management.facet;

import java.util.List;

import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.management.facet.fields.FacetConfigurationForm;

public interface AddEditFacetConfigurationView extends BaseView {
	void displayInvalidQuery(List<Integer> invalids);

	FacetConfigurationForm getForm();
}
