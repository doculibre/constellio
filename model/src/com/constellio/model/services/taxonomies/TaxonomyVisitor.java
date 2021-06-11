package com.constellio.model.services.taxonomies;

import com.constellio.model.services.taxonomies.CacheBasedTaxonomyVisitingServices.VisitedTaxonomyRecord;

public interface TaxonomyVisitor {

	TaxonomyVisitingStatus visit(VisitedTaxonomyRecord item);

}
