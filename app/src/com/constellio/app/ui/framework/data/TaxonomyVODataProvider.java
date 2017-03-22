package com.constellio.app.ui.framework.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.TaxonomyVO;
import com.constellio.app.ui.framework.builders.TaxonomyToVOBuilder;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.model.services.users.UserServices;

@SuppressWarnings("serial")
public class TaxonomyVODataProvider extends AbstractDataProvider {

	private transient TaxonomiesManager taxonomiesManager;
	private transient List<TaxonomyVO> taxonomyVOs;
	private transient UserServices userServices;
	private TaxonomyToVOBuilder voBuilder;
	private String collection;
	private String username;

	public TaxonomyVODataProvider(TaxonomyToVOBuilder voBuilder, ModelLayerFactory modelLayerFactory, String collection,
			String username) {
		this.voBuilder = voBuilder;
		this.collection = collection;
		this.username = username;
		init(modelLayerFactory);
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
		init(constellioFactories.getModelLayerFactory());
	}

	void init(ModelLayerFactory modelLayerFactory) {
		taxonomiesManager = modelLayerFactory.getTaxonomiesManager();
		userServices = modelLayerFactory.newUserServices();
		loadTaxonomiesVOs();
	}

	void loadTaxonomiesVOs() {
		List<TaxonomyVO> newTaxonomyVOs = new ArrayList<>();
		User user = userServices.getUserInCollection(username, collection);
		List<Taxonomy> taxonomies = taxonomiesManager.getAvailableTaxonomiesInHomePage(user);

		for (Taxonomy taxonomy : taxonomies) {
			TaxonomyVO taxonomyVO = voBuilder.build(taxonomy);
			newTaxonomyVOs.add(taxonomyVO);
		}
		sort(newTaxonomyVOs);
		setTaxonomyVOs(newTaxonomyVOs);
	}

	public void setTaxonomyVOs(List<TaxonomyVO> taxonomyVOs) {
		this.taxonomyVOs = taxonomyVOs;
	}

	public int size() {
		return taxonomyVOs.size();
	}

	void sort(List<TaxonomyVO> taxonomyVOs) {
		Collections.sort(taxonomyVOs, new Comparator<TaxonomyVO>() {
			@Override
			public int compare(TaxonomyVO o1, TaxonomyVO o2) {
				return o1.getCode().toLowerCase().compareTo(o2.getCode().toLowerCase());
			}
		});
	}

	public List<TaxonomyVO> getTaxonomyVOs() {
		return taxonomyVOs;
	}

	public List<String> getTaxonomyVOsCodes() {
		List<String> codes = new ArrayList<>();
		for (TaxonomyVO taxonomyVO : taxonomyVOs) {
			codes.add(taxonomyVO.getCode());
		}
		return codes;
	}

	public List<String> getTaxonomyVOsTitles() {
		List<String> titles = new ArrayList<>();
		for (TaxonomyVO taxonomyVO : taxonomyVOs) {
			titles.add(taxonomyVO.getTitle());
		}
		return titles;
	}
}
