package com.constellio.model.services.taxonomies.queryHandlers;

import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.taxonomies.LinkableTaxonomySearchResponse;
import com.constellio.model.services.taxonomies.TaxonomiesSearchOptions;
import com.constellio.model.services.taxonomies.TaxonomySearchRecord;

import java.util.List;

public interface TaxonomiesSearchServicesQueryHandler {


	LinkableTaxonomySearchResponse getLinkableConceptsForSelectionOfAPrincipalTaxonomyConceptBasedOnAuthorizations(
			GetChildrenContext ctx);

	LinkableTaxonomySearchResponse getLinkableConceptsForSelectionOfARecordUsingNonPrincipalTaxonomy(
			GetChildrenContext ctx);


	List<TaxonomySearchRecord> getVisibleChildConcept(GetChildrenContext ctx);


	LinkableTaxonomySearchResponse getVisibleChildrenRecords(GetChildrenContext ctx);

	LinkableTaxonomySearchResponse getVisibleRootConceptResponse(GetChildrenContext ctx);


	boolean isLinkable(final Record record, final Taxonomy taxonomy, TaxonomiesSearchOptions options);

	boolean findNonTaxonomyRecordsInStructure(Record record, TaxonomiesSearchOptions options);
}
