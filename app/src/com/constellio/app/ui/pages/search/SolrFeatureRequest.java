package com.constellio.app.ui.pages.search;

import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import org.apache.solr.client.solrj.SolrResponse;
import org.apache.solr.client.solrj.request.schema.AbstractSchemaRequest;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.ContentStream;
import org.apache.solr.common.util.ContentStreamBase;
import org.apache.solr.ltr.feature.SolrFeature;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class SolrFeatureRequest extends AbstractSchemaRequest<SolrResponse> {
	List<SolrFeature> features;

	public SolrFeatureRequest(METHOD m, SolrParams params) {
		super(m, "/records/schema/feature-store", params);
	}

	public SolrFeatureRequest(METHOD m) {
		this(m, null);
	}

	@Override
	public Collection<ContentStream> getContentStreams() throws IOException {
		List<SolrFeatureJson> jsonObjects = new ArrayList<>();
		for (SolrFeature sf : getFeatures()) {
			SolrFeatureJson sfj = new SolrFeatureJson();
			sfj.setName(sf.getName());
			sfj.setClazz(sf.getClass().getCanonicalName());
			sfj.setQuery(sf.paramsToMap());

			jsonObjects.add(sfj);
		}

		String str = new GsonBuilder().disableHtmlEscaping().create().toJson(jsonObjects);
		ContentStream stringStream = new ContentStreamBase.StringStream(str);
		return Collections.singletonList(stringStream);
	}

	public List<SolrFeature> getFeatures() {
		if (features == null) {
			features = new ArrayList<>();
		}

		return features;
	}

	public void setFeatures(List<SolrFeature> features) {
		this.features = features;
	}

	public static class SolrFeatureJson {
		@SerializedName("name")
		String name;
		@SerializedName("class")
		String clazz;
		@SerializedName("params")
		Map<String, Object> query = new HashMap<>();

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getClazz() {
			return clazz;
		}

		public void setClazz(String clazz) {
			this.clazz = clazz;
		}

		public Map<String, Object> getQuery() {
			return query;
		}

		public void setQuery(Map<String, Object> query) {
			this.query = query;
		}
	}
}
