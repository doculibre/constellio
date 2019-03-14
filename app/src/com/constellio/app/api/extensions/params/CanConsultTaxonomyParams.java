package com.constellio.app.api.extensions.params;

import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.wrappers.User;

public class CanConsultTaxonomyParams {
	private boolean defaultValue;
	private User user;
	private Taxonomy taxonomy;

	public CanConsultTaxonomyParams(boolean defaultValue, User user, Taxonomy taxonomy) {
		this.defaultValue = defaultValue;
		this.user = user;
		this.taxonomy = taxonomy;
	}

	public boolean getDefaultValue() {
		return defaultValue;
	}

	public User getUser() {
		return user;
	}

	public Taxonomy getTaxonomy() {
		return taxonomy;
	}
}
