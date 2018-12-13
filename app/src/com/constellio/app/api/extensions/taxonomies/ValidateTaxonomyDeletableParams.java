package com.constellio.app.api.extensions.taxonomies;

import com.constellio.app.ui.pages.base.SessionContextProvider;
import com.constellio.model.entities.Taxonomy;

public class ValidateTaxonomyDeletableParams {
	Taxonomy taxonomy;
	private SessionContextProvider sessionContextProvider;

	public ValidateTaxonomyDeletableParams(Taxonomy taxonomy, SessionContextProvider sessionContextProvider) {
		this.taxonomy = taxonomy;
		this.sessionContextProvider = sessionContextProvider;
	}

	public Taxonomy getTaxonomy() {
		return taxonomy;
	}

	public SessionContextProvider getSessionContextProvider() {
		return sessionContextProvider;
	}
}
