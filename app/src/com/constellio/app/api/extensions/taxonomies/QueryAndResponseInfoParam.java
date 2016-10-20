package com.constellio.app.api.extensions.taxonomies;

import com.constellio.model.entities.records.wrappers.SavedSearch;
import com.constellio.model.services.search.SPEQueryResponse;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.SolrQueryBuilderParams;
import org.joda.time.LocalDateTime;
import org.omg.CORBA.Object;

import javax.persistence.Tuple;

import java.util.HashMap;
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

    public String getCollection() {
        return savedSearch.getCollection();
    }

    public long getNumFound() {
        return speQueryResponse.getNumFound();
    }

    public long getQtime() {
        return speQueryResponse.getQtime();
    }

    public String getFrenchSolrQuery() {
        return  query.getCondition().getSolrQuery(new SolrQueryBuilderParams(true, "fr"));
    }

    public String getEnglishSolrQuery() {
        return  query.getCondition().getSolrQuery(new SolrQueryBuilderParams(true, "en"));
    }

    public String getSolrQuery(String language) {
        return  query.getCondition().getSolrQuery(new SolrQueryBuilderParams(true, language));
    }

    public Map<String, Object> getCriteria() {
        Map criteria = new HashMap<>();
//        criteria.put(savedSearch)
        return null;
    }
}
