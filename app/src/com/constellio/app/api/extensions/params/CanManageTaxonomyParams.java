package com.constellio.app.api.extensions.params;

import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.wrappers.User;

public class CanManageTaxonomyParams {
	private boolean defaultValue;
	private User user;
	private Taxonomy taxonomy;

	public CanManageTaxonomyParams(boolean defaultValue, User user, Taxonomy taxonomy) {
		this.defaultValue = defaultValue;
		this.user = user;
		this.taxonomy = taxonomy;
	}

	public boolean isDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(boolean defaultValue) {
		this.defaultValue = defaultValue;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Taxonomy getTaxonomy() {
		return taxonomy;
	}

	public void setTaxonomy(Taxonomy taxonomy) {
		this.taxonomy = taxonomy;
	}
}
