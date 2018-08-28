package com.constellio.app.ui.pages.search;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.framework.data.SolrFeatureDataProvider;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.User;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.ltr.feature.SolrFeature;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class SolrFeaturePresenter extends BasePresenter<SolrFeatureView> {

	private String STORE = "_DEFAULT_";
	private SolrFeatureDataProvider dataProvider;

	public SolrFeaturePresenter(SolrFeatureView view) {
		super(view);

		dataProvider = new SolrFeatureDataProvider(STORE, collection, modelLayerFactory);
	}

	public SolrFeatureDataProvider newDataProvider() {
		SolrClient solrClient = ConstellioFactories.getInstance().getDataLayerFactory().getRecordsVaultServer().getNestedSolrServer();
		SolrFeatureGetRequest request = new SolrFeatureGetRequest();

		try {
			request.process(solrClient);
			dataProvider.setFeatures(request.getFeatures());
		} catch (Exception e) {
			view.showErrorMessage($("SolrFeatureView.solrUpdateError"));
			e.printStackTrace();
		}

		return dataProvider;
	}

	public void addButtonClicked(SolrFeature feature) {
		List<SolrFeature> features = new ArrayList<>(dataProvider.listFeatures());
		features.add(0, feature);

		try {
			updateOnSolrServer(features);
			dataProvider.setFeatures(features);
		} catch (Exception e) {
			view.showErrorMessage($("SolrFeatureView.solrUpdateError"));

			e.printStackTrace();
		}
		view.refreshTable();
	}

	public void editButtonClicked(SolrFeature feature, SolrFeature oldFeature) {
		List<SolrFeature> features = new ArrayList<>(dataProvider.listFeatures());
		features.set(features.indexOf(oldFeature), feature);

		try {
			updateOnSolrServer(features);
			dataProvider.setFeatures(features);
		} catch (Exception e) {
			view.showErrorMessage($("SolrFeatureView.solrUpdateError"));

			e.printStackTrace();
		}
		view.refreshTable();
	}

	public void backButtonClicked() {
		view.navigate().to().adminModule();
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return userServices().has(user).globalPermissionInAnyCollection(CorePermissions.MANAGE_SECURITY);
	}

	public void deleteButtonClicked(SolrFeature feature) {
		List<SolrFeature> features = new ArrayList<>(dataProvider.listFeatures());
		features.remove(feature);

		try {
			if (features.isEmpty()) {
				deleteOnSolrServer();
			} else {
				updateOnSolrServer(features);
				dataProvider.setFeatures(features);
			}
		} catch (Exception e) {
			view.showErrorMessage($("SolrFeatureView.solrUpdateError"));

			e.printStackTrace();
		}
		view.refreshTable();
	}

	public SolrFeature getFeature(Integer itemId, SolrFeatureDataProvider provider) {
		Integer index = itemId;
		return provider.getFeature(index);
	}

	protected void deleteOnSolrServer()
			throws IOException, SolrServerException {
		SolrClient solrClient = ConstellioFactories.getInstance().getDataLayerFactory().getRecordsVaultServer().getNestedSolrServer();
		SolrFeatureRequest sfr = new SolrFeatureDeleteRequest();
		sfr.process(solrClient);
	}

	protected void updateOnSolrServer(List<SolrFeature> features)
			throws IOException, SolrServerException {
		deleteOnSolrServer();

		SolrFeatureRequest sfr = new SolrFeaturePutRequest();
		sfr.setFeatures(features);
		SolrClient solrClient = ConstellioFactories.getInstance().getDataLayerFactory().getRecordsVaultServer().getNestedSolrServer();
		sfr.process(solrClient);
	}
}
