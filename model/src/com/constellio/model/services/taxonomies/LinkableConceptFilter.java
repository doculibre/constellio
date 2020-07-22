package com.constellio.model.services.taxonomies;

import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import lombok.AllArgsConstructor;
import lombok.Getter;

public interface LinkableConceptFilter {

	boolean isLinkable(LinkableConceptFilterParams params);

	@AllArgsConstructor
	class LinkableConceptFilterParams {

		@Getter
		Record record;

		@Getter
		Taxonomy taxonomy;

	}
}
