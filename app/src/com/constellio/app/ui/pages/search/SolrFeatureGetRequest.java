package com.constellio.app.ui.pages.search;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrResponse;
import org.apache.solr.client.solrj.response.SolrResponseBase;
import org.apache.solr.common.util.ContentStream;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.ltr.feature.SolrFeature;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class SolrFeatureGetRequest extends SolrFeatureRequest {
	private GetResponse response;

	public SolrFeatureGetRequest() {
		super(METHOD.GET);

		setPath(getPath() + "/_DEFAULT_");

		response = new GetResponse();
	}

	@Override
	protected SolrResponse createResponse(SolrClient client) {
		return response;
	}

	@Override
	public Collection<ContentStream> getContentStreams() throws IOException {
		return null;
	}

	public List<SolrFeature> getFeatures() {
		return response.getFeatures();
	}

	private class GetResponse extends SolrResponseBase {
		private List<SolrFeature> features = new ArrayList<>();

		@Override
		public void setResponse(NamedList<Object> response) {
			super.setResponse(response);

			ArrayList fs = (ArrayList) response.get("features");
			for (int i = 0; fs != null && i < fs.size(); i++) {
				Map f = (Map) fs.get(i);
				SolrFeature feature = new SolrFeature((String) f.get("name"), null);

				Map<String, Object> params = (Map<String, Object>) f.get("params");
				if (params != null) {
					feature.setQ((String) params.get("q"));
					feature.setFq((List<String>) params.get("fq"));
					feature.setDf((String) params.get("df"));
				}

				features.add(feature);
			}
		}

		public List<SolrFeature> getFeatures() {
			return features;
		}
	}
}
