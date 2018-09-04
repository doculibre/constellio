package com.constellio.app.ui.pages.search;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrResponse;
import org.apache.solr.client.solrj.response.schema.SchemaResponse;

public class SolrFeaturePutRequest extends SolrFeatureRequest {
	public SolrFeaturePutRequest() {
		super(METHOD.PUT);
	}

	@Override
	protected SolrResponse createResponse(SolrClient client) {
		return new SchemaResponse.UpdateResponse();
	}
}
