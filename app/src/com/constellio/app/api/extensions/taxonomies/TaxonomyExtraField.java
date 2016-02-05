package com.constellio.app.api.extensions.taxonomies;

import com.vaadin.ui.Component;

public interface TaxonomyExtraField {

	String getCode();

	String getLabel();

	Component buildComponent();
}
