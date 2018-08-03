package com.constellio.app.ui.pages.search;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.framework.data.SolrFeatureDataProvider;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.User;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.solr.client.solrj.ResponseParser;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.ModifiableSolrParams;
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

	private String getSolrServerUrl() {
		return ConstellioFactories.getInstance().getDataLayerConfiguration().getRecordsDaoHttpSolrServerUrl();
	}

	public SolrFeatureDataProvider newDataProvider() {
		SolrClient solrClient = new HttpSolrFeatureClient(getSolrServerUrl());
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
		SolrFeatureRequest sfr = new SolrFeatureDeleteRequest();
		sfr.process(new HttpSolrFeatureClient(getSolrServerUrl()));
	}

	protected void updateOnSolrServer(List<SolrFeature> features)
			throws IOException, SolrServerException {
		deleteOnSolrServer();

		SolrFeatureRequest sfr = new SolrFeaturePutRequest();
		sfr.setFeatures(features);
		sfr.process(new HttpSolrFeatureClient(getSolrServerUrl()));
	}

	private class HttpSolrFeatureClient extends HttpSolrClient {
		private static final String DEFAULT_PATH = "/select";

		public HttpSolrFeatureClient(String baseURL) {
			super(baseURL);
		}

		@Override
		protected HttpRequestBase createMethod(SolrRequest request, String collection)
				throws IOException, SolrServerException {
			String path = requestWriter.getPath(request);
			if (path == null || !path.startsWith("/")) {
				path = DEFAULT_PATH;
			}

			ResponseParser parser = request.getResponseParser();
			if (parser == null) {
				parser = this.parser;
			}

			// The parser 'wt=' and 'version=' params are used instead of the original
			// params
			ModifiableSolrParams wparams = new ModifiableSolrParams(request.getParams());
			if (parser != null) {
				wparams.set(CommonParams.WT, parser.getWriterType());
				wparams.set(CommonParams.VERSION, parser.getVersion());
			}
			if (invariantParams != null) {
				wparams.add(invariantParams);
			}

			String basePath = baseUrl;
			if (collection != null) {
				basePath += "/" + collection;
			}

			if (SolrRequest.METHOD.DELETE == request.getMethod()) {
				return new HttpDelete(basePath + path + wparams.toQueryString());
			} else {
				return super.createMethod(request, collection);
			}
		}
	}
}
