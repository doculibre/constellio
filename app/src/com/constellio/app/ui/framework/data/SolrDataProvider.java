package com.constellio.app.ui.framework.data;

import org.apache.solr.client.solrj.response.QueryResponse;

public abstract class SolrDataProvider extends AbstractDataProvider {
    public abstract QueryResponse getQueryResponse();
}
