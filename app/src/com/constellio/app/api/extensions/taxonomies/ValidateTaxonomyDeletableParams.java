package com.constellio.app.api.extensions.taxonomies;

import com.constellio.model.entities.Taxonomy;
import com.constellio.model.frameworks.validation.ValidationErrors;

public class ValidateTaxonomyDeletableParams {
	Taxonomy taxonomy;
	private ValidationErrors validationErrors;

	public ValidateTaxonomyDeletableParams(Taxonomy taxonomy, ValidationErrors validationErrors) {
		this.taxonomy = taxonomy;
		this.validationErrors = validationErrors;
	}

	public Taxonomy getTaxonomy() {
		return taxonomy;
	}

	public ValidationErrors getValidationErrors() {
		return validationErrors;
	}
}
