package com.constellio.app.ui.framework.data;

import com.constellio.model.services.factories.ModelLayerFactory;
import org.apache.solr.ltr.feature.SolrFeature;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@SuppressWarnings("serial")
public class SolrFeatureDataProvider extends AbstractDataProvider {
	private List<SolrFeature> features = new ArrayList<>();

	private String store;
	private String collection;
	private ModelLayerFactory modelLayerFactory;

	public SolrFeatureDataProvider(String store, String collection,
								   ModelLayerFactory modelLayerFactory) {
		this.store = store;
		this.collection = collection;
		this.modelLayerFactory = modelLayerFactory;
	}

	public void setFeatures(List<SolrFeature> features) {
		this.features = features;
	}

	public List<SolrFeature> listFeatures() {
		return features;
	}

	public List<SolrFeature> listFeatures(int startIndex, int count) {
		int toIndex = startIndex + count;
		List subList = new ArrayList();
		if (startIndex > features.size()) {
			return subList;
		} else if (toIndex > features.size()) {
			toIndex = features.size();
		}

		return features.subList(startIndex, toIndex);
	}

	public int size() {
		return features.size();
	}

	private void sort(List<SolrFeature> features) {
		Collections.sort(features, new Comparator<SolrFeature>() {
			@Override
			public int compare(SolrFeature o1, SolrFeature o2) {
				return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
			}
		});
	}

	public SolrFeature getFeature(Integer index) {
		return features.get(index);
	}
}
