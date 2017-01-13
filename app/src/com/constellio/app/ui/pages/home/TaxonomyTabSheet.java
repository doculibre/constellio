package com.constellio.app.ui.pages.home;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.framework.data.RecordLazyTreeDataProvider;
import com.constellio.app.ui.pages.base.PresenterService;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.taxonomies.TaxonomiesManager;

public class TaxonomyTabSheet implements Serializable {
	private transient ModelLayerFactory modelLayerFactory;
	private transient User user;

	private int defaultTab = 0;

	public TaxonomyTabSheet(ModelLayerFactory modelLayerFactory, SessionContext sessionContext) {
		init(modelLayerFactory, sessionContext);
	}

	public List<RecordLazyTreeDataProvider> getDataProviders() {
		List<RecordLazyTreeDataProvider> providers = new ArrayList<>();
		for (String taxonomy : getTaxonomyCodes()) {
			providers.add(new RecordLazyTreeDataProvider(taxonomy));
		}
		return providers;
	}

	public int getDefaultTab() {
		return defaultTab;
	}

	private void init(ModelLayerFactory modelLayerFactory, SessionContext sessionContext) {
		this.modelLayerFactory = modelLayerFactory;
		user = new PresenterService(modelLayerFactory).getCurrentUser(sessionContext);
		
		PresenterService presenterService = new PresenterService(modelLayerFactory);
		String userDefaultTaxonomy = user.getDefaultTaxonomy();
		String configDefaultTaxonomy = presenterService.getSystemConfigs().getDefaultTaxonomy();

		int index = 0;
		for (String taxonomy : getTaxonomyCodes()) {
			if (userDefaultTaxonomy != null) {
				if (taxonomy.equals(userDefaultTaxonomy)) {
					defaultTab = index;
				}
			} else if (taxonomy.equals(configDefaultTaxonomy)) {
				defaultTab = index;
			}
			index++;
		}
	}

	private List<String> getTaxonomyCodes() {
		TaxonomiesManager taxonomiesManager = modelLayerFactory.getTaxonomiesManager();
		List<String> result = new ArrayList<>();
		for (Taxonomy taxonomy : taxonomiesManager.getAvailableTaxonomiesInHomePage(user)) {
			result.add(taxonomy.getCode());
		}
		return result;
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		init(ConstellioFactories.getInstance().getModelLayerFactory(), ConstellioUI.getCurrentSessionContext());
	}
}
