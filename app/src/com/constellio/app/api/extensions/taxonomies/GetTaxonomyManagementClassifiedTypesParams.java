package com.constellio.app.api.extensions.taxonomies;

import com.constellio.app.ui.pages.base.SessionContextProvider;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;

public class GetTaxonomyManagementClassifiedTypesParams {

	Taxonomy taxonomy;

	Record record;
	private SessionContextProvider sessionContextProvider;

	public GetTaxonomyManagementClassifiedTypesParams(Taxonomy taxonomy, Record record,
			SessionContextProvider sessionContextProvider) {
		this.taxonomy = taxonomy;
		this.record = record;
		this.sessionContextProvider = sessionContextProvider;
	}

	public Taxonomy getTaxonomy() {
		return taxonomy;
	}

	public Record getRecord() {
		return record;
	}

	public boolean isTaxonomy(String code) {
		return code.equals(taxonomy.getCode());
	}

	public SessionContextProvider getSessionContextProvider() {
		return sessionContextProvider;
	}
}

