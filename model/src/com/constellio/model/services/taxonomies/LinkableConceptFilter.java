package com.constellio.model.services.taxonomies;

import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;

public interface LinkableConceptFilter {

	boolean isLinkable(LinkableConceptFilterParams params);

	interface LinkableConceptFilterParams {

		Record getRecord();

		Taxonomy getTaxonomy();

	}
}
