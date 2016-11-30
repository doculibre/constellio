package com.constellio.app.modules.rm.reports.builders.search;

import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

import java.util.List;

/**
 * Created by Constelio on 2016-11-29.
 */
public class SearchResultReportParameters {

    private final List<String> selectedRecords;
    private final String schemaType;
    private final String collection;
    private final String username;
    private final String reportTitle;
    private final LogicalSearchQuery searchQuery;

    public SearchResultReportParameters(List<String> selectedRecords, String schemaType,
                                           String collection, String reportTitle, User user, LogicalSearchQuery searchQuery) {
        this.selectedRecords = selectedRecords;
        this.schemaType = schemaType;
        this.collection = collection;
        this.reportTitle = reportTitle;
        this.searchQuery = searchQuery;
        if (user != null) {
            this.username = user.getUsername();
        } else {
            username = null;
        }
    }

    public List<String> getSelectedRecords() {
        return selectedRecords;
    }

    public String getSchemaType() {
        return schemaType;
    }

    public String getCollection() {
        return collection;
    }

    public String getUsername() {
        return username;
    }

    public String getReportTitle() {
        return reportTitle;
    }

    public LogicalSearchQuery getSearchQuery() {
        return searchQuery;
    }
}
