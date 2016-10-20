package com.constellio.app.api.extensions.taxonomies;

import com.constellio.model.entities.records.wrappers.SavedSearch;
import com.constellio.model.services.search.SPEQueryResponse;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import org.joda.time.LocalDateTime;

import java.util.Date;
import java.util.List;

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
}
