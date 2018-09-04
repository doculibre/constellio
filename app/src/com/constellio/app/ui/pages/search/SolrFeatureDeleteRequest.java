package com.constellio.app.ui.pages.search;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrResponse;
import org.apache.solr.client.solrj.response.schema.SchemaResponse;

public class SolrFeatureDeleteRequest extends SolrFeatureRequest {
	public SolrFeatureDeleteRequest() {
		super(METHOD.DELETE);

		setPath(getPath() + "/_DEFAULT_");
	}

	@Override
	protected SolrResponse createResponse(SolrClient client) {
		return new SchemaResponse.UpdateResponse();
	}
}
