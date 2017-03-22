package com.constellio.app.ui.framework.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.SearchBoostVO;
import com.constellio.app.ui.framework.builders.SearchBoostToVOBuilder;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.SearchBoostManager;
import com.constellio.model.services.search.entities.SearchBoost;

@SuppressWarnings("serial")
public class SearchBoostDataProvider extends AbstractDataProvider {

	private transient SearchBoostManager manager;
	private static final String METADATA_TYPE = "metadata";
	private static final String QUERY_TYPE = "query";

	private String collection;
	private transient SearchBoostToVOBuilder voBuilder;
	private transient List<SearchBoostVO> searchBoostVOs;
	private String type;

	public SearchBoostDataProvider(String type, String collection, SearchBoostToVOBuilder voBuilder,
			ModelLayerFactory modelLayerFactory) {
		this.type = type;
		this.voBuilder = voBuilder;
		this.collection = collection;
		init(modelLayerFactory);
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
		init(constellioFactories.getModelLayerFactory());
	}

	void init(ModelLayerFactory modelLayerFactory) {
		manager = modelLayerFactory.getSearchBoostManager();
		loadSearchBoostVOs();
	}

	public void setSearchBoostVOs(List<SearchBoostVO> searchBoostVOs) {
		this.searchBoostVOs = searchBoostVOs;
	}

	private void loadSearchBoostVOs() {
		List<SearchBoostVO> searchBoostVOs = new ArrayList<>();
		List<SearchBoost> searchBoostList = new ArrayList<>();
		if (QUERY_TYPE.equals(type)) {
			searchBoostList = manager.getAllSearchBoostsByQueryType(collection);
		} else if (METADATA_TYPE.equals(type)) {
			searchBoostList = manager.getAllSearchBoostsByMetadataType(collection);
		}
		for (SearchBoost searchBoost : searchBoostList) {
			searchBoostVOs.add(voBuilder.build(searchBoost));
		}
		sort(searchBoostVOs);
		setSearchBoostVOs(searchBoostVOs);
	}

	public List<SearchBoostVO> listSearchBoostVOs() {
		return searchBoostVOs;
	}

	public List<SearchBoostVO> listBoostFields(int startIndex, int count) {
		int toIndex = startIndex + count;
		List subList = new ArrayList();
		if (startIndex > searchBoostVOs.size()) {
			return subList;
		} else if (toIndex > searchBoostVOs.size()) {
			toIndex = searchBoostVOs.size();
		}
		return searchBoostVOs.subList(startIndex, toIndex);
	}

	public int size() {
		return searchBoostVOs.size();
	}

	private void sort(List<SearchBoostVO> searchBoostVOs) {
		Collections.sort(searchBoostVOs, new Comparator<SearchBoostVO>() {
			@Override
			public int compare(SearchBoostVO o1, SearchBoostVO o2) {
				return o1.getLabel().toLowerCase().compareTo(o2.getLabel().toLowerCase());
			}
		});
	}

	public SearchBoostVO getSearchBoostVO(Integer index) {
		return searchBoostVOs.get(index);
	}
}
