package com.constellio.app.api.extensions.taxonomies;

import com.constellio.app.ui.pages.search.criteria.Criterion;
import com.constellio.model.entities.records.wrappers.SavedSearch;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.search.SPEQueryResponse;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.SolrQueryBuilderParams;
import org.joda.time.LocalDateTime;
import org.omg.CORBA.Object;

import javax.persistence.Tuple;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

/**
 * Created by Constelio on 2016-10-19.
 */
public class QueryAndResponseInfoParam {

    public SPEQueryResponse speQueryResponse;

    public LogicalSearchQuery query;

    public SavedSearch savedSearch;

    public LocalDateTime queryDateTime;

    public String language;

    public String userCode;

    public SPEQueryResponse getSpeQueryResponse() {
        return speQueryResponse;
    }

    public QueryAndResponseInfoParam setSpeQueryResponse(SPEQueryResponse speQueryResponse) {
        this.speQueryResponse = speQueryResponse;
        return this;
    }

    public LogicalSearchQuery getQuery() {
        return query;
    }

    public QueryAndResponseInfoParam setQuery(LogicalSearchQuery query) {
        this.query = query;
        return this;
    }

    public LocalDateTime getQueryDateTime() {
        return queryDateTime;
    }

    public QueryAndResponseInfoParam setQueryDateTime(LocalDateTime queryDateTime) {
        this.queryDateTime = queryDateTime;
        return this;
    }

    public SavedSearch getSavedSearch() {
        return savedSearch;
    }

    public QueryAndResponseInfoParam setSavedSearch(SavedSearch savedSearch) {
        this.savedSearch = savedSearch;
        return this;
    }

    public String getUserID() {
        return savedSearch.getUser();
    }

    public String getUserCode() {
        return userCode;
    }

    public QueryAndResponseInfoParam setUserCode(String userCode) {
        this.userCode = userCode;
        return this;
    }

    public String getCollection() {
        return savedSearch.getCollection();
    }

    public long getNumFound() {
        return speQueryResponse.getNumFound();
    }

    public long getQtime() {
        return speQueryResponse.getQtime();
    }

    public String getSolrQuery() {
        return  query.getCondition().getSolrQuery(new SolrQueryBuilderParams(true, language));
    }

    public List<Criterion> getCriterionList() {
        return savedSearch.getAdvancedSearch();
    }

    public String getCriterionListAsString() {
        List<Criterion> criterionList = getCriterionList();
        StringBuilder sb = new StringBuilder();
        for(Criterion criterion: criterionList) {
            sb.append(convertCriterionToString(criterion));
        }
        return sb.toString();
    }

    private String convertCriterionToString(Criterion criterion) {
        StringBuilder sb = new StringBuilder();
        if(criterion.isLeftParens()) {
            sb.append("(");
        }
        sb.append(new SchemaUtils().getLocalCodeFromMetadataCode(criterion.getMetadataCode()));
        sb.append(" " + criterion.getSearchOperator().toString() + " ");
        sb.append(criterion.getValue());
        if(criterion.isRightParens()) {
            sb.append(")");
        }
        sb.append(" " + criterion.getBooleanOperator().toString() + " ");
        return  sb.toString();
    }

    public String getLanguage() {
        return language;
    }

    public QueryAndResponseInfoParam setLanguage(String language) {
        this.language = language;
        return this;
    }
}
