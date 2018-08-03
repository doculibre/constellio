package com.constellio.model.services.taxonomies;

import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;

import java.util.List;

public interface TaxonomiesSearchServices {

	public List<TaxonomySearchRecord> getVisibleRootConcept(User user, String collection, String taxonomyCode,
															TaxonomiesSearchOptions options);

	public List<TaxonomySearchRecord> getVisibleChildConcept(User user, String taxonomyCode, Record record,
															 TaxonomiesSearchOptions options);

	public boolean findNonTaxonomyRecordsInStructure(Record record, TaxonomiesSearchOptions options);

	public List<TaxonomySearchRecord> getLinkableRootConcept(User user, String collection, String taxonomyCode,
															 String selectedType, TaxonomiesSearchOptions options);

	public LinkableTaxonomySearchResponse getLinkableRootConceptResponse(User user, String collection,
																		 String usingTaxonomyCode,
																		 String selectedType,
																		 TaxonomiesSearchOptions options);

	public List<TaxonomySearchRecord> getLinkableChildConcept(User user, Record record, String usingTaxonomy,
															  String selectedType,
															  TaxonomiesSearchOptions options);

	public LinkableTaxonomySearchResponse getLinkableChildConceptResponse(User user, Record inRecord,
																		  String usingTaxonomy,
																		  String selectedType,
																		  TaxonomiesSearchOptions options);

	public List<TaxonomySearchRecord> getVisibleChildConcept(User user, Record record, TaxonomiesSearchOptions options);

	public String facetQueryFor(Record record);

	public LinkableTaxonomySearchResponse getVisibleChildConceptResponse(User user, String taxonomyCode, Record record,
																		 TaxonomiesSearchOptions options);

	public LinkableTaxonomySearchResponse getVisibleRootConceptResponse(User user, String collection,
																		String taxonomyCode,
																		TaxonomiesSearchOptions options,
																		String forSelectionOfSchemaType);

	public boolean isLinkable(final Record record, final Taxonomy taxonomy, TaxonomiesSearchOptions options);

	public boolean hasLinkableConceptInHierarchy(final Record concept, final Taxonomy taxonomy,
												 TaxonomiesSearchOptions options);

	TaxonomiesSearchServicesCache getCache();
}
