package com.constellio.app.api.extensions;

import com.constellio.app.api.extensions.params.DecorateMainComponentAfterInitExtensionParams;
import com.constellio.app.api.extensions.params.PagesComponentsExtensionParams;
import com.constellio.app.api.extensions.params.UpdateComponentExtensionParams;
import com.constellio.app.ui.pages.search.criteria.Criterion;
import com.vaadin.ui.Component;

public class SearchCriterionExtension {

	public Component getComponentForCriterion(Criterion criterion) {
		return null;
	}
}
