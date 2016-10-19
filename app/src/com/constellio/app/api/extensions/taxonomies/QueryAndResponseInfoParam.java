package com.constellio.app.api.extensions.taxonomies;

import com.constellio.model.services.search.SPEQueryResponse;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

import java.util.Date;
import java.util.List;

/**
 * Created by Constelio on 2016-10-19.
 */
public class QueryAndResponseInfoParam {

    public SPEQueryResponse speQueryResponse;

    public LogicalSearchQuery query;

    public Date queryDate;

    public String userID;

    public List<String> searchTerms;

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

    public Date getQueryDate() {
        return queryDate;
    }

    public QueryAndResponseInfoParam setQueryDate(Date queryDate) {
        this.queryDate = queryDate;
        return this;
    }

    public String getUserID() {
        return userID;
    }

    public QueryAndResponseInfoParam setUserID(String userID) {
        this.userID = userID;
        return this;
    }

    public List<String> getSearchTerms() {
        return searchTerms;
    }

    public QueryAndResponseInfoParam setSearchTerms(List<String> searchTerms) {
        this.searchTerms = searchTerms;
        return this;
    }
}
