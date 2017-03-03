package com.constellio.model.services.taxonomies;

import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.allConditions;

/**
 * Created by francisbaril on 2017-02-18.
 */
public class TaxonomiesSearchFilter {

    LogicalSearchCondition linkableConceptsCondition;

    public LogicalSearchCondition getLinkableConceptsCondition() {
        return linkableConceptsCondition;
    }

    public void setLinkableConceptsCondition(LogicalSearchCondition linkableConceptsCondition) {
        this.linkableConceptsCondition = linkableConceptsCondition;
    }
}
