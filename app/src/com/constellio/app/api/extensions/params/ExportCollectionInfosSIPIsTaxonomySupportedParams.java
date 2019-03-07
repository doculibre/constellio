package com.constellio.app.api.extensions.params;

import com.constellio.model.entities.Taxonomy;

public class ExportCollectionInfosSIPIsTaxonomySupportedParams {

	Taxonomy taxonomy;

	public ExportCollectionInfosSIPIsTaxonomySupportedParams(Taxonomy taxonomy) {
		this.taxonomy = taxonomy;
	}

	public Taxonomy getTaxonomy() {
		return taxonomy;
	}
}
