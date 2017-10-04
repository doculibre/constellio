package com.constellio.model.services.taxonomies;

/**
 * Created by francisbaril on 2017-02-18.
 */
public class TaxonomiesSearchFilter {

	LinkableConceptFilter linkableConceptsFilter;

	//    public LogicalSearchCondition getLinkableConceptsCondition() {
	//        return linkableConceptsCondition;
	//    }
	//
	//    public void setLinkableConceptsCondition(LogicalSearchCondition linkableConceptsCondition) {
	//        this.linkableConceptsCondition = linkableConceptsCondition;
	//    }

	public LinkableConceptFilter getLinkableConceptsFilter() {
		return linkableConceptsFilter;
	}

	public TaxonomiesSearchFilter setLinkableConceptsFilter(
			LinkableConceptFilter linkableConceptsFilter) {
		this.linkableConceptsFilter = linkableConceptsFilter;
		return this;
	}
}
