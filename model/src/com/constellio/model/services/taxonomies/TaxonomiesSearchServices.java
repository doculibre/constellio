package com.constellio.model.services.taxonomies;

import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;

import java.util.List;

public interface TaxonomiesSearchServices {

	List<TaxonomySearchRecord> getVisibleRootConcept(User user, String collection, String taxonomyCode,
													 TaxonomiesSearchOptions options);

	List<TaxonomySearchRecord> getVisibleChildConcept(User user, String taxonomyCode, Record record,
													  TaxonomiesSearchOptions options);

	boolean findNonTaxonomyRecordsInStructure(Record record, TaxonomiesSearchOptions options);

	List<TaxonomySearchRecord> getLinkableRootConcept(User user, String collection, String taxonomyCode,
													  String selectedType, TaxonomiesSearchOptions options);

	LinkableTaxonomySearchResponse getLinkableRootConceptResponse(User user, String collection,
																  String usingTaxonomyCode,
																  String selectedType,
																  TaxonomiesSearchOptions options);

	List<TaxonomySearchRecord> getLinkableChildConcept(User user, Record record, String usingTaxonomy,
													   String selectedType,
													   TaxonomiesSearchOptions options);

	LinkableTaxonomySearchResponse getLinkableChildConceptResponse(User user, Record inRecord,
																   String usingTaxonomy,
																   String selectedType,
																   TaxonomiesSearchOptions options);

	List<TaxonomySearchRecord> getVisibleChildConcept(User user, Record record, TaxonomiesSearchOptions options);

	LinkableTaxonomySearchResponse getVisibleChildConceptResponse(User user, String taxonomyCode, Record record,
																  TaxonomiesSearchOptions options);

	LinkableTaxonomySearchResponse getVisibleRootConceptResponse(User user, String collection,
																 String taxonomyCode,
																 TaxonomiesSearchOptions options,
																 String forSelectionOfSchemaType);

	boolean isLinkable(final Record record, final Taxonomy taxonomy, TaxonomiesSearchOptions options);


	TaxonomiesSearchServicesCache getCache();
}
